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
    private static final int INDEX_DISPLAY_NAME = 0;
    private static final int INDEX_CONTACT_ID = 1;
    private static final int INDEX_MIMETYPE = 2;
    private static final int INDEX_VALUE = 3;
    private static final int INDEX_LABEL = 4;
    private static final int INDEX_CUSTOM_LABEL = 5;
    private Uri uriContact;
    private ImageView contactPhoto;
    private List<DetailValue> phones;
    private List<DetailValue> emails;
    private LinearLayout linearLayout;

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
       /* phonesLayout = (LinearLayout) findViewById(R.id.phones_layout);
        emailsLayout = (LinearLayout) findViewById(R.id.emails_layout);
        phonesListView = (ListView) findViewById(R.id.phones_list_view);
        emailsListView = (ListView) findViewById(R.id.emails_list_view);*/
        linearLayout = (LinearLayout) findViewById(R.id.contact_detail_layout);
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
        LayoutInflater inflater = getLayoutInflater();
        linearLayout.removeAllViews();
        if (!phones.isEmpty()) {
            createViewsForDetailValues(inflater, getString(R.string.phone_header), phones, linearLayout);
        }
        if (!emails.isEmpty()) {
            createViewsForDetailValues(inflater, getString(R.string.email_header), emails, linearLayout);
        }
    }

    private void createViewsForDetailValues(LayoutInflater inflater, String headerStr, List<DetailValue> detailValues, LinearLayout layout) {
        View header = inflater.inflate(R.layout.contact_detail_header, null);
        ((TextView)header.findViewById(R.id.contact_detail_header)).setText(headerStr);
        layout.addView(header);
        for (DetailValue detailValue : detailValues) {
            View view = inflater.inflate(R.layout.contact_detail_item, null);
            ((TextView)view.findViewById(R.id.contact_detail_value)).setText(detailValue.value);
            ((TextView)view.findViewById(R.id.contact_detail_label)).setText(detailValue.label);
            layout.addView(view);
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

    Bitmap getLargePhoto(long contactId) {
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

    Bitmap getThumbnailPhoto(long contactId) {
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
}