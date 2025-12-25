package com.rt2zz.reactnativecontacts;

import java.util.LinkedHashMap;
import java.util.Map;

import android.provider.ContactsContract;

public final class Social {

    public static final String DATA1 = ContactsContract.Data.DATA1;
    public static final String DATA2 = ContactsContract.Data.DATA2;
    public static final String DATA3 = ContactsContract.Data.DATA3;
    public static final String ACCOUNT_NAME = "account_name";
    public static final String ACCOUNT_TYPE = "account_type";

    public static final String WHATSAPP = "vnd.android.cursor.item/vnd.com.whatsapp.profile";
    public static final String TELEGRAM = "vnd.android.cursor.item/vnd.org.telegram.messenger.android.profile";
    public static final String FACEBOOK = "vnd.android.cursor.item/vnd.com.facebook.profile";

    private static final Map<String, Descriptor> REGISTRY = new LinkedHashMap<>();

    static {
        REGISTRY.put(WHATSAPP, new Descriptor(WHATSAPP, DATA1, DATA3, ACCOUNT_NAME, ACCOUNT_TYPE));
        REGISTRY.put(TELEGRAM, new Descriptor(TELEGRAM, DATA1, DATA3, ACCOUNT_NAME, ACCOUNT_TYPE));
        REGISTRY.put(FACEBOOK, new Descriptor(FACEBOOK, DATA1, DATA2, ACCOUNT_NAME, ACCOUNT_TYPE));
    }

    public static void register(Descriptor descriptor) {
        REGISTRY.put(descriptor.mimeType, descriptor);
    }

    public static Descriptor get(String mimeType) {
        return REGISTRY.get(mimeType);
    }

    public static final class Descriptor {
        public final String mimeType;
        public final String socialIdField;
        public final String phoneField;
        public final String accountNameField;
        public final String accountTypeField;

        public Descriptor(String mimeType, String socialIdField, String phoneField, String accountNameField, String accountTypeField) {
            this.mimeType = mimeType;
            this.socialIdField = socialIdField;
            this.phoneField = phoneField;
            this.accountNameField = accountNameField;
            this.accountTypeField = accountTypeField;
        }
    }
}