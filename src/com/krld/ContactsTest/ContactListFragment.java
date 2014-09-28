package com.krld.ContactsTest;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.*;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import java.io.FileDescriptor;
import java.io.IOException;

public class ContactListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String SELECTION = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
            + ("1") + "'";
    private static final String[] PROJECTION =
            new String[]{ContactsContract.Contacts._ID,
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY,
                    ContactsContract.Contacts.LOOKUP_KEY,
                    ContactsContract.Contacts.PHOTO_THUMBNAIL_URI};
    private static final int INDEX_CONTACT_ID = 0;
    private static final int INDEX_DISPLAY_NAME = 1;
    private static final int INDEX_LOOKUP_KEY = 2;
    private static final int INDEX_PHOTO_THUMBNAIL = 3;

    private static final String[] DETAIL_PROJECTION =
            new String[]{ContactsContract.Data.CONTACT_ID,
                    ContactsContract.Data.DATA1};
    private static final int INDEX_DETAIL_CONTACT_ID = 0;
    private static final int INDEX_DETAIL_DATA1 = 1;

    private ContactsAdapter adapter;

    private static final String SORT_ORDER =
            ContactsContract.Contacts.SORT_KEY_PRIMARY;
    private Cursor curPhone;
    private Cursor curEmail;
    private Bitmap defaultIcon;
    private Callbacks callbacks;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        adapter = new ContactsAdapter(getActivity());
        setListAdapter(adapter);

      //  someLayoutAdjustment();
        initDefaultIcon();
        initDetailInfo();
        getLoaderManager().initLoader(0, null, this);
    }

    private void someLayoutAdjustment() {
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) getListView()
                .getLayoutParams();
        int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics());
        mlp.setMargins(px, px, px, px);
    }

    private void initDefaultIcon() {
        defaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_contact_picture_holo_light);
    }

    private void initDetailInfo() {
        ContentResolver cr = getActivity().getContentResolver();
        curPhone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, DETAIL_PROJECTION,
                SELECTION, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + ", " + ContactsContract.CommonDataKinds.Phone.DATA1 + " DESC");
        curEmail = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, DETAIL_PROJECTION,
                SELECTION, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity(), ContactsContract.Contacts.CONTENT_URI,
                PROJECTION, SELECTION, null, SORT_ORDER);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.contact_list_actions, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_new_contact:
                addNewContact();
                return true;
            case R.id.sign_out:
                returnToLogin();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void returnToLogin() {
        getActivity().finish();
    }

    private void addNewContact() {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor cursor = adapter.getCursor();
        cursor.moveToPosition(position);
        Uri uri = ContactsContract.Contacts.getLookupUri(cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID)),
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));
        callbacks.onContactSelected(uri);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        callbacks = (Callbacks) activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbacks = null;
    }

    interface Callbacks {
        void onContactSelected(Uri uri);
    }

    private class ContactsAdapter extends CursorAdapter {
        private final LayoutInflater inflater;

        public ContactsAdapter(Context context) {
            super(context, null, 0);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            final View itemLayout =
                    inflater.inflate(R.layout.contact_list_item, viewGroup, false);

            final ViewHolder holder = new ViewHolder();
            holder.contactName = (TextView) itemLayout.findViewById(R.id.contact_name);
            holder.phone = (TextView) itemLayout.findViewById(R.id.phone);
            holder.email = (TextView) itemLayout.findViewById(R.id.email);
            holder.icon = (QuickContactBadge) itemLayout.findViewById(android.R.id.icon);

            itemLayout.setTag(holder);

            return itemLayout;
        }

        @Override
        public void bindView(View view, Context context, Cursor cur) {
            final ViewHolder holder = (ViewHolder) view.getTag();
            int contactId = cur.getInt(INDEX_CONTACT_ID);
            String contactName = cur.getString(INDEX_DISPLAY_NAME);

            holder.contactName.setText(contactName);
            holder.phone.setText(getFromDetailCursor(contactId, curPhone));
            holder.email.setText(getFromDetailCursor(contactId, curEmail));

            final Uri contactUri = ContactsContract.Contacts.getLookupUri(
                    cur.getLong(INDEX_CONTACT_ID),
                    cur.getString(INDEX_LOOKUP_KEY));

            holder.icon.assignContactUri(contactUri);

            String photoThumbnail = cur.getString(INDEX_PHOTO_THUMBNAIL);
            holder.icon.setImageBitmap(defaultIcon);
            new AsyncTaskDownloader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Object[]{holder.icon, photoThumbnail});
        }

        private Bitmap loadContactPhotoThumbnail(String photoData) {
            AssetFileDescriptor afd = null;
            try {
                Uri thumbUri;
                thumbUri = Uri.parse(photoData);
                afd = getActivity().getContentResolver().
                        openAssetFileDescriptor(thumbUri, "r");

                FileDescriptor fileDescriptor = afd.getFileDescriptor();
                if (fileDescriptor != null) {
                    return BitmapFactory.decodeFileDescriptor(
                            fileDescriptor, null, null);
                }
            } catch (Exception e) {
            } finally {
                if (afd != null) {
                    try {
                        afd.close();
                    } catch (IOException e) {
                    }
                }
            }
            return null;
        }

        private String getFromDetailCursor(int contactId, Cursor cursor) {
            cursor.moveToFirst();
            int id = cursor.getInt(INDEX_DETAIL_CONTACT_ID);
            boolean cursorEnded = false;
            while (id != contactId) {
                if (id > contactId) {
                    cursorEnded = true;
                    break;
                }
                if (!cursor.moveToNext()) {
                    cursorEnded = true;
                    break;
                }
                id = cursor.getInt(INDEX_DETAIL_CONTACT_ID);
            }
            if (cursorEnded) {
                return "";
            }
            return cursor.getString(INDEX_DETAIL_DATA1);
        }

        private class ViewHolder {
            TextView contactName;
            TextView phone;
            QuickContactBadge icon;
            public TextView email;
        }

        private class AsyncTaskDownloader extends AsyncTask<Object, Void, Object> {

            @Override
            protected Object doInBackground(Object... params) {
                Bitmap mThumbnail =
                        loadContactPhotoThumbnail((String) params[1]);
                return new Object[]{params[0], mThumbnail};
            }

            @Override
            protected void onPostExecute(Object o) {
                Object[] array = (Object[]) o;
                QuickContactBadge icon = (QuickContactBadge) array[0];
                Bitmap thumbnailBitmap = (Bitmap) array[1];
                if (thumbnailBitmap != null) {
                    icon.setImageBitmap(thumbnailBitmap);
                }
            }
        }
    }
}
