package com.rt2zz.reactnativecontacts;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.rt2zz.reactnativecontacts.impl.ContactsManagerImpl;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ContactsManager extends NativeContactsSpec implements ActivityEventListener {

    private final ContactsManagerImpl contactsManagerImpl;

    public ContactsManager(ReactApplicationContext reactContext) {
        super(reactContext);
        this.contactsManagerImpl = new ContactsManagerImpl(reactContext, true);
        reactContext.addActivityEventListener(this);
    }

    @Override
    public void removeContactsFromGroup(String groupId, ReadableArray contactIds, Promise promise) {
        promise.reject("E_NOT_IMPLEMENTED", "removeContactsFromGroup not implemented yet");
    }

    @Override
    public void addContactsToGroup(String groupId, ReadableArray contactIds, Promise promise) {
        promise.reject("E_NOT_IMPLEMENTED", "addContactsToGroup not implemented yet");
    }

    @Override
    public void contactsInGroup(String groupId, Promise promise) {
        promise.reject("E_NOT_IMPLEMENTED", "contactsInGroup not implemented yet");
    }

    @Override
    public void addGroup(ReadableMap groupData, Promise promise) {
        promise.reject("E_NOT_IMPLEMENTED", "addGroup not implemented yet");
    }

    @Override
    public void updateGroup(String groupId, ReadableMap groupData, Promise promise) {
        promise.reject("E_NOT_IMPLEMENTED", "updateGroup not implemented yet");
    }

    @Override
    public void deleteGroup(String groupId, Promise promise) {
        promise.reject("E_NOT_IMPLEMENTED", "deleteGroup not implemented yet");
    }

    @Override
    public void getGroup(String groupId, Promise promise) {
        promise.reject("E_NOT_IMPLEMENTED", "getGroup not implemented yet");
    }

    @Override
    public void getGroups(Promise promise) {
        promise.reject("E_NOT_IMPLEMENTED", "getGroups not implemented yet");
    }

    /*
     * Returns all contactable records on phone
     * queries CommonDataKinds.Contactables to get phones and emails
     */
    @Override
    public void getAll(Promise promise) {
        contactsManagerImpl.getAll(promise);
    }

    /**
     * Introduced for iOS compatibility. Same as getAll
     *
     * @param promise promise
     */
    @Override
    public void getAllWithoutPhotos(Promise promise) {
        contactsManagerImpl.getAllWithoutPhotos(promise);
    }



    @Override
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
    @Override
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
    @Override
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
    @Override
    public void getContactsByEmailAddress(final String emailAddress, final Promise promise) {
        contactsManagerImpl.getContactsByEmailAddress(emailAddress, promise);
    }

    /**
     * Retrieves <code>thumbnailPath</code> for contact, or <code>null</code> if not
     * available.
     *
     * @param contactId contact identifier, <code>recordID</code>
     */
    @Override
    public void getPhotoForId(final String contactId, final Promise promise) {
        contactsManagerImpl.getPhotoForId(contactId, promise);
    }

    /**
     * Retrieves <code>contact</code> for contact, or <code>null</code> if not
     * available.
     *
     * @param contactId contact identifier, <code>recordID</code>
     */
    @Override
    public void getContactById(final String contactId, final Promise promise) {
        contactsManagerImpl.getContactById(contactId, promise);
    }

    @Override
    public void writePhotoToPath(final String contactId, final String file, final Promise promise) {
        contactsManagerImpl.writePhotoToPath(contactId, file, promise);
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
    @Override
    public void openContactForm(ReadableMap contact, Promise promise) {
        contactsManagerImpl.openContactForm(contact, promise);
    }

    /*
     * Open contact in native app
     */
    @Override
    public void openExistingContact(ReadableMap contact, Promise promise) {
        contactsManagerImpl.openExistingContact(contact, promise);
    }

    /*
     * View contact in native app
     */
    @Override
    public void viewExistingContact(ReadableMap contact, Promise promise) {
        contactsManagerImpl.viewExistingContact(contact, promise);
    }

    /*
     * Edit contact in native app
     */
    @Override
    public void editExistingContact(ReadableMap contact, Promise promise) {
        contactsManagerImpl.editExistingContact(contact, promise);
    }

    /*
     * Adds contact to phone's addressbook
     */
    @Override
    public void addContact(ReadableMap contact, Promise promise) {
        contactsManagerImpl.addContact(contact, promise);
    }

    public byte[] toByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
        return stream.toByteArray();
    }

    /*
     * Update contact to phone's addressbook
     */
    @Override
    public void updateContact(ReadableMap contact, Promise promise) {
        contactsManagerImpl.updateContact(contact, promise);
    }

    /*
     * Update contact to phone's addressbook
     */
    @Override
    public void deleteContact(ReadableMap contact, Promise promise) {
        contactsManagerImpl.deleteContact(contact, promise);
    }

    /*
     * Check permission
     */
    @Override
    public void checkPermission(Promise promise) {
        contactsManagerImpl.checkPermission(promise);
    }

    /*
     * Request permission
     */
    @Override
    public void requestPermission(Promise promise) {
        contactsManagerImpl.requestPermission(promise);
    }

    /*
     * Enable note usage
     */
    @Override
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
    }*/

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
