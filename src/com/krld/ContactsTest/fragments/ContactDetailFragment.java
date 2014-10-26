package com.krld.ContactsTest.fragments;

import android.app.Activity;
import android.app.Fragment;
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
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.krld.ContactsTest.R;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ContactDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String KEY_URI = "uri_key";

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
        uriContact = getArguments().getParcelable(KEY_URI);
        getLoaderManager().initLoader(0, null, this);
        setHasOptionsMenu(true);
        ActionBar supportActionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        if (supportActionBar != null)
            supportActionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.contact_detail, container, false);
        init(v);
        return v;
    }

    private void init(View v) {
        phones = new ArrayList<DetailValue>();
        linearLayout = (LinearLayout) v.findViewById(R.id.contact_detail_layout);
        emails = new ArrayList<DetailValue>();
        contactPhoto = (ImageView) v.findViewById(R.id.contact_photo);
        contactPhoto.setOnClickListener(new OnClickContactPhoto(contactPhoto));
        ((ActionBarActivity) getActivity()).getSupportActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    public static Fragment newInstance(Uri data) {
        Bundle args = new Bundle();
        args.putParcelable(KEY_URI, data);
        ContactDetailFragment fragment = new ContactDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Uri uri = Uri.withAppendedPath(uriContact, ContactsContract.Contacts.Data.CONTENT_DIRECTORY);
        return new CursorLoader(getActivity(), uri,
                PROJECTION,
                SELECTION,
                null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        getActivity().setTitle(data.getString(INDEX_DISPLAY_NAME));
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
        LayoutInflater inflater = getActivity().getLayoutInflater();
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
        ((TextView) header.findViewById(R.id.contact_detail_header)).setText(headerStr);
        layout.addView(header);
        for (DetailValue detailValue : detailValues) {
            View view = inflater.inflate(R.layout.contact_detail_item, null);
            ((TextView) view.findViewById(R.id.contact_detail_value)).setText(detailValue.value);
            ((TextView) view.findViewById(R.id.contact_detail_label)).setText(detailValue.label);
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
                    getActivity().getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return BitmapFactory.decodeStream(fd.createInputStream());
        } catch (IOException e) {
            return null;
        }
    }

    Bitmap getThumbnailPhoto(long contactId) {
        Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
        Uri photoUri = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        Cursor cursor = getActivity().getContentResolver().query(photoUri,
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.contact_detail_actions, menu);
    }

    private class DetailValue {
        public final String value;
        public final String label;

        public DetailValue(String value, String label) {
            this.value = value;
            this.label = label;

        }
    }

    private class OnClickContactPhoto implements View.OnClickListener {
        public static final int ANIM_DURATION = 500;
        private final Integer startHeight;
        private boolean mExpand = true;

        public OnClickContactPhoto(ImageView contactPhoto) {
            startHeight = contactPhoto.getLayoutParams().height;
        }

        @Override
        public void onClick(View v) {
            if (mExpand) {
                ((ImageView) v).setScaleType(ImageView.ScaleType.CENTER_CROP);
                v.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                int endHeight = getHeightView(getActivity(), v);
                v.getLayoutParams().height = startHeight;
                v.startAnimation(new ExpandCollapseAnimation(v, startHeight, endHeight, ANIM_DURATION, 0));
                mExpand = false;
            } else {
                v.startAnimation(new ExpandCollapseAnimation(v, v.getLayoutParams().height, startHeight, ANIM_DURATION, 1));
                mExpand = true;
            }

        }
    }

    public static int getHeightView(Activity activity, View view) {
        DisplayMetrics metrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int screenWidth = metrics.widthPixels;

        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(screenWidth, View.MeasureSpec.EXACTLY);

        view.measure(widthMeasureSpec, heightMeasureSpec);
        int height = view.getMeasuredHeight();
        return height;
    }

    public class ExpandCollapseAnimation extends Animation {
        private final int mStartHeight;
        private View mAnimatedView;
        private int mEndHeight;
        private int mType;

        /**
         * Initializes expand collapse animation, has two types, collapse (1) and expand (0).
         *
         * @param view        The view to animate
         * @param startHeight
         * @param endHeight
         * @param duration
         * @param type        The type of animation: 0 will expand from gone and 0 size to visible and layout size defined in xml.
         */
        public ExpandCollapseAnimation(View view, int startHeight, int endHeight, int duration, int type) {
            setDuration(duration);
            mAnimatedView = view;
            mEndHeight = endHeight;
            mStartHeight = startHeight;
            mType = type;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            super.applyTransformation(interpolatedTime, t);
            if (interpolatedTime < 1.0f) {
                if (mType == 0) {
                    mAnimatedView.getLayoutParams().height = mStartHeight + (int) ((mEndHeight - mStartHeight) * interpolatedTime);
                } else {
                    mAnimatedView.getLayoutParams().height = mStartHeight - (int) ((mStartHeight - mEndHeight) * interpolatedTime);
                }
                mAnimatedView.requestLayout();
            } else {
                if (mType == 0) {
                    mAnimatedView.getLayoutParams().height = mEndHeight;
                    mAnimatedView.requestLayout();
                } else {
                    mAnimatedView.getLayoutParams().height = mEndHeight;
                    mAnimatedView.requestLayout();
                }
            }
        }
    }
}
