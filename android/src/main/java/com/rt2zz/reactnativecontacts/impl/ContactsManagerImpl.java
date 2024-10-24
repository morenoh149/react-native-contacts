package com.rt2zz.reactnativecontacts.impl;

import android.Manifest;
import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.rt2zz.reactnativecontacts.ContactsProvider;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ContactsManagerImpl {

    private static final String PERMISSION_DENIED = "denied";
    private static final String PERMISSION_AUTHORIZED = "authorized";
    private static final String PERMISSION_READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    private static final int PERMISSION_REQUEST_CODE = 888;
    private static final int REQUEST_OPEN_CONTACT_FORM = 52941;
    private static final int REQUEST_OPEN_EXISTING_CONTACT = 52942;

    private static Promise updateContactPromise;
    private static Promise requestPromise;

    private final ReactApplicationContext reactApplicationContext;

    private Executor executor;


    public ContactsManagerImpl(ReactApplicationContext reactContext, boolean useSerialExecutor) {
        this.reactApplicationContext = reactContext;
        this.executor = initializeExecutor(useSerialExecutor);
    }

    private Executor initializeExecutor(boolean useSerialExecutor){
        if(useSerialExecutor){
            return Executors.newSingleThreadExecutor(); //AsyncTask.SERIAL_EXECUTOR
        }
        return Executors.newCachedThreadPool();
    }

    protected Executor getExecutor() {
        return executor;
    }

    @NonNull
    protected ReactApplicationContext getReactApplicationContext() {
        return Objects.requireNonNull(reactApplicationContext, "Context not initialized");
    }

    @Nullable
    protected final Activity getCurrentActivity() {
        return reactApplicationContext.getCurrentActivity();
    }

    /*
     * Returns all contactable records on phone
     * queries CommonDataKinds.Contactables to get phones and emails
     */
    public void getAll(Promise promise) {
        getAllContacts(promise);
    }

    /**
     * Introduced for iOS compatibility. Same as getAll
     *
     * @param promise promise
     */
    public void getAllWithoutPhotos(Promise promise) {
        getAllContacts(promise);
    }

    /**
     * Retrieves contacts.
     * Uses raw URI when <code>rawUri</code> is <code>true</code>, makes assets copy
     * otherwise.
     */
    private void getAllContacts(final Promise promise) {
        getExecutor().execute(() -> {
            Context context = getReactApplicationContext();
            ContentResolver cr = context.getContentResolver();
            ContactsProvider contactsProvider = new ContactsProvider(cr);
            WritableArray contacts = contactsProvider.getContacts();
            promise.resolve(contacts);
        });
    }

    public void getCount(final Promise promise) {
        getExecutor().execute(() -> {
            Context context = getReactApplicationContext();
            ContentResolver cr = context.getContentResolver();
            ContactsProvider contactsProvider = new ContactsProvider(cr);
            try {
                Integer contacts = contactsProvider.getContactsCount();
                promise.resolve(contacts);
            } catch (Exception e) {
                promise.reject(e);
            }
        });
    }

    /**
     * Retrieves contacts matching String.
     * Uses raw URI when <code>rawUri</code> is <code>true</code>, makes assets copy
     * otherwise.
     *
     * @param searchString String to match
     */
    public void getContactsMatchingString(final String searchString, final Promise promise) {
        getExecutor().execute(() -> {
            Context context = getReactApplicationContext();
            ContentResolver cr = context.getContentResolver();
            ContactsProvider contactsProvider = new ContactsProvider(cr);
            WritableArray contacts = contactsProvider.getContactsMatchingString(searchString);
            promise.resolve(contacts);
        });
    }

    /**
     * Retrieves contacts matching a phone number.
     * Uses raw URI when <code>rawUri</code> is <code>true</code>, makes assets copy
     * otherwise.
     *
     * @param phoneNumber phone number to match
     */
    public void getContactsByPhoneNumber(final String phoneNumber, final Promise promise) {
        getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Context context = getReactApplicationContext();
                ContentResolver cr = context.getContentResolver();
                ContactsProvider contactsProvider = new ContactsProvider(cr);
                WritableArray contacts = contactsProvider.getContactsByPhoneNumber(phoneNumber);
                promise.resolve(contacts);
            }
        });
    }

    /**
     * Retrieves contacts matching an email address.
     * Uses raw URI when <code>rawUri</code> is <code>true</code>, makes assets copy
     * otherwise.
     *
     * @param emailAddress email address to match
     */
    public void getContactsByEmailAddress(final String emailAddress, final Promise promise) {
        getExecutor().execute(() -> {
            Context context = getReactApplicationContext();
            ContentResolver cr = context.getContentResolver();
            ContactsProvider contactsProvider = new ContactsProvider(cr);
            WritableArray contacts = contactsProvider.getContactsByEmailAddress(emailAddress);
            promise.resolve(contacts);
        });
    }

    /**
     * Retrieves <code>thumbnailPath</code> for contact, or <code>null</code> if not
     * available.
     *
     * @param contactId contact identifier, <code>recordID</code>
     */
    public void getPhotoForId(final String contactId, final Promise promise) {
        getExecutor().execute(() -> {
            Context context = getReactApplicationContext();
            ContentResolver cr = context.getContentResolver();
            ContactsProvider contactsProvider = new ContactsProvider(cr);
            String photoUri = contactsProvider.getPhotoUriFromContactId(contactId);
            promise.resolve(photoUri);
        });
    }

    /**
     * Retrieves <code>contact</code> for contact, or <code>null</code> if not
     * available.
     *
     * @param contactId contact identifier, <code>recordID</code>
     */
    public void getContactById(final String contactId, final Promise promise) {
        getExecutor().execute(() -> {
            Context context = getReactApplicationContext();
            ContentResolver cr = context.getContentResolver();
            ContactsProvider contactsProvider = new ContactsProvider(cr);
            WritableMap contact = contactsProvider.getContactById(contactId);
            promise.resolve(contact);
        });
    }


    public void writePhotoToPath(final String contactId, final String file, final Promise promise) {
        getExecutor().execute(() -> {
            Context context = getReactApplicationContext();
            ContentResolver cr = context.getContentResolver();

            Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, Long.parseLong(contactId));
            InputStream inputStream = ContactsContract.Contacts.openContactPhotoInputStream(cr, uri);
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(file);
                BitmapFactory.decodeStream(inputStream).compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                promise.resolve(true);
            } catch (FileNotFoundException e) {
                promise.reject(e.toString());
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private Bitmap getThumbnailBitmap(String thumbnailPath) {
        // Thumbnail from absolute path
        Bitmap photo = BitmapFactory.decodeFile(thumbnailPath);

        if (photo == null) {
            // Try to find the thumbnail from assets
            AssetManager assetManager = getReactApplicationContext().getAssets();
            InputStream inputStream = null;
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
    public void openContactForm(ReadableMap contact, Promise promise) {

        String givenName = contact.hasKey("givenName") ? contact.getString("givenName") : null;
        String middleName = contact.hasKey("middleName") ? contact.getString("middleName") : null;
        String displayName = contact.hasKey("displayName") ? contact.getString("displayName") : null;
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
        String[] phonesLabels = null;
        Integer[] phonesLabelsTypes = null;
        if (phoneNumbers != null) {
            numOfPhones = phoneNumbers.size();
            phones = new String[numOfPhones];
            phonesLabels = new String[numOfPhones];
            phonesLabelsTypes = new Integer[numOfPhones];
            for (int i = 0; i < numOfPhones; i++) {
                phones[i] = phoneNumbers.getMap(i).getString("number");
                String label = phoneNumbers.getMap(i).getString("label");
                phonesLabels[i] = label;
                phonesLabelsTypes[i] = mapStringToPhoneType(label);
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
        String[] postalAddressesFormattedAddress = null;
        String[] postalAddressesLabel = null;
        Integer[] postalAddressesType = null;

        if (postalAddresses != null) {
            numOfPostalAddresses = postalAddresses.size();
            postalAddressesStreet = new String[numOfPostalAddresses];
            postalAddressesCity = new String[numOfPostalAddresses];
            postalAddressesState = new String[numOfPostalAddresses];
            postalAddressesRegion = new String[numOfPostalAddresses];
            postalAddressesPostCode = new String[numOfPostalAddresses];
            postalAddressesCountry = new String[numOfPostalAddresses];
            postalAddressesFormattedAddress = new String[numOfPostalAddresses];
            postalAddressesLabel = new String[numOfPostalAddresses];
            postalAddressesType = new Integer[numOfPostalAddresses];
            for (int i = 0; i < numOfPostalAddresses; i++) {
                postalAddressesStreet[i] = postalAddresses.getMap(i).getString("street");
                postalAddressesCity[i] = postalAddresses.getMap(i).getString("city");
                postalAddressesState[i] = postalAddresses.getMap(i).getString("state");
                postalAddressesRegion[i] = postalAddresses.getMap(i).getString("region");
                postalAddressesPostCode[i] = postalAddresses.getMap(i).getString("postCode");
                postalAddressesCountry[i] = postalAddresses.getMap(i).getString("country");
                postalAddressesFormattedAddress[i] = postalAddresses.getMap(i).getString("formattedAddress");
                postalAddressesLabel[i] = postalAddresses.getMap(i).getString("label");
                postalAddressesType[i] = mapStringToPostalAddressType(postalAddresses.getMap(i).getString("label"));
            }
        }

        ReadableArray imAddresses = contact.hasKey("imAddresses") ? contact.getArray("imAddresses") : null;
        int numOfIMAddresses = 0;
        String[] imAccounts = null;
        String[] imProtocols = null;
        if (imAddresses != null) {
            numOfIMAddresses = imAddresses.size();
            imAccounts = new String[numOfIMAddresses];
            imProtocols = new String[numOfIMAddresses];
            for (int i = 0; i < numOfIMAddresses; i++) {
                imAccounts[i] = imAddresses.getMap(i).getString("username");
                imProtocols[i] = imAddresses.getMap(i).getString("service");
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
        organization.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
        organization.put(ContactsContract.CommonDataKinds.Organization.COMPANY, company);
        organization.put(ContactsContract.CommonDataKinds.Organization.TITLE, jobTitle);
        organization.put(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, department);
        contactData.add(organization);

        for (int i = 0; i < numOfUrls; i++) {
            ContentValues url = new ContentValues();
            url.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
            url.put(ContactsContract.CommonDataKinds.Website.URL, urls[i]);
            contactData.add(url);
        }

        for (int i = 0; i < numOfEmails; i++) {
            ContentValues email = new ContentValues();
            email.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
            email.put(ContactsContract.CommonDataKinds.Email.TYPE, emailsLabels[i]);
            email.put(ContactsContract.CommonDataKinds.Email.ADDRESS, emails[i]);
            contactData.add(email);
        }

        for (int i = 0; i < numOfPhones; i++) {
            ContentValues phone = new ContentValues();
            phone.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            phone.put(ContactsContract.CommonDataKinds.Phone.TYPE, phonesLabelsTypes[i]);
            phone.put(ContactsContract.CommonDataKinds.Phone.LABEL, phonesLabels[i]);
            phone.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phones[i]);

            contactData.add(phone);
        }

        for (int i = 0; i < numOfPostalAddresses; i++) {
            ContentValues structuredPostal = new ContentValues();
            structuredPostal.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
            structuredPostal.put(ContactsContract.CommonDataKinds.StructuredPostal.STREET, postalAddressesStreet[i]);
            structuredPostal.put(ContactsContract.CommonDataKinds.StructuredPostal.CITY, postalAddressesCity[i]);
            structuredPostal.put(ContactsContract.CommonDataKinds.StructuredPostal.REGION, postalAddressesRegion[i]);
            structuredPostal.put(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, postalAddressesCountry[i]);
            structuredPostal.put(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, postalAddressesPostCode[i]);
            structuredPostal.put(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                    postalAddressesFormattedAddress[i]);
            structuredPostal.put(ContactsContract.CommonDataKinds.StructuredPostal.LABEL, postalAddressesLabel[i]);
            structuredPostal.put(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, postalAddressesType[i]);
            // No state column in StructuredPostal
            // structuredPostal.put(CommonDataKinds.StructuredPostal.???,
            // postalAddressesState[i]);
            contactData.add(structuredPostal);
        }

        for (int i = 0; i < numOfIMAddresses; i++) {
            ContentValues imAddress = new ContentValues();
            imAddress.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);
            imAddress.put(ContactsContract.CommonDataKinds.Im.DATA, imAccounts[i]);
            imAddress.put(ContactsContract.CommonDataKinds.Im.TYPE, ContactsContract.CommonDataKinds.Im.TYPE_HOME);
            imAddress.put(ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM);
            imAddress.put(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, imProtocols[i]);
            contactData.add(imAddress);
        }

        if (note != null) {
            ContentValues structuredNote = new ContentValues();
            structuredNote.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE);
            structuredNote.put(ContactsContract.CommonDataKinds.Note.NOTE, note);
            contactData.add(structuredNote);
        }

        if (thumbnailPath != null && !thumbnailPath.isEmpty()) {
            Bitmap photo = getThumbnailBitmap(thumbnailPath);

            if (photo != null) {
                ContentValues thumbnail = new ContentValues();
                thumbnail.put(ContactsContract.Data.RAW_CONTACT_ID, 0);
                thumbnail.put(ContactsContract.Data.IS_SUPER_PRIMARY, 1);
                thumbnail.put(ContactsContract.CommonDataKinds.Photo.PHOTO, toByteArray(photo));
                thumbnail.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);
                contactData.add(thumbnail);
            }
        }

        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, displayName);
        intent.putExtra("finishActivityOnSaveCompleted", true);
        intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, contactData);

        updateContactPromise = promise;
        getReactApplicationContext().startActivityForResult(intent, REQUEST_OPEN_CONTACT_FORM, Bundle.EMPTY);
    }

    /*
     * Open contact in native app
     */
    public void openExistingContact(ReadableMap contact, Promise promise) {

        String recordID = contact.hasKey("recordID") ? contact.getString("recordID") : null;

        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, recordID);
            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setDataAndType(uri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
            intent.putExtra("finishActivityOnSaveCompleted", true);

            updateContactPromise = promise;
            getReactApplicationContext().startActivityForResult(intent, REQUEST_OPEN_EXISTING_CONTACT, Bundle.EMPTY);

        } catch (Exception e) {
            promise.reject(e.toString());
        }
    }

    /*
     * View contact in native app
     */
    public void viewExistingContact(ReadableMap contact, Promise promise) {

        String recordID = contact.hasKey("recordID") ? contact.getString("recordID") : null;

        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, recordID);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
            intent.putExtra("finishActivityOnSaveCompleted", true);

            updateContactPromise = promise;
            getReactApplicationContext().startActivityForResult(intent, REQUEST_OPEN_EXISTING_CONTACT, Bundle.EMPTY);

        } catch (Exception e) {
            promise.reject(e.toString());
        }
    }

    /*
     * Edit contact in native app
     */
    public void editExistingContact(ReadableMap contact, Promise promise) {

        String recordID = contact.hasKey("recordID") ? contact.getString("recordID") : null;

        try {
            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, recordID);

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

            ArrayList<ContentValues> contactData = new ArrayList<>();
            for (int i = 0; i < numOfPhones; i++) {
                ContentValues phone = new ContentValues();
                phone.put(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
                phone.put(ContactsContract.CommonDataKinds.Phone.TYPE, phonesLabels[i]);
                phone.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phones[i]);
                contactData.add(phone);
            }

            Intent intent = new Intent(Intent.ACTION_EDIT);
            intent.setDataAndType(uri, ContactsContract.Contacts.CONTENT_ITEM_TYPE);
            intent.putExtra("finishActivityOnSaveCompleted", true);
            intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, contactData);

            updateContactPromise = promise;
            getReactApplicationContext().startActivityForResult(intent, REQUEST_OPEN_EXISTING_CONTACT, Bundle.EMPTY);

        } catch (Exception e) {
            promise.reject(e.toString());
        }
    }

    /*
     * Adds contact to phone's addressbook
     */
    public void addContact(ReadableMap contact, Promise promise) {
        if (contact == null) {
            promise.reject("New contact cannot be null.");
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

        ReadableArray imAddresses = contact.hasKey("imAddresses") ? contact.getArray("imAddresses") : null;
        int numOfIMAddresses = 0;
        String[] imAccounts = null;
        String[] imProtocols = null;
        if (imAddresses != null) {
            numOfIMAddresses = imAddresses.size();
            imAccounts = new String[numOfIMAddresses];
            imProtocols = new String[numOfIMAddresses];
            for (int i = 0; i < numOfIMAddresses; i++) {
                imAccounts[i] = imAddresses.getMap(i).getString("username");
                imProtocols[i] = imAddresses.getMap(i).getString("service");
            }
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ContentProviderOperation.Builder op = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null);
        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                // .withValue(StructuredName.DISPLAY_NAME, name)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, givenName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, middleName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, familyName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.PREFIX, prefix)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.SUFFIX, suffix);
        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Note.NOTE, note);
        ops.add(op.build());

        op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, jobTitle)
                .withValue(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, department);
        ops.add(op.build());

        // TODO not sure where to allow yields
        op.withYieldAllowed(true);

        for (int i = 0; i < numOfPhones; i++) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phones[i])
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phonesTypes[i])
                    .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, phonesLabels[i]);
            ops.add(op.build());
        }

        for (int i = 0; i < numOfUrls; i++) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Website.URL, urls[i]);
            ops.add(op.build());
        }

        for (int i = 0; i < numOfEmails; i++) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, emails[i])
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailsTypes[i])
                    .withValue(ContactsContract.CommonDataKinds.Email.LABEL, emailsLabels[i]);
            ops.add(op.build());
        }

        if (thumbnailPath != null && !thumbnailPath.isEmpty()) {
            Bitmap photo = getThumbnailBitmap(thumbnailPath);

            if (photo != null) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
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
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                                mapStringToPostalAddressType(address.getString("label")))
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.LABEL, address.getString("label"))
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, address.getString("street"))
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, address.getString("city"))
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, address.getString("state"))
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, address.getString("postCode"))
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, address.getString("country"));

                ops.add(op.build());
            }
        }

        for (int i = 0; i < numOfIMAddresses; i++) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Im.DATA, imAccounts[i])
                    .withValue(ContactsContract.CommonDataKinds.Im.TYPE, ContactsContract.CommonDataKinds.Im.TYPE_HOME)
                    .withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM)
                    .withValue(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, imProtocols[i]);
            ops.add(op.build());
        }

        Context ctx = getReactApplicationContext();
        try {
            ContentResolver cr = ctx.getContentResolver();
            ContentProviderResult[] result = cr.applyBatch(ContactsContract.AUTHORITY, ops);

            if (result != null && result.length > 0) {

                String rawId = String.valueOf(ContentUris.parseId(result[0].uri));

                ContactsProvider contactsProvider = new ContactsProvider(cr);
                WritableMap newlyAddedContact = contactsProvider.getContactByRawId(rawId);

                promise.resolve(newlyAddedContact); // success
            }
        } catch (Exception e) {
            promise.reject(e.toString());
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
    public void updateContact(ReadableMap contact, Promise promise) {

        String recordID = contact.hasKey("recordID") ? contact.getString("recordID") : null;
        String rawContactId = contact.hasKey("rawContactId") ? contact.getString("rawContactId") : null;

        if (rawContactId == null || recordID == null) {
            promise.reject("Invalid recordId or rawContactId");
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
                String postalLabel = getValueFromKey(postalAddresses.getMap(i), "label");
                postalAddressesStreet[i] = getValueFromKey(postalAddresses.getMap(i), "street");
                postalAddressesCity[i] = getValueFromKey(postalAddresses.getMap(i), "city");
                postalAddressesState[i] = getValueFromKey(postalAddresses.getMap(i), "state");
                postalAddressesRegion[i] = getValueFromKey(postalAddresses.getMap(i), "region");
                postalAddressesPostCode[i] = getValueFromKey(postalAddresses.getMap(i), "postCode");
                postalAddressesCountry[i] = getValueFromKey(postalAddresses.getMap(i), "country");
                postalAddressesType[i] = mapStringToPostalAddressType(postalLabel);
                postalAddressesLabel[i] = postalLabel;
            }
        }

        ReadableArray imAddresses = contact.hasKey("imAddresses") ? contact.getArray("imAddresses") : null;
        int numOfIMAddresses = 0;
        String[] imAccounts = null;
        String[] imProtocols = null;
        String[] imAddressIds = null;

        if (imAddresses != null) {
            numOfIMAddresses = imAddresses.size();
            imAccounts = new String[numOfIMAddresses];
            imProtocols = new String[numOfIMAddresses];
            imAddressIds = new String[numOfIMAddresses];
            for (int i = 0; i < numOfIMAddresses; i++) {
                ReadableMap imAddressMap = imAddresses.getMap(i);
                imAccounts[i] = imAddressMap.getString("username");
                imProtocols[i] = imAddressMap.getString("service");
                imAddressIds[i] = imAddressMap.hasKey("id") ? imAddressMap.getString("id") : null;
            }
        }

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ContentProviderOperation.Builder op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "=?", new String[] { String.valueOf(recordID) })
                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, givenName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, middleName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, familyName)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.PREFIX, prefix)
                .withValue(ContactsContract.CommonDataKinds.StructuredName.SUFFIX, suffix);
        ops.add(op.build());

        op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                        new String[] { String.valueOf(recordID), ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE })
                .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, company)
                .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, jobTitle)
                .withValue(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, department);
        ops.add(op.build());

        op.withYieldAllowed(true);

        if (phoneNumbers != null) {
            // remove existing phoneNumbers first
            op = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                            ContactsContract.Data.MIMETYPE + "=? AND " + ContactsContract.Data.RAW_CONTACT_ID + " = ?",
                            new String[] { String.valueOf(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE),
                                    String.valueOf(rawContactId) });
            ops.add(op.build());

            // add passed phonenumbers
            for (int i = 0; i < numOfPhones; i++) {
                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawContactId))
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phones[i])
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phonesTypes[i])
                        .withValue(ContactsContract.CommonDataKinds.Phone.LABEL, phonesLabels[i]);
                ops.add(op.build());
            }
        }

        for (int i = 0; i < numOfUrls; i++) {
            if (urlIds[i] == null) {
                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawContactId))
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Website.URL, urls[i]);
            } else {
                op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(ContactsContract.Data._ID + "=?", new String[] { String.valueOf(urlIds[i]) })
                        .withValue(ContactsContract.CommonDataKinds.Website.URL, urls[i]);
            }
            ops.add(op.build());
        }

        if (emailAddresses != null) {
            // remove existing emails first
            op = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                            ContactsContract.Data.MIMETYPE + "=? AND " + ContactsContract.Data.RAW_CONTACT_ID + " = ?",
                            new String[] { String.valueOf(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE),
                                    String.valueOf(rawContactId) });
            ops.add(op.build());

            // add passed email addresses
            for (int i = 0; i < numOfEmails; i++) {
                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawContactId))
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, emails[i])
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, emailsTypes[i])
                        .withValue(ContactsContract.CommonDataKinds.Email.LABEL, emailsLabels[i]);
                ops.add(op.build());
            }
        }

        // remove existing note first
        op = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                .withSelection(
                        ContactsContract.Data.MIMETYPE + "=? AND " + ContactsContract.Data.RAW_CONTACT_ID + " = ?",
                        new String[] { String.valueOf(ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE), String.valueOf(rawContactId) });
        ops.add(op.build());

        if (note != null) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawContactId))
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Note.NOTE, note);
            ops.add(op.build());
        }

        if (thumbnailPath != null && !thumbnailPath.isEmpty()) {
            Bitmap photo = getThumbnailBitmap(thumbnailPath);

            if (photo != null) {
                ops.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE,
                                ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, toByteArray(photo))
                        .build());
            }
        }

        if (postalAddresses != null) {
            // remove existing addresses
            op = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                            ContactsContract.Data.MIMETYPE + "=? AND " + ContactsContract.Data.RAW_CONTACT_ID + " = ?",
                            new String[] { String.valueOf(ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE),
                                    String.valueOf(rawContactId) });
            ops.add(op.build());

            for (int i = 0; i < numOfPostalAddresses; i++) {
                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawContactId))
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, postalAddressesType[i])
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.LABEL, postalAddressesLabel[i])
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, postalAddressesStreet[i])
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, postalAddressesCity[i])
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, postalAddressesState[i])
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, postalAddressesPostCode[i])
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, postalAddressesCountry[i]);
                ops.add(op.build());
            }
        }

        if (imAddresses != null) {
            // remove existing IM addresses
            op = ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                            ContactsContract.Data.MIMETYPE + "=? AND " + ContactsContract.Data.RAW_CONTACT_ID + " = ?",
                            new String[] { String.valueOf(ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE),
                                    String.valueOf(rawContactId) });
            ops.add(op.build());

            // add passed IM addresses
            for (int i = 0; i < numOfIMAddresses; i++) {
                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValue(ContactsContract.Data.RAW_CONTACT_ID, String.valueOf(rawContactId))
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Im.DATA, imAccounts[i])
                        .withValue(ContactsContract.CommonDataKinds.Im.TYPE, ContactsContract.CommonDataKinds.Im.TYPE_HOME)
                        .withValue(ContactsContract.CommonDataKinds.Im.PROTOCOL, ContactsContract.CommonDataKinds.Im.PROTOCOL_CUSTOM)
                        .withValue(ContactsContract.CommonDataKinds.Im.CUSTOM_PROTOCOL, imProtocols[i]);
                ops.add(op.build());
            }
        }

        Context ctx = getReactApplicationContext();
        try {
            ContentResolver cr = ctx.getContentResolver();
            ContentProviderResult[] result = cr.applyBatch(ContactsContract.AUTHORITY, ops);

            if (result != null && result.length > 0) {

                ContactsProvider contactsProvider = new ContactsProvider(cr);
                WritableMap updatedContact = contactsProvider.getContactById(recordID);

                promise.resolve(updatedContact); // success
            }
        } catch (Exception e) {
            promise.reject(e.toString());
        }
    }

    /*
     * Update contact to phone's addressbook
     */
    public void deleteContact(ReadableMap contact, Promise promise) {

        String recordID = contact.hasKey("recordID") ? contact.getString("recordID") : null;

        try {
            Context ctx = getReactApplicationContext();

            Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, recordID);
            ContentResolver cr = ctx.getContentResolver();
            int deleted = cr.delete(uri, null, null);

            if (deleted > 0)
                promise.resolve(recordID); // success
            else
                promise.resolve(null); // something was wrong

        } catch (Exception e) {
            promise.reject(e.toString());
        }
    }

    /*
     * Check permission
     */
    public void checkPermission(Promise promise) {
        promise.resolve(isPermissionGranted());
    }

    /*
     * Request permission
     */
    public void requestPermission(Promise promise) {
        requestReadContactsPermission(promise);
    }

    /*
     * Enable note usage
     */
    public void iosEnableNotesUsage(boolean enabled) {
        // this method is only needed for iOS
    }

    private void requestReadContactsPermission(Promise promise) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            promise.reject(PERMISSION_DENIED);
            return;
        }

        if (isPermissionGranted().equals(PERMISSION_AUTHORIZED)) {
            promise.resolve(PERMISSION_AUTHORIZED);
            return;
        }

        requestPromise = promise;
        ActivityCompat.requestPermissions(currentActivity, new String[] { PERMISSION_READ_CONTACTS },
                PERMISSION_REQUEST_CODE);
    }

    protected static void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                                     @NonNull int[] grantResults) {
        if (requestPromise == null) {
            return;
        }

        if (requestCode != PERMISSION_REQUEST_CODE) {
            requestPromise.resolve(PERMISSION_DENIED);
            return;
        }

        Hashtable<String, Boolean> results = new Hashtable<>();
        for (int i = 0; i < permissions.length; i++) {
            results.put(permissions[i], grantResults[i] == PackageManager.PERMISSION_GRANTED);
        }

        if (results.containsKey(PERMISSION_READ_CONTACTS) && results.get(PERMISSION_READ_CONTACTS)) {
            requestPromise.resolve(PERMISSION_AUTHORIZED);
        } else {
            requestPromise.resolve(PERMISSION_DENIED);
        }

        requestPromise = null;
    }

    /*
     * Get string value from key
     */
    private String getValueFromKey(ReadableMap item, String key) {
        return item.hasKey(key) ? item.getString(key) : "";
    }

    /*
     * Check if READ_CONTACTS permission is granted
     */
    private String isPermissionGranted() {
        // return -1 for denied and 1
        int res = ActivityCompat.checkSelfPermission(getReactApplicationContext(), PERMISSION_READ_CONTACTS);
        return (res == PackageManager.PERMISSION_GRANTED) ? PERMISSION_AUTHORIZED : PERMISSION_DENIED;
    }

    /*
     * TODO support all phone types
     * http://developer.android.com/reference/android/provider/ContactsContract.
     * CommonDataKinds.Phone.html
     */
    private int mapStringToPhoneType(String label) {
        int phoneType;
        switch (label) {
            case "home":
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_HOME;
                break;
            case "work":
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK;
                break;
            case "mobile":
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
                break;
            case "main":
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_MAIN;
                break;
            case "work fax":
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK;
                break;
            case "home fax":
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME;
                break;
            case "pager":
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_PAGER;
                break;
            case "work_pager":
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER;
                break;
            case "work_mobile":
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE;
                break;
            case "other":
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_OTHER;
                break;
            case "cell":
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE;
                break;
            default:
                phoneType = ContactsContract.CommonDataKinds.Phone.TYPE_CUSTOM;
                break;
        }
        return phoneType;
    }

    /*
     * TODO support TYPE_CUSTOM
     * http://developer.android.com/reference/android/provider/ContactsContract.
     * CommonDataKinds.Email.html
     */
    private int mapStringToEmailType(String label) {
        int emailType;
        switch (label) {
            case "home":
                emailType = ContactsContract.CommonDataKinds.Email.TYPE_HOME;
                break;
            case "work":
                emailType = ContactsContract.CommonDataKinds.Email.TYPE_WORK;
                break;
            case "mobile":
                emailType = ContactsContract.CommonDataKinds.Email.TYPE_MOBILE;
                break;
            case "other":
                emailType = ContactsContract.CommonDataKinds.Email.TYPE_OTHER;
                break;
            case "personal":
                emailType = ContactsContract.CommonDataKinds.Email.TYPE_HOME;
                break;
            default:
                emailType = ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM;
                break;
        }
        return emailType;
    }

    private int mapStringToPostalAddressType(String label) {
        int postalAddressType;
        switch (label) {
            case "home":
                postalAddressType = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME;
                break;
            case "work":
                postalAddressType = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK;
                break;
            default:
                postalAddressType = ContactsContract.CommonDataKinds.StructuredPostal.TYPE_CUSTOM;
                break;
        }
        return postalAddressType;
    }


    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_OPEN_CONTACT_FORM && requestCode != REQUEST_OPEN_EXISTING_CONTACT) {
            return;
        }

        if (updateContactPromise == null) {
            return;
        }

        if (resultCode != Activity.RESULT_OK) {
            updateContactPromise.resolve(null); // user probably pressed cancel
            updateContactPromise = null;
            return;
        }

        if (data == null) {
            updateContactPromise.reject("Error received activity result with no data!");
            updateContactPromise = null;
            return;
        }

        try {
            Uri contactUri = data.getData();

            if (contactUri == null) {
                updateContactPromise.reject("Error wrong data. No content uri found!"); // something was wrong
                updateContactPromise = null;
                return;
            }

            Context ctx = getReactApplicationContext();
            ContentResolver cr = ctx.getContentResolver();
            ContactsProvider contactsProvider = new ContactsProvider(cr);
            WritableMap newlyModifiedContact = contactsProvider.getContactById(contactUri.getLastPathSegment());

            updateContactPromise.resolve(newlyModifiedContact); // success
        } catch (Exception e) {
            updateContactPromise.reject(e.getMessage());
        }
        updateContactPromise = null;
    }


    public void onNewIntent(Intent intent) {
    }

}
