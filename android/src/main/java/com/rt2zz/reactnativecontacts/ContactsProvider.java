package com.rt2zz.reactnativecontacts;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static android.provider.ContactsContract.CommonDataKinds.Contactables;
import static android.provider.ContactsContract.CommonDataKinds.Email;
import static android.provider.ContactsContract.CommonDataKinds.Event;
import static android.provider.ContactsContract.CommonDataKinds.Organization;
import static android.provider.ContactsContract.CommonDataKinds.Phone;
import static android.provider.ContactsContract.CommonDataKinds.StructuredName;
import static android.provider.ContactsContract.CommonDataKinds.Note;
import static android.provider.ContactsContract.CommonDataKinds.Website;
import static android.provider.ContactsContract.CommonDataKinds.Im;
import static android.provider.ContactsContract.CommonDataKinds.StructuredPostal;

public class ContactsProvider {
    public static final int ID_FOR_PROFILE_CONTACT = -1;

    public static final String NAME = "RCTContacts";
    private static final List<String> JUST_ME_PROJECTION = new ArrayList<String>() {
        {
            add((ContactsContract.Data._ID));
            add(ContactsContract.Data.CONTACT_ID);
            add(ContactsContract.Data.RAW_CONTACT_ID);
            add(ContactsContract.Data.LOOKUP_KEY);
            add(ContactsContract.Data.STARRED);
            add(ContactsContract.Contacts.Data.MIMETYPE);
            add(ContactsContract.Profile.DISPLAY_NAME);
            add(Contactables.PHOTO_URI);
            add(StructuredName.DISPLAY_NAME);
            add(StructuredName.GIVEN_NAME);
            add(StructuredName.MIDDLE_NAME);
            add(StructuredName.FAMILY_NAME);
            add(StructuredName.PREFIX);
            add(StructuredName.SUFFIX);
            add(Phone.NUMBER);
            add(Phone.NORMALIZED_NUMBER);
            add(Phone.TYPE);
            add(Phone.LABEL);
            add(Email.DATA);
            add(Email.ADDRESS);
            add(Email.TYPE);
            add(Email.LABEL);
            add(Organization.COMPANY);
            add(Organization.TITLE);
            add(Organization.DEPARTMENT);
            add(StructuredPostal.FORMATTED_ADDRESS);
            add(StructuredPostal.TYPE);
            add(StructuredPostal.LABEL);
            add(StructuredPostal.STREET);
            add(StructuredPostal.POBOX);
            add(StructuredPostal.NEIGHBORHOOD);
            add(StructuredPostal.CITY);
            add(StructuredPostal.REGION);
            add(StructuredPostal.POSTCODE);
            add(StructuredPostal.COUNTRY);
            add(Note.NOTE);
            add(Website.URL);
            add(Im.DATA);
            add(Event.START_DATE);
            add(Event.TYPE);
        }
    };

    private static final List<String> FULL_PROJECTION = new ArrayList<String>() {
        {
            addAll(JUST_ME_PROJECTION);
        }
    };

    private static final List<String> PHOTO_PROJECTION = new ArrayList<String>() {
        {
            add(Contactables.PHOTO_URI);
        }
    };

    private final ContentResolver contentResolver;

    public ContactsProvider(ContentResolver contentResolver) {
        this.contentResolver = contentResolver;
    }

    public WritableArray getContactsMatchingString(String searchString) {
        Map<String, Contact> matchingContacts;
        {
            Cursor cursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    FULL_PROJECTION.toArray(new String[FULL_PROJECTION.size()]),
                    ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " LIKE ? OR " +
                            Organization.COMPANY + " LIKE ?",
                    new String[] { "%" + searchString + "%", "%" + searchString + "%" },
                    null);

            try {
                matchingContacts = loadContactsFrom(cursor);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        WritableArray contacts = Arguments.createArray();
        for (Contact contact : matchingContacts.values()) {
            contacts.pushMap(contact.toMap());
        }
        return contacts;
    }

    public WritableArray getContactsByPhoneNumber(String phoneNumber) {
        Map<String, Contact> matchingContacts;
        {
            Cursor cursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    FULL_PROJECTION.toArray(new String[FULL_PROJECTION.size()]),
                    ContactsContract.CommonDataKinds.Phone.NUMBER + " LIKE ? OR "
                            + ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER + " LIKE ?",
                    new String[] { "%" + phoneNumber + "%", "%" + phoneNumber + "%" },
                    null);

            try {
                matchingContacts = loadContactsFrom(cursor);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        WritableArray contacts = Arguments.createArray();
        for (Contact contact : matchingContacts.values()) {
            contacts.pushMap(contact.toMap());
        }
        return contacts;
    }

    public WritableArray getContactsByEmailAddress(String emailAddress) {
        Map<String, Contact> matchingContacts;
        {
            Cursor cursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    FULL_PROJECTION.toArray(new String[FULL_PROJECTION.size()]),
                    ContactsContract.CommonDataKinds.Email.ADDRESS + " LIKE ?",
                    new String[] { "%" + emailAddress + "%" },
                    null);

            try {
                matchingContacts = loadContactsFrom(cursor);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        WritableArray contacts = Arguments.createArray();
        for (Contact contact : matchingContacts.values()) {
            contacts.pushMap(contact.toMap());
        }
        return contacts;
    }

    public WritableMap getContactByRawId(String contactRawId) {

        // Get Contact Id from Raw Contact Id
        String[] projections = new String[] { ContactsContract.RawContacts.CONTACT_ID };
        String select = ContactsContract.RawContacts._ID + "= ?";
        String[] selectionArgs = new String[] { contactRawId };
        Cursor rawCursor = contentResolver.query(ContactsContract.RawContacts.CONTENT_URI, projections, select,
                selectionArgs, null);
        String contactId = null;
        if (rawCursor.getCount() == 0) {
            /* contact id not found */
        }

        if (cursorMoveToNext(rawCursor)) {
            int columnIndex;
            columnIndex = rawCursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID);
            if (columnIndex == -1) {
                /* trouble getting contact id */
            } else {
                contactId = rawCursor.getString(columnIndex);
            }
        }

        rawCursor.close();

        // Now that we have the real contact id, fetch information
        return getContactById(contactId);
    }

    public WritableMap getContactById(String contactId) {

        Map<String, Contact> matchingContacts;
        {
            Cursor cursor = contentResolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    FULL_PROJECTION.toArray(new String[FULL_PROJECTION.size()]),
                    ContactsContract.RawContacts.CONTACT_ID + " = ?",
                    new String[] { contactId },
                    null);

            try {
                matchingContacts = loadContactsFrom(cursor);
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (matchingContacts.values().size() > 0) {
            return matchingContacts.values().iterator().next().toMap();
        }

        return null;
    }

    public Integer getContactsCount() {
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        int count = cursor.getCount();

        return count;
    }

    public WritableArray getContacts() {
        Map<String, Contact> justMe;
        {
            Cursor cursor = contentResolver.query(
                    Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                            ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                    JUST_ME_PROJECTION.toArray(new String[JUST_ME_PROJECTION.size()]),
                    null,
                    null,
                    null);

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
                    new String[] {
                            Email.CONTENT_ITEM_TYPE,
                            Phone.CONTENT_ITEM_TYPE,
                            StructuredName.CONTENT_ITEM_TYPE,
                            Organization.CONTENT_ITEM_TYPE,
                            StructuredPostal.CONTENT_ITEM_TYPE,
                            Note.CONTENT_ITEM_TYPE,
                            Website.CONTENT_ITEM_TYPE,
                            Im.CONTENT_ITEM_TYPE,
                            Event.CONTENT_ITEM_TYPE,
                    },
                    null);

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

    private Boolean cursorMoveToNext(Cursor cursor) {
        try {
            return cursor.moveToNext();
        } catch (RuntimeException error) {
            return false;
        }
    }

    @NonNull
    private Map<String, Contact> loadContactsFrom(Cursor cursor) {

        Map<String, Contact> map = new LinkedHashMap<>();

        while (cursor != null && cursorMoveToNext(cursor)) {

            int columnIndexContactId = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID);
            int columnIndexId = cursor.getColumnIndex(ContactsContract.Data._ID);
            int columnIndexRawContactId = cursor.getColumnIndex(ContactsContract.Data.RAW_CONTACT_ID);
            String contactId;
            String id;
            String rawContactId;
            if (columnIndexContactId != -1) {
                contactId = cursor.getString(columnIndexContactId);
            } else {
                // todo - double check this, it may not be necessary any more
                contactId = String.valueOf(ID_FOR_PROFILE_CONTACT);// no contact id for 'ME' user
            }

            if (columnIndexId != -1) {
                id = cursor.getString(columnIndexId);
            } else {
                // todo - double check this, it may not be necessary any more
                id = String.valueOf(ID_FOR_PROFILE_CONTACT);// no contact id for 'ME' user
            }

            if (columnIndexRawContactId != -1) {
                rawContactId = cursor.getString(columnIndexRawContactId);
            } else {
                // todo - double check this, it may not be necessary any more
                rawContactId = String.valueOf(ID_FOR_PROFILE_CONTACT);// no contact id for 'ME' user
            }

            if (!map.containsKey(contactId)) {
                map.put(contactId, new Contact(contactId));
            }

            Contact contact = map.get(contactId);
            String mimeType = cursor.getString(cursor.getColumnIndex(ContactsContract.Data.MIMETYPE));
            String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            int starred = cursor.getInt(cursor.getColumnIndex(ContactsContract.Data.STARRED));
            Boolean isStarred = starred == 1;
            contact.rawContactId = rawContactId;
            if (!TextUtils.isEmpty(name) && TextUtils.isEmpty(contact.displayName)) {
                contact.displayName = name;
            }
            contact.isStarred = isStarred;

            if (TextUtils.isEmpty(contact.photoUri)) {
                String rawPhotoURI = cursor.getString(cursor.getColumnIndex(Contactables.PHOTO_URI));
                if (!TextUtils.isEmpty(rawPhotoURI)) {
                    contact.photoUri = rawPhotoURI;
                    contact.hasPhoto = true;
                }
            }

            switch (mimeType) {
                case StructuredName.CONTENT_ITEM_TYPE:
                    contact.givenName = cursor.getString(cursor.getColumnIndex(StructuredName.GIVEN_NAME));
                    if (cursor.getString(cursor.getColumnIndex(StructuredName.MIDDLE_NAME)) != null) {
                        contact.middleName = cursor.getString(cursor.getColumnIndex(StructuredName.MIDDLE_NAME));
                    } else {
                        contact.middleName = "";
                    }
                    if (cursor.getString(cursor.getColumnIndex(StructuredName.FAMILY_NAME)) != null) {
                        contact.familyName = cursor.getString(cursor.getColumnIndex(StructuredName.FAMILY_NAME));
                    } else {
                        contact.familyName = "";
                    }
                    contact.prefix = cursor.getString(cursor.getColumnIndex(StructuredName.PREFIX));
                    contact.suffix = cursor.getString(cursor.getColumnIndex(StructuredName.SUFFIX));
                    break;
                case Phone.CONTENT_ITEM_TYPE:
                    String phoneNumber = cursor.getString(cursor.getColumnIndex(Phone.NUMBER));
                    int phoneType = cursor.getInt(cursor.getColumnIndex(Phone.TYPE));

                    if (!TextUtils.isEmpty(phoneNumber)) {
                        final String label;
                        int labelIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.LABEL);
                        if (labelIndex >= 0) {
                            String typeLabel = cursor.getString(labelIndex);
                            label = ContactsContract.CommonDataKinds.Phone
                                    .getTypeLabel(Resources.getSystem(), phoneType, typeLabel).toString();
                        } else {
                            label = "other";
                        }
                        contact.phones.add(new Contact.Item(label, phoneNumber, id));
                    }
                    break;
                case Email.CONTENT_ITEM_TYPE:
                    String email = cursor.getString(cursor.getColumnIndex(Email.ADDRESS));
                    int emailType = cursor.getInt(cursor.getColumnIndex(Email.TYPE));
                    if (!TextUtils.isEmpty(email)) {
                        String label;
                        switch (emailType) {
                            case Email.TYPE_HOME:
                                label = "home";
                                break;
                            case Email.TYPE_WORK:
                                label = "work";
                                break;
                            case Email.TYPE_MOBILE:
                                label = "mobile";
                                break;
                            case Email.TYPE_OTHER:
                                label = "other";
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
                        contact.emails.add(new Contact.Item(label, email, id));
                    }
                    break;
                case Website.CONTENT_ITEM_TYPE:
                    String url = cursor.getString(cursor.getColumnIndex(Website.URL));
                    int websiteType = cursor.getInt(cursor.getColumnIndex(Website.TYPE));
                    if (!TextUtils.isEmpty(url)) {
                        String label;
                        switch (websiteType) {
                            case Website.TYPE_HOMEPAGE:
                                label = "homepage";
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
                            case Website.TYPE_FTP:
                                label = "ftp";
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
                        contact.urls.add(new Contact.Item(label, url, id));
                    }
                    break;
                case Im.CONTENT_ITEM_TYPE:
                    String username = cursor.getString(cursor.getColumnIndex(Im.DATA));
                    int imType = cursor.getInt(cursor.getColumnIndex(Im.PROTOCOL));
                    if (!TextUtils.isEmpty(username)) {
                        String label;
                        switch (imType) {
                            case Im.PROTOCOL_AIM:
                                label = "AIM";
                                break;
                            case Im.PROTOCOL_MSN:
                                label = "MSN";
                                break;
                            case Im.PROTOCOL_YAHOO:
                                label = "Yahoo";
                                break;
                            case Im.PROTOCOL_SKYPE:
                                label = "Skype";
                                break;
                            case Im.PROTOCOL_QQ:
                                label = "QQ";
                                break;
                            case Im.PROTOCOL_GOOGLE_TALK:
                                label = "Google Talk";
                                break;
                            case Im.PROTOCOL_ICQ:
                                label = "ICQ";
                                break;
                            case Im.PROTOCOL_JABBER:
                                label = "Jabber";
                                break;
                            case Im.PROTOCOL_NETMEETING:
                                label = "NetMeeting";
                                break;
                            case Im.PROTOCOL_CUSTOM:
                                if (cursor.getString(cursor.getColumnIndex(Im.CUSTOM_PROTOCOL)) != null) {
                                    label = cursor.getString(cursor.getColumnIndex(Im.CUSTOM_PROTOCOL));
                                } else {
                                    label = "";
                                }
                                break;
                            default:
                                label = "other";
                        }
                        contact.instantMessengers.add(new Contact.Item(label, username, id));
                    }
                    break;
                case Organization.CONTENT_ITEM_TYPE:
                    contact.company = cursor.getString(cursor.getColumnIndex(Organization.COMPANY));
                    contact.jobTitle = cursor.getString(cursor.getColumnIndex(Organization.TITLE));
                    contact.department = cursor.getString(cursor.getColumnIndex(Organization.DEPARTMENT));
                    break;
                case StructuredPostal.CONTENT_ITEM_TYPE:
                    contact.postalAddresses.add(new Contact.PostalAddressItem(cursor));
                    break;
                case Event.CONTENT_ITEM_TYPE:
                    int eventType = cursor.getInt(cursor.getColumnIndex(Event.TYPE));
                    if (eventType == Event.TYPE_BIRTHDAY) {
                        try {
                            String birthday = cursor.getString(cursor.getColumnIndex(Event.START_DATE)).replace("--",
                                    "");
                            String[] yearMonthDay = birthday.split("-");
                            List<String> yearMonthDayList = Arrays.asList(yearMonthDay);

                            if (yearMonthDayList.size() == 2) {
                                // birthday is formatted "12-31"
                                int month = Integer.parseInt(yearMonthDayList.get(0));
                                int day = Integer.parseInt(yearMonthDayList.get(1));
                                if (month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                                    contact.birthday = new Contact.Birthday(month, day);
                                }
                            } else if (yearMonthDayList.size() == 3) {
                                // birthday is formatted "1986-12-31"
                                int year = Integer.parseInt(yearMonthDayList.get(0));
                                int month = Integer.parseInt(yearMonthDayList.get(1));
                                int day = Integer.parseInt(yearMonthDayList.get(2));
                                if (year > 0 && month >= 1 && month <= 12 && day >= 1 && day <= 31) {
                                    contact.birthday = new Contact.Birthday(year, month, day);
                                }
                            }
                        } catch (NumberFormatException | ArrayIndexOutOfBoundsException | NullPointerException e) {
                            // whoops, birthday isn't in the format we expect
                            Log.w("ContactsProvider", e.toString());

                        }
                    }
                    break;
                case Note.CONTENT_ITEM_TYPE:
                    contact.note = cursor.getString(cursor.getColumnIndex(Note.NOTE));
                    break;
            }
        }

        return map;
    }

    public String getPhotoUriFromContactId(String contactId) {
        Cursor cursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI,
                PHOTO_PROJECTION.toArray(new String[PHOTO_PROJECTION.size()]),
                ContactsContract.RawContacts.CONTACT_ID + " = ?",
                new String[] { contactId },
                null);
        try {
            if (cursor != null && cursorMoveToNext(cursor)) {
                String rawPhotoURI = cursor.getString(cursor.getColumnIndex(Contactables.PHOTO_URI));
                if (!TextUtils.isEmpty(rawPhotoURI)) {
                    return rawPhotoURI;
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

    private static class Contact {
        private String contactId;
        private String rawContactId;
        private String displayName;
        private String givenName = "";
        private String middleName = "";
        private String familyName = "";
        private String prefix = "";
        private String suffix = "";
        private String company = "";
        private String jobTitle = "";
        private String department = "";
        private String note = "";
        private List<Item> urls = new ArrayList<>();
        private List<Item> instantMessengers = new ArrayList<>();
        private boolean hasPhoto = false;
        private boolean isStarred = false;
        private String photoUri;
        private List<Item> emails = new ArrayList<>();
        private List<Item> phones = new ArrayList<>();
        private List<PostalAddressItem> postalAddresses = new ArrayList<>();
        private Birthday birthday;

        public Contact(String contactId) {
            this.contactId = contactId;
        }

        public WritableMap toMap() {
            WritableMap contact = Arguments.createMap();
            contact.putString("recordID", contactId);
            contact.putString("rawContactId", rawContactId);
            contact.putString("givenName", TextUtils.isEmpty(givenName) ? displayName : givenName);
            contact.putString("displayName", displayName);
            contact.putString("middleName", middleName);
            contact.putString("familyName", familyName);
            contact.putString("prefix", prefix);
            contact.putString("suffix", suffix);
            contact.putString("company", company);
            contact.putString("jobTitle", jobTitle);
            contact.putString("department", department);
            contact.putString("note", note);
            contact.putBoolean("hasThumbnail", this.hasPhoto);
            contact.putString("thumbnailPath", photoUri == null ? "" : photoUri);
            contact.putBoolean("isStarred", this.isStarred);

            WritableArray phoneNumbers = Arguments.createArray();
            for (Item item : phones) {
                WritableMap map = Arguments.createMap();
                map.putString("number", item.value);
                map.putString("label", item.label);
                map.putString("id", item.id);
                phoneNumbers.pushMap(map);
            }
            contact.putArray("phoneNumbers", phoneNumbers);

            WritableArray urlAddresses = Arguments.createArray();
            for (Item item : urls) {
                WritableMap map = Arguments.createMap();
                map.putString("url", item.value);
                map.putString("id", item.id);
                urlAddresses.pushMap(map);
            }
            contact.putArray("urlAddresses", urlAddresses);

            WritableArray imAddresses = Arguments.createArray();
            for (Item item : instantMessengers) {
                WritableMap map = Arguments.createMap();
                map.putString("username", item.value);
                map.putString("service", item.label);
                imAddresses.pushMap(map);
            }
            contact.putArray("imAddresses", imAddresses);

            WritableArray emailAddresses = Arguments.createArray();
            for (Item item : emails) {
                WritableMap map = Arguments.createMap();
                map.putString("email", item.value);
                map.putString("label", item.label);
                map.putString("id", item.id);
                emailAddresses.pushMap(map);
            }
            contact.putArray("emailAddresses", emailAddresses);

            WritableArray postalAddresses = Arguments.createArray();
            for (PostalAddressItem item : this.postalAddresses) {
                postalAddresses.pushMap(item.map);
            }
            contact.putArray("postalAddresses", postalAddresses);

            WritableMap birthdayMap = Arguments.createMap();
            if (birthday != null) {
                if (birthday.year > 0) {
                    birthdayMap.putInt("year", birthday.year);
                }
                birthdayMap.putInt("month", birthday.month);
                birthdayMap.putInt("day", birthday.day);
                contact.putMap("birthday", birthdayMap);
            }

            return contact;
        }

        public static class Item {
            public String label;
            public String value;
            public String id;

            public Item(String label, String value, String id) {
                this.id = id;
                this.label = label;
                this.value = value;
            }

            public Item(String label, String value) {
                this.label = label;
                this.value = value;
            }
        }

        public static class Birthday {
            public int year = 0;
            public int month = 0;
            public int day = 0;

            public Birthday(int year, int month, int day) {
                this.year = year;
                this.month = month;
                this.day = day;
            }

            public Birthday(int month, int day) {
                this.month = month;
                this.day = day;
            }
        }

        public static class PostalAddressItem {
            public final WritableMap map;

            public PostalAddressItem(Cursor cursor) {
                map = Arguments.createMap();

                map.putString("label", getLabel(cursor));
                putString(cursor, "formattedAddress", StructuredPostal.FORMATTED_ADDRESS);
                putString(cursor, "street", StructuredPostal.STREET);
                putString(cursor, "pobox", StructuredPostal.POBOX);
                putString(cursor, "neighborhood", StructuredPostal.NEIGHBORHOOD);
                putString(cursor, "city", StructuredPostal.CITY);
                putString(cursor, "region", StructuredPostal.REGION);
                putString(cursor, "state", StructuredPostal.REGION);
                putString(cursor, "postCode", StructuredPostal.POSTCODE);
                putString(cursor, "country", StructuredPostal.COUNTRY);
            }

            private void putString(Cursor cursor, String key, String androidKey) {
                final String value = cursor.getString(cursor.getColumnIndex(androidKey));
                if (!TextUtils.isEmpty(value))
                    map.putString(key, value);
            }

            static String getLabel(Cursor cursor) {
                switch (cursor.getInt(cursor.getColumnIndex(StructuredPostal.TYPE))) {
                    case StructuredPostal.TYPE_HOME:
                        return "home";
                    case StructuredPostal.TYPE_WORK:
                        return "work";
                    case StructuredPostal.TYPE_CUSTOM:
                        final String label = cursor.getString(cursor.getColumnIndex(StructuredPostal.LABEL));
                        return label != null ? label : "";
                }
                return "other";
            }
        }
    }
}
