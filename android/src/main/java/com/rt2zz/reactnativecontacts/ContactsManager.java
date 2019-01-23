package com.rt2zz.reactnativecontacts;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.ContentUris;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.Manifest;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.Website;
import android.provider.ContactsContract.RawContacts;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Arguments;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;
import java.util.Hashtable;

public class ContactsManager extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static final String PERMISSION_DENIED = "denied";
    private static final String PERMISSION_AUTHORIZED = "authorized";
    private static final String PERMISSION_READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    private static final int PERMISSION_REQUEST_CODE = 888;

    private static final int REQUEST_OPEN_CONTACT_FORM = 52941;
    private static final int REQUEST_OPEN_EXISTING_CONTACT = 52942;

    private static Callback updateContactCallback;
    private static Callback requestCallback;

    public ContactsManager(ReactApplicationContext reactContext) {
        super(reactContext);
        reactContext.addActivityEventListener(this);
    }

    /*
     * Returns all contactable records on phone
     * queries CommonDataKinds.Contactables to get phones and emails
     */
    @ReactMethod
    public void getAll(final Callback callback) {
        getAllContacts(callback);
    }

    /**
     * Introduced for iOS compatibility.  Same as getAll
     *
     * @param callback callback
     */
    @ReactMethod
    public void getAllWithoutPhotos(final Callback callback) {
        getAllContacts(callback);
    }

    /**
     * Retrieves contacts.
     * Uses raw URI when <code>rawUri</code> is <code>true</code>, makes assets copy otherwise.
     *
     * @param callback user provided callback to run at completion
     */
    private void getAllContacts(final Callback callback) {
        AsyncTask<Void,Void,Void> myAsyncTask = new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(final Void ... params) {
                Context context = getReactApplicationContext();
                ContentResolver cr = context.getContentResolver();

                ContactsProvider contactsProvider = new ContactsProvider(cr);
                WritableArray contacts = contactsProvider.getContacts();

                callback.invoke(null, contacts);
                return null;
            }
        };
        myAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /*
     * Returns all contacts matching string
     */
    @ReactMethod
    public void getContactsMatchingString(final String searchString, final Callback callback) {
        getAllContactsMatchingString(searchString, callback);
    }

    /**
     * Retrieves contacts matching String.
     * Uses raw URI when <code>rawUri</code> is <code>true</code>, makes assets copy otherwise.
     *
     * @param searchString String to match
     * @param callback user provided callback to run at completion
     */
    private void getAllContactsMatchingString(final String searchString, final Callback callback) {
        AsyncTask<Void,Void,Void> myAsyncTask = new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(final Void ... params) {
                Context context = getReactApplicationContext();
                ContentResolver cr = context.getContentResolver();
                ContactsProvider contactsProvider = new ContactsProvider(cr);
                WritableArray contacts = contactsProvider.getContactsMatchingString(searchString);

                callback.invoke(null, contacts);
                return null;
            }
        };
        myAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Retrieves <code>thumbnailPath</code> for contact, or <code>null</code> if not available.
     *
     * @param contactId contact identifier, <code>recordID</code>
     * @param callback callback
     */
    @ReactMethod
    public void getPhotoForId(final String contactId, final Callback callback) {
        AsyncTask<Void,Void,Void> myAsyncTask = new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(final Void ... params) {
                Context context = getReactApplicationContext();
                ContentResolver cr = context.getContentResolver();
                ContactsProvider contactsProvider = new ContactsProvider(cr);
                String photoUri = contactsProvider.getPhotoUriFromContactId(contactId);

                callback.invoke(null, photoUri);
                return null;
            }
        };
        myAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @ReactMethod
    public void writePhotoToPath(final String contactId, final String file, final Callback callback) {
        AsyncTask<Void,Void,Void> myAsyncTask = new AsyncTask<Void,Void,Void>() {
            @Override
            protected Void doInBackground(final Void ... params) {
                Context context = getReactApplicationContext();
                ContentResolver cr = context.getContentResolver();

                Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
                InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
                OutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(file);
                    BitmapFactory.decodeStream(inputStream).compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                    callback.invoke(null, true);
                } catch (FileNotFoundException e) {
                    callback.invoke(e.toString());
                } finally {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        myAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    private Bitmap getThumbnailBitmap(String thumbnailPath) {
        // Thumbnail from absolute path
        Bitmap photo = BitmapFactory.decodeFile(thumbnailPath);

        if (photo == null) {
            // Try to find the thumbnail from assets
            AssetManager assetManager = getReactApplicationContext().getAssets();
            InputStream  inputStream = null;
            try {
                inputStream = assetManager.open(thumbnailPath);
                photo = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return photo;
    }

    /*
     * Start open contact form
     */
    @ReactMethod
    public void openContactForm(ReadableMap contact, Callback callback) {

        String givenName = contact.hasKey("givenName") ? contact.getString("givenName") : null;
        String middleName = contact.hasKey("middleName") ? contact.getString("middleName") : null;
        String displayName = contact.hasKey("displayName") ? contact.getString("displayName") : null;
        String familyName = contact.hasKey("familyName") ? contact.getString("familyName") : null;
        String prefix = contact.hasKey("prefix") ? contact.getString("prefix") : null;
        String suffix = contact.hasKey("suffix") ? contact.getString("suffix") : null;
        String company = contact.hasKey("company") ? contact.getString("company") : null;
        String jobTitle = contact.hasKey("jobTitle") ? contact.getString("jobTitle") : null;
        String department = contact.hasKey("department") ? contact.getString("department") : null;
        String thumbnailPath = contact.hasKey("thumbnailPath") ? contact.getString("thumbnailPath") : null;

        ReadableArray phoneNumbers = contact.hasKey("phoneNumbers") ? contact.getArray("phoneNumbers") : null;
        int numOfPhones = 0;
        String[] phones = null;
        Integer[] phonesLabels = null;
        if (phoneNumbers != null) {
            numOfPhones = phoneNumbers.size();
            phones = new String[numOfPhones];
            phonesLabels = new Integer[numOfPhones];
            for (int i = 0; i < numOfPhones; i++) {
                phones[i] = phoneNumbers.getMap(i).getString("number");
                String label = phoneNumbers.getMap(i).getString("label");
                phonesLabels[i] = mapStringToPhoneType(label);
            }
        }

        ReadableArray urlAddresses = contact.hasKey("urlAddresses") ? contact.getArray("urlAddresses") : null;
        int numOfUrls = 0;
        String[] urls = null;
        if (urlAddresses != null) {
            numOfUrls = urlAddresses.size();
            urls = new String[numOfUrls];
            for (int i = 0; i < numOfUrls; i++) {
                urls[i] = urlAddresses.getMap(i).getString("url");
            }
        }

        ReadableArray emailAddresses = contact.hasKey("emailAddresses") ? contact.getArray("emailAddresses") : null;
        int numOfEmails = 0;
        String[] emails = null;
        Integer[] emailsLabels = null;
        if (emailAddresses != null) {
            numOfEmails = emailAddresses.size();
            emails = new String[numOfEmails];
            emailsLabels = new Integer[numOfEmails];
            for (int i = 0; i < numOfEmails; i++) {
                emails[i] = emailAddresses.getMap(i).getString("email");
                String label = emailAddresses.getMap(i).getString("label");
                emailsLabels[i] = mapStringToEmailType(label);
            }
        }

        ReadableArray postalAddresses = contact.hasKey("postalAddresses") ? contact.getArray("postalAddresses") : null;
        int numOfPostalAddresses = 0;
        String[] postalAddressesStreet = null;
        String[] postalAddressesCity = null;
        String[] postalAddressesState = null;
        String[] postalAddressesRegion = null;
        String[] postalAddressesPostCode = null;
        String[] postalAddressesCountry = null;
        Integer[] postalAddressesLabel = null;
        if (postalAddresses != null) {
            numOfPostalAddresses = postalAddresses.size();
            postalAddressesStreet = new String[numOfPostalAddresses];
            postalAddressesCity = new String[numOfPostalAddresses];
            postalAddressesState = new String[numOfPostalAddresses];
            postalAddressesRegion = new String[numOfPostalAddresses];
            postalAddressesPostCode = new String[numOfPostalAddresses];
            postalAddressesCountry = new String[numOfPostalAddresses];
            postalAddressesLabel = new Integer[numOfPostalAddresses];
            for (int i = 0; i < numOfPostalAddresses; i++) {
                postalAddressesStreet[i] = postalAddresses.getMap(i).getString("street");
                postalAddressesCity[i] = postalAddresses.getMap(i).getString("city");
                postalAddressesState[i] = postalAddresses.getMap(i).getString("state");
                postalAddressesRegion[i] = postalAddresses.getMap(i).getString("region");
                postalAddressesPostCode[i] = postalAddresses.getMap(i).getString("postCode");
                postalAddressesCountry[i] = postalAddresses.getMap(i).getString("country");
                postalAddressesLabel[i] = mapStringToPostalAddressType(postalAddresses.getMap(i).getString("label"));
            }
        }

        ArrayList<ContentValues> contactData = new ArrayList<>();

        ContentValues name = new ContentValues();
        name.put(ContactsContract.Contacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.Identity.CONTENT_ITEM_TYPE);
        name.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, givenName);
        name.put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, familyName);
        name.put(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, middleName);
        name.put(ContactsContract.CommonDataKinds.StructuredName.PREFIX, prefix);
        name.put(ContactsContract.CommonDataKinds.StructuredName.SUFFIX, suffix);
        contactData.add(name);

        ContentValues organization = new ContentValues();
        organization.put(ContactsContract.Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
        organization.put(Organization.COMPANY, company);
        organization.put(Organization.TITLE, jobTitle);
        organization.put(Organization.DEPARTMENT, department);
        contactData.add(organization);

        for (int i = 0; i < numOfUrls; i++) {
            ContentValues url = new ContentValues();
            url.put(ContactsContract.Data.MIMETYPE, CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            url.put(CommonDataKinds.Website.URL, urls[i]);
            contactData.add(url);
        }

        for (int i = 0; i < numOfEmails; i++) {
            ContentValues email = new ContentValues();
            email.put(ContactsContract.Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE);
            email.put(CommonDataKinds.Email.TYPE, emailsLabels[i]);
            email.put(CommonDataKinds.Email.ADDRESS, emails[i]);
            contactData.add(email);
        }

        for (int i = 0; i < numOfPhones; i++) {
            ContentValues phone = new ContentValues();
            phone.put(ContactsContract.Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            phone.put(CommonDataKinds.Phone.TYPE, phonesLabels[i]);
            phone.put(CommonDataKinds.Phone.NUMBER, phones[i]);
            contactData.add(phone);
        }

        for (int i = 0; i < numOfPostalAddresses; i++) {
            ContentValues structuredPostal = new ContentValues();
            structuredPostal.put(ContactsContract.Data.MIMETYPE, CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
            structuredPostal.put(CommonDataKinds.StructuredPostal.STREET, postalAddressesStreet[i]);
            structuredPostal.put(CommonDataKinds.StructuredPostal.CITY, postalAddressesCity[i]);
            structuredPostal.put(CommonDataKinds.StructuredPostal.REGION, postalAddressesRegion[i]);
            structuredPostal.put(CommonDataKinds.StructuredPostal.COUNTRY, postalAddressesCountry[i]);
            structuredPostal.put(CommonDataKinds.StructuredPostal.POSTCODE, postalAddressesPostCode[i]);
            //No state column in StructuredPostal
            //structuredPostal.put(CommonDataKinds.StructuredPostal.???, postalAddressesState[i]);
            contactData.add(structuredPostal);
        }

        if(thumbnailPath != null && !thumbnailPath.isEmpty()) {
            Bitmap photo = getThumbnailBitmap(thumbnailPath);

            if(photo != null) {
                ContentValues thumbnail = new ContentValues();
                thumbnail.put(ContactsContract.Data.RAW_CONTACT_ID, 0);
                thumbnail.put(ContactsContract.Data.IS_SUPER_PRIMARY, 1);
                thumbnail.put(ContactsContract.CommonDataKinds.Photo.PHOTO, toByteArray(photo));
                thumbnail.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE );
                contactData.add(thumbnail);
            }
        }

        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, displayName);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, contactData);

        updateContactCallback = callback;
        getReactApplicationContext().startActivityForResult(intent, REQUEST_OPEN_CONTACT_FORM, Bundle.EMPTY);
    }

    /*
     * Open contact in native app
     */
    @ReactMethod
    public void openExistingContact(ReadableMap contact, Callback callback) {

        String recordID = contact.hasKey("recordID") ? contact.getString("recordID") : null;

        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, recordID);
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setDataAndType(uri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
            intent.putExtra("finishActivityOnSaveCompleted", true);

            updateContactCallback = callback;
            getReactApplicationContext().startActivityForResult(intent, REQUEST_OPEN_EXISTING_CONTACT, Bundle.EMPTY);

        } catch (Exception e) {
            callback.invoke(e.toString());
        }
    }

    /*
     * Adds contact to phone's addressbook
     */
    @ReactMethod
    public void addContact(ReadableMap contact, Callback callback) {
        String givenName = contact.hasKey("givenName") ? contact.getString("givenName") : null;
        String middleName = contact.hasKey("middleName") ? contact.getString("middleName") : null;
        String familyName = contact.hasKey("familyName") ? contact.getString("familyName") : null;
        String prefix = contact.hasKey("prefix") ? contact.getString("prefix") : null;
        String suffix = contact.hasKey("suffix") ? contact.getString("suffix") : null;
        String company = contact.hasKey("company") ? contact.getString("company") : null;
        String jobTitle = contact.hasKey("jobTitle") ? contact.getString("jobTitle") : null;
        String department = contact.hasKey("department") ? contact.getString("department") : null;
        String note = contact.hasKey("note") ? contact.getString("note") : null;
        String thumbnailPath = contact.hasKey("thumbnailPath") ? contact.getString("thumbnailPath") : null;

        ReadableArray phoneNumbers = contact.hasKey("phoneNumbers") ? contact.getArray("phoneNumbers") : null;
        int numOfPhones = 0;
        String[] phones = null;
        Integer[] phonesTypes = null;
        String[] phonesLabels = null;
        if (phoneNumbers != null) {
            numOfPhones = phoneNumbers.size();
            phones = new String[numOfPhones];
            phonesTypes = new Integer[numOfPhones];
            phonesLabels = new String[numOfPhones];
            for (int i = 0; i < numOfPhones; i++) {
                phones[i] = phoneNumbers.getMap(i).getString("number");
                String label = phoneNumbers.getMap(i).getString("label");
                phonesTypes[i] = mapStringToPhoneType(label);
                phonesLabels[i] = label;
            }
        }

        ReadableArray urlAddresses = contact.hasKey("urlAddresses") ? contact.getArray("urlAddresses") : null;
        int numOfUrls = 0;
        String[] urls = null;
        if (urlAddresses != null) {
            numOfUrls = urlAddresses.size();
            urls = new String[numOfUrls];
            for (int i = 0; i < numOfUrls; i++) {
                urls[i] = urlAddresses.getMap(i).getString("url");
            }
        }

        ReadableArray emailAddresses = contact.hasKey("emailAddresses") ? contact.getArray("emailAddresses") : null;
        int numOfEmails = 0;
        String[] emails = null;
        Integer[] emailsTypes = null;
        String[] emailsLabels = null;
        if (emailAddresses != null) {
            numOfEmails = emailAddresses.size();
            emails = new String[numOfEmails];
            emailsTypes = new Integer[numOfEmails];
            emailsLabels = new String[numOfEmails];
            for (int i = 0; i < numOfEmails; i++) {
                emails[i] = emailAddresses.getMap(i).getString("email");
                String label = emailAddresses.getMap(i).getString("label");
                emailsTypes[i] = mapStringToEmailType(label);
                emailsLabels[i] = label;
            }
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ContentProviderOperation.Builder op = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null);
        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                // .withValue(StructuredName.DISPLAY_NAME, name)
                .withValue(StructuredName.GIVEN_NAME, givenName)
                .withValue(StructuredName.MIDDLE_NAME, middleName)
                .withValue(StructuredName.FAMILY_NAME, familyName)
                .withValue(StructuredName.PREFIX, prefix)
                .withValue(StructuredName.SUFFIX, suffix);
        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, Note.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Note.NOTE, note);
        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
                .withValue(Organization.COMPANY, company)
                .withValue(Organization.TITLE, jobTitle)
                .withValue(Organization.DEPARTMENT, department);
        ops.add(op.build());

        //TODO not sure where to allow yields
        op.withYieldAllowed(true);

        for (int i = 0; i < numOfPhones; i++) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Phone.NUMBER, phones[i])
                    .withValue(CommonDataKinds.Phone.TYPE, phonesTypes[i])
                    .withValue(CommonDataKinds.Phone.LABEL, phonesLabels[i]);
            ops.add(op.build());
        }

        for (int i = 0; i < numOfUrls; i++) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Website.URL, urls[i]);
            ops.add(op.build());
        }

        for (int i = 0; i < numOfEmails; i++) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Email.ADDRESS, emails[i])
                    .withValue(CommonDataKinds.Email.TYPE, emailsTypes[i])
                    .withValue(CommonDataKinds.Email.LABEL, emailsLabels[i]);
            ops.add(op.build());
        }

        if(thumbnailPath != null && !thumbnailPath.isEmpty()) {
            Bitmap photo = getThumbnailBitmap(thumbnailPath);

            if(photo != null) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, toByteArray(photo))
                        .build());
            }
        }

        ReadableArray postalAddresses = contact.hasKey("postalAddresses") ? contact.getArray("postalAddresses") : null;
        if (postalAddresses != null) {
            for (int i = 0; i < postalAddresses.size(); i++) {
                ReadableMap address = postalAddresses.getMap(i);

                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(CommonDataKinds.StructuredPostal.TYPE, mapStringToPostalAddressType(address.getString("label")))
                        .withValue(CommonDataKinds.StructuredPostal.LABEL, address.getString("label"))
                        .withValue(CommonDataKinds.StructuredPostal.STREET, address.getString("street"))
                        .withValue(CommonDataKinds.StructuredPostal.CITY, address.getString("city"))
                        .withValue(CommonDataKinds.StructuredPostal.REGION, address.getString("state"))
                        .withValue(CommonDataKinds.StructuredPostal.POSTCODE, address.getString("postCode"))
                        .withValue(CommonDataKinds.StructuredPostal.COUNTRY, address.getString("country"));

                ops.add(op.build());
            }
        }

        Context ctx = getReactApplicationContext();
        try {
            ContentResolver cr = ctx.getContentResolver();
            ContentProviderResult[] result = cr.applyBatch(ContactsContract.AUTHORITY, ops);

            if(result != null && result.length > 0) {

                String rawId = String.valueOf(ContentUris.parseId(result[0].uri));

                ContactsProvider contactsProvider = new ContactsProvider(cr);
                WritableMap newlyAddedContact = contactsProvider.getContactByRawId(rawId);

                callback.invoke(null, newlyAddedContact); // success
            }
        } catch (Exception e) {
            callback.invoke(e.toString());
        }
    }

    public byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    /*
     * Update contact to phone's addressbook
     */
    @ReactMethod
    public void updateContact(ReadableMap contact, Callback callback) {

        String recordID = contact.hasKey("recordID") ? contact.getString("recordID") : null;
        String rawContactId = contact.hasKey("rawContactId") ? contact.getString("rawContactId") : null;

        if (rawContactId == null || recordID == null) {
            callback.invoke("Invalid recordId or rawContactId");
            return;
        }

        String givenName = contact.hasKey("givenName") ? contact.getString("givenName") : null;
        String middleName = contact.hasKey("middleName") ? contact.getString("middleName") : null;
        String familyName = contact.hasKey("familyName") ? contact.getString("familyName") : null;
        String prefix = contact.hasKey("prefix") ? contact.getString("prefix") : null;
        String suffix = contact.hasKey("suffix") ? contact.getString("suffix") : null;
        String company = contact.hasKey("company") ? contact.getString("company") : null;
        String jobTitle = contact.hasKey("jobTitle") ? contact.getString("jobTitle") : null;
        String department = contact.hasKey("department") ? contact.getString("department") : null;
        String note = contact.hasKey("note") ? contact.getString("note") : null;
        String thumbnailPath = contact.hasKey("thumbnailPath") ? contact.getString("thumbnailPath") : null;

        ReadableArray phoneNumbers = contact.hasKey("phoneNumbers") ? contact.getArray("phoneNumbers") : null;
        int numOfPhones = 0;
        String[] phones = null;
        Integer[] phonesTypes = null;
        String[] phonesLabels = null;
        String[] phoneIds = null;
        if (phoneNumbers != null) {
            numOfPhones = phoneNumbers.size();
            phones = new String[numOfPhones];
            phonesTypes = new Integer[numOfPhones];
            phonesLabels = new String[numOfPhones];
            phoneIds = new String[numOfPhones];
            for (int i = 0; i < numOfPhones; i++) {
                ReadableMap phoneMap = phoneNumbers.getMap(i);
                String phoneNumber = phoneMap.getString("number");
                String phoneLabel = phoneMap.getString("label");
                String phoneId = phoneMap.hasKey("id") ? phoneMap.getString("id") : null;
                phones[i] = phoneNumber;
                phonesTypes[i] = mapStringToPhoneType(phoneLabel);
                phonesLabels[i] = phoneLabel;
                phoneIds[i] = phoneId;
            }
        }

        ReadableArray urlAddresses = contact.hasKey("urlAddresses") ? contact.getArray("urlAddresses") : null;
        int numOfUrls = 0;
        String[] urls = null;
        String[] urlIds = null;

        if (urlAddresses != null) {
            numOfUrls = urlAddresses.size();
            urls = new String[numOfUrls];
            urlIds = new String[numOfUrls];
            for (int i = 0; i < numOfUrls; i++) {
                ReadableMap urlMap = urlAddresses.getMap(i);
                urls[i] = urlMap.getString("url");
                urlIds[i] = urlMap.hasKey("id") ? urlMap.getString("id") : null;
            }
        }

        ReadableArray emailAddresses = contact.hasKey("emailAddresses") ? contact.getArray("emailAddresses") : null;
        int numOfEmails = 0;
        String[] emails = null;
        Integer[] emailsTypes = null;
        String[] emailsLabels = null;
        String[] emailIds = null;

        if (emailAddresses != null) {
            numOfEmails = emailAddresses.size();
            emails = new String[numOfEmails];
            emailIds = new String[numOfEmails];
            emailsTypes = new Integer[numOfEmails];
            emailsLabels = new String[numOfEmails];
            for (int i = 0; i < numOfEmails; i++) {
                ReadableMap emailMap = emailAddresses.getMap(i);
                emails[i] = emailMap.getString("email");
                String label = emailMap.getString("label");
                emailsTypes[i] = mapStringToEmailType(label);
                emailsLabels[i] = label;
                emailIds[i] = emailMap.hasKey("id") ? emailMap.getString("id") : null;
            }
        }

        ReadableArray postalAddresses = contact.hasKey("postalAddresses") ? contact.getArray("postalAddresses") : null;
        int numOfPostalAddresses = 0;
        String[] postalAddressesStreet = null;
        String[] postalAddressesCity = null;
        String[] postalAddressesState = null;
        String[] postalAddressesRegion = null;
        String[] postalAddressesPostCode = null;
        String[] postalAddressesCountry = null;
        Integer[] postalAddressesType = null;
        String[] postalAddressesLabel = null;
        if (postalAddresses != null) {
            numOfPostalAddresses = postalAddresses.size();
            postalAddressesStreet = new String[numOfPostalAddresses];
            postalAddressesCity = new String[numOfPostalAddresses];
            postalAddressesState = new String[numOfPostalAddresses];
            postalAddressesRegion = new String[numOfPostalAddresses];
            postalAddressesPostCode = new String[numOfPostalAddresses];
            postalAddressesCountry = new String[numOfPostalAddresses];
            postalAddressesType = new Integer[numOfPostalAddresses];
            postalAddressesLabel = new String[numOfPostalAddresses];
            for (int i = 0; i < numOfPostalAddresses; i++) {
                String postalLabel = postalAddresses.getMap(i).getString("label");
                postalAddressesStreet[i] = postalAddresses.getMap(i).getString("street");
                postalAddressesCity[i] = postalAddresses.getMap(i).getString("city");
                postalAddressesState[i] = postalAddresses.getMap(i).getString("state");
                postalAddressesRegion[i] = postalAddresses.getMap(i).getString("region");
                postalAddressesPostCode[i] = postalAddresses.getMap(i).getString("postCode");
                postalAddressesCountry[i] = postalAddresses.getMap(i).getString("country");
                postalAddressesType[i] = mapStringToPostalAddressType(postalLabel);
                postalAddressesLabel[i] = postalLabel;
            }
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ContentProviderOperation.Builder op = ContentProviderOperation.newUpdate(RawContacts.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "=?", new String[]{String.valueOf(recordID)})
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null);
        ops.add(op.build());

        op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "=?", new String[]{String.valueOf(recordID)})
                .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.GIVEN_NAME, givenName)
                .withValue(StructuredName.MIDDLE_NAME, middleName)
                .withValue(StructuredName.FAMILY_NAME, familyName)
                .withValue(StructuredName.PREFIX, prefix)
                .withValue(StructuredName.SUFFIX, suffix);
        ops.add(op.build());

        op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[]{String.valueOf(recordID), Organization.CONTENT_ITEM_TYPE})
                .withValue(Organization.COMPANY, company)
                .withValue(Organization.TITLE, jobTitle)
                .withValue(Organization.DEPARTMENT, department);
        ops.add(op.build());

        op.withYieldAllowed(true);


        if (phoneNumbers != null) {
            // remove existing phoneNumbers first
            op = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        ContactsContract.Data.MIMETYPE  + "=? AND "+ ContactsContract.Data.RAW_CONTACT_ID + " = ?",
                        new String[]{String.valueOf(CommonDataKinds.Phone.CONTENT_ITEM_TYPE), String.valueOf(rawContactId)}
                    );
            ops.add(op.build());

            // add passed phonenumbers
            for (int i = 0; i < numOfPhones; i++) {
                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawContactId))
                        .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(CommonDataKinds.Phone.NUMBER, phones[i])
                        .withValue(CommonDataKinds.Phone.TYPE, phonesTypes[i])
                        .withValue(CommonDataKinds.Phone.LABEL, phonesLabels[i]);
                ops.add(op.build());
            }
        }

        for (int i = 0; i < numOfUrls; i++) {
            if (urlIds[i] == null) {
                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawContactId))
                        .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                        .withValue(CommonDataKinds.Website.URL, urls[i]);
            } else {
                op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(ContactsContract.Data._ID + "=?", new String[]{String.valueOf(urlIds[i])})
                        .withValue(CommonDataKinds.Website.URL, urls[i]);
            }
            ops.add(op.build());
        }

        if (emailAddresses != null){
            // remove existing emails first
            op = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        ContactsContract.Data.MIMETYPE  + "=? AND "+ ContactsContract.Data.RAW_CONTACT_ID + " = ?",
                        new String[]{String.valueOf(CommonDataKinds.Email.CONTENT_ITEM_TYPE), String.valueOf(rawContactId)}
                    );
            ops.add(op.build());

            // add passed email addresses
            for (int i = 0; i < numOfEmails; i++) {
                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawContactId))
                        .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(CommonDataKinds.Email.ADDRESS, emails[i])
                        .withValue(CommonDataKinds.Email.TYPE, emailsTypes[i])
                        .withValue(CommonDataKinds.Email.LABEL, emailsLabels[i]);
                ops.add(op.build());
            }
        }

        if(thumbnailPath != null && !thumbnailPath.isEmpty()) {
            Bitmap photo = getThumbnailBitmap(thumbnailPath);

            if(photo != null) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, toByteArray(photo))
                        .build());
            }
        }

        if (postalAddresses != null){
            //remove existing addresses
             op = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        ContactsContract.Data.MIMETYPE  + "=? AND "+ ContactsContract.Data.RAW_CONTACT_ID + " = ?",
                        new String[]{String.valueOf(CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE), String.valueOf(rawContactId)}
                    );
            ops.add(op.build());

            for (int i = 0; i < numOfPostalAddresses; i++) {
                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawContactId))
                        .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(CommonDataKinds.StructuredPostal.TYPE, postalAddressesType[i])
                        .withValue(CommonDataKinds.StructuredPostal.LABEL, postalAddressesLabel[i])
                        .withValue(CommonDataKinds.StructuredPostal.STREET, postalAddressesStreet[i])
                        .withValue(CommonDataKinds.StructuredPostal.CITY, postalAddressesCity[i])
                        .withValue(CommonDataKinds.StructuredPostal.REGION, postalAddressesState[i])
                        .withValue(CommonDataKinds.StructuredPostal.POSTCODE, postalAddressesPostCode[i])
                        .withValue(CommonDataKinds.StructuredPostal.COUNTRY, postalAddressesCountry[i]);
                ops.add(op.build());
            }
        }

        Context ctx = getReactApplicationContext();
        try {
            ContentResolver cr = ctx.getContentResolver();
            ContentProviderResult[] result = cr.applyBatch(ContactsContract.AUTHORITY, ops);

            if(result != null && result.length > 0) {

                ContactsProvider contactsProvider = new ContactsProvider(cr);
                WritableMap updatedContact = contactsProvider.getContactById(recordID);

                callback.invoke(null, updatedContact); // success
            }
        } catch (Exception e) {
            callback.invoke(e.toString());
        }
    }

    /*
     * Update contact to phone's addressbook
     */
    @ReactMethod
    public void deleteContact(ReadableMap contact, Callback callback) {

        String recordID = contact.hasKey("recordID") ? contact.getString("recordID") : null;

        try {
               Context ctx = getReactApplicationContext();

               Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,recordID);
               ContentResolver cr = ctx.getContentResolver();
               int deleted = cr.delete(uri,null,null);

               if(deleted > 0)
                 callback.invoke(null, recordID); // success
               else
                 callback.invoke(null, null); // something was wrong

        } catch (Exception e) {
            callback.invoke(e.toString(), null);
        }
    }
    /*
     * Check permission
     */
    @ReactMethod
    public void checkPermission(Callback callback) {
        callback.invoke(null, isPermissionGranted());
    }

    /*
     * Request permission
     */
    @ReactMethod
    public void requestPermission(Callback callback) {
        requestReadContactsPermission(callback);
    }

    private void requestReadContactsPermission(Callback callback) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            callback.invoke(null, PERMISSION_DENIED);
            return;
        }

        if (isPermissionGranted().equals(PERMISSION_AUTHORIZED)) {
            callback.invoke(null, PERMISSION_AUTHORIZED);
            return;
        }

        requestCallback = callback;
        ActivityCompat.requestPermissions(currentActivity, new String[]{PERMISSION_READ_CONTACTS}, PERMISSION_REQUEST_CODE);
    }

    protected static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        if (requestCallback == null) {
            return;
        }

        if (requestCode != PERMISSION_REQUEST_CODE) {
            requestCallback.invoke(null, PERMISSION_DENIED);
            return;
        }

        Hashtable<String, Boolean> results = new Hashtable<>();
        for (int i = 0; i < permissions.length; i++) {
            results.put(permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED);
        }

        if (results.containsKey(PERMISSION_READ_CONTACTS) && results.get(PERMISSION_READ_CONTACTS)) {
            requestCallback.invoke(null, PERMISSION_AUTHORIZED);
        } else {
            requestCallback.invoke(null, PERMISSION_DENIED);
        }

        requestCallback = null;
    }

    /*
     * Check if READ_CONTACTS permission is granted
     */
    private String isPermissionGranted() {
        // return -1 for denied and 1
        int res = getReactApplicationContext().checkCallingOrSelfPermission(PERMISSION_READ_CONTACTS);
        return (res == PackageManager.PERMISSION_GRANTED) ? PERMISSION_AUTHORIZED : PERMISSION_DENIED;
    }

    /*
     * TODO support all phone types
     * http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Phone.html
     */
    private int mapStringToPhoneType(String label) {
        int phoneType;
        switch (label) {
            case "home":
                phoneType = CommonDataKinds.Phone.TYPE_HOME;
                break;
            case "work":
                phoneType = CommonDataKinds.Phone.TYPE_WORK;
                break;
            case "mobile":
                phoneType = CommonDataKinds.Phone.TYPE_MOBILE;
                break;
            case "main":
                phoneType = CommonDataKinds.Phone.TYPE_MAIN;
                break;
            case "work fax":
                phoneType = CommonDataKinds.Phone.TYPE_FAX_WORK;
                break;
            case "home fax":
                phoneType = CommonDataKinds.Phone.TYPE_FAX_HOME;
                break;
            case "pager":
                phoneType = CommonDataKinds.Phone.TYPE_PAGER;
                break;
            case "work_pager":
                phoneType = CommonDataKinds.Phone.TYPE_WORK_PAGER;
                break;
            case "work_mobile":
                phoneType = CommonDataKinds.Phone.TYPE_WORK_MOBILE;
                break;
            default:
                phoneType = CommonDataKinds.Phone.TYPE_CUSTOM;
                break;
        }
        return phoneType;
    }

    /*
     * TODO support TYPE_CUSTOM
     * http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Email.html
     */
    private int mapStringToEmailType(String label) {
        int emailType;
        switch (label) {
            case "home":
                emailType = CommonDataKinds.Email.TYPE_HOME;
                break;
            case "work":
                emailType = CommonDataKinds.Email.TYPE_WORK;
                break;
            case "mobile":
                emailType = CommonDataKinds.Email.TYPE_MOBILE;
                break;
            default:
                emailType = CommonDataKinds.Email.TYPE_CUSTOM;
                break;
        }
        return emailType;
    }

    private int mapStringToPostalAddressType(String label) {
        int postalAddressType;
        switch (label) {
            case "home":
                postalAddressType = CommonDataKinds.StructuredPostal.TYPE_HOME;
                break;
            case "work":
                postalAddressType = CommonDataKinds.StructuredPostal.TYPE_WORK;
                break;
            default:
                postalAddressType = CommonDataKinds.StructuredPostal.TYPE_CUSTOM;
                break;
        }
        return postalAddressType;
    }

    @Override
    public String getName() {
        return "Contacts";
    }

    /*
     * Required for ActivityEventListener
     */
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_OPEN_CONTACT_FORM && requestCode != REQUEST_OPEN_EXISTING_CONTACT) {
            return;
        }

        if (updateContactCallback == null) {
            return;
        }

        if (resultCode != Activity.RESULT_OK) {
            updateContactCallback.invoke(null, null); // user probably pressed cancel
            updateContactCallback = null;
            return;
        }

        if (data == null) {
            updateContactCallback.invoke("Error received activity result with no data!", null);
            updateContactCallback = null;
            return;
        }

        try {
            Uri contactUri = data.getData();

            if (contactUri == null) {
                updateContactCallback.invoke("Error wrong data. No content uri found!", null); // something was wrong
                updateContactCallback = null;
                return;
            }

            Context ctx = getReactApplicationContext();
            ContentResolver cr = ctx.getContentResolver();
            ContactsProvider contactsProvider = new ContactsProvider(cr);
            WritableMap newlyModifiedContact = contactsProvider.getContactById(contactUri.getLastPathSegment());

            updateContactCallback.invoke(null, newlyModifiedContact); // success
        } catch (Exception e) {
            updateContactCallback.invoke(e.getMessage(), null);
        }
        updateContactCallback = null;
    }

    /*
     * Required for ActivityEventListener
     */
    @Override
    public void onNewIntent(Intent intent) {
    }

}
