# React Native Contacts
To contribute read [CONTRIBUTING.md](CONTRIBUTING.md).

Ask questions on [stackoverflow](https://stackoverflow.com/questions/tagged/react-native-contacts) not the issue tracker.

## Usage
`getAll` is a database intensive process, and can take a long time to complete depending on the size of the contacts list. Because of this, it is recommended you access the `getAll` method before it is needed, and cache the results for future use.

```es
import Contacts from 'react-native-contacts';

Contacts.getAll((err, contacts) => {
  if (err) throw err;

  // contacts returned
  console.log(contacts)
})
```

`getContactMatchingString` is meant to alleviate the amount of time it takes to get all contacts, by filtering on the native side based on a string.

```es
import Contacts from 'react-native-contacts';

Contacts.getContactsMatchingString("filter", (err, contacts) => {
  if (err) throw err;

  // contacts matching "filter"
  console.log(contacts)
})
```
## Installation
To use this module you have to install it and configure the permissions. Please read this entire section.

### Automatic
with npm

    npm install react-native-contacts --save
_the `--save` is necessary for [automatic linking](https://facebook.github.io/react-native/docs/linking-libraries-ios.html#automatic-linking)_

with yarn

    yarn add react-native-contacts
and then do

    react-native link
If you get an error about
`import Contacts from 'react-native-contacts'; is undefined.` try manual linking
below.  

Also, supporting older versions of Android (API level <= 22) requires extra permissions; see the [Android permissions](#android-1) section.

### Manual
#### iOS

1. In XCode, in the project navigator, right click Libraries `Add Files to [your project's name]`
1. add `./node_modules/react-native-contacts/ios/RCTContacts.xcodeproj`
1. In the XCode project navigator, select your project,
select the `Build Phases` tab and in the `Link Binary With Libraries` section
add `libRCTContacts.a`

#### Android
1. In `android/settings.gradle`

```gradle
...
include ':react-native-contacts'
project(':react-native-contacts').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-contacts/android')
```

3. In `android/app/build.gradle`

```gradle
...
dependencies {
    ...
    implementation project(':react-native-contacts')
}
```

4. register module (in MainApplication.java)

```java
import com.rt2zz.reactnativecontacts.ReactNativeContacts; // <--- import

public class MainActivity extends ReactActivity {
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
### Permissions
#### iOS

Add kit specific "permission" keys to your Xcode `Info.plist` file, in order to make `requestPermission` work. Otherwise your app crashes when requesting the specific permission.

Open Xcode > open ios/yourApp.xcodeproj > Info.plist > Add key `Privacy - Contacts Usage Description` with your kit specific permission. The value for the key is optional in development. If you submit to the App Store the value must explain why you need this permission.

<img width="338" alt="screen shot 2016-09-21 at 13 13 21" src="https://cloud.githubusercontent.com/assets/5707542/18704973/3cde3b44-7ffd-11e6-918b-63888e33f983.png">

#### Android
Android requires allowing permissions with https://facebook.github.io/react-native/docs/permissionsandroid.html
The `READ_CONTACTS` permission is automatically added to the `AndroidManifest.xml`, so you just need request it. If your app also needs to create contacts, don't forget to add `WRITE_CONTACTS` permission to the manifest and request it at runtime.  

If you wish to support older versions of Android (API level <= 22), you may need to add `READ_PROFILE` and/or `WRITE_PROFILE` permissions as well to `AndroidManifest.xml` ([#331](https://github.com/rt2zz/react-native-contacts/issues/331)).

## API
 * `getAll` (callback) - returns *all* contacts as an array of objects
 * `getAllWithoutPhotos` - same as `getAll` on Android, but on iOS it will not return uris for contact photos (because there's a significant overhead in creating the images)
 * `getPhotoForId(contactId, callback)` - returns a URI (or null) for a contacts photo
 * `addContact` (contact, callback) - adds a contact to the AddressBook.  
 * `openContactForm` (contact, callback) - create a new contact and display in contactsUI.  
 * `updateContact` (contact, callback) - where contact is an object with a valid recordID  
 * `deleteContact` (contact, callback) - where contact is an object with a valid recordID  
 * `getContactsMatchingString` (string, callback) - where string is any string to match a name (first, middle, family) to
 * `checkPermission` (callback) - checks permission to access Contacts _ios only_
 * `requestPermission` (callback) - request permission to access Contacts _ios only_

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
  familyName: "Nietzsche",
  givenName: "Friedrich",
}

Contacts.openContactForm(newPerson, (err) => {
  if (err) throw err;
  // form is open
})
```
You may want to edit the contact before saving it into your phone book. So using `openContactForm` allow you to prompt default phone create contacts UI and the new to-be-added contact will be display on the contacts UI view. Click save or cancel button will exit the contacts UI view.

## Updating and Deleting Contacts
Example
```es
Contacts.getAll((err, contacts) => {
  if (err) throw err;

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

  //delete the second record
  Contacts.deleteContact(contacts[1], (err, recordId) => {
    if (err) throw err;
    // contact deleted
  })
})
```
Update and delete reference contacts by their recordID (as returned by the OS in getContacts). Apple does not guarantee the recordID will not change, e.g. it may be reassigned during a phone migration. Consequently you should always grab a fresh contact list with `getContacts` before performing update and delete operations.

You can also delete a record using only it's recordID
```es
Contacts.deleteContact({recordID: 1}, (err, recordId) => {
  if (err) throw err;
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
