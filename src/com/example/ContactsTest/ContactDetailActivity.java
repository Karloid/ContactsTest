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
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ImageView;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class ContactDetailActivity extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String[] PROJECTION = new String[]{ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Data.CONTACT_ID};
    private static final String SELECTION = null;
    public static final int INDEX_DISPLAY_NAME = 0;
    private static final int INDEX_CONTACT_ID = 1;
    private Uri uriContact;
    private ImageView contactPhoto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.contact_detail);
        uriContact = getIntent().getData();
        initViews();
        getLoaderManager().initLoader(0, null, this);
    }

    private void initViews() {
        contactPhoto = (ImageView) findViewById(R.id.contact_photo);
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
}