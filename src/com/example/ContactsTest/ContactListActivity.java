package com.example.ContactsTest;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.*;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Andrey on 9/11/2014.
 */
public class ContactListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor>, AdapterView.OnItemClickListener {
    public static final String SELECTION = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
            + ("1") + "'";
    private static final String TAG = "DEBUG_TAG";
    // This is the Adapter being used to display the list's data
    ContactsAdapter adapter;

    // These are the Contacts rows that we will retrieve
    static final String[] PROJECTION = new String[]{
            ContactsContract.Data.DISPLAY_NAME, ContactsContract.Data.CONTACT_ID};

    static final String SORT_ORDER =
            ContactsContract.Contacts.SORT_KEY_PRIMARY;
    private Cursor curPhone;
    private Cursor curEmail;
    private Bitmap defaultIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);

        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);


        ArrayList<String> contacts = new ArrayList<String>();

        adapter = new ContactsAdapter(this);
        setListAdapter(adapter);
        getListView().setOnItemClickListener(this);

        someLayoutAdjustment();
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
        ContentResolver cr = getContentResolver();
        curPhone = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                SELECTION, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
        curEmail = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                SELECTION, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI,
                null, SELECTION, null, SORT_ORDER);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Log.i(TAG, "onListItemClick!") ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contact_list_actions, menu);
        return super.onCreateOptionsMenu(menu);
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
        finish();
    }

    private void addNewContact() {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.i(TAG, "onItemClick!") ;
        Cursor cursor = adapter.getCursor();
        cursor.moveToPosition(position);
        Uri uri = ContactsContract.Contacts.getLookupUri(cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts._ID)),
                cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));
        Intent intent = new Intent(this, ContactDetailActivity.class);
        intent.setData(uri);
        startActivity(intent);
    }


    private class ContactsAdapter extends CursorAdapter {
        private final LayoutInflater mInflater;

        public ContactsAdapter(Context context) {
            super(context, null, 0);
            mInflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
            final View itemLayout =
                    mInflater.inflate(R.layout.contact_list_item, viewGroup, false);

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
            String contactName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
            int contactId = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts._ID));

            holder.contactName.setText(contactName);
            holder.phone.setText(getFromDetailCursor(contactId, curPhone, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE));
            holder.email.setText(getFromDetailCursor(contactId, curEmail, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE));

            final Uri contactUri = ContactsContract.Contacts.getLookupUri(
                    cur.getLong(cur.getColumnIndex(ContactsContract.Contacts._ID)),
                    cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));

            holder.icon.assignContactUri(contactUri);

            String photoThumbnail = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
            Bitmap mThumbnail =
                    loadContactPhotoThumbnail(photoThumbnail);
            if (mThumbnail != null) {
                holder.icon.setImageBitmap(mThumbnail);
            } else {
                holder.icon.setImageBitmap(defaultIcon);
            }
        }

        private Bitmap loadContactPhotoThumbnail(String photoData) {
            AssetFileDescriptor afd = null;
            try {
                Uri thumbUri;
                if (Build.VERSION.SDK_INT
                        >=
                        Build.VERSION_CODES.HONEYCOMB) {
                    thumbUri = Uri.parse(photoData);
                } else {
                    final Uri contactUri = Uri.withAppendedPath(
                            ContactsContract.Contacts.CONTENT_URI, photoData);

                    thumbUri =
                            Uri.withAppendedPath(
                                    contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
                }

                afd = ContactListActivity.this.getContentResolver().
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

        private String getFromDetailCursor(int contactId, Cursor cursor, String contentItemType) {
            cursor.moveToFirst();
            int id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
            String mimeType = cursor.getString(curPhone.getColumnIndex(ContactsContract.Data.MIMETYPE));
            boolean cursorEnded = false;
            boolean idReached = false;
            while (id != contactId || !mimeType.equals(contentItemType)) {
                if (id == contactId) {
                    idReached = true;
                } else if ((idReached && id != contactId) || id > contactId) {
                    cursorEnded = true;
                    break;
                }
                if (!cursor.moveToNext()) {
                    cursorEnded = true;
                    break;
                }
                id = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
            }
            if (cursorEnded) {
                return "";
            }
            return cursor.getString(cursor.getColumnIndex(ContactsContract.Data.DATA1));
        }

        private class ViewHolder {
            TextView contactName;
            TextView phone;
            QuickContactBadge icon;
            public TextView email;
        }


    }
}
