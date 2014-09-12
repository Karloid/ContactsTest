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
import android.util.TypedValue;
import android.view.*;
import android.widget.*;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Andrey on 9/11/2014.
 */
public class ContactListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    public static final String SELECTION = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
            + ("1") + "'";
    // This is the Adapter being used to display the list's data
    ContactsAdapter mAdapter;

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

        // Create a progress bar to display while the list loads
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);

        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);


        ArrayList<String> contacts = new ArrayList<String>();

        mAdapter = new ContactsAdapter(this);
        setListAdapter(mAdapter);

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

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
       /* return new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI,
                null, null, null, SORT_ORDER);*/
        return new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI,
                null, SELECTION, null, SORT_ORDER);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        // Do something when a list item is clicked
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
            // Gets handles to individual view resources
            final ViewHolder holder = (ViewHolder) view.getTag();
   /*          for (String str : cur.getColumnNames()) {
                Log.i("DEBUG_TEST", str);
            }
            Log.i("DEBUG_TEST", "=====");*/
            String contactName = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
            int contactId = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts._ID));

            holder.contactName.setText(contactName);
            holder.phone.setText(getFromDetailCursor(contactId, curPhone, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE));
            holder.email.setText(getFromDetailCursor(contactId, curEmail, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE));

            final Uri contactUri = ContactsContract.Contacts.getLookupUri(
                    cur.getLong(cur.getColumnIndex(ContactsContract.Contacts._ID)),
                    cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));

            // Binds the contact's lookup Uri to the QuickContactBadge
            holder.icon.assignContactUri(contactUri);

            // Loads the thumbnail image pointed to by photoUri into the QuickContactBadge in a
            // background worker thread
            String photoThumbail = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
            Bitmap mThumbnail =
                    loadContactPhotoThumbnail(photoThumbail);
            if (mThumbnail != null) {
                holder.icon.setImageBitmap(mThumbnail);
            } else {
                holder.icon.setImageBitmap(defaultIcon);
            }
        }

        /**
         * Load a contact photo thumbnail and return it as a Bitmap,
         * resizing the image to the provided image dimensions as needed.
         *
         * @param photoData photo ID Prior to Honeycomb, the contact's _ID value.
         *                  For Honeycomb and later, the value of PHOTO_THUMBNAIL_URI.
         * @return A thumbnail Bitmap, sized to the provided width and height.
         * Returns null if the thumbnail is not found.
         */
        private Bitmap loadContactPhotoThumbnail(String photoData) {
            // Creates an asset file descriptor for the thumbnail file.
            AssetFileDescriptor afd = null;
            // try-catch block for file not found
            try {
                // Creates a holder for the URI.
                Uri thumbUri;
                // If Android 3.0 or later
                if (Build.VERSION.SDK_INT
                        >=
                        Build.VERSION_CODES.HONEYCOMB) {
                    // Sets the URI from the incoming PHOTO_THUMBNAIL_URI
                    thumbUri = Uri.parse(photoData);
                } else {
                    // Prior to Android 3.0, constructs a photo Uri using _ID
                /*
                 * Creates a contact URI from the Contacts content URI
                 * incoming photoData (_ID)
                 */
                    final Uri contactUri = Uri.withAppendedPath(
                            ContactsContract.Contacts.CONTENT_URI, photoData);
                /*
                 * Creates a photo URI by appending the content URI of
                 * Contacts.Photo.
                 */
                    thumbUri =
                            Uri.withAppendedPath(
                                    contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
                }

        /*
         * Retrieves an AssetFileDescriptor object for the thumbnail
         * URI
         * using ContentResolver.openAssetFileDescriptor
         */

                afd = ContactListActivity.this.getContentResolver().
                        openAssetFileDescriptor(thumbUri, "r");

        /*
         * Gets a file descriptor from the asset file descriptor.
         * This object can be used across processes.
         */
                FileDescriptor fileDescriptor = afd.getFileDescriptor();
                // Decode the photo file and return the result as a Bitmap
                // If the file descriptor is valid
                if (fileDescriptor != null) {
                    // Decodes the bitmap
                    return BitmapFactory.decodeFileDescriptor(
                            fileDescriptor, null, null);
                }
                // If the file isn't found
            } catch (Exception e) {
            /*
             * Handle file not found errors
             */
                // In all cases, close the asset file descriptor
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
