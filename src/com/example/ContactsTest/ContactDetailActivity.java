package com.example.ContactsTest;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactDetailActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String[] PROJECTION =
            new String[]{ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.Data.CONTACT_ID,
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.Data.DATA1,
                    ContactsContract.Data.DATA2,
                    ContactsContract.Data.DATA3};
    private static final String SELECTION = null;
    public static final int INDEX_DISPLAY_NAME = 0;
    private static final int INDEX_CONTACT_ID = 1;
    private static final int INDEX_MIMETYPE = 2;
    private static final int INDEX_VALUE = 3;
    private static final int INDEX_LABEL = 4;
    private static final int INDEX_CUSTOM_LABEL = 5;
    private static final String TAG = "detail_debug";
    private Uri uriContact;
    private ImageView contactPhoto;
    private List<DetailValue> phones;
    private List<DetailValue> emails;
    private LinearLayout phonesLayout;
    private LinearLayout emailsLayout;
    private ListView phonesListView;
    private ListView emailsListView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_detail);
        uriContact = getIntent().getData();
        init();
        getLoaderManager().initLoader(0, null, this);
    }

    private void init() {
        phones = new ArrayList<DetailValue>();
        phonesLayout = (LinearLayout) findViewById(R.id.phones_layout);
        emailsLayout = (LinearLayout) findViewById(R.id.emails_layout);
        phonesListView = (ListView) findViewById(R.id.phones_list_view);
        emailsListView = (ListView) findViewById(R.id.emails_list_view);
        emails = new ArrayList<DetailValue>();
        contactPhoto = (ImageView) findViewById(R.id.contact_photo);
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri uri = Uri.withAppendedPath(uriContact, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
        return new CursorLoader(this, uri,
                PROJECTION,
                SELECTION,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        setTitle(data.getString(INDEX_DISPLAY_NAME));
        int contactId = data.getInt(INDEX_CONTACT_ID);
        setPhoto(contactId);
        do {
            String mimeType = data.getString(INDEX_MIMETYPE);
            String value = data.getString(INDEX_VALUE);
            int labelType = data.getInt(INDEX_LABEL);
            String customLabel = data.getString(INDEX_CUSTOM_LABEL);
            String typeLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(getResources()
                    , labelType, customLabel).toString();
            if (mimeType.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
                phones.add(new DetailValue(value, typeLabel));
            }
            if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                emails.add(new DetailValue(value, typeLabel));
            }
        } while (data.moveToNext());
        for (DetailValue phone : phones) {
            Log.i(TAG, "" + phone);
        }
        for (DetailValue email : emails) {
            Log.i(TAG, "" + email);
        }
        if (!phones.isEmpty()) {
            phonesLayout.setVisibility(View.VISIBLE);
            phonesListView.setAdapter(new DetailArrayAdapter(this, R.layout.contact_detail_item, phones));
        } else {
            phonesLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void setPhoto(int contactId) {
        Bitmap photo = getLargePhoto(contactId);
        if (photo == null) {
            photo = getThumbnailPhoto(contactId);
        }
        if (photo != null) {
            contactPhoto.setImageBitmap(photo);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public Bitmap getLargePhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri displayPhotoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.DISPLAY_PHOTO);
        try {
            AssetFileDescriptor fd =
                    getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return BitmapFactory.decodeStream(fd.createInputStream());
        } catch (IOException e) {
            return null;
        }
    }

    public Bitmap getThumbnailPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = getContentResolver().query(photoUri,
                new String[]{ContactsContract.Contacts.Photo.PHOTO}, null, null, null);
        if (cursor == null) {
            return null;
        }
        try {
            if (cursor.moveToFirst()) {
                byte[] data = cursor.getBlob(0);
                if (data != null) {
                    return BitmapFactory.decodeStream(new ByteArrayInputStream(data));
                }
            }
        } finally {
            cursor.close();
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.contact_detail_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private class DetailValue {
        public final String value;
        public final String label;

        public DetailValue(String value, String label) {
            this.value = value;
            this.label = label;

        }

        @Override
        public String toString() {
            return "DetailValue{" +
                    "value='" + value + '\'' +
                    ", label='" + label + '\'' +
                    '}';
        }
    }

    private class DetailArrayAdapter extends ArrayAdapter<DetailValue> {
        private final List<DetailValue> values;
        private final ContactDetailActivity context;
        private final int layoutId;
        private final LayoutInflater inflater;

        public DetailArrayAdapter(ContactDetailActivity context, int layoutId, List<DetailValue> values) {
            super(context, layoutId, values);
            this.layoutId = layoutId;
            this.context = context;
            this.values = values;
            this.inflater = context.getLayoutInflater();

        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            if (view == null) {
                view = inflater.inflate(layoutId, parent, false);
                holder = new ViewHolder();
                holder.value = (TextView)view.findViewById(R.id.contact_detail_value);
                holder.label = (TextView)view.findViewById(R.id.contact_detail_label);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }
            DetailValue detailValue = values.get(position);
            holder.value.setText(detailValue.value);
            holder.label.setText(detailValue.label);
            return view;
        }

        private class ViewHolder {
            public TextView value;
            public TextView label;
        }
    }
}