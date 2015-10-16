package com.rt2zz.reactnativecontacts;

import java.util.*;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

public class ReactNativeContacts implements ReactPackage {

  @Override
  public List<NativeModule> createNativeModules(
                              ReactApplicationContext reactContext) {
    List<NativeModule> modules = new ArrayList<>();

    modules.add(new ContactsManager(reactContext));
    return modules;
  }

  @Override
  public List<Class<? extends JavaScriptModule>> createJSModules() {
    return Collections.emptyList();
  }

  @Override
  public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
    return Arrays.<ViewManager>asList();
  }
}


// package com.rt2zz.reactnativecontacts;
//
// import com.facebook.react.bridge.NativeModule;
// import com.facebook.react.bridge.ReactApplicationContext;
// import com.facebook.react.bridge.Callback;
// import com.facebook.react.bridge.ReactContext;
// import com.facebook.react.bridge.ReactContextBaseJavaModule;
// import com.facebook.react.bridge.ReactMethod;
//
// import java.util.Map;
//
// public class ReactNativeContacts extends ReactContextBaseJavaModule {
//
//   public ReactNativeContacts(ReactApplicationContext reactContext) {
//     super(reactContext);
//   }
//
//   @ReactMethod
//   public void getContacts(Callback callback, int duration) {
//     callback.invoke("hi");
//   }
//
//   @Override
//   public String getName() {
//     return "ReactNativeContacts";
//   }
// }

// package com.rt2zz.reactnativecontacts;
//
// import com.facebook.react.ReactPackage;
// import com.facebook.react.bridge.JavaScriptModule;
// import com.facebook.react.bridge.NativeModule;
// import com.facebook.react.bridge.Callback;
// import com.facebook.react.bridge.ReactApplicationContext;
// import com.facebook.react.uimanager.ViewManager;
//
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.Collections;
// import java.util.List;
//
// public class ReactNativeContacts implements ReactPackage {
//
//     public ReactNativeContacts() {
//     }
//
//     @Override
//     public List<NativeModule> createNativeModules(
//             ReactApplicationContext reactContext) {
//         return new ArrayList<>();
//     }
//
//     @Override
//     public List<Class<? extends JavaScriptModule>> createJSModules() {
//         return Collections.emptyList();
//     }
//
//     @Override
//     public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
//         return Arrays.<ViewManager>asList(
//                 new IconManager(mAllIconFonts)
//         );
//     }
// }
