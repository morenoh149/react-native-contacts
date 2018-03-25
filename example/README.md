# Example

This example was created with [create react native app](https://github.com/react-community/create-react-native-app).

## Running
* `npm install` to install project dependencies
* `npm link ../` allows you to quickly test out native code changes
(obj-c/java) without reinstalling npm dependencies

### Android
* make sure you have android sdks installed via android studio
* create and launch an avd via android studio
* `react-native run-android` to install the example app
* `adb shell input keyevent 82` to enable live reloading
* `react-native log-android` to see logs
* add a contact to the addressbook
* 

## Wishlist

* ~~render all contacts in ScrollView by calling `getAll()`~~
* ~~button to add dummy contact to devices' addressbook via `addContact()`~~
* ~~button to mutate first contact's name to 'RN tester' via `updateContact()`~~
* ~~button to delete first contact via `deleteContact()`~~
* TextInput to test `get with options`
* TextInput to test `groups`
