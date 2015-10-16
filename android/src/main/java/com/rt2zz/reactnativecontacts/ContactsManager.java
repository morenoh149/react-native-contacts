package com.rt2zz.reactnativecontacts;

import android.provider.ContactsContract;
import android.content.ContentResolver;
import android.content.Context;

import android.widget.Toast;

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

  @ReactMethod
  public void getContacts(Callback callback) {
    ContentResolver cr = getReactApplicationContext().getContentResolver();
    Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

    WritableArray contacts = Arguments.createArray();

    while (cur.moveToNext())
    {
      WritableMap contact = Arguments.createMap();

      int id = cur.getInt(cur.getColumnIndex(ContactsContract.Contacts._ID));
      String stringId = Integer.toString(id);
      String whereName = ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID + " = " + id;
      String[] whereNameParams = new String[] { ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };
      Cursor nameCur = getReactApplicationContext().getContentResolver().query(ContactsContract.Data.CONTENT_URI, null, whereName, whereNameParams, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME);
      while (nameCur.moveToNext()) {
          String given = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
          String family = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
          String middle = nameCur.getString(nameCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
          // Toast.makeText(getReactApplicationContext(), "Name: " + given + " Family: " +  family + " Displayname: "  + given, Toast.LENGTH_LONG).show();
          contact.putString("givenName", given);
          contact.putString("familyName", family);
          contact.putString("middleName", middle);
      }

      // String givenName = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME));
      // String familyName = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME));
      // String middleName = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME));
      //
      // // Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, id);
      // // Uri thumbnailPath = Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);

      WritableArray phoneNumbers = Arguments.createArray();

      if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
         Cursor pCur = cr.query(
                   ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                   null,
                   ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                   new String[]{stringId}, null);
         while (pCur.moveToNext()) {
             WritableMap phoneNoMap = Arguments.createMap();
             String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
             phoneNoMap.putString("number", phoneNo);
             phoneNumbers.pushMap(phoneNoMap);
            //  Toast.makeText(getReactApplicationContext(), "Phone No: " + phoneNo, Toast.LENGTH_SHORT).show();
         }
        pCur.close();
      }

      contact.putArray("phoneNumbers", phoneNumbers);
      // contact.putString("thumbnailPath", thumbnailPath.toString());
      contact.putInt("recordID", id);
      contacts.pushMap(contact);
    }
    cur.close();
    callback.invoke(contacts);

  }

  @Override
  public String getName() {
    return "ReactNativeContacts";
  }
}
