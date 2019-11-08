![react-native-contacts](https://github.com/rt2zz/react-native-contacts/raw/master/example/logo.png)

To contribute read [CONTRIBUTING.md](CONTRIBUTING.md).

Ask questions on [stackoverflow](https://stackoverflow.com/questions/tagged/react-native-contacts) not the issue tracker.

## Usage
`getAll` is a database intensive process, and can take a long time to complete depending on the size of the contacts list. Because of this, it is recommended you access the `getAll` method before it is needed, and cache the results for future use.
```es
import Contacts from 'react-native-contacts';

Contacts.getAll((err, contacts) => {
  if (err) {
    throw err;
  }
  // contacts returned
})
```
See the full [API](#api) for more methods.

### Android permissions
On android you must request permissions beforehand
```es
import { PermissionsAndroid } from 'react-native';
import Contacts from 'react-native-contacts';

PermissionsAndroid.request(
  PermissionsAndroid.PERMISSIONS.READ_CONTACTS,
  {
    'title': 'Contacts',
    'message': 'This app would like to view your contacts.'
  }
).then(() => {
  Contacts.getAll((err, contacts) => {
    if (err === 'denied'){
      // error
    } else {
      // contacts returned in Array
    }
  })
})
```

## Installation
_Please read this entire section._

### npm

```
npm install react-native-contacts --save
```

### yarn

```
yarn add react-native-contacts
```

### react native version 60 and above

If you are using react native version 0.60 or above you do not have to link this library.

#### ios
Starting with 0.60 on iOS you have to do the following:

- Add the following line inside `ios/Podfile`

```
target 'app' do
  ...
  pod 'react-native-contacts', :path => '../node_modules/react-native-contacts' <-- add me
  ...
end
```

- Add the following line inside `ios/yourProject/Info.plist`

```
<dict>
  ...
  <key>NSContactsUsageDescription</key>
  <string>Reason your app needs permission to access contacts</string>
  ...
</dict>
```

- Run `pod install` in folder `ios`


### react native below 60

#### iOS

Using the same instructions as https://facebook.github.io/react-native/docs/linking-libraries-ios.html
1. open in xcode `open ios/yourProject.xcodeproj/`
1. drag `./node_modules/react-native-contacts/ios/RCTContacts.xcodeproj` to `Libraries` in your project view.
1. In the XCode project navigator, select your project,
select the `Build Phases` tab drag `Libraries > RCTContacts.xcodeproj > Products > libRCTContacts.a` into the `Link Binary With Libraries` section. Video to clarify 
  [Adding Camera Roll to an ios project in React Native](https://www.youtube.com/watch?v=e3ReNbQu79c).

Run the app via the Run button in xcode or `react-native run-ios` in the terminal.

### Android
For react native versions 0.60 and above you have to use Android X. Android X support was added to react-native-contacts in version 5.x+. If you are using rn 0.59 and below install rnc versions 4.x instead.

1. In `android/settings.gradle`

```gradle
...
include ':react-native-contacts'
project(':react-native-contacts').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-contacts/android')
```

2. In `android/app/build.gradle`

```gradle
...
dependencies {
    ...
    implementation project(':react-native-contacts')
}
```

3. register module

```java
//  MainApplication.java
import com.rt2zz.reactnativecontacts.ReactNativeContacts; // <--- import

public class MainApplication extends Application implements ReactApplication {
  ......

  @Override
  protected List<ReactPackage> getPackages() {
    return Arrays.<ReactPackage>asList(
            new MainReactPackage(),
            new ReactNativeContacts()); // <------ add this
  }
  ......
}
```

#### Permissions
##### API 23+
Android requires allowing permissions with https://facebook.github.io/react-native/docs/permissionsandroid.html
The `READ_CONTACTS` permission is automatically added to `AndroidManifest.xml`, so you just need request it. If your app creates contacts add `WRITE_CONTACTS` permission to `AndroidManifest.xml` and request the permission at runtime.
```xml
...
<uses-permission android:name="android.permission.WRITE_CONTACTS" />
...
```

##### API 22 and below
Add `READ_PROFILE` and/or `WRITE_PROFILE` permissions to `AndroidManifest.xml`
```xml
...
<uses-permission android:name="android.permission.READ_PROFILE" />
...
```

#### ProGuard

If you use Proguard, the snippet below on proguard-rules.pro 
Without it, your apk release version could failed

```
-keep class com.rt2zz.reactnativecontacts.** {*;}
-keepclassmembers class com.rt2zz.reactnativecontacts.** {*;}
```

### All RN versions

#### ios
Add kit specific "permission" keys to your Xcode `Info.plist` file, in order to make `requestPermission` work. Otherwise your app crashes when requesting the specific permission. Open `Info.plist`. Add key `Privacy - Contacts Usage Description` with your kit specific permission. The value for the key is optional in development. If you submit to the App Store the value must explain why you need this permission.

<img width="338" alt="screen shot 2016-09-21 at 13 13 21" src="https://cloud.githubusercontent.com/assets/5707542/18704973/3cde3b44-7ffd-11e6-918b-63888e33f983.png">

## API
 * `getAll` (callback) - returns *all* contacts as an array of objects
 * `getAllWithoutPhotos` - same as `getAll` on Android, but on iOS it will not return uris for contact photos (because there's a significant overhead in creating the images)
 * `getContactById(contactId, callback)` - returns contact with defined contactId (or null if it doesn't exist)
 * `getPhotoForId(contactId, callback)` - returns a URI (or null) for a contacts photo
 * `addContact` (contact, callback) - adds a contact to the AddressBook.  
 * `openContactForm` (contact, callback) - create a new contact and display in contactsUI.  
 * `openExistingContact` (contact, callback) - where contact is an object with a valid recordID
 * `updateContact` (contact, callback) - where contact is an object with a valid recordID  
 * `deleteContact` (contact, callback) - where contact is an object with a valid recordID  
 * `getContactsMatchingString` (string, callback) - where string is any string to match a name (first, middle, family) to
 * `getContactsByPhoneNumber` (string, callback) - where string is a phone number to match to.
 * `getContactsByEmailAddress` (string, callback) - where string is an email address to match to.
 * `checkPermission` (callback) - checks permission to access Contacts _ios only_
 * `requestPermission` (callback) - request permission to access Contacts _ios only_
 * `writePhotoToPath` (callback) - writes the contact photo to a given path _android only_

Callbacks follow node-style:
```sh
callback <Function>
  err <Error>
  response <Object>
```

## Example Contact Record
```es
{
  recordID: '6b2237ee0df85980',
  company: "",
  emailAddresses: [{
    label: "work",
    email: "carl-jung@example.com",
  }],
  familyName: "Jung",
  givenName: "Carl",
  jobTitle: "",
  note: 'some text',
  urlAddresses: [{
    label: "home",
    url: "www.jung.com",
  }],
  middleName: "",
  phoneNumbers: [{
    label: "mobile",
    number: "(555) 555-5555",
  }],
  hasThumbnail: true,
  thumbnailPath: 'content://com.android.contacts/display_photo/3',
  postalAddresses: [
    {
      street: '123 Fake Street',
      city: 'Sample City',
      state: 'CA',
      region: 'CA',
      postCode: '90210',
      country: 'USA',
      label: 'home'
    }
  ],
  birthday: {"year": 1988, "month": 0, "day": 1 }
}
```
**NOTE**
* on Android versions below 8 the entire display name is passed in the `givenName` field. `middleName` and `familyName` will be `""`.
* on iOS the note field is not available.

## Adding Contacts
Currently all fields from the contact record except for thumbnailPath are supported for writing
```es
var newPerson = {
  emailAddresses: [{
    label: "work",
    email: "mrniet@example.com",
  }],
  familyName: "Nietzsche",
  givenName: "Friedrich",
}

Contacts.addContact(newPerson, (err) => {
  if (err) throw err;
  // save successful
})
```

## Open Contact Form
Currently all fields from the contact record except for thumbnailPath are supported for writing
```es
var newPerson = {
  emailAddresses: [{
    label: "work",
    email: "mrniet@example.com",
  }],
  displayName: "Friedrich Nietzsche"
}

Contacts.openContactForm(newPerson, (err, contact) => {
  if (err) throw err;
  // contact has been saved
})
```
You may want to edit the contact before saving it into your phone book. So using `openContactForm` allow you to prompt default phone create contacts UI and the new to-be-added contact will be display on the contacts UI view. Click save or cancel button will exit the contacts UI view.

## Updating Contacts
Example
```es
Contacts.getAll((err, contacts) => {
  if (err) {
    throw err;
  }

  // update the first record
  let someRecord = contacts[0]
  someRecord.emailAddresses.push({
    label: "junk",
    email: "mrniet+junkmail@test.com",
  })
  Contacts.updateContact(someRecord, (err) => {
    if (err) throw err;
    // record updated
  })
})
```
Update reference contacts by their recordID (as returned by the OS in getContacts). Apple does not guarantee the recordID will not change, e.g. it may be reassigned during a phone migration. Consequently you should always grab a fresh contact list with `getContacts` before performing update operations.

### Bugs
There are issues with updating contacts on Android:
1. custom labels get overwritten to "Other",
1. postal address update code doesn't exist. (it exists for addContact)
See https://github.com/rt2zz/react-native-contacts/issues/332#issuecomment-455675041 for current discussions.

## Delete Contacts
You can delete a record using only it's recordID
```es
Contacts.deleteContact({recordID: 1}, (err, recordId) => {
  if (err) {
    throw err;
  }
  // contact deleted
})
```
Or by passing the full contact object with a `recordID` field.
```es
Contacts.deleteContact(contact, (err, recordId) => {
  if (err) {
    throw err;
  }
  // contact deleted
})
```

## Displaying Thumbnails

The thumbnailPath is the direct URI for the temp location of the contact's cropped thumbnail image.

```es
<Image source={{uri: contact.thumbnailPath}} />
```

## Permissions Methods (optional)
`checkPermission` (callback) - checks permission to access Contacts.  
`requestPermission` (callback) - request permission to access Contacts.  

Usage as follows:
```es
Contacts.checkPermission((err, permission) => {
  if (err) throw err;

  // Contacts.PERMISSION_AUTHORIZED || Contacts.PERMISSION_UNDEFINED || Contacts.PERMISSION_DENIED
  if (permission === 'undefined') {
    Contacts.requestPermission((err, permission) => {
      // ...
    })
  }
  if (permission === 'authorized') {
    // yay!
  }
  if (permission === 'denied') {
    // x.x
  }
})
```

These methods are only useful on iOS. For Android you'll have to use https://facebook.github.io/react-native/docs/permissionsandroid.html

These methods do **not** re-request permission if permission has already been granted or denied. This is a limitation in iOS, the best you can do is prompt the user with instructions for how to enable contacts from the phone settings page `Settings > [app name] > contacts`.

## Example
You can find an example app/showcase [here](https://github.com/rt2zz/react-native-contacts/tree/master/example)

![react-native-contacts example](https://github.com/rt2zz/react-native-contacts/raw/master/example/react-native-contacts.gif)


<h2 align="center">Maintainers</h2>

<table>
  <tbody>
    <tr>
      <td align="center">
        <a href="https://github.com/morenoh149">
          <img width="150" height="150" src="https://github.com/morenoh149.png?size=150">
          </br>
          Harry Moreno
        </a>
      </td>
    </tr>
  <tbody>
</table>

## LICENSE

[MIT License](LICENSE)
