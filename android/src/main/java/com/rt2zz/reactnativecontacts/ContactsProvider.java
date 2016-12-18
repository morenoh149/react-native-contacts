package com.rt2zz.reactnativecontacts;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.provider.ContactsContract.CommonDataKinds.Contactables;
import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.StructuredName;
import static android.provider.ContactsContract.CommonDataKinds.Organization;
import static android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import static android.provider.ContactsContract.CommonDataKinds.Website;
import static android.provider.ContactsContract.CommonDataKinds.Note;
import static android.provider.ContactsContract.CommonDataKinds.Event;
import static android.provider.ContactsContract.CommonDataKinds.Nickname;

public class ContactsProvider {
    public static final int ID_FOR_PROFILE_CONTACT = -1;

    private static final List<String> JUST_ME_PROJECTION = new ArrayList<String>() {{
        add(ContactsContract.Contacts.Data.MIMETYPE);
        add(ContactsContract.Profile.DISPLAY_NAME);
        add(Contactables.PHOTO_URI);
        add(StructuredName.DISPLAY_NAME);
        add(StructuredName.GIVEN_NAME);
        add(StructuredName.MIDDLE_NAME);
        add(StructuredName.FAMILY_NAME);
        add(StructuredName.PHONETIC_GIVEN_NAME);
        add(StructuredName.PHONETIC_MIDDLE_NAME);
        add(StructuredName.PHONETIC_FAMILY_NAME);
        add(Phone.NUMBER);
        add(Phone.TYPE);
        add(Phone.LABEL);
        add(Email.DATA);
        add(Email.ADDRESS);
        add(Email.TYPE);
        add(Email.LABEL);
        add(StructuredPostal.TYPE);
        add(StructuredPostal.STREET);
        add(StructuredPostal.CITY);
        add(StructuredPostal.REGION);
        add(StructuredPostal.POSTCODE);
        add(StructuredPostal.COUNTRY);
        add(Organization.COMPANY);
        add(Organization.TITLE);
        add(Website.URL);
        add(Website.TYPE);
        add(Note.NOTE);
        add(Event.TYPE);
        add(Event.START_DATE);
        add(Nickname.NAME);
    }};

    private static final List<String> FULL_PROJECTION = new ArrayList<String>() {{
        add(ContactsContract.Data.CONTACT_ID);
        add(ContactsContract.RawContacts.SOURCE_ID);
        addAll(JUST_ME_PROJECTION);
    }};

    private final ContentResolver contentResolver;
    private final Context context;

    public ContactsProvider(ContentResolver contentResolver, Context context) {
        this.contentResolver = contentResolver;
        this.context = context;
    }

    public WritableArray getContacts() {
        Map<String, Contact> justMe;
        {
            Cursor cursor = contentResolver.query(
                    Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI, ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                    JUST_ME_PROJECTION.toArray(new String[JUST_ME_PROJECTION.size()]),
                    null,
                    null,
                    null
            );

            try {
                justMe = loadContactsFrom(cursor);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        Map<String, Contact> everyoneElse;
        {
            Cursor cursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    FULL_PROJECTION.toArray(new String[FULL_PROJECTION.size()]),
                    ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=? OR "
                    + ContactsContract.Data.MIMETYPE + "=?",
                    new String[]{
                        Email.CONTENT_ITEM_TYPE,
                        Phone.CONTENT_ITEM_TYPE,
                        StructuredName.CONTENT_ITEM_TYPE,
                        StructuredPostal.CONTENT_ITEM_TYPE,
                        Organization.CONTENT_ITEM_TYPE,
                        Website.CONTENT_ITEM_TYPE,
                        Note.CONTENT_ITEM_TYPE,
                        Event.CONTENT_ITEM_TYPE,
                        Nickname.CONTENT_ITEM_TYPE
                      },
                    null
            );

            try {
                everyoneElse = loadContactsFrom(cursor);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        WritableArray contacts = Arguments.createArray();
        for (Contact contact : justMe.values()) {
            contacts.pushMap(contact.toMap());
        }
        for (Contact contact : everyoneElse.values()) {
            contacts.pushMap(contact.toMap());
        }

        return contacts;
    }

    @NonNull
    private Map<String, Contact> loadContactsFrom(Cursor cursor) {

        Map<String, Contact> map = new LinkedHashMap<>();

        while (cursor != null && cursor.moveToNext()) {

            int columnIndex = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID);
            String contactId;
            if (columnIndex != -1) {
                contactId = String.valueOf(cursor.getInt(columnIndex));
            } else {
                contactId = String.valueOf(ID_FOR_PROFILE_CONTACT);//no contact id for 'ME' user
            }

            columnIndex = cursor.getColumnIndex(ContactsContract.RawContacts.SOURCE_ID);
            if (columnIndex != -1) {
                String uid = cursor.getString(columnIndex);
                if (uid != null) {
                    contactId = uid;
                }
            }

            if (!map.containsKey(contactId)) {
                map.put(contactId, new Contact(contactId));
            }

            Contact contact = map.get(contactId);

            String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));

            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            if (!TextUtils.isEmpty(name) && TextUtils.isEmpty(contact.displayName)) {
                contact.displayName = name;
            }

            String rawPhotoURI = cursor.getString(cursor.getColumnIndex(Contactables.PHOTO_URI));
            if (!TextUtils.isEmpty(rawPhotoURI) && TextUtils.isEmpty(contact.photoUri)) {
                contact.photoUri = getPhotoURIFromContactURI(rawPhotoURI, contactId);
            }

            if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
                contact.givenName  = cursor.getString(cursor.getColumnIndex(StructuredName.GIVEN_NAME));
                contact.middleName = cursor.getString(cursor.getColumnIndex(StructuredName.MIDDLE_NAME));
                contact.familyName = cursor.getString(cursor.getColumnIndex(StructuredName.FAMILY_NAME));
                contact.phoneticGivenName  = cursor.getString(cursor.getColumnIndex(StructuredName.PHONETIC_GIVEN_NAME));
                contact.phoneticMiddleName = cursor.getString(cursor.getColumnIndex(StructuredName.PHONETIC_MIDDLE_NAME));
                contact.phoneticFamilyName = cursor.getString(cursor.getColumnIndex(StructuredName.PHONETIC_FAMILY_NAME));
            } else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                String phoneNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
                int type = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));

                if (!TextUtils.isEmpty(phoneNumber)) {
                    String label;
                    switch (type) {
                        case Phone.TYPE_HOME:
                            label = "home";
                            break;
                        case Phone.TYPE_WORK:
                            label = "work";
                            break;
                        case Phone.TYPE_MOBILE:
                            label = "mobile";
                            break;
                        default:
                            label = "other";
                    }
                    contact.phones.add(new Contact.Item(label, phoneNumber));
                }
            } else if (mimeType.equals(Email.CONTENT_ITEM_TYPE)) {
                String email = cursor.getString(cursor.getColumnIndex(Email.ADDRESS));
                int type = cursor.getInt(cursor.getColumnIndex(Email.TYPE));

                if (!TextUtils.isEmpty(email)) {
                    String label;
                    switch (type) {
                        case Email.TYPE_HOME:
                            label = "home";
                            break;
                        case Email.TYPE_WORK:
                            label = "work";
                            break;
                        case Email.TYPE_MOBILE:
                            label = "mobile";
                            break;
                        case Email.TYPE_CUSTOM:
                            if (cursor.getString(cursor.getColumnIndex(Email.LABEL)) != null) {
                                label = cursor.getString(cursor.getColumnIndex(Email.LABEL)).toLowerCase();
                            } else {
                                label = "";
                            }
                            break;
                        default:
                            label = "other";
                    }
                    contact.emails.add(new Contact.Item(label, email));
                }
            } else if (mimeType.equals(StructuredPostal.CONTENT_ITEM_TYPE)) {
                Contact.PostalAddress address = new Contact.PostalAddress();
                address.street   = cursor.getString(cursor.getColumnIndex(StructuredPostal.STREET));
                address.city     = cursor.getString(cursor.getColumnIndex(StructuredPostal.CITY));
                address.region   = cursor.getString(cursor.getColumnIndex(StructuredPostal.REGION));
                address.postcode = cursor.getString(cursor.getColumnIndex(StructuredPostal.POSTCODE));
                address.country  = cursor.getString(cursor.getColumnIndex(StructuredPostal.COUNTRY));

                int type = cursor.getInt(cursor.getColumnIndex(StructuredPostal.TYPE));

                String label;
                switch (type) {
                    case StructuredPostal.TYPE_HOME:
                        label = "home";
                        break;
                    case StructuredPostal.TYPE_WORK:
                        label = "work";
                        break;
                    default:
                        label = "other";
                }
                address.label = label;
                contact.postalAddresses.add(address);
            } else if (mimeType.equals(Website.CONTENT_ITEM_TYPE)) {
                String url = cursor.getString(cursor.getColumnIndex(Website.URL));
                int type = cursor.getInt(cursor.getColumnIndex(Website.TYPE));

                if (!TextUtils.isEmpty(url)) {
                    String label;
                    switch (type) {
                        case Website.TYPE_HOMEPAGE:
                            label = "hompagee";
                            break;
                        case Website.TYPE_BLOG:
                            label = "blog";
                            break;
                        case Website.TYPE_PROFILE:
                            label = "profile";
                            break;
                        case Website.TYPE_HOME:
                            label = "home";
                            break;
                        case Website.TYPE_WORK:
                            label = "work";
                            break;
                        case Website.TYPE_CUSTOM:
                            if (cursor.getString(cursor.getColumnIndex(Website.LABEL)) != null) {
                                label = cursor.getString(cursor.getColumnIndex(Website.LABEL)).toLowerCase();
                            } else {
                                label = "";
                            }
                            break;
                        default:
                            label = "other";
                    }
                    contact.websites.add(new Contact.Item(label, url));
                }
            } else if (mimeType.equals(Organization.CONTENT_ITEM_TYPE)) {
                contact.company = cursor.getString(cursor.getColumnIndex(Organization.COMPANY));
                contact.jobTitle = cursor.getString(cursor.getColumnIndex(Organization.TITLE));
            } else if (mimeType.equals(Note.CONTENT_ITEM_TYPE)) {
                contact.note = cursor.getString(cursor.getColumnIndex(Note.NOTE));
            } else if (mimeType.equals(Event.CONTENT_ITEM_TYPE)) {
                contact.birthday.setDate("birthday",cursor.getString(cursor.getColumnIndex(Event.START_DATE)));
            } else if (mimeType.equals(Nickname.CONTENT_ITEM_TYPE)) {
                contact.nickName = cursor.getString(cursor.getColumnIndex(Nickname.NAME));
            }

        }

        return map;
    }

    private String getPhotoURIFromContactURI(String contactURIString, String contactId) {
        Uri contactURI = Uri.parse(contactURIString);

        try {
            InputStream photoStream = contentResolver.openInputStream(contactURI);

            if (photoStream == null)
                return "";

            try {
                BufferedInputStream in = new BufferedInputStream(photoStream);
                File outputDir = context.getCacheDir(); // context being the Activity pointer
                File outputFile = File.createTempFile("contact" + contactId, ".jpg", outputDir);
                FileOutputStream output = new FileOutputStream(outputFile);

                try {
                    int count;
                    byte[] buffer = new byte[4098];

                    while ((count = in.read(buffer)) > 0) {
                        output.write(buffer, 0, count);
                    }
                } catch (IOException e) {
                    output.close();
                }

                in.close();

                return "file://" + outputFile.getAbsolutePath();
            } finally {
                photoStream.close();
            }
        } catch (IOException e) {
            Log.w("RNContacts", "Failed to get photo uri", e);
            return "";
        }
    }

    private static class Contact {
        private String contactId;
        private String displayName;
        private String givenName = "";
        private String middleName = "";
        private String familyName = "";
        private String phoneticGivenName = "";
        private String phoneticMiddleName = "";
        private String phoneticFamilyName = "";
        private String nickName = "";
        private String company = "";
        private String jobTitle ="";
        private String note = "";
        private String photoUri;
        private List<Item> emails = new ArrayList<>();
        private List<Item> phones = new ArrayList<>();
        private List<Item> websites = new ArrayList<>();
        private List<PostalAddress> postalAddresses = new ArrayList<>();
        private SimpleDate birthday = new SimpleDate();

        public Contact(String contactId) {
            this.contactId = contactId;
        }

        public WritableMap toMap() {
            WritableMap contact = Arguments.createMap();
            contact.putString("recordID", contactId);
            contact.putString("givenName", TextUtils.isEmpty(givenName) ? displayName : givenName);
            contact.putString("middleName", middleName);
            contact.putString("familyName", familyName);
            contact.putString("phoneticGivenName", phoneticGivenName);
            contact.putString("phoneticMiddleName", phoneticMiddleName);
            contact.putString("phoneticFamilyName", phoneticFamilyName);
            contact.putString("nickName", nickName);

            contact.putString("company", company);
            contact.putString("jobTitle", jobTitle);
            contact.putString("note", note);
            contact.putString("thumbnailPath", photoUri == null ? "" : photoUri);

            WritableArray phoneNumbers = Arguments.createArray();
            for (Item item : phones) {
                WritableMap map = Arguments.createMap();
                map.putString("number", item.value);
                map.putString("label", item.label);
                phoneNumbers.pushMap(map);
            }
            contact.putArray("phoneNumbers", phoneNumbers);

            WritableArray emailAddresses = Arguments.createArray();
            for (Item item : emails) {
                WritableMap map = Arguments.createMap();
                map.putString("email", item.value);
                map.putString("label", item.label);
                emailAddresses.pushMap(map);
            }
            contact.putArray("emailAddresses", emailAddresses);

            WritableArray websitesArray = Arguments.createArray();
            for (Item item : websites) {
                WritableMap map = Arguments.createMap();
                map.putString("url", item.value);
                map.putString("label", item.label);
                websitesArray.pushMap(map);
            }
            contact.putArray("websites", websitesArray);

            //TODO: postal addresses
            WritableArray postalArray = Arguments.createArray();
            for (PostalAddress address : postalAddresses) {
                WritableMap map = Arguments.createMap();
                map.putString("label", address.label);
                map.putString("street", address.street);
                map.putString("city", address.city);
                map.putString("region", address.region);
                map.putString("country", address.country);
                map.putString("postcode", address.postcode);
                postalArray.pushMap(map);
            }
            contact.putArray("postalAddresses", postalArray);

            if(this.birthday != null ) {
                WritableMap map = Arguments.createMap();
                map.putInt("year",this.birthday.year);
                map.putInt("month",this.birthday.month);
                map.putInt("day",this.birthday.day);
                contact.putMap("birthday",map);
            }
            return contact;
        }

        public static class Item {
            public String label;
            public String value;

            public Item(String label, String value) {
                this.label = label;
                this.value = value;
            }
        }

        public static class SimpleDate {
            public int year;
            public int month;
            public int day;
            public String label;

            public void setDate(String label, String androidDateString) {
                String[] vals = androidDateString.split("-");
                int idx = 0;
                if (androidDateString.startsWith("--")) idx = 1; // no year in date
                this.label = label;

                try {
                  if(!TextUtils.isEmpty(vals[idx])) this.year  = Integer.parseInt(vals[idx]);
                  idx++;
                  if(!TextUtils.isEmpty(vals[idx])) this.month = Integer.parseInt(vals[idx]);
                  idx++;
                  if(!TextUtils.isEmpty(vals[idx])) this.day   = Integer.parseInt(vals[idx]);
                } catch (Exception e) {
                  Log.e("ContactsProvider: date tokenize failed:",e.toString());
                }
            }

            public SimpleDate() {
                this.label = "";
                this.year = this.month = this.day = 0;
            }

        }

        public static class PostalAddress {
            public String label;
            public String street;
            public String city;
            public String region;
            public String country;
            public String postcode;
        }

    }
}
