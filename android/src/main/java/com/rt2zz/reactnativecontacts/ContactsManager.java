package com.rt2zz.reactnativecontacts;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.net.Uri;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;

public class ContactsManager extends ReactContextBaseJavaModule {
  private ReactApplicationContext reactContext;

  public ContactsManager(ReactApplicationContext reactContext) {
    super(reactContext);

    this.reactContext = reactContext;
  }

  /*
   * Returns all contactable records on phone
   * queries CommonDataKinds.Contactables to get phones and emails
   */
  @ReactMethod
  public void getAll(Callback callback) {
    ContentResolver cr = getReactApplicationContext().getContentResolver();
    Uri uri = ContactsContract.Contacts.CONTENT_URI;
    String selection = ContactsContract.Contacts.IN_VISIBLE_GROUP + " = " + 1;
    Resources resources = this.reactContext.getResources();
    String sortBy = CommonDataKinds.Contactables.LOOKUP_KEY;
    String [] projection = new String[]{
            CommonDataKinds.Contactables._ID,
            CommonDataKinds.Contactables.DISPLAY_NAME,
            CommonDataKinds.Contactables.LOOKUP_KEY,
            CommonDataKinds.Contactables.PHOTO_URI
    };
    Cursor cursor = cr.query(uri, projection, selection, null, sortBy);

    WritableArray contacts = Arguments.createArray(); // resultSet

    if (cursor.getCount() == 0) {
      callback.invoke(null, contacts); // return empty if no contacts
      return;
    }

    int idColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables._ID);
    int nameColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.DISPLAY_NAME);
    int lookupColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.LOOKUP_KEY);
    int photoColumnIndex = cursor.getColumnIndex(CommonDataKinds.Contactables.PHOTO_URI);

    String lookupKey = "";

    while (cursor.moveToNext()) {
      WritableMap contact = Arguments.createMap();
      if(lookupColumnIndex == -1){
        continue;
      }
      String currentLookupKey = cursor.getString(lookupColumnIndex);

      if (!lookupKey.equals(currentLookupKey)) { // new contact

        lookupKey = currentLookupKey;

        String id = "";
        if(idColumnIndex != -1){
          id = cursor.getString(idColumnIndex);
          contact.putInt("recordID", Integer.parseInt(id));
        }


        // add photo
        if(photoColumnIndex != -1){
          String photoURI = cursor.getString(photoColumnIndex);
          contact.putString("thumbnailPath", photoURI == null ? "" : photoURI);
        }


        // add name fields
        if(nameColumnIndex != -1){
          String displayName = cursor.getString(nameColumnIndex);
          contact.putString("givenName", displayName);
        }

        contact.putString("middleName", "");
        contact.putString("familyName", "");

        contact.putArray("emailAddresses", getEmailAddresses(cr, id, resources));
        contact.putArray("phoneNumbers", getPhoneNumbers(cr, id, resources));
        contacts.pushMap(contact);
      }
    }
    cursor.close();
    callback.invoke(null, contacts);
  }

  private WritableArray getPhoneNumbers(ContentResolver cr, String id,  Resources resources) {
    WritableArray phoneNumbers = Arguments.createArray();
    String [] projection = new String[]{
            CommonDataKinds.Phone.NUMBER,
            CommonDataKinds.Phone.TYPE,
            CommonDataKinds.Phone.LABEL
    };
    Cursor mCursor = cr.query(CommonDataKinds.Phone.CONTENT_URI, projection,
            CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
    while (mCursor.moveToNext()) {

      WritableMap map = Arguments.createMap();
      String value = mCursor.getString(mCursor.getColumnIndex(CommonDataKinds.Phone.NUMBER));
      int type = mCursor.getInt(mCursor.getColumnIndex(CommonDataKinds.Phone.TYPE));
      String label = mCursor.getString(mCursor.getColumnIndex(CommonDataKinds.Phone.LABEL));
      CharSequence labelType = CommonDataKinds.Phone.getTypeLabel(resources, type, label);
      label = labelType.toString().toLowerCase();

      map.putString("number", value);
      map.putString("label", label);
      phoneNumbers.pushMap(map);
    }

    mCursor.close();
    return phoneNumbers;
  }

  private WritableArray getEmailAddresses(ContentResolver cr, String id, Resources resources) {
    WritableArray phoneNumbers = Arguments.createArray();
    String [] projection = new String[]{
            CommonDataKinds.Email.ADDRESS,
            CommonDataKinds.Email.TYPE,
            CommonDataKinds.Email.LABEL
    };
    Cursor mCursor = cr.query(CommonDataKinds.Email.CONTENT_URI, projection,
            CommonDataKinds.Email.CONTACT_ID + " = " + id, null, null);
    while (mCursor.moveToNext()) {

      WritableMap map = Arguments.createMap();
      String value = mCursor.getString(mCursor.getColumnIndex(CommonDataKinds.Email.ADDRESS));
      int type = mCursor.getInt(mCursor.getColumnIndex(CommonDataKinds.Email.TYPE));
      String label = mCursor.getString(mCursor.getColumnIndex(CommonDataKinds.Email.LABEL));
      CharSequence labelType = CommonDataKinds.Email.getTypeLabel(resources, type, label);
      label = labelType.toString().toLowerCase();

      map.putString("email", value);
      map.putString("label", label);
      phoneNumbers.pushMap(map);
    }

    mCursor.close();
    return phoneNumbers;
  }


  /*
   * Adds contact to phone's addressbook
   */
  @ReactMethod
  public void addContact(ReadableMap contact, Callback callback) {

    String givenName = contact.hasKey("givenName") ? contact.getString("givenName") : null;
    String middleName = contact.hasKey("middleName") ? contact.getString("middleName") : null;
    String familyName = contact.hasKey("familyName") ? contact.getString("familyName") : null;

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
      for(int i=0; i < numOfPhones; i++) {
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
      for(int i=0; i < numOfEmails; i++) {
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
      .withValue(StructuredName.FAMILY_NAME, familyName);
    ops.add(op.build());

    //TODO not sure where to allow yields
    op.withYieldAllowed(true);

    for (int i=0; i < numOfPhones; i++) {
      op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
        .withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
        .withValue(CommonDataKinds.Phone.NUMBER, phones[i])
        .withValue(CommonDataKinds.Phone.TYPE, phonesLabels[i]);
      ops.add(op.build());
    }

    for (int i=0; i < numOfEmails; i++) {
      op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
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
      default: phoneType = CommonDataKinds.Phone.TYPE_OTHER;
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
      default: emailType = CommonDataKinds.Email.TYPE_OTHER;
        break;
    }
    return emailType;
  }

  @Override
  public String getName() {
    return "Contacts";
  }
}
