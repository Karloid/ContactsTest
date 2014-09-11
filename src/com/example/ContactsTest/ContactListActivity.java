package com.example.ContactsTest;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.*;
import android.widget.*;

import java.util.ArrayList;

/**
 * Created by Andrey on 9/11/2014.
 */
public class ContactListActivity extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    // This is the Adapter being used to display the list's data
    ContactsAdapter mAdapter;

    // These are the Contacts rows that we will retrieve
    static final String[] PROJECTION = new String[]{
            ContactsContract.Data.DISPLAY_NAME, ContactsContract.Data.CONTACT_ID};

    static final String SORT_ORDER =
            ContactsContract.Contacts.SORT_KEY_PRIMARY;

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

        // Prepare the loader.  Either re-connect with an existing one,
        // or start a new one.
        getLoaderManager().initLoader(0, null, this);
    }

    // Called when a new Loader needs to be created
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
       /* return new CursorLoader(this, ContactsContract.Contacts.CONTENT_URI,
                null, null, null, SORT_ORDER);*/
        return new CursorLoader(this, ContactsContract.Data.CONTENT_URI,
                null, ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '"
                + ("1") + "'", null, SORT_ORDER);
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
        private String primaryId = null;

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
            String contactName = cur.getString(cur.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
            String curId = cur.getString(cur.getColumnIndex(ContactsContract.Data.CONTACT_ID));
            if (primaryId == null || !primaryId.equals(curId)) {
                Log.i("DEBUG_TEST", "contactName: " + contactName + " primaryId: " + primaryId);
                String phone = "";
                String email = "";
                while (cur.moveToNext()) {
                    String supId = cur.getString(cur.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                    String curContactName = cur.getString(cur.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                    Log.i("DEBUG_TEST", "contactName: " + curContactName + " curId: " + curId + " supId: " + supId);
                    if (!supId.equals(curId)) {
                        //  cur.moveToPrevious();
                        Log.i("DEBUG_TEST", "BREAK! ===========");
                        break;
                    }
                }
                //   cur.getString(cur.getColumnIndex(ContactsContract.Data.MIMETYPE))
                //  cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                primaryId = curId;

                holder.contactName.setText(contactName);
                holder.phone.setText(phone);
                holder.email.setText(email);
            } else {

            }
/*            final ViewHolder holder = (ViewHolder) view.getTag();

            // For Android 3.0 and later, gets the thumbnail image Uri from the current Cursor row.
            // For platforms earlier than 3.0, this isn't necessary, because the thumbnail is
            // generated from the other fields in the row.
            final String photoUri = cur.getString(ContactsQuery.PHOTO_THUMBNAIL_DATA);

            final String displayName = cur.getString(ContactsQuery.DISPLAY_NAME);

            final int startIndex = indexOfSearchQuery(displayName);

            if (startIndex == -1) {
                // If the user didn't do a search, or the search string didn't match a display
                // name, show the display name without highlighting
                holder.text1.setText(displayName);

                if (TextUtils.isEmpty(mSearchTerm)) {
                    // If the search search is empty, hide the second line of text
                    holder.text2.setVisibility(View.GONE);
                } else {
                    // Shows a second line of text that indicates the search string matched
                    // something other than the display name
                    holder.text2.setVisibility(View.VISIBLE);
                }
            } else {
                // If the search string matched the display name, applies a SpannableString to
                // highlight the search string with the displayed display name

                // Wraps the display name in the SpannableString
                final SpannableString highlightedName = new SpannableString(displayName);

                // Sets the span to start at the starting point of the match and end at "length"
                // characters beyond the starting point
                highlightedName.setSpan(highlightTextSpan, startIndex,
                        startIndex + mSearchTerm.length(), 0);

                // Binds the SpannableString to the display name View object
                holder.text1.setText(highlightedName);

                // Since the search string matched the name, this hides the secondary message
                holder.text2.setVisibility(View.GONE);
            }

            // Processes the QuickContactBadge. A QuickContactBadge first appears as a contact's
            // thumbnail image with styling that indicates it can be touched for additional
            // information. When the user clicks the image, the badge expands into a dialog box
            // containing the contact's details and icons for the built-in apps that can handle
            // each detail type.

            // Generates the contact lookup Uri
            final Uri contactUri = ContactsContract.Contacts.getLookupUri(
                    cur.getLong(ContactsQuery.ID),
                    cur.getString(ContactsQuery.LOOKUP_KEY));

            // Binds the contact's lookup Uri to the QuickContactBadge
            holder.icon.assignContactUri(contactUri);

            // Loads the thumbnail image pointed to by photoUri into the QuickContactBadge in a
            // background worker thread
            mImageLoader.loadImage(photoUri, holder.icon);*/
        }

        private class ViewHolder {
            TextView contactName;
            TextView phone;
            QuickContactBadge icon;
            public TextView email;
        }


    }
}
