
# React Native Contacts
Rx support with [react-native-contacts-rx](https://github.com/JeanLebrument/react-native-contacts-rx)

## Status
* Preliminary iOS and Android support
* API subject to revision, changelog in release notes  

| Feature | iOS | Android |
| ------- | --- | ------- |
| `getAll`  | ✔   | ✔ |
| `addContact` | ✔ | ✔ |
| `updateContact` | ✔ | ✔ |
| `deleteContact` | ✔ | ✔ |
| get with options | 😞 | 😞 |
| groups  | 😞 | 😞 |



## API
`getAll` (callback) - returns *all* contacts as an array of objects  
`addContact` (contact, callback) - adds a contact to the AddressBook.  
`updateContact` (contact, callback) - where contact is an object with a valid recordID  
`deleteContact` (contact, callback) - where contact is an object with a valid recordID  
`checkPermission` (callback) - checks permission to access Contacts  
`requestPermission` (callback) - request permission to access Contacts

## contact object
The following contact fields are supported on iOS and Android. Where a field label is indicated, the Contact field populates the native field with as the label.

| Key Name   | Value Type | iOS* | Android | Description |
|------------|------------|------|---------|-------------|
| recordID   | Integer    | R  | R     | Native contact manger record ID for this contact. Returned by getAll() and used to indicate contact record for updateContact(). Ignored by addContact(). Value is native platform dependent.
| familyName | String     | R/W  | R/W     | Family name or "last name"
| givenName  | String     | R/W  | R/W     | Given name or "first name"
| middleName | String     | R/W  | R/W     | Middle name or names
| nickName   | String     | R/W     | R/W     | Contact's nickname
| phoneticFamilyName | String | R/W | R/W | Phonetic representation of familyName
| phoneticMiddleName | String | R/W | R/W | Phonetic representation of middleName
| phoneticGivenName | String | R/W  | R/W | Phonetic representation of givenName
| company    | String     | R/W  | R/W     | Where the Contact works
| jobTitle   | String     | R/W  | R/W     | Contact's job title
| phoneNumbers | Array    | R/W  | R/W     | see [phoneNumbers](#phonenumbers)
| emailAddresses | Array  | R/W  | R/W     | see [emailAddresses](#emailaddresses)
| websites   | Array      | R/W  | R/W     | see [websites](#websites)
| postalAddresses| Array  | R/W  | R/W     | see [postalAddresses](#postaladdresses)
| note       | String     | R/W  | R/W     | Note about contact. Appears in "Notes" on native Contact Manager
| birthday   | Object     | R/W    | R/W     | The contact's birthday, with or without year, as ```{ year: int, month: int, day: int }```[1]
| thumbnailPath | String  | R/W  | R/W     | A 'file://' URL pointing to the contact's thumbnail image on the native device filesystem. See [Notes on adding and updating thumbnailPath](#notes-on-adding-and-updatring-thumbnailPath)


[1] Android: Not all contact managers show birthday, however value can be written, read, and synced

#### phoneNumbers

An array of Objects containing phone numbers with the following key names:

| Key Name   | Value Type | Description |
|------------|------------|-------------|
| label      | String     | One of: *home, work, mobile, fax, other*. An unrecognzied label will be added as 'other'|
| number     | String     | A String containing the phone number associated with *label*
| primary    | boolean    | Default = *false* *(WIP) Indicates this number is the Contact's primary number. If more than one phone number is provided to addContact() in the array, and none or more than one have primary set to *true*, the number added as the primary contact number is undefined


#### emailAddresses

An array of Objects containing email addresses with the following key names:

| Key Name   | Value Type | Description |
|------------|------------|-------------|
| label      | String     | One of: *home, work, mobile, other*. An unrecognzied label will be added as 'other'|
| email      | String     | A String containing the email address associated with *label*
| primary    | boolean    | Default = *false* *(WIP) Indicates this email address is the Contact's primary email address. If more than one email address is provided to addContact() in the array, and none or more than one have primary set to *true*, the address added as the primary contact email address is undefined


#### websites

An array of Objects containing phone numbers with the following key names:

| Key Name   | Value Type | Description |
|------------|------------|-------------|
| label      | String     | One of: *home, work, homepage, profile, blog, other*. An unrecognzied label will be added as 'other'|
| url     | String     | A String containing the URL associated with *label*

#### postalAddresses

An array of Objects containing postal (physical) addresses with the following key names:

| Key Name   | Value Type | Description |
|------------|------------|-------------|
| label      | String     | One of: *home, work, other*. An unrecognzied label will be added as 'other'|
| street     | String     | The complete street address ("123 N. Main Street Suite 500") associated with *label*
| city       | String     | The city in which the address resides.
| region     | String     | A location designation between *city* and *country*, if customary or necessary. For addresses in the USA this is the "state", in Canada the "province", etc. *Platform note: Maps to "REGION" on Android and "STATE" on iOS.*
| country    | String     | The *ISO 3166-1 ALPHA-2* country code. (Multilingual JSON files can be found at https://github.com/umpirsky/country-list)
| postcode   | String     | The postal code (zip code) for this address.
| primary    | boolean    | Default = *false* *(WIP) Indicates this postal address is the Contact's primary postal address. If more than one postal address is provided to addContact() in the array, and none or more than one have primary set to *true*, the address added as the primary postal address is undefined

#### socialServices

** IMPLEMENTATION PENDING **

An array of Objects containing social media accounts for the contact with the following key names. All keys except "serviceId" are optional.

Android implementation notes: As of API 25, the native contact manager only supports IM services, not social media services.

| Key Name   | Value Type | Description |
|------------|------------|-------------|
| serviceId  | String     | A label indicating the social media service. iOS currently supports: "facebook", "flickr", linkedin", "myspace", sinaweibo", tencentweibo", "twitter", "yelp", and "gamecenter", and "custom".
| serviceName | String | When serviceId is "custom", a string that represents the name of the service for use in user interfaces.
| url | String | A URL associated with the social profile
| uid | String | The user ID for this contact on this service
| name | String | The name of this user on this service


#### Notes on Adding and Updating thumbnailPath

When calling addContact():

>Should be the full size image for the contact in jpeg format. If the image is high-resoltuion, a low-resolution thumbnail will automatically be generated to be used in conjunction with the contact image.

When calling updateContact():

>If you are updating a contact retrieved from getAll(), remove thumbnailPath from your contact object if the photo should not be updated. The API can not determine if the current profile photo and thumbnailPath are the same and will always copy the thumbnailPath image (which is slow)


## Example Contact Record
```js
{
  recordID: 1,

  familyName: "Jung",
  givenName: "Carl",
  middleName: "C.",
  nickName: "Carl-o",
  phoenticGivenName: "CAR-el",
  phoneticFamilyName: "YUH-ng",

  company: "Foomatics, Inc.",
  jobTitle: "Developer",

  phoneNumbers: [
    {
      label: "mobile",
      number: "(555) 555-5555",
      primary: true,
    },{
      label: "work",
      number: "(555) 555-5556",
    }
  ],

  emailAddresses: [{
    label: "work",
    email: "carl-jung@example.com",
  }],

  websites: [{
    label: "homepage",
    url: "http://www.carljung.com/",
  }],

  postalAddresses: [{
    label: "work",
    street: "123 N Main Street",
    city: "Chicago",
    region: "IL", //Illinois in USA
    postcode: "12345-A234",
    country: "US",
  }],

  note: "This is a note",

  birthday: {
      month: 6,
      day: 12,
  },

  thumbnailPath: "file:///device/path/to/image.jpg",
}
```

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
If you are using an older version of RN (i.e. `MainApplication.java` does not contain this method (or doesn't exist) and MainActivity.java starts with `public class MainActivity extends Activity`) please see the [old instructions](https://github.com/rt2zz/react-native-contacts/tree/1ce4b876a416bc2ca3c53e7d7e0296f7fcb7ce40#android)

* add Contacts permission (in android/app/src/main/AndroidManifest.xml)
(only add the permissions you need)
```xml
...
  <uses-permission android:name="android.permission.READ_CONTACTS" />
  <uses-permission android:name="android.permission.WRITE_CONTACTS" />
  <uses-permission android:name="android.permission.READ_PROFILE" />
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
