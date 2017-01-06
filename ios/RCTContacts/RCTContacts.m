#import <AddressBook/AddressBook.h>
#import <UIKit/UIKit.h>
#import "RCTContacts.h"
#import <Contacts/Contacts.h>

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
    CNContactStore * contactStore = [[CNContactStore alloc] init];
    CNEntityType entityType = CNEntityTypeContacts;
    if( [CNContactStore authorizationStatusForEntityType:entityType] == CNAuthorizationStatusNotDetermined)
    {
        [contactStore requestAccessForEntityType:entityType completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if(granted){
                [self retrieveContactsFromAddressBook:contactStore withThumbnails:withThumbnails withCallback:callback];
            }else{
                NSDictionary *error = @{
                                        @"type": @"permissionDenied"
                                        };
                callback(@[error, [NSNull null]]);
            }
        }];
    }
    else if( [CNContactStore authorizationStatusForEntityType:entityType]== CNAuthorizationStatusAuthorized)
    {
        [self retrieveContactsFromAddressBook:contactStore withThumbnails:withThumbnails withCallback:callback];
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

-(void) retrieveContactsFromAddressBook:(CNContactStore*)contactStore
                         withThumbnails:(BOOL) withThumbnails
                           withCallback:(RCTResponseSenderBlock) callback
{
    NSMutableArray *contacts = [[NSMutableArray alloc] init];

    NSError* contactError;
    [contactStore containersMatchingPredicate:[CNContainer predicateForContainersWithIdentifiers: @[contactStore.defaultContainerIdentifier]] error:&contactError];


    NSMutableArray *keysToFetch = [[NSMutableArray alloc]init];
    [keysToFetch addObjectsFromArray:@[
                                       CNContactEmailAddressesKey,
                                       CNContactPhoneNumbersKey,
                                       CNContactFamilyNameKey,
                                       CNContactGivenNameKey,
                                       CNContactMiddleNameKey,
                                       CNContactPostalAddressesKey,
                                       CNContactOrganizationNameKey,
                                       CNContactJobTitleKey,
                                       CNContactImageDataAvailableKey
                                       ]];

    if(withThumbnails) {
        [keysToFetch addObject:CNContactThumbnailImageDataKey];
    }

    CNContactFetchRequest * request = [[CNContactFetchRequest alloc]initWithKeysToFetch:keysToFetch];
    BOOL success = [contactStore enumerateContactsWithFetchRequest:request error:&contactError usingBlock:^(CNContact * __nonnull contact, BOOL * __nonnull stop){
        NSDictionary *contactDict = [self dictionaryRepresentationForABPerson: contact withThumbnails:withThumbnails];

        [contacts addObject:contactDict];
    }];

    callback(@[[NSNull null], contacts]);
}

-(NSDictionary*) dictionaryRepresentationForABPerson:(CNContact *) person
                                      withThumbnails:(BOOL)withThumbnails
{
    NSMutableDictionary* output = [NSMutableDictionary dictionary];

    NSString *recordID = person.identifier;
    NSString *givenName = person.givenName;
    NSString *familyName = person.familyName;
    NSString *middleName = person.middleName;
    NSString *company = person.organizationName;
    NSString *jobTitle = person.jobTitle;

    [output setObject: recordID forKey: @"recordID"];

    BOOL hasName = false;
    if (givenName) {
        [output setObject: (givenName) ? givenName : @"" forKey:@"givenName"];
        hasName = true;
    }

    if (familyName) {
        [output setObject: (familyName) ? familyName : @"" forKey:@"familyName"];
        hasName = true;
    }

    if(middleName){
        [output setObject: (middleName) ? middleName : @"" forKey:@"middleName"];
    }

    if(company){
        [output setObject: (company) ? company : @"" forKey:@"company"];
    }

    if(jobTitle){
        [output setObject: (jobTitle) ? jobTitle : @"" forKey:@"jobTitle"];
    }

    //handle phone numbers
    NSMutableArray *phoneNumbers = [[NSMutableArray alloc] init];

    for (CNLabeledValue<CNPhoneNumber*>* labeledValue in person.phoneNumbers) {
        NSMutableDictionary* phone = [NSMutableDictionary dictionary];

        CNPhoneNumber* cnPhoneNumber = [labeledValue value];
        [phone setObject: cnPhoneNumber.stringValue forKey:@"number"];
        [phone setObject: [CNLabeledValue localizedStringForLabel:[labeledValue label]] forKey:@"label"];
        [phoneNumbers addObject:phone];
    }

    [output setObject: phoneNumbers forKey:@"phoneNumbers"];
    //end phone numbers

    //handle emails
    NSMutableArray *emailAddreses = [[NSMutableArray alloc] init];

    for (CNLabeledValue<NSString*>* labeledValue in person.emailAddresses) {
        NSMutableDictionary* email = [NSMutableDictionary dictionary];

        [email setObject: [labeledValue value] forKey:@"email"];
        [email setObject: [CNLabeledValue localizedStringForLabel:[labeledValue label]] forKey:@"label"];
        [emailAddreses addObject:email];
    }

    [output setObject: emailAddreses forKey:@"emailAddresses"];
    //end emails

    //handle postal addresses
    NSMutableArray *postalAddresses = [[NSMutableArray alloc] init];

    for (CNLabeledValue<CNPostalAddress*>* labeledValue in person.postalAddresses) {
        CNPostalAddress* postalAddress = labeledValue.value;
        NSMutableDictionary* address = [NSMutableDictionary dictionary];

        NSString* street = postalAddress.street;
        if(street){
            [address setObject:street forKey:@"street"];
        }
        NSString* city = postalAddress.city;
        if(city){
            [address setObject:city forKey:@"city"];
        }
        NSString* region = postalAddress.city;
        if(region){
            [address setObject:region forKey:@"region"];
        }
        NSString* postCode = postalAddress.postalCode;
        if(postCode){
            [address setObject:postCode forKey:@"postCode"];
        }
        NSString* country = postalAddress.country;
        if(country){
            [address setObject:country forKey:@"country"];
        }

        NSString *addressLabel = labeledValue.label;
        [address setObject:[CNLabeledValue localizedStringForLabel:addressLabel] forKey:@"label"];

        [postalAddresses addObject:address];
    }

    [output setObject:postalAddresses forKey:@"postalAddresses"];
    //end postal addresses

    [output setValue:[NSNumber numberWithBool:person.imageDataAvailable] forKey:@"hasThumbnail"];
    if (withThumbnails) {
        [output setObject: [self getFilePathForThumbnailImage:person recordID:recordID] forKey:@"thumbnailPath"];
    }

    return output;
}

- (NSString *)thumbnailFilePath:(NSString *)recordID
{
    NSString *filename = [recordID stringByReplacingOccurrencesOfString:@":ABPerson" withString:@""];
    NSString* filepath = [NSString stringWithFormat:@"%@/contact_%@.png", [self getPathForDirectory:NSCachesDirectory], filename];
    return filepath;
}

-(NSString *) getFilePathForThumbnailImage:(CNContact*) contact recordID:(NSString*) recordID
{
    NSString *filepath = [self thumbnailFilePath:recordID];

    if([[NSFileManager defaultManager] fileExistsAtPath:filepath]) {
        return filepath;
    }

    if (contact.imageDataAvailable){
        NSData *contactImageData = contact.thumbnailImageData;

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

RCT_EXPORT_METHOD(getPhotoForId:(nonnull NSString *)recordID callback:(RCTResponseSenderBlock)callback)
{
    CNContactStore * contactStore = [[CNContactStore alloc] init];
    CNEntityType entityType = CNEntityTypeContacts;
    if([CNContactStore authorizationStatusForEntityType:entityType] == CNAuthorizationStatusNotDetermined)
    {
        [contactStore requestAccessForEntityType:entityType completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if(granted){
                callback(@[[NSNull null], [self getFilePathForThumbnailImage:recordID addressBook:contactStore]]);
            }
        }];
    }
    else if( [CNContactStore authorizationStatusForEntityType:entityType]== CNAuthorizationStatusAuthorized)
    {
        callback(@[[NSNull null], [self getFilePathForThumbnailImage:recordID addressBook:contactStore]]);
    }
}

-(NSString *) getFilePathForThumbnailImage:(NSString *)recordID
                               addressBook:(CNContactStore*)addressBook
{
    NSString *filepath = [self thumbnailFilePath:recordID];

    if([[NSFileManager defaultManager] fileExistsAtPath:filepath]) {
        return filepath;
    }

    NSError* contactError;
    //    [addressBook containersMatchingPredicate:[CNContainer predicateForContainersWithIdentifiers: @[addressBook.defaultContainerIdentifier]] error:&contactError];
    NSArray * keysToFetch =@[CNContactThumbnailImageDataKey, CNContactImageDataAvailableKey];
    CNContact* contact = [addressBook unifiedContactWithIdentifier:recordID keysToFetch:keysToFetch error:&contactError];

    return [self getFilePathForThumbnailImage:contact recordID:recordID];
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
    NSString *company = [contactData valueForKey:@"company"];
    NSString *jobTitle = [contactData valueForKey:@"jobTitle"];
    ABRecordSetValue(record, kABPersonFirstNameProperty, (__bridge CFStringRef) givenName, &error);
    ABRecordSetValue(record, kABPersonLastNameProperty, (__bridge CFStringRef) familyName, &error);
    ABRecordSetValue(record, kABPersonMiddleNameProperty, (__bridge CFStringRef) middleName, &error);
    ABRecordSetValue(record, kABPersonOrganizationProperty, (__bridge CFStringRef) company, &error);
    ABRecordSetValue(record, kABPersonJobTitleProperty, (__bridge CFStringRef) jobTitle, &error);

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
