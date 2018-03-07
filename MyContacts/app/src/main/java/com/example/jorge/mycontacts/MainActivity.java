package com.example.jorge.mycontacts;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    SimpleCursorAdapter mAdapter;
    MatrixCursor mMatrixCursor;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // The contacts from the contacts content provider is stored in this cursor
        mMatrixCursor = new MatrixCursor(new String[] { "_id","name","details"} );

        // Adapter to set data in the listview
        mAdapter = new SimpleCursorAdapter(getBaseContext(), R.layout.lv_layout, null, new String[] { "name","details"},
                new int[] { R.id.tv_name,R.id.tv_details}, 0);

        // Getting reference to listview
        ListView lstContacts = (ListView) findViewById(R.id.lst_contacts);

        // Setting the adapter to listview
        lstContacts.setAdapter(mAdapter);

        // Creating an AsyncTask object to retrieve and load listview with contacts
        final ListViewContactsLoader listViewContactsLoader = new ListViewContactsLoader();

        // Starting the AsyncTask process to retrieve and load listview with contacts
        listViewContactsLoader.execute();



        lstContacts.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {


                String number = ""+mMatrixCursor.getString(2);

                Toast.makeText(MainActivity.this, number, Toast.LENGTH_LONG).show();

                return false;

            }

        });






    }

    /** An AsyncTask class to retrieve and load listview with contacts */
    private class ListViewContactsLoader extends AsyncTask<Void, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Void... params) {

            Uri contactsUri = ContactsContract.Contacts.CONTENT_URI;

            // Querying the table ContactsContract.Contacts to retrieve all the contacts
            Cursor contactsCursor = getContentResolver().query(contactsUri, null, null, null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC ");

            if(contactsCursor.moveToFirst()){

                do{

                    long contactId = contactsCursor.getLong(contactsCursor.getColumnIndex("_ID"));

                    Uri dataUri = ContactsContract.Data.CONTENT_URI;

                    // Querying the table ContactsContract.Data to retrieve individual items like
                    // home phone, mobile phone, work email etc corresponding to each contact
                    Cursor dataCursor = getContentResolver().query(dataUri, null,
                            ContactsContract.Data.CONTACT_ID + "=" + contactId,
                            null, null);

                    String displayName="";
                    String homePhone="";
                    String mobilePhone="";
                    String workPhone="";

                    if(dataCursor.moveToFirst()){

                        // Getting Display Name
                        displayName = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME ));

                        do{

                            // Getting Phone numbers
                            if(dataCursor.getString(dataCursor.getColumnIndex("mimetype")).equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)){
                                switch(dataCursor.getInt(dataCursor.getColumnIndex("data2"))){
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME :
                                        homePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE :
                                        mobilePhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK :
                                        workPhone = dataCursor.getString(dataCursor.getColumnIndex("data1"));
                                        break;
                                }
                            }

                        }while(dataCursor.moveToNext());

                        String details = "";
                        // Concatenating various information to single string
                        if(homePhone != null && !homePhone.equals("") )
                            details = "" + homePhone + "\n";
                        if(mobilePhone != null && !mobilePhone.equals("") )
                            details += "" + mobilePhone + "\n";
                        if(workPhone != null && !workPhone.equals("") )
                            details += "" + workPhone + "\n";

                        // Adding id, display name, path to photo and other details to cursor
                        mMatrixCursor.addRow(new Object[]{ Long.toString(contactId),displayName,details});

                    }

                }while(contactsCursor.moveToNext());

            }

            return mMatrixCursor;

        }

        @Override
        protected void onPostExecute(Cursor result) {
            // Setting the cursor containing contacts to listview
            mAdapter.swapCursor(result);

        }

    }













}
