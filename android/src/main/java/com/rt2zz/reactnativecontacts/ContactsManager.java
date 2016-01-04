package com.rt2zz.reactnativecontacts;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.net.Uri;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

public class ContactsManager extends ReactContextBaseJavaModule {

  public ContactsManager(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  /*
   * Returns all contactable records on phone
   * queries CommonDataKinds.Contactables to get phones and emails
   */
  @ReactMethod
  public void getAll(Callback callback) {
    ContentResolver cr = getReactApplicationContext().getContentResolver();
    Uri uri = CommonDataKinds.Contactables.CONTENT_URI;
    String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = " + 1;
    String sortBy = CommonDataKinds.Contactables.LOOKUP_KEY;

    Cursor cursor = cr.query(uri, null, selection, null, sortBy);

    WritableArray contacts = Arguments.createArray(); // resultSet

    if (cursor.getCount() == 0) {
      callback.invoke(null, contacts); // return empty if no contacts
      return;
    }

    int idColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables._ID);
    int nameColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.DISPLAY_NAME);
    int lookupColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.LOOKUP_KEY);
    int typeColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.MIMETYPE);
    int photoColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.PHOTO_URI);

    cursor.moveToFirst();
    String lookupKey = "";
    WritableMap contact = Arguments.createMap();
    WritableArray phoneNumbers = Arguments.createArray();
    WritableArray emailAddresses = Arguments.createArray();
    do {
      String currentLookupKey = cursor.getString(lookupColumnIndex);

      if (!lookupKey.equals(currentLookupKey)) { // new contact
        if (!lookupKey.equals("")) { // push accumulated contact
          contact.putArray("emailAddresses", emailAddresses);
          contact.putArray("phoneNumbers", phoneNumbers);
          contacts.pushMap(contact);
        }
        lookupKey = currentLookupKey;
        contact = Arguments.createMap();
        phoneNumbers = Arguments.createArray();
        emailAddresses = Arguments.createArray();

        String id = cursor.getString(idColumnIndex);
        contact.putInt("recordID", Integer.parseInt(id));

        // add photo
        String photoURI = cursor.getString(photoColumnIndex);
        contact.putString("thumbnailPath", photoURI == null ? "" : photoURI);

        // add name fields
        String displayName = cursor.getString(nameColumnIndex);
        contact.putString("givenName", displayName);
        contact.putString("middleName", "");
        contact.putString("familyName", "");

        String mimeType = cursor.getString(typeColumnIndex);
        if (mimeType.equals(CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
          WritableMap phoneNoMap = parsePhoneRow(cursor);
          phoneNumbers.pushMap(phoneNoMap);
        } else if (mimeType.equals(CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
          WritableMap emailMap = parseEmailRow(cursor);
          emailAddresses.pushMap(emailMap);
        }
      } else { // same contact
        String mimeType = cursor.getString(typeColumnIndex);
        if (mimeType.equals(CommonDataKinds.Phone.CONTENT_ITEM_TYPE)) {
          WritableMap phoneNoMap = parsePhoneRow(cursor);
          phoneNumbers.pushMap(phoneNoMap);
        } else if (mimeType.equals(CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
          WritableMap emailMap = parseEmailRow(cursor);
          emailAddresses.pushMap(emailMap);
        }
      }
    } while (cursor.moveToNext());
    cursor.close();

    // push last contact
    contact.putArray("emailAddresses", emailAddresses);
    contact.putArray("phoneNumbers", phoneNumbers);
    contacts.pushMap(contact);

    callback.invoke(null, contacts);
  }

  /*
   * converts email row into a object
   * {label: 'work', email: 'carl-jung@example.com'}
   */
  private WritableMap parseEmailRow(Cursor cursor) {
    int emailAddressColumnIndex = cursor.getColumnIndex(CommonDataKinds.Email.ADDRESS);
    int emailTypeColumnIndex = cursor.getColumnIndex(CommonDataKinds.Email.TYPE);
    int emailLabelColumnIndex = cursor.getColumnIndex(CommonDataKinds.Email.LABEL);

    WritableMap emailAddressMap = Arguments.createMap();
    String emailAddress = cursor.getString(emailAddressColumnIndex);
    emailAddressMap.putString("email", emailAddress);

    int type = cursor.getInt(emailTypeColumnIndex);
    if (type == CommonDataKinds.Email.TYPE_HOME) {
      emailAddressMap.putString("label", "home");
    } else if (type == CommonDataKinds.Email.TYPE_MOBILE) {
      emailAddressMap.putString("label", "mobile");
    } else if (type == CommonDataKinds.Email.TYPE_OTHER) {
      emailAddressMap.putString("label", "other");
    } else if (type == CommonDataKinds.Email.TYPE_WORK) {
      emailAddressMap.putString("label", "work");
    } else if (type == CommonDataKinds.Email.TYPE_CUSTOM) {
      emailAddressMap.putString("label", cursor.getString(emailLabelColumnIndex));
    } else {
      emailAddressMap.putString("label", "other");
    }
    return emailAddressMap;
  }

  /*
   * converts phone number row into an map (json object)
   * {label: 'mobile', number: '(555) 555-5555'}
   * TODO support all phone types
   * http://developer.android.com/reference/android/provider/ContactsContract.CommonDataKinds.Phone.html
   */
  private WritableMap parsePhoneRow(Cursor cursor) {
    int phoneNumberColumnIndex = cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER);
    int phoneTypeColumnIndex = cursor.getColumnIndex(CommonDataKinds.Phone.TYPE);
    int phoneLabelColumnIndex = cursor.getColumnIndex(CommonDataKinds.Phone.LABEL);

    WritableMap phoneNumberMap = Arguments.createMap();
    String phoneNumber = cursor.getString(phoneNumberColumnIndex);
    phoneNumberMap.putString("number", phoneNumber);

    int type = cursor.getInt(phoneTypeColumnIndex);
    if (type == CommonDataKinds.Phone.TYPE_HOME) {
      phoneNumberMap.putString("label", "home");
    } else if (type == CommonDataKinds.Phone.TYPE_MOBILE) {
      phoneNumberMap.putString("label", "mobile");
    } else if (type == CommonDataKinds.Phone.TYPE_WORK) {
      phoneNumberMap.putString("label", "work");
    } else {
      phoneNumberMap.putString("label", "other");
    }
    return phoneNumberMap;
  }

  @Override
  public String getName() {
    return "Contacts";
  }
}
