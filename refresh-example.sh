#!/bin/bash
set -e

echo "You should run this from directory where you have cloned the react-native-contacts repo"
echo "You should only do this when your git working set is completely clean (e.g., git reset --hard)"
echo "You must have already run \`npm install\` in the repository so \`npx react-native\` will work"
echo "This scaffolding refresh has been tested on macOS, if you use it on linux, it might not work"

# Copy the important files out temporarily
if [ -d TEMP ]; then
  echo "TEMP directory already exists - we use that to store files while refreshing."
  exit 1
else
  echo "Saving files to TEMP while refreshing scaffolding..."
  mkdir -p TEMP/android
  mkdir -p TEMP/ios/example/
  cp example/README.md TEMP/
  cp example/android/local.properties TEMP/android/
  cp example/App.js TEMP/
  cp example/ios/example/Info.plist TEMP/ios/example/
fi

# Purge the old sample
\rm -fr example

# Make the new example
npx react-native init example
pushd example
npm install github:rt2zz/react-native-contacts
npx react-native link react-native-contacts

# Patch the AndroidManifest directly to add our permissions
sed -i -e 's/INTERNET" \/>/INTERNET" \/><uses-permission android:name="android.permission.READ_PROFILE" \/>/' android/app/src/main/AndroidManifest.xml
rm -f android/app/src/main/AndroidManifest.xml??

# Copy the important files back in
popd
echo "Copying react-native-contacts example files into refreshed example..."
cp -frv TEMP/* example/

# Clean up after ourselves
\rm -fr TEMP
