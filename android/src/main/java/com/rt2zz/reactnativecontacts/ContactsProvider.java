package com.rt2zz.reactnativecontacts;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.List;

public class ContactsProvider {

    private final Uri QUERY_URI = ContactsContract.Contacts.CONTENT_URI;
    private final String CONTACT_ID = ContactsContract.Contacts._ID;
    private final String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    private final String PHOTO_URI = ContactsContract.CommonDataKinds.Contactables.PHOTO_URI;
    private final Uri EMAIL_CONTENT_URI = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
    private final String EMAIL_CONTACT_ID = ContactsContract.CommonDataKinds.Email.CONTACT_ID;
    private final String EMAIL_DATA = ContactsContract.CommonDataKinds.Email.DATA;
    private final String HAS_PHONE_NUMBER = ContactsContract.Contacts.HAS_PHONE_NUMBER;
    private final String PHONE_NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;
    private final Uri PHONE_CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private final String PHONE_CONTACT_ID = ContactsContract.CommonDataKinds.Phone.CONTACT_ID;
    private final String STARRED_CONTACT = ContactsContract.Contacts.STARRED;
    private final ContentResolver contentResolver;

    public ContactsProvider(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public WritableArray getContacts() {
        List<Contact> contactList = new ArrayList<>();
        String[] projection = new String[]{CONTACT_ID, DISPLAY_NAME, PHOTO_URI, HAS_PHONE_NUMBER, STARRED_CONTACT};
        String selection = null;
        Cursor cursor = contentResolver.query(QUERY_URI, projection, selection, null, null);

        while (cursor.moveToNext()) {
            contactList.add(createContact(cursor));
        }

        cursor.close();

        WritableArray contacts = Arguments.createArray();
        for (Contact contact : contactList) {
            contacts.pushMap(contact.toMap());
        }

        return contacts;
    }

    private Contact createContact(Cursor cursor) {
        int contactId = cursor.getInt(cursor.getColumnIndex(CONTACT_ID));
        String name = cursor.getString(cursor.getColumnIndex(DISPLAY_NAME));
        String photoUri = cursor.getString(cursor.getColumnIndex(PHOTO_URI));

        Contact contact = new Contact(contactId, name, photoUri);

        getPhone(cursor, contactId, contact);
        getEmail(contactId, contact);
        return contact;
    }

    private void getEmail(int contactId, Contact contact) {
        Cursor emailCursor = contentResolver.query(EMAIL_CONTENT_URI, null, EMAIL_CONTACT_ID + " = ?", new String[]{String.valueOf(contactId)}, null);

        while (emailCursor.moveToNext()) {
            String email = emailCursor.getString(emailCursor.getColumnIndex(EMAIL_DATA));
            int type = emailCursor.getInt(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));

            if (!TextUtils.isEmpty(email)) {
                String label;
                switch (type) {
                    case ContactsContract.CommonDataKinds.Email.TYPE_HOME:
                        label = "home";
                        break;
                    case ContactsContract.CommonDataKinds.Email.TYPE_WORK:
                        label = "work";
                        break;
                    case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE:
                        label = "mobile";
                        break;
                    case ContactsContract.CommonDataKinds.Email.TYPE_CUSTOM:
                        label = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL));
                        break;
                    default:
                        label = "other";
                }
                contact.emails.add(new Contact.Item(label, email));
            }
        }
        emailCursor.close();
    }

    private void getPhone(Cursor cursor, int contactId, Contact contact) {
        int hasPhoneNumber = Integer.parseInt(cursor.getString(cursor.getColumnIndex(HAS_PHONE_NUMBER)));
        if (hasPhoneNumber > 0) {
            Cursor phoneCursor = contentResolver.query(PHONE_CONTENT_URI, null, PHONE_CONTACT_ID + " = ?", new String[]{String.valueOf(contactId)}, null);

            while (phoneCursor.moveToNext()) {
                String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(PHONE_NUMBER));
                int type = phoneCursor.getInt(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));

                String label;
                switch (type) {
                    case ContactsContract.CommonDataKinds.Phone.TYPE_HOME:
                        label = "home";
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_WORK:
                        label = "work";
                        break;
                    case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE:
                        label = "mobile";
                        break;
                    default:
                        label = "other";
                }
                contact.phones.add(new Contact.Item(label, phoneNumber));
            }
            phoneCursor.close();
        }
    }

    public static class Contact {
        private final int id;
        private final String name;
        private final String photoUri;
        private final List<Item> emails = new ArrayList<>();
        private final List<Item> phones = new ArrayList<>();

        public Contact(int contactId, String name, String photoUri) {
            this.id = contactId;
            this.name = name;
            this.photoUri = photoUri;
        }

        public WritableMap toMap() {
            WritableMap contact = Arguments.createMap();
            contact.putInt("recordID", id);
            contact.putString("givenName", name);
            contact.putString("middleName", "");
            contact.putString("familyName", "");
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
