var ReactNative = require('react-native')
var Rx = require('rx');
var Contacts = ReactNative.NativeModules.Contacts;

function _execAddressBookMethodWithParamsErrorData(func) {
  return Rx.Observable.create(function(observer) {
    func(function(error, data) {
      if (error) {
        observer.onError(error);
      } else {
        observer.onNext(data);
        observer.onCompleted();
      }
    });

    return null;
  });
}

function _execAddressBookMethodWithParamError(func) {
  return Rx.Observable.create(function(observer) {
    func(function(error) {
      if (error) {
        observer.onError(error);
      } else {
        observer.onCompleted();
      }
    });

    return null;
  });
}

Contacts.rx_checkPermission = function() {
  return _execAddressBookMethodWithParamsErrorData(Contacts.checkPermission);
};

Contacts.rx_requestPermission = function() {
  return _execAddressBookMethodWithParamsErrorData(Contacts.requestPermission);
};

Contacts.rx_getAll = function() {
  return _execAddressBookMethodWithParamsErrorData(Contacts.getAll);
};

Contacts.rx_addContact = function() {
  return _execAddressBookMethodWithParamError(Contacts.addContact);
}

Contacts.rx_updateContact = function() {
  return _execAddressBookMethodWithParamError(Contacts.updateContact);
}

Contacts.rx_deleteContact = function() {
  return _execAddressBookMethodWithParamError(Contacts.deleteContact);
}

module.exports = Contacts;
