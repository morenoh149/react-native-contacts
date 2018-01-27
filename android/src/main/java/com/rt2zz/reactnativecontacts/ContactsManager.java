package com.rt2zz.reactnativecontacts;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.RawContacts;

import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;

import java.util.ArrayList;
import java.util.Hashtable;

public class ContactsManager extends ReactContextBaseJavaModule {

    private static final String PERMISSION_DENIED = "denied";
    private static final String PERMISSION_AUTHORIZED = "authorized";
    private static final String PERMISSION_READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    private static final int PERMISSION_REQUEST_CODE = 888;

    private static Callback requestCallback;

    public ContactsManager(ReactApplicationContext reactContext) {
        super(reactContext);
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
     * @param callback user provided callback to run at completion
     */
    private void getAllContacts(final Callback callback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Context context = getReactApplicationContext();
                ContentResolver cr = context.getContentResolver();

                ContactsProvider contactsProvider = new ContactsProvider(cr);
                WritableArray contacts = contactsProvider.getContacts();

                callback.invoke(null, contacts);
            }
        });
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
     * @param searchString String to match
     * @param callback user provided callback to run at completion
     */
    private void getAllContactsMatchingString(final String searchString, final Callback callback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Context context = getReactApplicationContext();
                ContentResolver cr = context.getContentResolver();
                ContactsProvider contactsProvider = new ContactsProvider(cr);
                WritableArray contacts = contactsProvider.getContactsMatchingString(searchString);

                callback.invoke(null, contacts);
            }
        });
    }

    /**
     * Retrieves <code>thumbnailPath</code> for contact, or <code>null</code> if not available.
     * @param contactId contact identifier, <code>recordID</code>
     * @param callback callback
     */
    @ReactMethod
    public void getPhotoForId(final String contactId, final Callback callback) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Context context = getReactApplicationContext();
                ContentResolver cr = context.getContentResolver();
                ContactsProvider contactsProvider = new ContactsProvider(cr);
                String photoUri = contactsProvider.getPhotoUriFromContactId(contactId);

                callback.invoke(null, photoUri);
            }
        });
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
            postalAddressesLabel =  new Integer[numOfPostalAddresses];
            for (int i = 0; i <  numOfPostalAddresses ; i++) {
                postalAddressesStreet[i] =  postalAddresses.getMap(i).getString("street");
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

        Intent intent = new Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, displayName);
        intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, contactData);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Context context = getReactApplicationContext();
        context.startActivity(intent);

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

        // String name = givenName;
        // name += middleName != "" ? " " + middleName : "";
        // name += familyName != "" ? " " + familyName : "";

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
                    .withValue(CommonDataKinds.Phone.TYPE, phonesLabels[i]);
            ops.add(op.build());
        }

        for (int i = 0; i < numOfEmails; i++) {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Email.ADDRESS, emails[i])
                    .withValue(CommonDataKinds.Email.TYPE, emailsLabels[i]);
            ops.add(op.build());
        }

        ReadableArray postalAddresses = contact.hasKey("postalAddresses") ? contact.getArray("postalAddresses") : null;
        if (postalAddresses != null) {
            for (int i = 0; i <  postalAddresses.size() ; i++) {
                ReadableMap address = postalAddresses.getMap(i);

                op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(CommonDataKinds.StructuredPostal.TYPE, mapStringToPostalAddressType(address.getString("label")))
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
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
            callback.invoke(); // success
        } catch (Exception e) {
            callback.invoke(e.toString());
        }
    }
    /*
     * Update contact to phone's addressbook
     */
    @ReactMethod
    public void updateContact(ReadableMap contact, Callback callback) {

        String recordID = contact.hasKey("recordID") ? contact.getString("recordID") : null;

        String givenName = contact.hasKey("givenName") ? contact.getString("givenName") : null;
        String middleName = contact.hasKey("middleName") ? contact.getString("middleName") : null;
        String familyName = contact.hasKey("familyName") ? contact.getString("familyName") : null;
        String prefix = contact.hasKey("prefix") ? contact.getString("prefix") : null;
        String suffix = contact.hasKey("suffix") ? contact.getString("suffix") : null;
        String company = contact.hasKey("company") ? contact.getString("company") : null;
        String jobTitle = contact.hasKey("jobTitle") ? contact.getString("jobTitle") : null;
        String department = contact.hasKey("department") ? contact.getString("department") : null;

        ReadableArray phoneNumbers = contact.hasKey("phoneNumbers") ? contact.getArray("phoneNumbers") : null;
        int numOfPhones = 0;
        String[] phones = null;
        Integer[] phonesLabels = null;
        if (phoneNumbers != null) {
            numOfPhones = phoneNumbers.size();
            phones = new String[numOfPhones];
            phonesLabels = new Integer[numOfPhones];
            for (int i = 0; i < numOfPhones; i++) {
                ReadableMap phoneMap = phoneNumbers.getMap(i);
                String phoneNumber = phoneMap.getString("number");
                String phoneLabel = phoneMap.getString("label");
                phones[i] = phoneNumber;
                phonesLabels[i] = mapStringToPhoneType(phoneLabel);
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
                ReadableMap emailMap = emailAddresses.getMap(i);
                emails[i] = emailMap.getString("email");
                String label = emailMap.getString("label");
                emailsLabels[i] = mapStringToEmailType(label);
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

        for (int i = 0; i < numOfPhones; i++) {
            op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[]{String.valueOf(recordID), CommonDataKinds.Phone.CONTENT_ITEM_TYPE})
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Phone.NUMBER, phones[i])
                    .withValue(CommonDataKinds.Phone.TYPE, phonesLabels[i]);
            ops.add(op.build());
        }

        for (int i = 0; i < numOfEmails; i++) {
            op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[]{String.valueOf(recordID), CommonDataKinds.Email.CONTENT_ITEM_TYPE})
                    .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(CommonDataKinds.Email.ADDRESS, emails[i])
                    .withValue(CommonDataKinds.Email.TYPE, emailsLabels[i]);
            ops.add(op.build());
        }

        Context ctx = getReactApplicationContext();
        try {
            ContentResolver cr = ctx.getContentResolver();
            cr.applyBatch(ContactsContract.AUTHORITY, ops);
            callback.invoke(); // success
        } catch (Exception e) {
            callback.invoke(e.toString());
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
        for (int i=0; i < permissions.length; i++) {
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
            default:
                phoneType = CommonDataKinds.Phone.TYPE_OTHER;
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
                emailType = CommonDataKinds.Email.TYPE_OTHER;
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
                postalAddressType = CommonDataKinds.StructuredPostal.TYPE_OTHER;
                break;
        }
        return postalAddressType;
    }


    @Override
    public String getName() {
        return "Contacts";
    }
}
