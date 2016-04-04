package com.rt2zz.reactnativecontacts;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.net.Uri;

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

      ContactsProvider contactsProvider = new ContactsProvider(cr);
      WritableArray contacts = contactsProvider.getContacts();

      callback.invoke(null, contacts);
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
