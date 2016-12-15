package com.rt2zz.reactnativecontacts;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;

import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.RawContacts;
import android.net.Uri;
import android.os.AsyncTask;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public class ContactsManager extends ReactContextBaseJavaModule {

    public ContactsManager(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    public class PostalAddress {
      public String street;
      public String city;
      public String region;
      public String country;
      public String postcode;
    }

    /*
     * Returns all contactable records on phone
     * queries CommonDataKinds.Contactables to get phones and emails
     */
    @ReactMethod
    public void getAll(final Callback callback) {
      AsyncTask.execute(new Runnable() {
          @Override
          public void run() {
              Context context = getReactApplicationContext();
              ContentResolver cr = context.getContentResolver();

              ContactsProvider contactsProvider = new ContactsProvider(cr, context);
              WritableArray contacts = contactsProvider.getContacts();

              callback.invoke(null, contacts);
          }
      });
    }

    /*
     * Adds contact to phone's addressbook
     */
    @ReactMethod
    public void addContact(ReadableMap contact, Callback callback) { //, ReadableMap options) {

      long rawContactId = writeContact(false, contact, callback, null);
      return;

    }

    /*
     * Update contact to phone's addressbook
     */
    @ReactMethod
    public void updateContact(ReadableMap contact, Callback callback) {

        long rawContactId = writeContact(true, contact, callback, null);
        return;

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
        callback.invoke(null, isPermissionGranted());
    }

    /*
     * Write contact details, either new or update, return rawContactId
     */
     private long writeContact(Boolean update, ReadableMap contact, Callback callback, ReadableMap options) {

       //android.util.Log.d("ReactNativeContacts: writeContact update=",String.valueOf(update));

       String recordID = contact.hasKey("recordID") ? String.valueOf(contact.getString("recordID")) : null;

       ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
       ContentProviderOperation.Builder op;

       if(update==true) {
         op = ContentProviderOperation.newUpdate(RawContacts.CONTENT_URI)
          .withSelection(ContactsContract.Data.CONTACT_ID + "=?", new String[]{String.valueOf(recordID)});
       } else {
         op = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
       }
       op.withValue(RawContacts.ACCOUNT_TYPE, null)
         .withValue(RawContacts.ACCOUNT_NAME, null);
       ops.add(op.build());

       // Name
       String givenName  = contact.hasKey("givenName") ? contact.getString("givenName") : null;
       String middleName = contact.hasKey("middleName") ? contact.getString("middleName") : null;
       String familyName = contact.hasKey("familyName") ? contact.getString("familyName") : null;
       String phoneticGivenName  = contact.hasKey("phoneticGivenName") ? contact.getString("phoneticGivenName") : null;
       String phoneticMiddleName = contact.hasKey("phoneticMiddleName") ? contact.getString("phoneticMiddleName") : null;
       String phoneticFamilyName = contact.hasKey("phoneticFamilyName") ? contact.getString("phoneticFamilyName") : null;
       if(update==true) {
         op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
          .withSelection(ContactsContract.Data.CONTACT_ID + "=?", new String[]{recordID});
       } else {
         op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
          .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
       }
       op.withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
         .withValue(StructuredName.GIVEN_NAME, givenName)
         .withValue(StructuredName.MIDDLE_NAME, middleName)
         .withValue(StructuredName.FAMILY_NAME, familyName)
         .withValue(StructuredName.PHONETIC_GIVEN_NAME, phoneticGivenName)
         .withValue(StructuredName.PHONETIC_MIDDLE_NAME, phoneticMiddleName)
         .withValue(StructuredName.PHONETIC_FAMILY_NAME, phoneticFamilyName);
       ops.add(op.build());

       // Phone Numbers
       Map<String, String> phoneNumbers = mapReadableArrayKVS(contact, "phoneNumbers", "label", "number");
       if(phoneNumbers != null) {
         for (Map.Entry<String, String> kvpair : phoneNumbers.entrySet()) {
           if(update==true) {
             op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
              .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[]{recordID, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE});
           } else {
             op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
              .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
           }
           op.withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
             .withValue(CommonDataKinds.Phone.NUMBER, kvpair.getValue())
             .withValue(CommonDataKinds.Phone.TYPE, mapStringToPhoneType(kvpair.getKey()));
           ops.add(op.build());
         }
       }

       // Email addresses
       Map<String, String> emailAddresses = mapReadableArrayKVS(contact, "emailAddresses", "label", "email");
       if(emailAddresses != null) {
         for (Map.Entry<String, String> kvpair : emailAddresses.entrySet()) {
           if(update==true) {
             op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
              .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[]{recordID, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE});
           } else {
             op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
              .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
           }
           op.withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Email.CONTENT_ITEM_TYPE)
             .withValue(CommonDataKinds.Email.ADDRESS, kvpair.getValue())
             .withValue(CommonDataKinds.Email.TYPE, mapStringToEmailType(kvpair.getKey()));
           ops.add(op.build());
         }
       }

       //Postal addresses
       Map<String, PostalAddress> postalAddresses = mapPostalAddressArray(contact, "postalAddresses");
       if(postalAddresses != null) {
         for (Map.Entry<String, PostalAddress> paddr : postalAddresses.entrySet()) {
           if(update==true) {
             op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
              .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[]{recordID, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE });
           } else {
             op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
              .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
           }
           PostalAddress paddr_p = paddr.getValue();
           op.withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
             .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, mapStringToPostalType(paddr.getKey()))
             .withValue(ContactsContract.CommonDataKinds.StructuredPostal.STREET, paddr_p.street)
             .withValue(ContactsContract.CommonDataKinds.StructuredPostal.CITY, paddr_p.city)
             .withValue(ContactsContract.CommonDataKinds.StructuredPostal.REGION, paddr_p.region)
             .withValue(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, paddr_p.postcode)
             .withValue(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY, paddr_p.country);
           ops.add(op.build());
         }
       }

       // Company and job title
       String company = contact.hasKey("company") ? contact.getString("company") : null;
       String jobTitle = contact.hasKey("jobTitle") ? contact.getString("jobTitle") : null;
       if (company != null || jobTitle != null) {
         if(update==true) {
           op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
            .withSelection(ContactsContract.Data.CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[]{recordID, Organization.CONTENT_ITEM_TYPE});
         } else {
           op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
         }
         op.withValue(ContactsContract.Data.MIMETYPE, Organization.CONTENT_ITEM_TYPE)
           .withValue(Organization.COMPANY, company)
           .withValue(Organization.TITLE, jobTitle);
         ops.add(op.build());
       }

       // Websites
       Map<String, String> websites = mapReadableArrayKVS(contact, "websites", "label", "url");
       if(websites != null) {
         for (Map.Entry<String, String> kvpair : websites.entrySet()) {

           if(update==true) {
             op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
              .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[]{recordID, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE});
           } else {
             op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
              .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
           }
           op.withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Website.CONTENT_ITEM_TYPE)
             .withValue(CommonDataKinds.Website.URL, kvpair.getValue())
             .withValue(CommonDataKinds.Website.TYPE, mapStringToWebsiteType(kvpair.getKey()));
           ops.add(op.build());
         }
       }

       // Note
       String note = contact.hasKey("note") ? contact.getString("note") : null;
       if (note != null) {
           if(update==true) {
             op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
              .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[]{recordID, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE});
           } else {
             op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
              .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
           }
           op.withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Note.CONTENT_ITEM_TYPE)
             .withValue(CommonDataKinds.Note.NOTE, note);
           ops.add(op.build());
       }

       // Birthday
       if ( contact.hasKey("birthday") ) {
           // Event.START_DATE is a string that (seems) to only accept "yyyy-MM-dd" format
           String birthdayS = new String();
           ReadableMap bdate = contact.getMap("birthday");
           if( bdate.hasKey("year" )) birthdayS+=bdate.getInt("year")+"-";
           else birthdayS+="--"; //no year
           if( bdate.hasKey("month")) birthdayS+=bdate.getInt("month");
           if( bdate.hasKey("day"  )) birthdayS+="-"+bdate.getInt("day");
//android.util.Log.d("ReactNativeContacts: Birthday=",birthdayS);
           if(update==true) {
             op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
              .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + " = ?",new String[]{recordID, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE});
           } else {
             op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
              .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
           }
           op.withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Event.CONTENT_ITEM_TYPE)
             .withValue(CommonDataKinds.Event.TYPE, CommonDataKinds.Event.TYPE_BIRTHDAY)
             .withValue(CommonDataKinds.Event.START_DATE, birthdayS);
           ops.add(op.build());
       }

       // Nickname
       String nickname = contact.hasKey("nickName") ? contact.getString("nickName") : null;
       if (nickname != null) {
          if(update==true) {
            op = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
              .withSelection(ContactsContract.Data.RAW_CONTACT_ID + "=? AND " + ContactsContract.Data.MIMETYPE + " = ?", new String[]{recordID, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE});
          } else {
            op = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
             .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
          }
           op.withValue(ContactsContract.Data.MIMETYPE, CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
             .withValue(CommonDataKinds.Nickname.NAME, nickname);
           ops.add(op.build());
       }

       //Yield point comes after all operations on a single contact, but need to figure out where to add to ensure usage
       //op.withYieldAllowed(true);

       //Add the contact and get the new rawContactId
       Context ctx = getReactApplicationContext();
       long rawContactId;
       try {
           ContentResolver cr = ctx.getContentResolver();
           ContentProviderResult[] results = cr.applyBatch(ContactsContract.AUTHORITY, ops);
           rawContactId = ContentUris.parseId(results[0].uri);
       } catch (Exception e) {
           callback.invoke(e.toString());
           return 0;
       }

       // Photo
       String thumbnailPath = contact.hasKey("thumbnailPath") ? contact.getString("thumbnailPath") : null;
       if(thumbnailPath != null && thumbnailPath.length() > 0) {
       Uri rawContactPhotoUri = Uri.withAppendedPath(
         ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
         RawContacts.DisplayPhoto.CONTENT_DIRECTORY);
         try {
             File fileIn = new File(thumbnailPath.substring(7)); // remove "file://"
             FileInputStream tnInputStream = new FileInputStream(fileIn);
             AssetFileDescriptor fd =
                 ctx.getContentResolver().openAssetFileDescriptor(rawContactPhotoUri, "rw");
             OutputStream os = fd.createOutputStream();
             int bytesRead = 0;
             byte[] bbuf = new byte[1024*8];
             while ((bytesRead = tnInputStream.read(bbuf)) != -1) {
               os.write(bbuf,0,bytesRead);
             }
             os.close();
             fd.close();
             tnInputStream.close();
         } catch (Exception e) {
           callback.invoke(e.toString());
           return 0;
         }
       }

       return rawContactId;

       /* TODO: Non-silent processing */
     }

    /*
     * Check if READ_CONTACTS permission is granted
     */
    private String isPermissionGranted() {
        String permission = "android.permission.READ_CONTACTS";
        // return -1 for denied and 1
        int res = getReactApplicationContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED) ? "authorized" : "denied";
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

    // TODO: Convert this to a static Hashmap
    private int mapStringToWebsiteType(String label) {
        switch (label) {
            case "homepage":
                return CommonDataKinds.Website.TYPE_HOMEPAGE;
            case "blog":
                return CommonDataKinds.Website.TYPE_BLOG;
            case "profile":
                return CommonDataKinds.Website.TYPE_PROFILE;
            case "home":
                return CommonDataKinds.Website.TYPE_HOME;
            case "work":
                return CommonDataKinds.Website.TYPE_WORK;
        }
        return CommonDataKinds.Website.TYPE_OTHER;
    }

    // TODO: Convert this to a static Hashmap
    private int mapStringToPostalType(String label) {
        switch (label) {
            case "home":
                return CommonDataKinds.StructuredPostal.TYPE_HOME;
            case "work":
                return CommonDataKinds.StructuredPostal.TYPE_WORK;
        }
        return CommonDataKinds.StructuredPostal.TYPE_OTHER;
    }

    private Map<String, String> mapReadableArrayKVS(ReadableMap contact, String key, String keyID, String valueID) {

        if (!contact.hasKey(key)) {
            return null;
        }

        ReadableArray kvMap = contact.getArray(key);
        int numEntries = kvMap.size();
        Map<String, String> retMap = new HashMap<String, String>(numEntries, (float)1.0);

        for (int i = 0; i < numEntries; i++) {
            String retKey = kvMap.getMap(i).getString(keyID);
            String retValue = kvMap.getMap(i).getString(valueID);
            retMap.put(retKey, retValue);
        }

        return retMap;
    }

    private Map<String, PostalAddress> mapPostalAddressArray(ReadableMap contact, String key) {

      if (!contact.hasKey(key)) {
          return null;
      }

      ReadableArray kvMap = contact.getArray(key);
      int numEntries = kvMap.size();
      Map<String, PostalAddress> retMap = new HashMap<String, PostalAddress>(numEntries, (float)1.0);

      for (int i = 0; i < numEntries; i++) {
          String retKey = kvMap.getMap(i).getString("label");
          PostalAddress retValue = new PostalAddress();
          retValue.street   = kvMap.getMap(i).getString("street");
          retValue.city     = kvMap.getMap(i).getString("city");
          retValue.region   = kvMap.getMap(i).getString("region");
          retValue.country  = kvMap.getMap(i).getString("country");
          retValue.postcode = kvMap.getMap(i).getString("postcode");
          retMap.put(retKey, retValue);
      }

      return retMap;

    }

    @Override
    public String getName() {
        return "Contacts";
    }
}
