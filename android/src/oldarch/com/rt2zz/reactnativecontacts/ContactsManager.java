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
import android.provider.ContactsContract.RawContacts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableArray;
import com.rt2zz.reactnativecontacts.impl.ContactsManagerImpl;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.io.InputStream;
import java.util.Hashtable;

public class ContactsManager extends ReactContextBaseJavaModule implements ActivityEventListener {

    private static final String PERMISSION_DENIED = "denied";
    private static final String PERMISSION_AUTHORIZED = "authorized";
    private static final String PERMISSION_READ_CONTACTS = Manifest.permission.READ_CONTACTS;
    private static final int PERMISSION_REQUEST_CODE = 888;

    private static final int REQUEST_OPEN_CONTACT_FORM = 52941;
    private static final int REQUEST_OPEN_EXISTING_CONTACT = 52942;

    private static Promise updateContactPromise;
    private static Promise requestPromise;

    private final ContactsManagerImpl contactsManagerImpl;

    public ContactsManager(ReactApplicationContext reactContext) {
        super(reactContext);
        contactsManagerImpl = new ContactsManagerImpl(reactContext, true);
        reactContext.addActivityEventListener(this);
    }

    /*
     * Returns all contactable records on phone
     * queries CommonDataKinds.Contactables to get phones and emails
     */
    @ReactMethod
    public void getAll(Promise promise) {
        contactsManagerImpl.getAll(promise);
    }

    /**
     * Introduced for iOS compatibility. Same as getAll
     *
     * @param promise promise
     */
    @ReactMethod
    public void getAllWithoutPhotos(Promise promise) {
        contactsManagerImpl.getAllWithoutPhotos(promise);
    }


    @ReactMethod
    public void getCount(final Promise promise) {
        contactsManagerImpl.getCount(promise);
    }

    /**
     * Retrieves contacts matching String.
     * Uses raw URI when <code>rawUri</code> is <code>true</code>, makes assets copy
     * otherwise.
     *
     * @param searchString String to match
     */
    @ReactMethod
    public void getContactsMatchingString(final String searchString, final Promise promise) {
        contactsManagerImpl.getContactsMatchingString(searchString, promise);
    }

    /**
     * Retrieves contacts matching a phone number.
     * Uses raw URI when <code>rawUri</code> is <code>true</code>, makes assets copy
     * otherwise.
     *
     * @param phoneNumber phone number to match
     */
    @ReactMethod
    public void getContactsByPhoneNumber(final String phoneNumber, final Promise promise) {
        contactsManagerImpl.getContactsByPhoneNumber(phoneNumber, promise);
    }

    /**
     * Retrieves contacts matching an email address.
     * Uses raw URI when <code>rawUri</code> is <code>true</code>, makes assets copy
     * otherwise.
     *
     * @param emailAddress email address to match
     */
    @ReactMethod
    public void getContactsByEmailAddress(final String emailAddress, final Promise promise) {
        contactsManagerImpl.getContactsByEmailAddress(emailAddress, promise);
    }

    /**
     * Retrieves <code>thumbnailPath</code> for contact, or <code>null</code> if not
     * available.
     *
     * @param contactId contact identifier, <code>recordID</code>
     */
    @ReactMethod
    public void getPhotoForId(final String contactId, final Promise promise) {
        contactsManagerImpl.getPhotoForId(contactId, promise);
    }

    /**
     * Retrieves <code>contact</code> for contact, or <code>null</code> if not
     * available.
     *
     * @param contactId contact identifier, <code>recordID</code>
     */
    @ReactMethod
    public void getContactById(final String contactId, final Promise promise) {
        contactsManagerImpl.getContactById(contactId, promise);
    }

    @ReactMethod
    public void writePhotoToPath(final String contactId, final String file, final Promise promise) {
        contactsManagerImpl.writePhotoToPath(contactId, file, promise);
    }

    /*
     * Start open contact form
     */
    @ReactMethod
    public void openContactForm(ReadableMap contact, Promise promise) {
        contactsManagerImpl.openContactForm(contact, promise);
    }

    /*
     * Open contact in native app
     */
    @ReactMethod
    public void openExistingContact(ReadableMap contact, Promise promise) {
        contactsManagerImpl.openExistingContact(contact, promise);
    }

    /*
     * View contact in native app
     */
    @ReactMethod
    public void viewExistingContact(ReadableMap contact, Promise promise) {
        contactsManagerImpl.viewExistingContact(contact, promise);
    }

    /*
     * Edit contact in native app
     */
    @ReactMethod
    public void editExistingContact(ReadableMap contact, Promise promise) {
        contactsManagerImpl.editExistingContact(contact, promise);
    }

    /*
     * Adds contact to phone's addressbook
     */
    @ReactMethod
    public void addContact(ReadableMap contact, Promise promise) {
        contactsManagerImpl.addContact(contact, promise);
    }

    /*
     * Update contact to phone's addressbook
     */
    @ReactMethod
    public void updateContact(ReadableMap contact, Promise promise) {
        contactsManagerImpl.updateContact(contact, promise);
    }

    /*
     * Update contact to phone's addressbook
     */
    @ReactMethod
    public void deleteContact(ReadableMap contact, Promise promise) {
        contactsManagerImpl.deleteContact(contact, promise);
    }

    /*
     * Check permission
     */
    @ReactMethod
    public void checkPermission(Promise promise) {
        contactsManagerImpl.checkPermission(promise);
    }

    /*
     * Request permission
     */
    @ReactMethod
    public void requestPermission(Promise promise) {
        contactsManagerImpl.requestPermission(promise);
    }

    /*
     * Enable note usage
     */
    @ReactMethod
    public void iosEnableNotesUsage(boolean enabled) {
        // this method is only needed for iOS
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
            case "other":
                phoneType = CommonDataKinds.Phone.TYPE_OTHER;
                break;
            case "cell":
                phoneType = CommonDataKinds.Phone.TYPE_MOBILE;
                break;
            default:
                phoneType = CommonDataKinds.Phone.TYPE_CUSTOM;
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
                emailType = CommonDataKinds.Email.TYPE_HOME;
                break;
            case "work":
                emailType = CommonDataKinds.Email.TYPE_WORK;
                break;
            case "mobile":
                emailType = CommonDataKinds.Email.TYPE_MOBILE;
                break;
            case "other":
                emailType = CommonDataKinds.Email.TYPE_OTHER;
                break;
            case "personal":
                emailType = CommonDataKinds.Email.TYPE_HOME;
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
        return ContactsProvider.NAME;
    }

    /*
     * Required for ActivityEventListener
     */
    @Override
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

    /*
     * Required for ActivityEventListener
     */
    @Override
    public void onNewIntent(Intent intent) {
    }

}
