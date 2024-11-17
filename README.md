![react-native-contacts](https://github.com/rt2zz/react-native-contacts/raw/master/example/logo.png)

To contribute read [CONTRIBUTING.md](CONTRIBUTING.md).

Ask questions on [stackoverflow](https://stackoverflow.com/questions/tagged/react-native-contacts) not the issue tracker.

## Usage
`getAll` is a database intensive process, and can take a long time to complete depending on the size of the contacts list. Because of this, it is recommended you access the `getAll` method before it is needed, and cache the results for future use.
```js
import Contacts from 'react-native-contacts';

Contacts.getAll().then(contacts => {
  // contacts returned
})
```
See the full [API](#api) for more methods.

### Android permissions
On android you must request permissions beforehand
```js
import { PermissionsAndroid } from 'react-native';
import Contacts from 'react-native-contacts';

PermissionsAndroid.request(PermissionsAndroid.PERMISSIONS.READ_CONTACTS, {
        title: 'Contacts',
        message: 'This app would like to view your contacts.',
        buttonPositive: 'Please accept bare mortal',
    })
        .then((res) => {
            console.log('Permission: ', res);
            Contacts.getAll()
                .then((contacts) => {
                    // work with contacts
                    console.log(contacts);
                })
                .catch((e) => {
                    console.log(e);
                });
        })
        .catch((error) => {
            console.error('Permission error: ', error);
        });
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
## You no longer need to include the pod line in the PodFile since V7.0.0+, we now support autolinking!
If you were previously using manually linking follow these steps to upgrade
```
react-native unlink react-native-contacts
npm install latest version of react-native-contacts
You're good to go!
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
The `READ_CONTACTS` permission must be added to your main application's `AndroidManifest.xml`. If your app creates contacts add `WRITE_CONTACTS` permission to `AndroidManifest.xml` and request the permission at runtime.
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

##### Accessing note filed on iOS 13 (optional)
If you'd like to read/write the contact's notes, call the `iosEnableNotesUsage(true)` method before accessing the contact infos. Also, a `com.apple.developer.contacts.notes` entitlement must be added to the project. Before submitting your app to the AppStore, the permission for using the entitlement has to be granted as well. You can find a more detailed explanation [here](https://developer.apple.com/documentation/bundleresources/entitlements/com_apple_developer_contacts_notes?language=objc).

## API
 * `getAll`: Promise<Contact[]> - returns *all* contacts as an array of objects
 * `getAllWithoutPhotos` - same as `getAll` on Android, but on iOS it will not return uris for contact photos (because there's a significant overhead in creating the images)
 * `getContactById(contactId)`: Promise<Contact> - returns contact with defined contactId (or null if it doesn't exist)
 * `getCount()`: Promise<number> - returns the number of contacts
 * `getPhotoForId(contactId)`: Promise<string> - returns a URI (or null) for a contacts photo
 * `addContact(contact)`: Promise<Contact> - adds a contact to the AddressBook.  
 * `openContactForm(contact)` - create a new contact and display in contactsUI. 
 * `openExistingContact(contact)` - open existing contact (edit mode), where contact is an object with a valid recordID
 * `viewExistingContact(contact)` - open existing contact (view mode), where contact is an object with a valid recordID
 * `editExistingContact(contact)`: Promise<Contact> - add numbers to the contact, where the contact is an object with a valid recordID and an array of phoneNumbers
 * `updateContact(contact)`: Promise<Contact> - where contact is an object with a valid recordID  
 * `deleteContact(contact)` - where contact is an object with a valid recordID  
 * `getContactsMatchingString(string)`: Promise<Contact[]> - where string is any string to match a name (first, middle, family) to
 * `getContactsByPhoneNumber(string)`: Promise<Contact[]> - where string is a phone number to match to.
 * `getContactsByEmailAddress(string)`: Promise<Contact[]> - where string is an email address to match to.
 * `checkPermission()`: Promise<string> - checks permission to access Contacts _ios only_
 * `requestPermission()`: Promise<string> - request permission to access Contacts _ios only_
 * `writePhotoToPath()` - writes the contact photo to a given path _android only_

 ### ios group specific functions
 * `getGroups()`: Promise - returns an array of all groups. Each group contains `{ identifier: string; name: string;}`
 * `getGroup: (identifier: string)`: Promise - returns the group matching the provided group identifier.
 * `deleteGroup(identifier: string)`: Promise - deletes a group by group identifier.
 * `updateGroup(identifier: string, groupData: Pick<Group, 'name'>`: Promise - updates an existing group's details. You can only change the group name.
 * `addGroup(group: Pick<Group, 'name'>)`: Promise - adds a new group. Group name should be provided.
 * `contactsInGroup(identifier: string)`: Promise - retrieves all contacts within a specified group.
 * `addContactsToGroup(groupIdentifier: string, contactIdentifiers: string[])`: Promise - adds contacts to a group. Only contacts with id that has `:ABperson` as suffix can be added.
 * `removeContactsFromGroup(groupIdentifier: string, contactIdentifiers: string[])`: Promise - removes specified contacts from a group.

## Example Contact Record
```js
{
  recordID: '6b2237ee0df85980',
  backTitle: '',
  company: '',
  emailAddresses: [{
    label: 'work',
    email: 'carl-jung@example.com',
  }],
  familyName: 'Jung',
  givenName: 'Carl',
  middleName: '',
  jobTitle: '',
  phoneNumbers: [{
    label: 'mobile',
    number: '(555) 555-5555',
  }],
  hasThumbnail: true,
  thumbnailPath: 'content://com.android.contacts/display_photo/3',
  postalAddresses: [{
    label: 'home',
    formattedAddress: '',
    street: '123 Fake Street',
    pobox: '',
    neighborhood: '',
    city: 'Sample City',
    region: 'CA',
    state: 'CA',
    postCode: '90210',
    country: 'USA',
  }],
  prefix: 'MR',
  suffix: '',
  department: '',
  birthday: {'year': 1988, 'month': 1, 'day': 1 },
  imAddresses: [
    { username: '0123456789', service: 'ICQ'},
    { username: 'johndoe123', service: 'Facebook'}
  ],
  isStarred: false,
}
```

### Android only

* on Android versions below 8 the entire display name is passed in the `givenName` field. `middleName` and `familyName` will be `""`.
* isStarred field
* writePhotoToPath() - writes the contact photo to a given path

## iOS only

checkPermission(): Promise - checks permission to access Contacts
requestPermission(): Promise - request permission to access Contacts

## Adding Contacts
Currently all fields from the contact record except for thumbnailPath are supported for writing
```js
var newPerson = {
  emailAddresses: [{
    label: "work",
    email: "mrniet@example.com",
  }],
  familyName: "Nietzsche",
  givenName: "Friedrich",
}

Contacts.addContact(newPerson)
```

## Open Contact Form
Currently all fields from the contact record except for thumbnailPath are supported for writing
```js
var newPerson = {
  emailAddresses: [{
    label: "work",
    email: "mrniet@example.com",
  }],
  displayName: "Friedrich Nietzsche"
}

Contacts.openContactForm(newPerson).then(contact => {
  // contact has been saved
})
```
You may want to edit the contact before saving it into your phone book. So using `openContactForm` allow you to prompt default phone create contacts UI and the new to-be-added contact will be display on the contacts UI view. Click save or cancel button will exit the contacts UI view.

## Updating Contacts
Example
```js
Contacts.getAll().then(contacts => {
  // update the first record
  let someRecord = contacts[0]
  someRecord.emailAddresses.push({
    label: "junk",
    email: "mrniet+junkmail@test.com",
  })
  Contacts.updateContact(someRecord).then(() => {
    // record updated
  })
})
```
Update reference contacts by their recordID (as returned by the OS in getContacts). Apple does not guarantee the recordID will not change, e.g. it may be reassigned during a phone migration. Consequently you should always grab a fresh contact list with `getContacts` before performing update operations.

## Add numbers to an existing contact
Example
```js
var newPerson = { 
  recordID: '6b2237ee0df85980',
  phoneNumbers: [{
    label: 'mobile',
    number: '(555) 555-5555',
  }, ...
  ]
}

Contacts.editExistingContact(newPerson).then(contact => {
    //contact updated
});
```
Add one or more phone numbers to an existing contact. 
On Android the edited page will be opened. 
On iOS the already edited contact will be opened with the possibility of further modification.

### Bugs
There are issues with updating contacts on Android:
1. custom labels get overwritten to "Other",
1. postal address update code doesn't exist. (it exists for addContact)
See https://github.com/rt2zz/react-native-contacts/issues/332#issuecomment-455675041 for current discussions.

## Delete Contacts
You can delete a record using only it's recordID
```js
Contacts.deleteContact({recordID: 1}).then(recordId => {
  // contact deleted
})
```
Or by passing the full contact object with a `recordID` field.
```js
Contacts.deleteContact(contact).then((recordId) => {
  // contact deleted
})
```

## Displaying Thumbnails

The thumbnailPath is the direct URI for the temp location of the contact's cropped thumbnail image.

```js
<Image source={{uri: contact.thumbnailPath}} />
```

## Permissions Methods (optional)
`checkPermission` - checks permission to access Contacts.  
`requestPermission` - request permission to access Contacts.  

Usage as follows:
```js
Contacts.checkPermission().then(permission => {
  // Contacts.PERMISSION_AUTHORIZED || Contacts.PERMISSION_UNDEFINED || Contacts.PERMISSION_LIMITED || Contacts.PERMISSION_DENIED
  if (permission === 'undefined') {
    Contacts.requestPermission().then(permission => {
      // ...
    })
  }
  if (permission === 'authorized') {
    // yay!
  }
  if (permission === 'limited') {
    // ...
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
<p>If your business needs premium react native support please reach out to the maintainer.</p>
<a href="https://harrymoreno.com/hire-me">harrymoreno.com</a>
<table>
  <tbody>
    <tr>
      <td align="center">
        <a href="https://harrymoreno.com/hire-me">
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
