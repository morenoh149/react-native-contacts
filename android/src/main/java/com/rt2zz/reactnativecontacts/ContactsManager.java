package com.rt2zz.reactnativecontacts;

import android.provider.ContactsContract;
import android.content.ContentResolver;
import android.content.Context;

import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.widget.AdapterView;
import android.database.Cursor;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.Map;

public class ContactsManager extends ReactContextBaseJavaModule {

  public ContactsManager(ReactApplicationContext reactContext) {
    super(reactContext);
  }

  @ReactMethod
  public void getContacts(Callback callback) {
    callback.invoke("@TODO");
  }

  @Override
  public String getName() {
    return "ReactNativeContacts";
  }
}
