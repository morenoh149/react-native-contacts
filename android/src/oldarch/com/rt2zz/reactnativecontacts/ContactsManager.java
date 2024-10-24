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

    /*
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

     */


    @Override
    public String getName() {
        return ContactsProvider.NAME;
    }

    /*
     * Required for ActivityEventListener
     */
    @Override
    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
       contactsManagerImpl.onActivityResult(activity, requestCode, resultCode, data);
    }

    /*
     * Required for ActivityEventListener
     */
    @Override
    public void onNewIntent(Intent intent) {
        contactsManagerImpl.onNewIntent(intent);
    }

}
