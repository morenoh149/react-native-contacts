#import <AddressBook/AddressBook.h>
#import <UIKit/UIKit.h>
#import "RCTContacts.h"
#import "RCTLog.h"

@implementation RCTContacts

RCT_EXPORT_MODULE();

- (NSDictionary *)constantsToExport
{
  return @{
    @"PERMISSION_DENIED": @"denied",
    @"PERMISSION_AUTHORIZED": @"authorized",
    @"PERMISSION_UNDEFINED": @"undefined"
  };
}

RCT_EXPORT_METHOD(checkPermission:(RCTResponseSenderBlock) callback)
{
  int authStatus = ABAddressBookGetAuthorizationStatus();
  if ( authStatus == kABAuthorizationStatusDenied || authStatus == kABAuthorizationStatusRestricted){
    callback(@[[NSNull null], @"denied"]);
  } else if (authStatus == kABAuthorizationStatusAuthorized){
    callback(@[[NSNull null], @"authorized"]);
  } else { //ABAddressBookGetAuthorizationStatus() == kABAuthorizationStatusNotDetermined
    callback(@[[NSNull null], @"undefined"]);
  }
}

RCT_EXPORT_METHOD(requestPermission:(RCTResponseSenderBlock) callback)
{
  ABAddressBookRequestAccessWithCompletion(ABAddressBookCreateWithOptions(NULL, nil), ^(bool granted, CFErrorRef error) {
    if (!granted){
      [self checkPermission:callback];
      return;
    }
    [self checkPermission:callback];
  });
}

-(void) getAllContacts:(RCTResponseSenderBlock) callback
        withThumbnails:(BOOL) withThumbnails
{
    ABAddressBookRef addressBookRef = ABAddressBookCreateWithOptions(NULL, nil);
    int authStatus = ABAddressBookGetAuthorizationStatus();
    if(authStatus != kABAuthorizationStatusAuthorized){
        ABAddressBookRequestAccessWithCompletion(addressBookRef, ^(bool granted, CFErrorRef error) {
            if(granted){
                [self retrieveContactsFromAddressBook:addressBookRef withThumbnails:withThumbnails withCallback:callback];
            }else{
                NSDictionary *error = @{
                                        @"type": @"permissionDenied"
                                        };
                callback(@[error, [NSNull null]]);
            }
        });
    }
    else{
        [self retrieveContactsFromAddressBook:addressBookRef withThumbnails:withThumbnails withCallback:callback];
    }
}

RCT_EXPORT_METHOD(getAll:(RCTResponseSenderBlock) callback)
{
    [self getAllContacts:callback withThumbnails:true];
}

RCT_EXPORT_METHOD(getAllWithoutPhotos:(RCTResponseSenderBlock) callback)
{
    [self getAllContacts:callback withThumbnails:false];
}

-(void) retrieveContactsFromAddressBook:(ABAddressBookRef)addressBookRef
                         withThumbnails:(BOOL) withThumbnails
                           withCallback:(RCTResponseSenderBlock) callback
{
  NSArray *allContacts = (__bridge_transfer NSArray *)ABAddressBookCopyArrayOfAllPeopleInSourceWithSortOrdering(addressBookRef, NULL, kABPersonSortByLastName);
  int totalContacts = (int)[allContacts count];
  int currentIndex = 0;
  int maxIndex = --totalContacts;

  NSMutableArray *contacts = [[NSMutableArray alloc] init];

  while (currentIndex <= maxIndex){
    NSDictionary *contact = [self dictionaryRepresentationForABPerson: (ABRecordRef)[allContacts objectAtIndex:(long)currentIndex] withThumbnails:withThumbnails];

    if(contact){
      [contacts addObject:contact];
    }
    currentIndex++;
  }
  callback(@[[NSNull null], contacts]);
}

-(NSDictionary*) dictionaryRepresentationForABPerson:(ABRecordRef) person
                                      withThumbnails:(BOOL)withThumbnails
{
  NSMutableDictionary* contact = [NSMutableDictionary dictionary];

  NSNumber *recordID = [NSNumber numberWithInteger:(ABRecordGetRecordID(person))];
  NSString *givenName = (__bridge_transfer NSString *)(ABRecordCopyValue(person, kABPersonFirstNameProperty));
  NSString *familyName = (__bridge_transfer NSString *)(ABRecordCopyValue(person, kABPersonLastNameProperty));
  NSString *middleName = (__bridge_transfer NSString *)(ABRecordCopyValue(person, kABPersonMiddleNameProperty));

  [contact setObject: recordID forKey: @"recordID"];

  BOOL hasName = false;
  if (givenName) {
    [contact setObject: givenName forKey:@"givenName"];
    hasName = true;
  }

  if (familyName) {
    [contact setObject: familyName forKey:@"familyName"];
    hasName = true;
  }

  if(middleName){
    [contact setObject: (middleName) ? middleName : @"" forKey:@"middleName"];
  }

  if(!hasName){
    //nameless contact, do not include in results
    return nil;
  }

  //handle phone numbers
  NSMutableArray *phoneNumbers = [[NSMutableArray alloc] init];

  ABMultiValueRef multiPhones = ABRecordCopyValue(person, kABPersonPhoneProperty);
  for(CFIndex i=0;i<ABMultiValueGetCount(multiPhones);i++) {
    CFStringRef phoneNumberRef = ABMultiValueCopyValueAtIndex(multiPhones, i);
    CFStringRef phoneLabelRef = ABMultiValueCopyLabelAtIndex(multiPhones, i);
    NSString *phoneNumber = (__bridge_transfer NSString *) phoneNumberRef;
    NSString *phoneLabel = (__bridge_transfer NSString *) ABAddressBookCopyLocalizedLabel(phoneLabelRef);
    if(phoneNumberRef){
      CFRelease(phoneNumberRef);
    }
    if(phoneLabelRef){
      CFRelease(phoneLabelRef);
    }
    NSMutableDictionary* phone = [NSMutableDictionary dictionary];
    [phone setObject: phoneNumber forKey:@"number"];
    [phone setObject: phoneLabel forKey:@"label"];
    [phoneNumbers addObject:phone];
  }

  [contact setObject: phoneNumbers forKey:@"phoneNumbers"];
  //end phone numbers

  //handle emails
  NSMutableArray *emailAddreses = [[NSMutableArray alloc] init];
  ABMultiValueRef multiEmails = ABRecordCopyValue(person, kABPersonEmailProperty);
  for(CFIndex i=0;i<ABMultiValueGetCount(multiEmails);i++) {
    CFStringRef emailAddressRef = ABMultiValueCopyValueAtIndex(multiEmails, i);
    CFStringRef emailLabelRef = ABMultiValueCopyLabelAtIndex(multiEmails, i);
    NSString *emailAddress = (__bridge_transfer NSString *) emailAddressRef;
    NSString *emailLabel = (__bridge_transfer NSString *) ABAddressBookCopyLocalizedLabel(emailLabelRef);
    if(emailAddressRef){
      CFRelease(emailAddressRef);
    }
    if(emailLabelRef){
      CFRelease(emailLabelRef);
    }
    NSMutableDictionary* email = [NSMutableDictionary dictionary];
    [email setObject: emailAddress forKey:@"email"];
    [email setObject: emailLabel forKey:@"label"];
    [emailAddreses addObject:email];
  }
  [contact setObject: emailAddreses forKey:@"emailAddresses"];
  //end emails
  
  NSMutableArray *postalAddresses = [[NSMutableArray alloc] init];
  ABMultiValueRef multiPostalAddresses = ABRecordCopyValue(person, kABPersonAddressProperty);
  for(CFIndex i=0;i<ABMultiValueGetCount(multiPostalAddresses);i++) {
    NSMutableDictionary* address = [NSMutableDictionary dictionary];

    NSDictionary *addressDict = (__bridge NSDictionary *)ABMultiValueCopyValueAtIndex(multiPostalAddresses, i);
    NSString* street = [addressDict objectForKey:(NSString*)kABPersonAddressStreetKey];
    if(street){
      [address setObject:street forKey:@"street"];
    }
    NSString* city = [addressDict objectForKey:(NSString*)kABPersonAddressCityKey];
    if(city){
      [address setObject:city forKey:@"city"];
    }
    NSString* region = [addressDict objectForKey:(NSString*)kABPersonAddressStateKey];
    if(region){
      [address setObject:region forKey:@"region"];
    }
    NSString* postCode = [addressDict objectForKey:(NSString*)kABPersonAddressZIPKey];
    if(postCode){
      [address setObject:postCode forKey:@"postCode"];
    }
    NSString* country = [addressDict objectForKey:(NSString*)kABPersonAddressCountryCodeKey];
    if(country){
      [address setObject:country forKey:@"country"];
    }

    CFStringRef addresssLabelRef = ABMultiValueCopyLabelAtIndex(multiPostalAddresses, i);
    NSString *addressLabel = (__bridge_transfer NSString *) ABAddressBookCopyLocalizedLabel(addresssLabelRef);
    if(addresssLabelRef){
      CFRelease(addresssLabelRef);
    }
    [address setObject:addressLabel forKey:@"label"];

    [postalAddresses addObject:address];
  }
  CFRelease(multiPostalAddresses);
  [contact setObject:postalAddresses forKey:@"postalAddresses"];

  [contact setValue:[NSNumber numberWithBool:ABPersonHasImageData(person)] forKey:@"hasThumbnail"];
  if (withThumbnails) {
    [contact setObject: [self getABPersonThumbnailFilepath:person] forKey:@"thumbnailPath"];
  }
  
  //handle birthday from contacts//
  CFDateRef birthDate = ABRecordCopyValue(person, kABPersonBirthdayProperty);
  NSDateFormatter *dateFormatter = nil;
  if(birthDate)
  {
    if (dateFormatter == nil) {
      dateFormatter = [[NSDateFormatter alloc] init];
      [dateFormatter setTimeStyle:NSDateFormatterMediumStyle];
      [dateFormatter setDateStyle:NSDateFormatterMediumStyle];
    }
    NSString* strBirthday = [dateFormatter stringFromDate:(__bridge_transfer NSDate *)birthDate ];
    [contact setObject: strBirthday forKey:@"birthday"];
  }
  CFRelease(birthDate);
  ///////////////////////////////////

  //handle websites
  NSMutableArray *websites = [[NSMutableArray alloc] init];
  ABMultiValueRef multiUrls = ABRecordCopyValue(person, kABPersonURLProperty);
  for(CFIndex i=0;i<ABMultiValueGetCount(multiUrls);i++) {
    CFStringRef urlAddressRef = ABMultiValueCopyValueAtIndex(multiUrls, i);
    CFStringRef urlLabelRef = ABMultiValueCopyLabelAtIndex(multiUrls, i);
    NSString *urlAddress = (__bridge_transfer NSString *) urlAddressRef;
    NSString *urlLabel = (__bridge_transfer NSString *) ABAddressBookCopyLocalizedLabel(urlLabelRef);
    if(urlAddressRef){
      CFRelease(urlAddressRef);
    }
    if(urlLabelRef){
      CFRelease(urlLabelRef);
    }
    NSMutableDictionary* url = [NSMutableDictionary dictionary];
    [url setObject: urlAddress forKey:@"url"];
    [url setObject: urlLabel forKey:@"label"];
    [websites addObject:url];
  }
  [contact setObject: websites forKey:@"websites"];
  //end websites
  
  //handle postalAddresses
  NSMutableArray *postalAddresses = [[NSMutableArray alloc] init];
  ABMultiValueRef multiAddresses = ABRecordCopyValue(person, kABPersonAddressProperty);
  for(CFIndex i=0;i<ABMultiValueGetCount(multiAddresses);i++) {
    CFStringRef addrLabelRef = ABMultiValueCopyLabelAtIndex(multiAddresses, i);
    NSString *addrLabel = (__bridge_transfer NSString *) ABAddressBookCopyLocalizedLabel(addrLabelRef);
    if(addrLabelRef){
      CFRelease(addrLabelRef);
    }
    CFDictionaryRef dict = ABMultiValueCopyValueAtIndex(multiAddresses, i);
    NSString * street = CFDictionaryGetValue(dict, kABPersonAddressStreetKey);
    NSString * city = CFDictionaryGetValue(dict, kABPersonAddressCityKey);
    NSString * region = CFDictionaryGetValue(dict, kABPersonAddressStateKey);
    NSString * postcode = CFDictionaryGetValue(dict, kABPersonAddressZIPKey);
    NSString * country = CFDictionaryGetValue(dict, kABPersonAddressCountryKey);
    NSMutableDictionary* address = [NSMutableDictionary dictionary];
    [address setObject: addrLabel forKey:@"label"];
    [address setObject: street forKey:@"street"];
    [address setObject: city forKey:@"city"];
    [address setObject: region forKey:@"region"];
    [address setObject: postcode forKey:@"postcode"];
    [address setObject: country forKey:@"country"];
    [postalAddresses addObject:address];
  }
  [contact setObject: postalAddresses forKey:@"postalAddresses"];
  //end postalAddresses
  
  //handle note, nickName, phoneticGivenName, phoneticFamilyName, phoneticMiddleName, company, jobTitle//
  NSString *note = (__bridge_transfer NSString *)(ABRecordCopyValue(person, kABPersonNoteProperty));
  NSString *nickName = (__bridge_transfer NSString *)(ABRecordCopyValue(person, kABPersonNicknameProperty));
  NSString *phoneticGivenName = (__bridge_transfer NSString *)(ABRecordCopyValue(person, kABPersonFirstNamePhoneticProperty));
  NSString *phoneticFamilyName = (__bridge_transfer NSString *)(ABRecordCopyValue(person, kABPersonLastNamePhoneticProperty));
  NSString *phoneticMiddleName = (__bridge_transfer NSString *)(ABRecordCopyValue(person, kABPersonMiddleNamePhoneticProperty));
  NSString *company = (__bridge_transfer NSString *)(ABRecordCopyValue(person, kABPersonOrganizationProperty));
  NSString *jobTitle = (__bridge_transfer NSString *)(ABRecordCopyValue(person, kABPersonJobTitleProperty));
  if(note)               [contact setObject: note forKey:@"note"];
  if(nickName)           [contact setObject: nickName forKey:@"nickName"];
  if(phoneticGivenName)  [contact setObject: phoneticGivenName forKey:@"phoneticGivenName"];
  if(phoneticFamilyName) [contact setObject: phoneticFamilyName forKey:@"phoneticFamilyName"];
  if(phoneticMiddleName) [contact setObject: phoneticMiddleName forKey:@"phoneticMiddleName"];
  if(company)            [contact setObject: company forKey:@"company"];
  if(jobTitle)           [contact setObject: jobTitle forKey:@"jobTitle"];
  ////////////
  return contact;
}

-(NSString *) getABPersonThumbnailFilepath:(ABRecordRef) person
{
    if (ABPersonHasImageData(person)){

        NSNumber *recordID = [NSNumber numberWithInteger:(ABRecordGetRecordID(person))];
        NSString* filepath = [NSString stringWithFormat:@"%@/contact_%@.png", [self getPathForDirectory:NSCachesDirectory], recordID];

        NSData *contactImageData = (__bridge NSData *)ABPersonCopyImageDataWithFormat(person, kABPersonImageFormatThumbnail);
        BOOL success = [[NSFileManager defaultManager] createFileAtPath:filepath contents:contactImageData attributes:nil];
        
        if (!success) {
            NSLog(@"Unable to copy image");
            return @"";
        }
        
        return filepath;
    }
    
    return @"";
}

- (NSString *)getPathForDirectory:(int)directory
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains(directory, NSUserDomainMask, YES);
    return [paths firstObject];
}

RCT_EXPORT_METHOD(getPhotoForId:(nonnull NSNumber *)recordID callback:(RCTResponseSenderBlock)callback)
{
    ABAddressBookRef addressBookRef = ABAddressBookCreateWithOptions(NULL, nil);
    int authStatus = ABAddressBookGetAuthorizationStatus();
    if(authStatus != kABAuthorizationStatusAuthorized){
        ABAddressBookRequestAccessWithCompletion(addressBookRef, ^(bool granted, CFErrorRef error) {
            if(granted){
                callback(@[[NSNull null], [self getABPersonThumbnailFilepathForId:recordID addressBook:addressBookRef]]);
            }else{
                NSDictionary *error = @{
                                        @"type": @"permissionDenied"
                                        };
                callback(@[error, [NSNull null]]);
            }
        });
    }
    else{
        callback(@[[NSNull null], [self getABPersonThumbnailFilepathForId:recordID addressBook:addressBookRef]]);
    }
}

-(NSString *) getABPersonThumbnailFilepathForId:(NSNumber *)recordID
                                    addressBook:(ABAddressBookRef)addressBookRef
{
    ABRecordID abRecordId = (ABRecordID)[recordID intValue];
    ABRecordRef person = ABAddressBookGetPersonWithRecordID(addressBookRef, abRecordId);
    return [self getABPersonThumbnailFilepath:person];
}


RCT_EXPORT_METHOD(addContact:(NSDictionary *)contactData callback:(RCTResponseSenderBlock)callback)
{
  //@TODO keep addressbookRef in singleton
  ABAddressBookRef addressBookRef = ABAddressBookCreateWithOptions(NULL, nil);
  ABRecordRef newPerson = ABPersonCreate();

  CFErrorRef error = NULL;
  ABAddressBookAddRecord(addressBookRef, newPerson, &error);
  //@TODO error handling

  [self updateRecord:newPerson onAddressBook:addressBookRef withData:contactData completionCallback:callback];
}

RCT_EXPORT_METHOD(updateContact:(NSDictionary *)contactData callback:(RCTResponseSenderBlock)callback)
{
  ABAddressBookRef addressBookRef = ABAddressBookCreateWithOptions(NULL, nil);
  int recordID = (int)[contactData[@"recordID"] integerValue];
  ABRecordRef record = ABAddressBookGetPersonWithRecordID(addressBookRef, recordID);
  [self updateRecord:record onAddressBook:addressBookRef withData:contactData completionCallback:callback];
}

-(void) updateRecord:(ABRecordRef)record onAddressBook:(ABAddressBookRef)addressBookRef withData:(NSDictionary *)contactData completionCallback:(RCTResponseSenderBlock)callback
{
  CFErrorRef error = NULL;
  NSString *givenName = [contactData valueForKey:@"givenName"];
  NSString *familyName = [contactData valueForKey:@"familyName"];
  NSString *middleName = [contactData valueForKey:@"middleName"];
  ABRecordSetValue(record, kABPersonFirstNameProperty, (__bridge CFStringRef) givenName, &error);
  ABRecordSetValue(record, kABPersonLastNameProperty, (__bridge CFStringRef) familyName, &error);
  ABRecordSetValue(record, kABPersonMiddleNameProperty, (__bridge CFStringRef) middleName, &error);

  ABMutableMultiValueRef multiPhone = ABMultiValueCreateMutable(kABMultiStringPropertyType);
  NSArray* phoneNumbers = [contactData valueForKey:@"phoneNumbers"];
  for (id phoneData in phoneNumbers) {
    NSString *label = [phoneData valueForKey:@"label"];
    NSString *number = [phoneData valueForKey:@"number"];

    if ([label isEqual: @"main"]){
      ABMultiValueAddValueAndLabel(multiPhone, (__bridge CFStringRef) number, kABPersonPhoneMainLabel, NULL);
    }
    else if ([label isEqual: @"mobile"]){
      ABMultiValueAddValueAndLabel(multiPhone, (__bridge CFStringRef) number, kABPersonPhoneMobileLabel, NULL);
    }
    else if ([label isEqual: @"iPhone"]){
      ABMultiValueAddValueAndLabel(multiPhone, (__bridge CFStringRef) number, kABPersonPhoneIPhoneLabel, NULL);
    }
    else{
      ABMultiValueAddValueAndLabel(multiPhone, (__bridge CFStringRef) number, (__bridge CFStringRef) label, NULL);
    }
  }
  ABRecordSetValue(record, kABPersonPhoneProperty, multiPhone, nil);
  CFRelease(multiPhone);

  ABMutableMultiValueRef multiEmail = ABMultiValueCreateMutable(kABMultiStringPropertyType);
  NSArray* emails = [contactData valueForKey:@"emailAddresses"];
  for (id emailData in emails) {
    NSString *label = [emailData valueForKey:@"label"];
    NSString *email = [emailData valueForKey:@"email"];

    ABMultiValueAddValueAndLabel(multiEmail, (__bridge CFStringRef) email, (__bridge CFStringRef) label, NULL);
  }
  ABRecordSetValue(record, kABPersonEmailProperty, multiEmail, nil);
  CFRelease(multiEmail);
  ABAddressBookSave(addressBookRef, &error);
 
  //Add Profile Image//
  NSString *thumbnailPath = [contactData valueForKey:@"thumbnailPath"];
  UIImage *img = [UIImage imageWithContentsOfFile:thumbnailPath];
  NSData *dataRef = UIImagePNGRepresentation(img);
  CFDataRef cfDataRef = CFDataCreate(NULL, [dataRef bytes], [dataRef length]);
  ABPersonRemoveImageData(record, &error);
  if (ABAddressBookSave(addressBookRef, &error))
  {
    ABPersonSetImageData(record, cfDataRef, &error);
    ABAddressBookSave(addressBookRef, &error);
  }
  /////////////////////
  
  //Add Birthday//
  id objBirthday = [contactData valueForKey:@"birthday"];
  NSString *strMonth = [objBirthday valueForKey:@"month"];
  NSString *strDay = [objBirthday valueForKey:@"day"];
  strDay = [NSString stringWithFormat:@"%i", strDay.intValue + 1];
  
  NSDateFormatter* formatter = [[NSDateFormatter alloc] init];
  [formatter setDateFormat:@"dd.MM.yyyy"];
  NSDate *birthday = [formatter dateFromString:[NSString stringWithFormat:@"%@.%@.1604",strDay, strMonth]];
  
  ABRecordSetValue(record, kABPersonBirthdayProperty,(__bridge CFDateRef)birthday, &error);
  ABAddressBookSave (addressBookRef,&error);
  ////////////////
  
  //Add Website URLs//
  ABMutableMultiValueRef multiURL = ABMultiValueCreateMutable(kABMultiStringPropertyType);
  NSArray* urlArray = [contactData valueForKey:@"websites"];
  for (id urlData in urlArray) {
    NSString *label = [urlData valueForKey:@"label"];
    NSString *url = [urlData valueForKey:@"url"];
    
    ABMultiValueAddValueAndLabel(multiURL, (__bridge CFStringRef) url, (__bridge CFStringRef) label, NULL);
  }
  ABRecordSetValue(record, kABPersonURLProperty, multiURL, nil);
  CFRelease(multiURL);
  ABAddressBookSave(addressBookRef, &error);
  ////////////////////
  
  //Add Address//
  ABMultiValueRef addresses = ABMultiValueCreateMutable(kABMultiDictionaryPropertyType);
  NSArray* addressArray = [contactData valueForKey:@"postalAddresses"];
  for (id addressData in addressArray) {
    NSString *label = [addressData valueForKey:@"label"];
    NSString *street = [addressData valueForKey:@"street"];
    NSString *city = [addressData valueForKey:@"city"];
    NSString *region = [addressData valueForKey:@"region"];
    NSString *postcode = [addressData valueForKey:@"postcode"];
    NSString *country = [addressData valueForKey:@"country"];
    NSDictionary *values = [NSDictionary dictionaryWithObjectsAndKeys:
                            street, (NSString *)kABPersonAddressStreetKey,
                            city, (NSString *)kABPersonAddressCityKey,
                            region, (NSString *)kABPersonAddressStateKey,
                            postcode, (NSString *)kABPersonAddressZIPKey,
                            country, (NSString *)kABPersonAddressCountryKey,
                            nil];
    ABMultiValueAddValueAndLabel(addresses, (__bridge CFDictionaryRef)values, (__bridge CFStringRef)(label), NULL);
  }
  ABRecordSetValue(record, kABPersonAddressProperty, addresses, &error);
  CFRelease(addresses);
  ABAddressBookSave(addressBookRef, &error);
  ////////////////////
  
  //Add note, nickName, phoneticGivenName, phoneticFamilyName, phoneticMiddleName, company, jobTitle//
  NSString *note = [contactData valueForKey:@"note"];
  NSString *nickName = [contactData valueForKey:@"nickName"];
  NSString *phoneticGivenName = [contactData valueForKey:@"phoneticGivenName"];
  NSString *phoneticFamilyName = [contactData valueForKey:@"phoneticFamilyName"];
  NSString *phoneticMiddleName = [contactData valueForKey:@"phoneticMiddleName"];
  NSString *company = [contactData valueForKey:@"company"];
  NSString *jobTitle = [contactData valueForKey:@"jobTitle"];
  ABRecordSetValue(record, kABPersonNoteProperty, (__bridge CFStringRef) note, &error);
  ABRecordSetValue(record, kABPersonNicknameProperty, (__bridge CFStringRef) nickName, &error);
  ABRecordSetValue(record, kABPersonFirstNamePhoneticProperty, (__bridge CFStringRef) phoneticGivenName, &error);
  ABRecordSetValue(record, kABPersonLastNamePhoneticProperty, (__bridge CFStringRef) phoneticFamilyName, &error);
  ABRecordSetValue(record, kABPersonMiddleNamePhoneticProperty, (__bridge CFStringRef) phoneticMiddleName, &error);
  ABRecordSetValue(record, kABPersonOrganizationProperty, (__bridge CFStringRef) company, &error);
  ABRecordSetValue(record, kABPersonJobTitleProperty, (__bridge CFStringRef) jobTitle, &error);
  ABAddressBookSave(addressBookRef, &error);
  ////////////
  
  CFRelease(addressBookRef);
  if (error != NULL)
  {
    CFStringRef errorDesc = CFErrorCopyDescription(error);
    NSString *nsErrorString = (__bridge NSString *)errorDesc;
    callback(@[nsErrorString]);
    CFRelease(errorDesc);
  }
  else{
    callback(@[[NSNull null]]);
  }
}

RCT_EXPORT_METHOD(deleteContact:(NSDictionary *)contactData callback:(RCTResponseSenderBlock)callback)
{
  CFErrorRef error = NULL;
  ABAddressBookRef addressBookRef = ABAddressBookCreateWithOptions(NULL, nil);
  int recordID = (int)[contactData[@"recordID"] integerValue];
  ABRecordRef record = ABAddressBookGetPersonWithRecordID(addressBookRef, recordID);
  ABAddressBookRemoveRecord(addressBookRef, record, &error);
  ABAddressBookSave(addressBookRef, &error);
  //@TODO handle error
  callback(@[[NSNull null], [NSNull null]]);
}

@end
