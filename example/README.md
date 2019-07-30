# react-native-contacts example project

## Installation

* `git clone https://github.com/rt2zz/react-native-contacts.git`
* `cd react-native-contacts/example`
* `npm install`

## Running Android

* make sure you have no other packagers running!
* start an emulator (e.g., using Android Studio -> Tools -> AVD Manager -> start one)
* `npx react-native run-android`

## Running iOS

* make sure you have no other packagers running!

### Without CocoaPods

* `npx react-native run-ios`

### With CocoaPods

* `cd ios && pod install && cd ..`
* `npx react-native run-ios`

## Troubleshooting

* if things don't work, clean up all your build and node_modules folders, npm install and rebuild
