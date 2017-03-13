# React Native Contacts
To contribute read [CONTRIBUTING.md](https://github.com/rt2zz/react-native-contacts).

Rx support with [react-native-contacts-rx](https://github.com/JeanLebrument/react-native-contacts-rx)

## Status
* Preliminary iOS and Android support
* API subject to revision, changelog in release notes  

| Feature | iOS | Android |
| ------- | --- | ------- |
| `getAll`  | ✔   | ✔ |
| `addContact` | ✔ | ✔ |
| `updateContact` | ✔ | ✔ |
| `deleteContact` | ✔ | 😞 |
| get with options | 😞 | 😞 |
| groups  | 😞 | 😞 |


## API

 * `getAll` (callback) - returns *all* contacts as an array of objects
 * `getAllWithoutPhotos` - same as `getAll` on Android, but on iOS it will not return uris for contact photos (because there's a significant overhead in creating the images)
 * `getPhotoForId(contactId, callback)` - returns a URI (or null) for a contacts photo
 * `addContact` (contact, callback) - adds a contact to the AddressBook.  
 * `updateContact` (contact, callback) - where contact is an object with a valid recordID  
 * `deleteContact` (contact, callback) - where contact is an object with a valid recordID  
 * `checkPermission` (callback) - checks permission to access Contacts  
 * `requestPermission` (callback) - request permission to access Contacts

## Usage
`getAll` is a database intensive process, and can take a long time to complete depending on the size of the contacts list. Because of this, it is recommended you access the `getAll` method before it is needed, and cache the results for future use.

Also there is a lot of room for performance enhancements in both iOS and android. PR's welcome!

```js
var Contacts = require('react-native-contacts')

Contacts.getAll((err, contacts) => {
  if(err && err.type === 'permissionDenied'){
    // x.x
  } else {
    console.log(contacts)
  }
})
```

## Example Contact Record
```js
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
  middleName: "",
  phoneNumbers: [{
    label: "mobile",
    number: "(555) 555-5555",
  }],
  thumbnailPath: 'content://com.android.contacts/display_photo/3',
  postalAddresses: 
    [ 
      {
        postCode: 'Postcooode',
        city: 'City',
        neighborhood: 'neighborhood',
        street: 'Home Street',
        formattedAddress: 'Home Street\nneighborhood\nCity Postcooode',
        label: 'work' 
      }
    ]
}
```
**NOTE**
* on Android the entire display name is passed in the `givenName` field. `middleName` and `familyName` will be `""`.

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

Contacts.addContact(newPerson, (err) => { /*...*/ })
```

## Updating and Deleting Contacts
```js
//contrived example
Contacts.getAll( (err, contacts) => {
  //update the first record
  let someRecord = contacts[0]
  someRecord.emailAddresses.push({
    label: "junk",
    email: "mrniet+junkmail@test.com",
  })
  Contacts.updateContact(someRecord, (err) => { /*...*/ })

  //delete the second record
  Contacts.deleteContact(contacts[1], (err) => { /*...*/ })
})
```
Update and delete reference contacts by their recordID (as returned by the OS in getContacts). Apple does not guarantee the recordID will not change, e.g. it may be reassigned during a phone migration. Consequently you should always grab a fresh contact list with `getContacts` before performing update and delete operations.

You can also delete a record using only it's recordID like follows: `Contacts.deleteContact({recordID: 1}, (err) => {})}`

## Displaying Thumbnails

The thumbnailPath is the direct URI for the temp location of the contact's cropped thumbnail image.

```js
<Image source={{uri: contact.thumbnailPath}} />
```

## Getting started
run `npm install react-native-contacts`

### iOS
1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. add `./node_modules/react-native-contacts/ios/RCTContacts.xcodeproj`
3. In the XCode project navigator, select your project, select the `Build Phases` tab and in the `Link Binary With Libraries` section add **libRCTContacts.a**

#### Permissions
As of Xcode 8 and React Native 0.33 it is now **necessary to add kit specific "permission" keys** to your Xcode `Info.plist` file, in order to make `requestPermission` work. Otherwise your app crashes when requesting the specific permission. I discovered this after days of frustration.

Open Xcode > Info.plist > Add a key (starting with "Privacy - ...") with your kit specific permission. The value for the key is optional.

You have to add the key "Privacy - Contacts Usage Description".

<img width="338" alt="screen shot 2016-09-21 at 13 13 21" src="https://cloud.githubusercontent.com/assets/5707542/18704973/3cde3b44-7ffd-11e6-918b-63888e33f983.png">

### Android

_For versions of RN before [v0.21.0](https://github.com/facebook/react-native/releases/tag/v0.21.0) use the [old instructions](https://github.com/rt2zz/react-native-contacts/tree/1ce4b876a416bc2ca3c53e7d7e0296f7fcb7ce40#android)._

* In `android/settings.gradle`
```gradle
...
include ':react-native-contacts'
project(':react-native-contacts').projectDir = new File(settingsDir, '../node_modules/react-native-contacts/android')
```

* In `android/app/build.gradle`
```gradle
...
dependencies {
    ...
    compile project(':react-native-contacts')
}
```

* register module (in android/app/src/main/java/com/[your-app-name]/MainApplication.java)
```java
	...

	import com.rt2zz.reactnativecontacts.ReactNativeContacts; 	// <--- import module!

	public class MainApplication extends Application implements ReactApplication {
	    ...
		
	    @Override
	    protected List<ReactPackage> getPackages() {
	      return Arrays.<ReactPackage>asList(
	        new MainReactPackage(),
	        new ReactNativeContacts() 	// <--- and add package
	      );
	    }

    	...
    }
```

* add Contacts permission (in android/app/src/main/AndroidManifest.xml)
  * only add the permissions you need
  * `READ_PROFILE` may be a required for other permissions
```xml
...
  <uses-permission android:name="android.permission.READ_PROFILE" />
  <uses-permission android:name="android.permission.READ_CONTACTS" />
  <uses-permission android:name="android.permission.WRITE_CONTACTS" />
...
```

## Permissions Methods (optional)
`checkPermission` (callback) - checks permission to access Contacts.  
`requestPermission` (callback) - request permission to access Contacts.  

Usage as follows:
```js
Contacts.checkPermission( (err, permission) => {
  // Contacts.PERMISSION_AUTHORIZED || Contacts.PERMISSION_UNDEFINED || Contacts.PERMISSION_DENIED
  if(permission === 'undefined'){
    Contacts.requestPermission( (err, permission) => {
      // ...
    })
  }
  if(permission === 'authorized'){
    // yay!
  }
  if(permission === 'denied'){
    // x.x
  }
})
```

These methods do **not** re-request permission if permission has already been granted or denied. This is a limitation in iOS, the best you can do is prompt the user with instructions for how to enable contacts from the phone settings page `Settings > [app name] > contacts`.

On android permission request is done upon install so this function will only show if the  permission has been granted.

## Todo
- [ ] android feature parity
- [ ] migrate iOS from AddressBook to Contacts
- [ ] implement `get` with options
- [ ] groups support

## LICENSE

[MIT License](LICENSE)
