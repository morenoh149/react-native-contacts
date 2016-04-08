package com.rt2zz.reactnativecontacts;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.provider.ContactsContract.CommonDataKinds.*;

public class ContactsProvider {

    private final ContentResolver contentResolver;

    public ContactsProvider(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public WritableArray getContacts() {
        Cursor cursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                new String[]{
                        ContactsContract.Data.CONTACT_ID,
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        Contactables.PHOTO_URI,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER,
                        StructuredName.DISPLAY_NAME,
                        StructuredName.GIVEN_NAME,
                        StructuredName.MIDDLE_NAME,
                        StructuredName.FAMILY_NAME,
                        Phone.NUMBER,
                        Phone.TYPE,
                        Phone.LABEL,
                        Email.DATA,
                        Email.ADDRESS,
                        Email.TYPE,
                        Email.LABEL
                },
                ContactsContract.Data.MIMETYPE + "=? OR " + ContactsContract.Data.MIMETYPE + "=? OR " + ContactsContract.Data.MIMETYPE + "=?",
                new String[]{Email.CONTENT_ITEM_TYPE, Phone.CONTENT_ITEM_TYPE, StructuredName.CONTENT_ITEM_TYPE},
                null
        );

        Map<Integer, Contact> map = new LinkedHashMap<>();

        while (cursor.moveToNext()) {

            int contactId = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
            if (!map.containsKey(contactId)) {
                map.put(contactId, new Contact(contactId));
            }
            Contact contact = map.get(contactId);

            String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));

            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            if (!TextUtils.isEmpty(name) && TextUtils.isEmpty(contact.displayName)) {
                contact.displayName = name;
            }

            String photoUri = cursor.getString(cursor.getColumnIndex(Contactables.PHOTO_URI));
            if (!TextUtils.isEmpty(photoUri)) {
                contact.photoUri = photoUri;
            }

            if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
                contact.givenName = cursor.getString(cursor.getColumnIndex(StructuredName.GIVEN_NAME));
                contact.middleName = cursor.getString(cursor.getColumnIndex(StructuredName.MIDDLE_NAME));
                contact.familyName = cursor.getString(cursor.getColumnIndex(StructuredName.FAMILY_NAME));
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
                            if(cursor.getString(cursor.getColumnIndex(Email.LABEL)) != null){
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
            }
        }

        cursor.close();

        WritableArray contacts = Arguments.createArray();
        for (Contact contact : map.values()) {
            contacts.pushMap(contact.toMap());
        }

        return contacts;
    }

    private static class Contact {
        private final int contactId;
        private String displayName;
        private String givenName = "";
        private String middleName = "";
        private String familyName = "";
        private String photoUri;
        private List<Item> emails = new ArrayList<>();
        private List<Item> phones = new ArrayList<>();

        public Contact(int contactId) {
            this.contactId = contactId;
        }

        public WritableMap toMap() {
            WritableMap contact = Arguments.createMap();
            contact.putInt("recordID", contactId);
            contact.putString("givenName", TextUtils.isEmpty(givenName) ? displayName : givenName);
            contact.putString("middleName", middleName);
            contact.putString("familyName", familyName);
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
    }

}