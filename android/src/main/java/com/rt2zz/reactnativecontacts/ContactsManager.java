package com.rt2zz.reactnativecontacts;

import android.provider.ContactsContract;
import android.content.ContentResolver;
import android.content.Context;

import android.database.Cursor;
import android.net.Uri;
import android.content.ContentUris;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.Map;

public class ContactsManager extends ReactContextBaseJavaModule {

  public ContactsManager(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  // @TODO can potentially be much faster: http://stackoverflow.com/questions/12109391/getting-name-and-email-from-contact-list-is-very-slow
  @ReactMethod
  public void getAll(Callback callback) {
    ContentResolver cr = getReactApplicationContext().getContentResolver();
    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

    WritableArray contacts = Arguments.createArray();

    if(cur == null) {
      callback.invoke(null, contacts);
    }

    while (cur.moveToNext())
    {
      WritableMap contact = Arguments.createMap();

      int id = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts._ID));
      String stringId = Integer.toString(id);
      String whereName = ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = " + id;
      String[] whereNameParams = new String[] { ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };
      Cursor nameCur = getReactApplicationContext().getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, whereName, whereNameParams, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
      if(nameCur != null) {
        while (nameCur.moveToNext()) {
            String given = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
            String family = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
            String middle = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
            contact.putString("givenName", given);
            contact.putString("familyName", family);
            contact.putString("middleName", middle);
        }
        nameCur.close();
      }

      WritableArray phoneNumbers = Arguments.createArray();
      if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
        Cursor pCur = cr.query(
                   ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                   null,
                   ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                   new String[]{stringId}, null);
        if(pCur != null) {
          while (pCur.moveToNext()) {
              WritableMap phoneNoMap = Arguments.createMap();
              String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
              phoneNoMap.putString("number", phoneNo);
              phoneNumbers.pushMap(phoneNoMap);
          }
          pCur.close();
        }
      }

      WritableArray emailAddresses = Arguments.createArray();
      Cursor eCur = cr.query(
              ContactsContract.CommonDataKinds.Email.CONTENT_URI,
              null,
              ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
              new String[] { stringId }, null);
      int labelId = eCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL);
      int emailId = eCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS);
      int typeId = eCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE);

      while (eCur.moveToNext()) {
        WritableMap emailMap = Arguments.createMap();
        emailMap.putString("address", eCur.getString(emailId));
        int type = eCur.getInt(typeId);
        if(type == 1){
          emailMap.putString("label", "home");
        } else if(type == 2){
          emailMap.putString("label", "work");
        } else if(type == 3){
          emailMap.putString("label", "other");
        } else if(type == 4){
          emailMap.putString("label", "mobile");
        } else if(type == 0){
          emailMap.putString("label", eCur.getString(labelId));
        }
        emailAddresses.pushMap(emailMap);
      }

      String thumbnailPath = this.getPhotoUri(id);
      contact.putArray("phoneNumbers", phoneNumbers);
      contact.putArray("emailAddresses", emailAddresses);
      contact.putString("thumbnailPath", thumbnailPath);
      contact.putInt("recordID", id);
      contacts.pushMap(contact);
    }
    cur.close();
    callback.invoke(null, contacts);
  }

  public String getPhotoUri(long contactId) {
    ContentResolver cr = getReactApplicationContext().getContentResolver();

    Cursor cursor = cr
        .query(ContactsContract.Data.CONTENT_URI,
            null,
            ContactsContract.Data.CONTACT_ID
                + "="
                + contactId
                + " AND "

                + ContactsContract.Data.MIMETYPE
                + "='"
                + ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                + "'", null, null);
    try {
        if (cursor != null) {
        if (!cursor.moveToFirst()) {
            return null; // no photo
        }
        } else {
        return null; // error in cursor process
        }

    } catch (Exception e) {
        e.printStackTrace();
        return null;
    }

    cursor.close();

    Uri person = ContentUris.withAppendedId(
        ContactsContract.Contacts.CONTENT_URI, contactId);
    return Uri.withAppendedPath(person,
        ContactsContract.Contacts.Photo.CONTENT_DIRECTORY).toString();
  }

  @Override
  public String getName() {
    return "Contacts";
  }
}
