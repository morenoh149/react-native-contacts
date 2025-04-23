#import <AddressBook/AddressBook.h>
#import <UIKit/UIKit.h>
#import "RCTContacts.h"
#import <AssetsLibrary/AssetsLibrary.h>
#import <React/RCTLog.h>
#import <React/RCTUtils.h>
#import <Photos/Photos.h>

@implementation RCTContacts {
    CNContactStore * contactStore;

    RCTPromiseResolveBlock updateContactPromise;
    CNMutableContact* selectedContact;
    
    BOOL notesUsageEnabled;
}

- (instancetype)init
{
    self = [super init];
    if (self) {
        [self preLoadContactView];
    }
    return self;
}

- (void)preLoadContactView
{
    // Init the contactViewController so it will display quicker first time it's accessed
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        NSLog(@"Preloading CNContactViewController");
        CNContactViewController *contactViewController = [CNContactViewController viewControllerForNewContact:nil];
        [contactViewController view];
    });
}

RCT_EXPORT_MODULE();

- (NSDictionary *)constantsToExport
{
    return @{
             @"PERMISSION_DENIED": @"denied",
             @"PERMISSION_AUTHORIZED": @"authorized",
             @"PERMISSION_LIMITED": @"limited",
             @"PERMISSION_UNDEFINED": @"undefined"
             };
}

RCT_REMAP_METHOD(getAll, withResolver:(RCTPromiseResolveBlock) resolve
                 withRejecter:(RCTPromiseRejectBlock) reject)
{
  [self getAll:resolve reject:reject];
}


RCT_EXPORT_METHOD(checkPermission:(RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock) __unused reject)
{
    CNAuthorizationStatus authStatus = [CNContactStore authorizationStatusForEntityType:CNEntityTypeContacts];

    if (authStatus == CNAuthorizationStatusDenied || authStatus == CNAuthorizationStatusRestricted) {
        resolve(@"denied");
    } else if (authStatus == CNAuthorizationStatusAuthorized) {
        resolve(@"authorized");
    } else if (@available(iOS 18, *)) {
        if (authStatus == CNAuthorizationStatusRestricted) {
            resolve(@"limited");
        } else {
            resolve(@"undefined");
        }
    } else {
        resolve(@"undefined");
    }
}


RCT_EXPORT_METHOD(requestPermission:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) __unused reject)
{
    CNContactStore* contactStore = [[CNContactStore alloc] init];

    [contactStore requestAccessForEntityType:CNEntityTypeContacts completionHandler:^(BOOL granted, NSError * _Nullable error) {
        [self checkPermission:resolve rejecter:reject];
    }];
}

RCT_EXPORT_METHOD(iosEnableNotesUsage:(BOOL) enabled)
{
    notesUsageEnabled = enabled;
}

RCT_EXPORT_METHOD(getContactsMatchingString:(NSString *)string resolver:(RCTPromiseResolveBlock) resolve
    rejecter:(RCTPromiseRejectBlock) reject)
{
    CNContactStore *contactStore = [[CNContactStore alloc] init];
    if (!contactStore)
        return;
    [self getContactsFromAddressBook:contactStore matchingString:string resolve:resolve];
}

-(void) getContactsFromAddressBook:(CNContactStore *)store
                    matchingString:(NSString *)searchString
                          resolve:(RCTPromiseResolveBlock) resolve
{
    NSMutableArray *contacts = [[NSMutableArray alloc] init];
    NSError *contactError = nil;
    NSMutableArray *keys = [NSMutableArray arrayWithArray: @[
        CNContactEmailAddressesKey,
        CNContactPhoneNumbersKey,
        CNContactFamilyNameKey,
        CNContactGivenNameKey,
        CNContactMiddleNameKey,
        CNContactPostalAddressesKey,
        CNContactOrganizationNameKey,
        CNContactJobTitleKey,
        CNContactImageDataAvailableKey,
        CNContactThumbnailImageDataKey,
        CNContactUrlAddressesKey,
        CNContactBirthdayKey,
        CNContactInstantMessageAddressesKey
    ]];
    if(notesUsageEnabled) {
        [keys addObject: CNContactNoteKey];
    }
    
    NSArray *arrayOfContacts = [store unifiedContactsMatchingPredicate:[CNContact predicateForContactsMatchingName:searchString]
                                                           keysToFetch:keys
                                                                 error:&contactError];
    [arrayOfContacts enumerateObjectsUsingBlock:^(CNContact * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        NSDictionary *contactDictionary = [self contactToDictionary:obj withThumbnails:true];
        [contacts addObject:contactDictionary];
    }];
    resolve(contacts);
}

RCT_EXPORT_METHOD(getContactsByPhoneNumber:(NSString *)string resolver:(RCTPromiseResolveBlock) resolve
    rejecter:(RCTPromiseRejectBlock) __unused reject)
{
    CNContactStore *contactStore = [[CNContactStore alloc] init];
    if (!contactStore)
        return;
    [self getContactsFromAddressBook:contactStore byPhoneNumber:string resolve:resolve];
}

-(void) getContactsFromAddressBook:(CNContactStore *)store
                    byPhoneNumber:(NSString *)phoneNumber
                          resolve:(RCTPromiseResolveBlock) resolve
{
    NSMutableArray *contacts = [[NSMutableArray alloc] init];
    NSError *contactError = nil;
    NSArray *keys = @[
                      CNContactEmailAddressesKey,
                      CNContactPhoneNumbersKey,
                      CNContactFamilyNameKey,
                      CNContactGivenNameKey,
                      CNContactMiddleNameKey,
                      CNContactPostalAddressesKey,
                      CNContactOrganizationNameKey,
                      CNContactJobTitleKey,
                      CNContactImageDataAvailableKey,
                      CNContactThumbnailImageDataKey,
                      CNContactUrlAddressesKey,
                      CNContactBirthdayKey,
                      CNContactInstantMessageAddressesKey
                      ];
    CNPhoneNumber *cnPhoneNumber = [[CNPhoneNumber alloc] initWithStringValue:phoneNumber];
    NSArray *arrayOfContacts = [store unifiedContactsMatchingPredicate:[CNContact predicateForContactsMatchingPhoneNumber:cnPhoneNumber]
                                                           keysToFetch:keys
                                                                 error:&contactError];
    [arrayOfContacts enumerateObjectsUsingBlock:^(CNContact * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        NSDictionary *contactDictionary = [self contactToDictionary:obj withThumbnails:true];
        [contacts addObject:contactDictionary];
    }];
    resolve(contacts);
}

RCT_EXPORT_METHOD(getContactsByEmailAddress:(NSString *)string
    resolver:(RCTPromiseResolveBlock) resolve
    rejecter:(RCTPromiseRejectBlock) __unused reject)
{
    CNContactStore *contactStore = [[CNContactStore alloc] init];
    if (!contactStore)
        return;
    [self getContactsFromAddressBook:contactStore byEmailAddress:string resolve:resolve];
}

-(void) getContactsFromAddressBook:(CNContactStore *)store
                    byEmailAddress:(NSString *)emailAddress
                          resolve:(RCTPromiseResolveBlock) resolve
{
    NSMutableArray *contacts = [[NSMutableArray alloc] init];
    NSError *contactError = nil;
    NSArray *keys = @[
                      CNContactEmailAddressesKey,
                      CNContactPhoneNumbersKey,
                      CNContactFamilyNameKey,
                      CNContactGivenNameKey,
                      CNContactMiddleNameKey,
                      CNContactPostalAddressesKey,
                      CNContactOrganizationNameKey,
                      CNContactJobTitleKey,
                      CNContactImageDataAvailableKey,
                      CNContactThumbnailImageDataKey,
                      CNContactUrlAddressesKey,
                      CNContactBirthdayKey,
                      CNContactInstantMessageAddressesKey
                      ];
    NSArray *arrayOfContacts = [store unifiedContactsMatchingPredicate:[CNContact predicateForContactsMatchingEmailAddress:emailAddress]
                                                           keysToFetch:keys
                                                                 error:&contactError];
    [arrayOfContacts enumerateObjectsUsingBlock:^(CNContact * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        NSDictionary *contactDictionary = [self contactToDictionary:obj withThumbnails:true];
        [contacts addObject:contactDictionary];
    }];
    resolve(contacts);
}

-(void) getAllContacts:(RCTPromiseResolveBlock) resolve
        reject:(RCTPromiseRejectBlock) reject
        withThumbnails:(BOOL) withThumbnails
{
    CNContactStore* contactStore = [self contactsStore:reject];
    if(!contactStore)
        return;

    resolve([self retrieveContactsFromAddressBook:contactStore withThumbnails:withThumbnails]);
}

-(void) getAllContactsCount:(RCTPromiseResolveBlock) resolve
    reject:(RCTPromiseRejectBlock) reject
{
    CNContactStore* contactStore = [self contactsStore:reject];
    if(!contactStore)
        return;

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
                                       CNContactImageDataAvailableKey,
                                       CNContactUrlAddressesKey,
                                       CNContactBirthdayKey,
                                       CNContactInstantMessageAddressesKey
                                       ]];

    CNContactFetchRequest * request = [[CNContactFetchRequest alloc]initWithKeysToFetch:keysToFetch];
    BOOL success = [contactStore enumerateContactsWithFetchRequest:request error:&contactError usingBlock:^(CNContact * __nonnull contact, BOOL * __nonnull stop){
        NSDictionary *contactDict = [self contactToDictionary: contact withThumbnails:false];
        [contacts addObject:contactDict];
    }];

    NSUInteger contactsCount = [contacts count];

    NSNumber *count = [NSNumber numberWithInt:contactsCount];

    resolve(count);
}

RCT_EXPORT_METHOD(getAll:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self getAllContacts:resolve reject:reject withThumbnails:true];
}

RCT_EXPORT_METHOD(getAllWithoutPhotos:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self getAllContacts:resolve reject:reject withThumbnails:false];
}

RCT_EXPORT_METHOD(getCount:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    [self getAllContactsCount:resolve reject:reject];
}

-(NSMutableArray*) retrieveContactsFromAddressBook:(CNContactStore*)contactStore
                         withThumbnails:(BOOL) withThumbnails
{
    NSMutableArray *contacts = [[NSMutableArray alloc] init];

    NSMutableArray *keysToFetch = [NSMutableArray arrayWithArray: @[
        CNContactEmailAddressesKey,
        CNContactPhoneNumbersKey,
        CNContactFamilyNameKey,
        CNContactGivenNameKey,
        CNContactMiddleNameKey,
        CNContactPostalAddressesKey,
        CNContactOrganizationNameKey,
        CNContactJobTitleKey,
        CNContactImageDataAvailableKey,
        CNContactUrlAddressesKey,
        CNContactBirthdayKey,
        CNContactInstantMessageAddressesKey
    ]];
    if(notesUsageEnabled) {
        [keysToFetch addObject: CNContactNoteKey];
    }

    if(withThumbnails) {
        [keysToFetch addObject:CNContactThumbnailImageDataKey];
    }

    CNContactFetchRequest * request = [[CNContactFetchRequest alloc] initWithKeysToFetch:keysToFetch];
    NSError* contactError;
    BOOL success = [contactStore enumerateContactsWithFetchRequest:request error:&contactError usingBlock:^(CNContact * __nonnull contact, BOOL * __nonnull stop){
        NSDictionary *contactDict = [self contactToDictionary: contact withThumbnails:withThumbnails];
        [contacts addObject:contactDict];
    }];

    return contacts;
}

-(NSDictionary*) contactToDictionary:(CNContact *) person
                      withThumbnails:(BOOL)withThumbnails
{
    NSMutableDictionary* output = [NSMutableDictionary dictionary];

    NSString *recordID = person.identifier;
    NSString *givenName = person.givenName;
    NSString *familyName = person.familyName;
    NSString *middleName = person.middleName;
    NSString *company = person.organizationName;
    NSString *jobTitle = person.jobTitle;
    NSDateComponents *birthday = person.birthday;

    [output setObject:recordID forKey: @"recordID"];

    if (givenName) {
        [output setObject: (givenName) ? givenName : @"" forKey:@"givenName"];
    }

    if (familyName) {
        [output setObject: (familyName) ? familyName : @"" forKey:@"familyName"];
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

    if(notesUsageEnabled){
        NSString *note = person.note;
        if(note){
            [output setObject: (note) ? note : @"" forKey:@"note"];
        }
    }

    if (birthday) {
        if (birthday.month != NSDateComponentUndefined && birthday.day != NSDateComponentUndefined) {
            //months are indexed to 0 in JavaScript (0 = January) so we subtract 1 from NSDateComponents.month
            if (birthday.year != NSDateComponentUndefined) {
                [output setObject:@{@"year": @(birthday.year), @"month": @(birthday.month), @"day": @(birthday.day)} forKey:@"birthday"];
            } else {
                [output setObject:@{@"month": @(birthday.month), @"day":@(birthday.day)} forKey:@"birthday"];
            }
        }
    }

    //handle phone numbers
    NSMutableArray *phoneNumbers = [[NSMutableArray alloc] init];

    for (CNLabeledValue<CNPhoneNumber*>* labeledValue in person.phoneNumbers) {
        NSMutableDictionary* phone = [NSMutableDictionary dictionary];
        NSString * label = [CNLabeledValue localizedStringForLabel:[labeledValue label]];
        NSString* value = [[labeledValue value] stringValue];

        if(value) {
            if(!label) {
                label = [CNLabeledValue localizedStringForLabel:@"other"];
            }
            [phone setObject: value forKey:@"number"];
            [phone setObject: label forKey:@"label"];
            [phoneNumbers addObject:phone];
        }
    }

    [output setObject: phoneNumbers forKey:@"phoneNumbers"];
    //end phone numbers

    //handle urls
    NSMutableArray *urlAddresses = [[NSMutableArray alloc] init];

    for (CNLabeledValue<NSString*>* labeledValue in person.urlAddresses) {
        NSMutableDictionary* url = [NSMutableDictionary dictionary];
        NSString* label = [CNLabeledValue localizedStringForLabel:[labeledValue label]];
        NSString* value = [labeledValue value];

        if(value) {
            if(!label) {
                label = [CNLabeledValue localizedStringForLabel:@"home"];
            }
            [url setObject: value forKey:@"url"];
            [url setObject: label forKey:@"label"];
            [urlAddresses addObject:url];
        } else {
            NSLog(@"ignoring blank url");
        }
    }

    [output setObject: urlAddresses forKey:@"urlAddresses"];

    //end urls

    //handle emails
    NSMutableArray *emailAddreses = [[NSMutableArray alloc] init];

    for (CNLabeledValue<NSString*>* labeledValue in person.emailAddresses) {
        NSMutableDictionary* email = [NSMutableDictionary dictionary];
        NSString* label = [CNLabeledValue localizedStringForLabel:[labeledValue label]];
        NSString* value = [labeledValue value];

        if(value) {
            if(!label) {
                label = [CNLabeledValue localizedStringForLabel:@"other"];
            }
            [email setObject: value forKey:@"email"];
            [email setObject: label forKey:@"label"];
            [emailAddreses addObject:email];
        } else {
            RCTLog(@"ignoring blank email");
        }
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
        NSString* state = postalAddress.state;
        if(state){
            [address setObject:state forKey:@"state"];
        }
        NSString* region = postalAddress.state;
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

        NSString* label = [CNLabeledValue localizedStringForLabel:labeledValue.label];
        if(label) {
            [address setObject:label forKey:@"label"];

            [postalAddresses addObject:address];
        }
    }

    [output setObject:postalAddresses forKey:@"postalAddresses"];
    //end postal addresses
    
    //handle instant message addresses
    NSMutableArray *imAddresses = [[NSMutableArray alloc] init];

    for (CNLabeledValue<CNInstantMessageAddress*>* labeledValue in person.instantMessageAddresses) {
        NSMutableDictionary* imAddress = [NSMutableDictionary dictionary];
        CNInstantMessageAddress* imAddressData = labeledValue.value;
        NSString* service = [CNLabeledValue localizedStringForLabel: imAddressData.service];
        NSString* username = imAddressData.username;

        if(username) {
            if(!service) {
                service = [CNLabeledValue localizedStringForLabel:@"other"];
            }
            [imAddress setObject: service forKey:@"service"];
            [imAddress setObject: username forKey:@"username"];
            [imAddresses addObject: imAddress];
        } else {
            RCTLog(@"ignoring blank instant message address");
        }
    }

    [output setObject: imAddresses forKey:@"imAddresses"];
    //end instant message addresses

    [output setValue:[NSNumber numberWithBool:person.imageDataAvailable] forKey:@"hasThumbnail"];
    if (withThumbnails) {
        [output setObject:[self getFilePathForThumbnailImage:person recordID:recordID] forKey:@"thumbnailPath"];
    }

    return output;
}

- (NSString *)thumbnailFilePath:(NSString *)recordID
{
    NSString *filename = [recordID stringByReplacingOccurrencesOfString:@":ABPerson" withString:@""];
    NSString* filepath = [NSString stringWithFormat:@"%@/rncontacts_%@.png", [self getPathForDirectory:NSCachesDirectory], filename];
    return filepath;
}

-(NSString *) getFilePathForThumbnailImage:(CNContact*) contact recordID:(NSString*) recordID
{
    if (contact.imageDataAvailable){
        NSString *filepath = [self thumbnailFilePath:recordID];
        NSData *contactImageData = contact.thumbnailImageData;

        if ([[NSFileManager defaultManager] fileExistsAtPath:filepath]) {
            NSData *existingImageData = [NSData dataWithContentsOfFile: filepath];

            if([contactImageData isEqual: existingImageData]) {
                return filepath;
            }
        }

        BOOL success = [[NSFileManager defaultManager] createFileAtPath:filepath contents:contactImageData attributes:nil];

        if (!success) {
            RCTLog(@"Unable to copy image");
            return @"";
        }

        return filepath;
    }

    return @"";
}

- (NSString *)getPathForDirectory:(int)directory
{
    NSArray *paths = NSSearchPathForDirectoriesInDomains((NSSearchPathDirectory)directory, NSUserDomainMask, YES);
    return [paths firstObject];
}

RCT_EXPORT_METHOD(getPhotoForId:(nonnull NSString *)recordID resolver:(RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject)
{
    CNContactStore *contactStore = [self contactsStore:reject];
    if (!contactStore)
        return;

    CNEntityType entityType = CNEntityTypeContacts;
    CNAuthorizationStatus authStatus = [CNContactStore authorizationStatusForEntityType:entityType];

    if (authStatus == CNAuthorizationStatusNotDetermined) {
        [contactStore requestAccessForEntityType:entityType completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if (granted) {
                resolve([self getFilePathForThumbnailImage:recordID addressBook:contactStore]);
            }
        }];
    } else if (authStatus == CNAuthorizationStatusAuthorized) {
        resolve([self getFilePathForThumbnailImage:recordID addressBook:contactStore]);
    } else if (@available(iOS 18, *)) {
        if (authStatus == CNAuthorizationStatusRestricted) {
            resolve([self getFilePathForThumbnailImage:recordID addressBook:contactStore]);
        }
    } else {
        reject(@"CONTACT_ACCESS_DENIED", @"Contact access is not authorized.", nil);
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
    NSArray * keysToFetch =@[CNContactThumbnailImageDataKey, CNContactImageDataAvailableKey];
    CNContact* contact = [addressBook unifiedContactWithIdentifier:recordID keysToFetch:keysToFetch error:&contactError];

    return [self getFilePathForThumbnailImage:contact recordID:recordID];
}


RCT_EXPORT_METHOD(getContactById:(nonnull NSString *)recordID resolver:(RCTPromiseResolveBlock)resolve
    rejecter:(RCTPromiseRejectBlock)reject)
{
    CNContactStore *contactStore = [self contactsStore:reject];
    if (!contactStore)
        return;

    CNEntityType entityType = CNEntityTypeContacts;
    
    CNAuthorizationStatus authStatus = [CNContactStore authorizationStatusForEntityType:entityType];

    if (authStatus == CNAuthorizationStatusNotDetermined) {
        [contactStore requestAccessForEntityType:entityType completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if (granted) {
                resolve([self getContact:recordID addressBook:contactStore withThumbnails:NO]);
            }
        }];
    } else if (authStatus == CNAuthorizationStatusAuthorized) {
        resolve([self getContact:recordID addressBook:contactStore withThumbnails:NO]);
    } else if (@available(iOS 18, *)) {
        if (authStatus == CNAuthorizationStatusRestricted) {
            resolve([self getContact:recordID addressBook:contactStore withThumbnails:NO]);
        }
    } else {
        reject(@"CONTACT_ACCESS_DENIED", @"Contact access is not authorized.", nil);
    }
}


-(NSDictionary *) getContact:(NSString *)recordID
                               addressBook:(CNContactStore*)addressBook
                               withThumbnails:(BOOL) withThumbnails
{
    NSError* contactError;
    NSMutableArray *keysToFetch = [NSMutableArray arrayWithArray: @[
        CNContactEmailAddressesKey,
        CNContactPhoneNumbersKey,
        CNContactFamilyNameKey,
        CNContactGivenNameKey,
        CNContactMiddleNameKey,
        CNContactPostalAddressesKey,
        CNContactOrganizationNameKey,
        CNContactJobTitleKey,
        CNContactImageDataAvailableKey,
        CNContactUrlAddressesKey,
        CNContactBirthdayKey,
        CNContactInstantMessageAddressesKey
    ]];
    if(notesUsageEnabled) {
        [keysToFetch addObject: CNContactNoteKey];
    }

    CNContact* contact = [addressBook unifiedContactWithIdentifier:recordID keysToFetch:keysToFetch error:&contactError];

    if(!contact)
        return nil;

    return [self contactToDictionary: contact withThumbnails:withThumbnails];
}


RCT_EXPORT_METHOD(addContact:(NSDictionary *)contactData resolver:(RCTPromiseResolveBlock) resolve
    rejecter:(RCTPromiseRejectBlock) reject)
{
    CNContactStore* contactStore = [self contactsStore:reject];
    if(!contactStore)
        return;

    CNMutableContact * contact = [[CNMutableContact alloc] init];

    [self updateRecord:contact withData:contactData];

    @try {
        CNSaveRequest *request = [[CNSaveRequest alloc] init];
        [request addContact:contact toContainerWithIdentifier:nil];

        [contactStore executeSaveRequest:request error:nil];

        NSDictionary *contactDict = [self contactToDictionary:contact withThumbnails:false];

        resolve(contactDict);
    }
    @catch (NSException *exception) {
        reject(@"Error", [exception reason], nil);
    }
}

RCT_EXPORT_METHOD(openContactForm:(NSDictionary *)contactData
    resolver:(RCTPromiseResolveBlock) resolve
    rejecter:(RCTPromiseRejectBlock) __unused reject)
{
    CNMutableContact * contact = [[CNMutableContact alloc] init];

    [self updateRecord:contact withData:contactData];

    CNContactViewController *controller = [CNContactViewController viewControllerForNewContact:contact];


    controller.delegate = self;

    dispatch_async(dispatch_get_main_queue(), ^{
        UINavigationController* navigation = [[UINavigationController alloc] initWithRootViewController:controller];
        UIViewController *presentingViewController = RCTPresentedViewController();
        [presentingViewController presentViewController:navigation animated:YES completion:nil];

        self->updateContactPromise = resolve;
    });
}

RCT_EXPORT_METHOD(openExistingContact:(NSDictionary *)contactData resolver:(RCTPromiseResolveBlock) resolve
    rejecter:(RCTPromiseRejectBlock) reject)
{
    if(!contactStore) {
        contactStore = [[CNContactStore alloc] init];
    }

    NSString* recordID = [contactData valueForKey:@"recordID"];
    NSString* backTitle = [contactData valueForKey:@"backTitle"];

    NSArray *keys = @[CNContactIdentifierKey,
                      CNContactEmailAddressesKey,
                      CNContactBirthdayKey,
                      CNContactImageDataKey,
                      CNContactPhoneNumbersKey,
                      [CNContactFormatter descriptorForRequiredKeysForStyle:CNContactFormatterStyleFullName],
                      [CNContactViewController descriptorForRequiredKeys]];

    @try {

        CNContact *contact = [contactStore unifiedContactWithIdentifier:recordID keysToFetch:keys error:nil];

        CNContactViewController *contactViewController = [CNContactViewController viewControllerForContact:contact];

        // Add a cancel button which will close the view
        contactViewController.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:backTitle == nil ? @"Cancel" : backTitle style:UIBarButtonItemStyleDone target:self action:@selector(cancelContactForm)];
        contactViewController.delegate = self;


        dispatch_async(dispatch_get_main_queue(), ^{
            UINavigationController* navigation = [[UINavigationController alloc] initWithRootViewController:contactViewController];

            UIViewController *currentViewController = [UIApplication sharedApplication].keyWindow.rootViewController;

            while (currentViewController.presentedViewController)
            {
                currentViewController = currentViewController.presentedViewController;
            }

            UIActivityIndicatorViewStyle activityIndicatorStyle;
            UIActivityIndicatorView *activityIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleMedium];
            UIColor *activityIndicatorBackgroundColor;
            if (@available(iOS 13, *)) {
                activityIndicatorStyle = UIActivityIndicatorViewStyleMedium;
                activityIndicatorBackgroundColor = [UIColor secondarySystemGroupedBackgroundColor];
            } else {
                activityIndicatorStyle = UIActivityIndicatorViewStyleMedium;
                activityIndicatorBackgroundColor = [UIColor whiteColor];;
            }

            // Cover the contact view with an activity indicator so we can put it in edit mode without user seeing the transition
            UIActivityIndicatorView *activityIndicatorView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:activityIndicatorStyle];
            activityIndicatorView.frame = UIApplication.sharedApplication.keyWindow.frame;
            [activityIndicatorView startAnimating];
            activityIndicatorView.backgroundColor = activityIndicatorBackgroundColor;
            [navigation.view addSubview:activityIndicatorView];

            [currentViewController presentViewController:navigation animated:YES completion:nil];


            // TODO should this 'fake click' method be used? For a brief instance
            // Fake click edit button to enter edit mode
            //                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
            //                    SEL selector = contactViewController.navigationItem.rightBarButtonItem.action;
            //                    NSLog(@"!!!!!!!!!!!!!!!!!! FAKE CLICK!!!  %@", NSStringFromSelector(selector));
            //                    id  target = contactViewController.navigationItem.rightBarButtonItem.target;
            //                    [target performSelector:selector];
            //                });


            // We need to wait for a short while otherwise contactViewController will not respond to the selector (it has not initialized)
            [contactViewController performSelector:@selector(toggleEditing:) withObject:nil afterDelay:0.1];

            // remove the activity indicator after a delay so the underlying transition will have time to complete
            dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                [activityIndicatorView removeFromSuperview];
            });

            self->updateContactPromise = resolve;
        });

    }
    @catch (NSException *exception) {
        reject(@"Error", [exception reason], nil);
    }
}

RCT_EXPORT_METHOD(viewExistingContact:(NSDictionary *)contactData resolver:(RCTPromiseResolveBlock) resolve
    rejecter:(RCTPromiseRejectBlock) reject)
{
    if(!contactStore) {
        contactStore = [[CNContactStore alloc] init];
    }

    NSString* recordID = [contactData valueForKey:@"recordID"];
    NSString* backTitle = [contactData valueForKey:@"backTitle"];

    NSArray *keys = @[CNContactIdentifierKey,
                      CNContactEmailAddressesKey,
                      CNContactBirthdayKey,
                      CNContactImageDataKey,
                      CNContactPhoneNumbersKey,
                      [CNContactFormatter descriptorForRequiredKeysForStyle:CNContactFormatterStyleFullName],
                      [CNContactViewController descriptorForRequiredKeys]];

    @try {

        CNContact *contact = [contactStore unifiedContactWithIdentifier:recordID keysToFetch:keys error:nil];

        CNContactViewController *contactViewController = [CNContactViewController viewControllerForContact:contact];

        // Add a cancel button which will close the view
        contactViewController.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:backTitle == nil ? @"Cancel" : backTitle style:UIBarButtonItemStylePlain target:self action:@selector(cancelContactForm)];
        contactViewController.delegate = self;


        dispatch_async(dispatch_get_main_queue(), ^{
            UINavigationController* navigation = [[UINavigationController alloc] initWithRootViewController:contactViewController];
            UIViewController *presentingViewController = RCTPresentedViewController();
            [presentingViewController presentViewController:navigation animated:YES completion:nil];

            updateContactPromise = resolve;
        });

    }
    @catch (NSException *exception) {
        reject(@"Error", [exception reason], nil);
    }
}

RCT_EXPORT_METHOD(editExistingContact:(NSDictionary *)contactData resolver:(RCTPromiseResolveBlock) resolve
    rejecter:(RCTPromiseRejectBlock) reject)
{
    CNContactStore* contactStore = [self contactsStore:reject];
    if(!contactStore)
        return;

    NSError* contactError;
    selectedContact = nil;
    NSString* recordID = [contactData valueForKey:@"recordID"];
    NSArray * keysToFetch =@[
                             CNContactEmailAddressesKey,
                             CNContactPhoneNumbersKey,
                             CNContactFamilyNameKey,
                             CNContactGivenNameKey,
                             CNContactMiddleNameKey,
                             CNContactPostalAddressesKey,
                             CNContactOrganizationNameKey,
                             CNContactJobTitleKey,
                             CNContactImageDataAvailableKey,
                             CNContactThumbnailImageDataKey,
                             CNContactImageDataKey,
                             CNContactUrlAddressesKey,
                             CNContactBirthdayKey,
                             CNContactIdentifierKey,
                             [CNContactFormatter descriptorForRequiredKeysForStyle:CNContactFormatterStyleFullName],
                             [CNContactViewController descriptorForRequiredKeys]];

    @try {
        CNMutableContact* record = [[contactStore unifiedContactWithIdentifier:recordID keysToFetch:keysToFetch error:&contactError] mutableCopy];

        NSMutableArray *phoneNumbers = [[NSMutableArray alloc]init];
        phoneNumbers = [NSMutableArray arrayWithArray:record.phoneNumbers];

        for (id phoneData in [contactData valueForKey:@"phoneNumbers"]) {
            NSString *number = [phoneData valueForKey:@"number"];

            CNLabeledValue *contactPhoneNumber = [CNLabeledValue labeledValueWithLabel:CNLabelOther value:[CNPhoneNumber phoneNumberWithStringValue:number]];
            //record.phoneNumbers = @[contactPhoneNumber];
            [phoneNumbers addObject:contactPhoneNumber];
        }

        NSArray *phoneNumbersNew = [[NSArray alloc]init];
        phoneNumbersNew = [NSArray arrayWithArray:phoneNumbers];


        record.phoneNumbers = phoneNumbersNew;

        CNSaveRequest *request = [[CNSaveRequest alloc] init];
        [request updateContact:record];
        
        selectedContact = record;

        [contactStore executeSaveRequest:request error:nil];

        CNContactViewController *controller = [CNContactViewController viewControllerForContact:record];
        //controller.title = @"Saved!";
        UIAlertController *alert=   [UIAlertController
            alertControllerWithTitle:@"Saved!"
            message:@"Number added to contact"
            preferredStyle:UIAlertControllerStyleAlert];
        //[controller presentViewController:alert animated:YES completion:nil];
        
        // Add a cancel button which will close the view
        controller.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Done"  style:UIBarButtonItemStylePlain target:self action:@selector(doneContactForm)];
        
        controller.delegate = self;
        controller.allowsEditing = true;
        controller.allowsActions = true;

        dispatch_async(dispatch_get_main_queue(), ^{
            UINavigationController* navigation = [[UINavigationController alloc] initWithRootViewController:controller];
            UIViewController *presentingViewController = RCTPresentedViewController();
            [presentingViewController presentViewController:navigation animated:YES completion:nil];
            [controller presentViewController:alert animated:YES completion:nil];

            self->updateContactPromise = resolve;
        });
        
        dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(3.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{

                [alert dismissViewControllerAnimated:YES completion:^{

                    //Dismissed
                }];

        });
    }
    @catch (NSException *exception) {
        reject(@"Error", [exception reason], nil);
    }
}

- (void)doneContactForm
{
    if (updateContactPromise != nil) {
        UIViewController *rootViewController = (UIViewController*)[[[[UIApplication sharedApplication] delegate] window] rootViewController];
        [rootViewController dismissViewControllerAnimated:YES completion:nil];

        NSDictionary *contactDict = [self contactToDictionary:selectedContact withThumbnails:true];
        updateContactPromise(contactDict);
        updateContactPromise = nil;
    }
}

- (void)cancelContactForm
{
    if (updateContactPromise != nil) {
        UIViewController *rootViewController = (UIViewController*)[[[[UIApplication sharedApplication] delegate] window] rootViewController];
        while (rootViewController.presentedViewController)
            {
                rootViewController = rootViewController.presentedViewController;
            }
        [rootViewController dismissViewControllerAnimated:YES completion:nil];

        updateContactPromise(nil);
        updateContactPromise = nil;
    }
}

//dismiss open contact page after done or cancel is clicked
- (void)contactViewController:(CNContactViewController *)viewController didCompleteWithContact:(CNContact *)contact {
    [viewController dismissViewControllerAnimated:YES completion:nil];

    if(updateContactPromise) {

        if (contact) {
            NSDictionary *contactDict = [self contactToDictionary:contact withThumbnails:true];
            updateContactPromise(contactDict);
        } else {
            updateContactPromise(nil);
        }

        updateContactPromise = nil;
    }
}

RCT_EXPORT_METHOD(updateContact:(NSDictionary *)contactData resolver:(RCTPromiseResolveBlock) resolve
    rejecter:(RCTPromiseRejectBlock) reject)
{
    CNContactStore* contactStore = [self contactsStore:reject];
    if(!contactStore)
        return;

    NSError* contactError;
    NSString* recordID = [contactData valueForKey:@"recordID"];
    NSMutableArray * keysToFetch = [NSMutableArray arrayWithArray: @[
        CNContactEmailAddressesKey,
        CNContactPhoneNumbersKey,
        CNContactFamilyNameKey,
        CNContactGivenNameKey,
        CNContactMiddleNameKey,
        CNContactPostalAddressesKey,
        CNContactOrganizationNameKey,
        CNContactJobTitleKey,
        CNContactImageDataAvailableKey,
        CNContactThumbnailImageDataKey,
        CNContactImageDataKey,
        CNContactUrlAddressesKey,
        CNContactBirthdayKey,
        CNContactInstantMessageAddressesKey
    ]];
    if(notesUsageEnabled) {
        [keysToFetch addObject: CNContactNoteKey];
    }

    @try {
        CNMutableContact* record = [[contactStore unifiedContactWithIdentifier:recordID keysToFetch:keysToFetch error:&contactError] mutableCopy];
        [self updateRecord:record withData:contactData];
        CNSaveRequest *request = [[CNSaveRequest alloc] init];
        [request updateContact:record];

        [contactStore executeSaveRequest:request error:nil];

        NSDictionary *contactDict = [self contactToDictionary:record withThumbnails:false];

        resolve(contactDict);
    }
    @catch (NSException *exception) {
        reject(@"Error", [exception reason], nil);
    }
}

-(void) updateRecord:(CNMutableContact *)contact withData:(NSDictionary *)contactData
{
    NSString *givenName = [contactData valueForKey:@"givenName"];
    NSString *familyName = [contactData valueForKey:@"familyName"];
    NSString *middleName = [contactData valueForKey:@"middleName"];
    NSString *company = [contactData valueForKey:@"company"];
    NSString *jobTitle = [contactData valueForKey:@"jobTitle"];

    NSDictionary *birthday = [contactData valueForKey:@"birthday"];

    contact.givenName = givenName;
    contact.familyName = familyName;
    contact.middleName = middleName;
    contact.organizationName = company;
    contact.jobTitle = jobTitle;
    
    if(notesUsageEnabled){
        NSString *note = [contactData valueForKey:@"note"];
        contact.note = note;
    }

    if (birthday) {
        NSDateComponents *components;
        if (contact.birthday != nil) {
            components = contact.birthday;
        } else {
            components = [[NSDateComponents alloc] init];
        }
        if (birthday[@"month"] && birthday[@"day"]) {
            if (birthday[@"year"]) {
                components.year = [birthday[@"year"] intValue];
            }
            components.month = [birthday[@"month"] intValue];
            components.day = [birthday[@"day"] intValue];
        }

        contact.birthday = components;
    }

    NSMutableArray *phoneNumbers = [[NSMutableArray alloc]init];

    for (id phoneData in [contactData valueForKey:@"phoneNumbers"]) {
        NSString *label = [phoneData valueForKey:@"label"];
        NSString *number = [phoneData valueForKey:@"number"];

        CNLabeledValue *phone;
        if ([label isEqual: @"main"]){
            phone = [[CNLabeledValue alloc] initWithLabel:CNLabelPhoneNumberMain value:[[CNPhoneNumber alloc] initWithStringValue:number]];
        }
        else if ([label isEqual: @"mobile"]){
            phone = [[CNLabeledValue alloc] initWithLabel:CNLabelPhoneNumberMobile value:[[CNPhoneNumber alloc] initWithStringValue:number]];
        }
        else if ([label isEqual: @"iPhone"]){
            phone = [[CNLabeledValue alloc] initWithLabel:CNLabelPhoneNumberiPhone value:[[CNPhoneNumber alloc] initWithStringValue:number]];
        }
        else{
            phone = [[CNLabeledValue alloc] initWithLabel:label value:[[CNPhoneNumber alloc] initWithStringValue:number]];
        }

        [phoneNumbers addObject:phone];
    }
    contact.phoneNumbers = phoneNumbers;


    NSMutableArray *urls = [[NSMutableArray alloc]init];

    for (id urlData in [contactData valueForKey:@"urlAddresses"]) {
        NSString *label = [urlData valueForKey:@"label"];
        NSString *url = [urlData valueForKey:@"url"];

        if(label && url) {
            [urls addObject:[[CNLabeledValue alloc] initWithLabel:label value:url]];
        }
    }

    contact.urlAddresses = urls;


    NSMutableArray *emails = [[NSMutableArray alloc]init];

    for (id emailData in [contactData valueForKey:@"emailAddresses"]) {
        NSString *label = [emailData valueForKey:@"label"];
        NSString *email = [emailData valueForKey:@"email"];

        if(label && email) {
            [emails addObject:[[CNLabeledValue alloc] initWithLabel:label value:email]];
        }
    }

    contact.emailAddresses = emails;

    NSMutableArray *postalAddresses = [[NSMutableArray alloc]init];

    for (id addressData in [contactData valueForKey:@"postalAddresses"]) {
        NSString *label = [addressData valueForKey:@"label"];
        NSString *street = [addressData valueForKey:@"street"];
        NSString *postalCode = [addressData valueForKey:@"postCode"];
        NSString *city = [addressData valueForKey:@"city"];
        NSString *country = [addressData valueForKey:@"country"];
        NSString *state = [addressData valueForKey:@"state"];

        if(label && street) {
            CNMutablePostalAddress *postalAddr = [[CNMutablePostalAddress alloc] init];
            postalAddr.street = street;
            postalAddr.postalCode = postalCode;
            postalAddr.city = city;
            postalAddr.country = country;
            postalAddr.state = state;
            [postalAddresses addObject:[[CNLabeledValue alloc] initWithLabel:label value: postalAddr]];
        }
    }

    contact.postalAddresses = postalAddresses;
    
    NSMutableArray<CNLabeledValue<CNInstantMessageAddress*>*> *imAddresses = [[NSMutableArray alloc] init];
    
    for (id imData in [contactData valueForKey:@"imAddresses"]) {
        NSString *service = [imData valueForKey:@"service"];
        NSString *username = [imData valueForKey:@"username"];

        if(service && username) {
            CNLabeledValue *imAddress = [[CNLabeledValue alloc] initWithLabel: @"instantMessageAddress" value: [[CNInstantMessageAddress alloc] initWithUsername: username service: service]];
            [imAddresses addObject: imAddress];
        }
    }

    contact.instantMessageAddresses = imAddresses;

    NSString *thumbnailPath = [contactData valueForKey:@"thumbnailPath"];

    if(thumbnailPath && [thumbnailPath rangeOfString:@"rncontacts_"].location == NSNotFound) {
        contact.imageData = [RCTContacts imageData:thumbnailPath];
    }
}

+ (NSData*) imageData:(NSString*)sourceUri
{
    NSURL *url = [NSURL URLWithString:sourceUri];

    if([sourceUri hasPrefix:@"assets-library"]){
        return [RCTContacts loadImageAsset:[NSURL URLWithString:sourceUri]];
    } else if (url && url.scheme && url.host) {
        return [NSData dataWithContentsOfURL:url];
    } else if ([sourceUri isAbsolutePath]) {
        return [NSData dataWithContentsOfFile:sourceUri];
    } else {
        sourceUri = [[NSBundle mainBundle] pathForResource:[sourceUri stringByDeletingPathExtension] ofType:[sourceUri pathExtension]];
        return [NSData dataWithContentsOfFile:sourceUri];
    }
}

enum { WDASSETURL_PENDINGREADS = 1, WDASSETURL_ALLFINISHED = 0};

+ (NSData *)loadImageAsset:(NSURL *)assetURL {
    __block NSData *imageData = nil;
    
    dispatch_semaphore_t semaphore = dispatch_semaphore_create(0);
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        PHFetchResult<PHAsset *> *result = [PHAsset fetchAssetsWithALAssetURLs:@[assetURL] options:nil];
        if (result.count > 0) {
            PHAsset *asset = result.firstObject;
            PHImageManager *imageManager = [PHImageManager defaultManager];
            PHImageRequestOptions *options = [[PHImageRequestOptions alloc] init];
            options.synchronous = YES; // Load image synchronously
            
            [imageManager requestImageDataForAsset:asset
                                          options:options
                                    resultHandler:^(NSData * _Nullable data, NSString * _Nullable dataUTI, UIImageOrientation orientation, NSDictionary * _Nullable info) {
                imageData = data;
                dispatch_semaphore_signal(semaphore);
            }];
        } else {
            dispatch_semaphore_signal(semaphore);
        }
    });
    
    dispatch_semaphore_wait(semaphore, DISPATCH_TIME_FOREVER);
    
    return imageData;
}

RCT_EXPORT_METHOD(deleteContact:(NSDictionary *)contactData resolver:(RCTPromiseResolveBlock) resolve
    rejecter:(RCTPromiseRejectBlock) reject)
{
    if(!contactStore) {
        contactStore = [[CNContactStore alloc] init];
    }

    NSString* recordID = [contactData valueForKey:@"recordID"];

    NSArray *keys = @[CNContactIdentifierKey];


    @try {

        CNMutableContact *contact = [[contactStore unifiedContactWithIdentifier:recordID keysToFetch:keys error:nil] mutableCopy];
        NSError *error;
        CNSaveRequest *saveRequest = [[CNSaveRequest alloc] init];
        [saveRequest deleteContact:contact];
        [contactStore executeSaveRequest:saveRequest error:&error];

        resolve(recordID);
    }
    @catch (NSException *exception) {
        reject(@"Error", [exception reason], nil);
    }
}

RCT_EXPORT_METHOD(writePhotoToPath:(nonnull NSString *)path resolver:(RCTPromiseResolveBlock) resolve rejecter:(RCTPromiseRejectBlock) reject)
{
    @try {
        //Nothing is implemented here
    } @catch (NSException *exception) {
        reject(@"Error", @"not implemented", nil);
    }
}

RCT_EXPORT_METHOD(getGroups:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject)
{
    if (!contactStore) {
        contactStore = [[CNContactStore alloc] init];
    }

    NSError *error = nil;
    NSArray<CNGroup *> *groups = [contactStore groupsMatchingPredicate:nil error:&error];

    if (error) {
        reject(@"get_groups_error", @"Failed to fetch groups", error);
        return;
    }

    NSMutableArray *groupArray = [NSMutableArray array];
    for (CNGroup *group in groups) {
        NSDictionary *groupDict = @{
            @"identifier": group.identifier ?: @"",
            @"name": group.name ?: @""
        };
        [groupArray addObject:groupDict];
    }

    resolve(groupArray);
}
RCT_EXPORT_METHOD(getGroup:(NSString *)identifier resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject)
{
    CNContactStore *contactStore = [self contactsStore:reject];
    if (!contactStore) {
        // contactsStore method handles rejection
        return;
    }
    
    NSError *error = nil;
    NSPredicate *predicate = [CNGroup predicateForGroupsWithIdentifiers:@[identifier]];
    NSArray<CNGroup *> *groups = [contactStore groupsMatchingPredicate:predicate error:&error];
    
    if (error) {
        reject(@"get_group_error", @"Failed to fetch group", error);
        return;
    }
    
    if (groups.count == 0) {
        reject(@"get_group_not_found", @"No group found with the given identifier", nil);
        return;
    }
    
    CNGroup *group = groups.firstObject;
    NSDictionary *groupDict = @{
        @"identifier": group.identifier ?: @"",
        @"name": group.name ?: @""
    };
    
    resolve(groupDict);
}

RCT_EXPORT_METHOD(deleteGroup:(NSString *)identifier
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject) {
    if (!contactStore) {
        contactStore = [[CNContactStore alloc] init];
    }

    NSError *error = nil;
    NSPredicate *predicate = [CNGroup predicateForGroupsWithIdentifiers:@[identifier]];
    NSArray<CNGroup *> *groups = [contactStore groupsMatchingPredicate:predicate error:&error];

    if (error) {
        reject(@"delete_group_error", @"Failed to fetch group", error);
        return;
    }

    if (groups.count == 0) {
        reject(@"delete_group_not_found", @"No group found with the given identifier", nil);
        return;
    }

    CNGroup *groupToDelete = groups.firstObject;
    CNMutableGroup *mutableGroup = [groupToDelete mutableCopy];
    CNSaveRequest *saveRequest = [[CNSaveRequest alloc] init];
    [saveRequest deleteGroup:mutableGroup];

    BOOL success = [contactStore executeSaveRequest:saveRequest error:&error];

    if (success) {
        resolve(@(YES));
    } else {
        reject(@"delete_group_failed", @"Failed to delete group", error);
    }
}

RCT_EXPORT_METHOD(updateGroup:(NSString *)identifier
                  groupData:(NSDictionary *)groupData
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    if (!contactStore) {
        contactStore = [[CNContactStore alloc] init];
    }

    NSError *error = nil;
    NSPredicate *predicate = [CNGroup predicateForGroupsWithIdentifiers:@[identifier]];
    NSArray<CNGroup *> *groups = [contactStore groupsMatchingPredicate:predicate error:&error];

    if (error) {
        reject(@"update_group_error", @"Failed to fetch group", error);
        return;
    }

    if (groups.count == 0) {
        reject(@"update_group_not_found", @"No group found with the given identifier", nil);
        return;
    }

    CNGroup *groupToUpdate = groups.firstObject;
    CNMutableGroup *mutableGroup = [groupToUpdate mutableCopy];

    // Update group details based on groupData
    NSString *newName = groupData[@"name"];
    if (newName && [newName isKindOfClass:[NSString class]] && newName.length > 0) {
        mutableGroup.name = newName;
    }

    CNSaveRequest *saveRequest = [[CNSaveRequest alloc] init];
    [saveRequest updateGroup:mutableGroup];

    BOOL success = [contactStore executeSaveRequest:saveRequest error:&error];

    if (success) {
        NSDictionary *updatedGroupDict = @{
            @"identifier": mutableGroup.identifier ?: @"",
            @"name": mutableGroup.name ?: @""
        };
        resolve(updatedGroupDict);
    } else {
        reject(@"update_group_failed", @"Failed to update group", error);
    }
}

RCT_EXPORT_METHOD(addGroup:(NSDictionary *)groupData
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    if (!groupData || ![groupData isKindOfClass:[NSDictionary class]]) {
        reject(@"invalid_data", @"Group data must be a dictionary", nil);
        return;
    }

    NSString *groupName = groupData[@"name"];
    if (!groupName || ![groupName isKindOfClass:[NSString class]] || groupName.length == 0) {
        reject(@"invalid_name", @"Group name is required and must be a non-empty string", nil);
        return;
    }

    CNContactStore *store = [self contactsStore:reject];
    if (!store) {
        // contactsStore method handles rejection
        return;
    }

    CNMutableGroup *mutableGroup = [[CNMutableGroup alloc] init];
    mutableGroup.name = groupName;

    CNSaveRequest *saveRequest = [[CNSaveRequest alloc] init];
    [saveRequest addGroup:mutableGroup toContainerWithIdentifier:nil]; // Add to default container

    NSError *error = nil;
    BOOL success = [store executeSaveRequest:saveRequest error:&error];
    if (success) {
        NSDictionary *groupDict = @{
            @"identifier": mutableGroup.identifier ?: @"",
            @"name": mutableGroup.name ?: @""
        };
        resolve(groupDict);
    } else {
        NSString *errorMessage = error.localizedDescription ?: @"Unknown error adding group";
        reject(@"add_group_error", errorMessage, error);
    }
}

RCT_EXPORT_METHOD(contactsInGroup:(NSString *)identifier
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    CNContactStore *store = [self contactsStore:reject];
    if (!store) {
        return;
    }
    
    NSError *error = nil;
    NSPredicate *predicate = [CNContact predicateForContactsInGroupWithIdentifier:identifier];
    NSArray *keysToFetch = @[
        CNContactIdentifierKey,
        CNContactGivenNameKey,
        CNContactFamilyNameKey,
        CNContactMiddleNameKey,
        CNContactEmailAddressesKey,
        CNContactPhoneNumbersKey,
        CNContactPostalAddressesKey,
        CNContactOrganizationNameKey,
        CNContactJobTitleKey,
        CNContactImageDataAvailableKey,
        CNContactThumbnailImageDataKey,
        CNContactUrlAddressesKey,
        CNContactBirthdayKey,
        CNContactInstantMessageAddressesKey
    ];
    
    if (notesUsageEnabled) {
        keysToFetch = [keysToFetch arrayByAddingObject:CNContactNoteKey];
    }
    
    CNContactFetchRequest *fetchRequest = [[CNContactFetchRequest alloc] initWithKeysToFetch:keysToFetch];
    fetchRequest.predicate = predicate;
    
    NSMutableArray *contacts = [NSMutableArray array];
    
    BOOL success = [store enumerateContactsWithFetchRequest:fetchRequest error:&error usingBlock:^(CNContact * _Nonnull contact, BOOL * _Nonnull stop) {
        NSDictionary *contactDict = [self contactToDictionary:contact withThumbnails:true];
        [contacts addObject:contactDict];
    }];
    
    if (!success) {
        reject(@"contacts_in_group_error", @"Failed to fetch contacts in group", error);
        return;
    }
    
    resolve(contacts);
}

RCT_EXPORT_METHOD(addContactsToGroup:(NSString *)groupId
                  contactIds:(NSArray<NSString *> *)contactIds
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
{
    // Ensure contactStore is initialized
    if (!contactStore) {
        contactStore = [[CNContactStore alloc] init];
    }
    
    // Check authorization
    CNAuthorizationStatus authStatus = [CNContactStore authorizationStatusForEntityType:CNEntityTypeContacts];
    if (@available(iOS 18.0, *)) {
        if (authStatus != CNAuthorizationStatusAuthorized && authStatus != CNAuthorizationStatusLimited) {
            reject(@"permission_denied", @"Contacts permission denied", nil);
            return;
        }
    }
    else if (authStatus != CNAuthorizationStatusAuthorized) {
        reject(@"permission_denied", @"Contacts permission denied", nil);
        return;
    }
    
    NSError *error = nil;
    
    // Fetch the group
    NSPredicate *predicate = [CNGroup predicateForGroupsWithIdentifiers:@[groupId]];
    NSArray<CNGroup *> *groups = [contactStore groupsMatchingPredicate:predicate error:&error];
    
    if (error) {
        reject(@"group_fetch_error", @"Failed to fetch group", error);
        return;
    }
    
    if (groups.count == 0) {
        reject(@"group_not_found", @"No group found with the given identifier", nil);
        return;
    }
    
    CNGroup *group = groups.firstObject;
    
    // Initialize CNSaveRequest
    CNSaveRequest *saveRequest = [[CNSaveRequest alloc] init];
    
    // Iterate over contactIds and add each contact to the group
    for (NSString *contactId in contactIds) {
        // Fetch the contact
        NSError *contactError = nil;
        CNContact *contact = [contactStore unifiedContactWithIdentifier:contactId
                                                          keysToFetch:@[CNContactIdentifierKey]
                                                                error:&contactError];
        if (contactError) {
            reject(@"contact_fetch_error", [NSString stringWithFormat:@"Failed to fetch contact with ID %@", contactId], contactError);
            return;
        }
        
        if (!contact) {
            reject(@"contact_not_found", [NSString stringWithFormat:@"No contact found with ID %@", contactId], nil);
            return;
        }
        
        // Add contact to group
        [saveRequest addMember:contact toGroup:group];
    }
    
    // Execute the save request
    BOOL success = [contactStore executeSaveRequest:saveRequest error:&error];
    
    if (success) {
        resolve(@(YES));
    } else {
        reject(@"add_contacts_error", @"Failed to add contacts to group", error);
    }
}

RCT_EXPORT_METHOD(removeContactsFromGroup:(NSString *)groupId
                      contactIds:(NSArray<NSString *> *)contactIds
                      resolver:(RCTPromiseResolveBlock)resolve
                      rejecter:(RCTPromiseRejectBlock)reject) {
    // Ensure contactStore is initialized
    if (!contactStore) {
        contactStore = [[CNContactStore alloc] init];
    }

    // Check authorization status
    CNAuthorizationStatus authStatus = [CNContactStore authorizationStatusForEntityType:CNEntityTypeContacts];
    if (@available(iOS 18.0, *)) {
        if (authStatus != CNAuthorizationStatusAuthorized && authStatus != CNAuthorizationStatusLimited) {
            reject(@"permission_denied", @"Contacts permission denied", nil);
            return;
        }
    }
    else if (authStatus != CNAuthorizationStatusAuthorized) {
        reject(@"permission_denied", @"Contacts permission denied", nil);
        return;
    }

    // Fetch the group
    NSError *error = nil;
    NSPredicate *predicate = [CNGroup predicateForGroupsWithIdentifiers:@[groupId]];
    NSArray<CNGroup *> *groups = [contactStore groupsMatchingPredicate:predicate error:&error];

    if (error) {
        reject(@"group_fetch_error", @"Failed to fetch group", error);
        return;
    }

    if (groups.count == 0) {
        reject(@"group_not_found", @"No group found with the given identifier", nil);
        return;
    }

    CNGroup *group = groups.firstObject;
    CNMutableGroup *mutableGroup = [group mutableCopy];
    CNSaveRequest *saveRequest = [[CNSaveRequest alloc] init];

    // Iterate over contactIds and remove each contact from the group
    for (NSString *contactId in contactIds) {
        NSError *contactError = nil;
        CNContact *contact = [contactStore unifiedContactWithIdentifier:contactId
                                                          keysToFetch:@[CNContactIdentifierKey]
                                                                error:&contactError];
        if (contactError) {
            reject(@"contact_fetch_error", [NSString stringWithFormat:@"Failed to fetch contact with ID %@", contactId], contactError);
            return;
        }

        if (!contact) {
            reject(@"contact_not_found", [NSString stringWithFormat:@"No contact found with ID %@", contactId], nil);
            return;
        }

        [saveRequest removeMember:contact fromGroup:mutableGroup];
    }

    // Execute the save request
    BOOL success = [contactStore executeSaveRequest:saveRequest error:&error];

    if (success) {
        resolve(@(YES));
    } else {
        reject(@"remove_contacts_error", @"Failed to remove contacts from group", error);
    }
}


-(CNContactStore*) contactsStore: (RCTPromiseRejectBlock) reject {
    if(!contactStore) {
        CNContactStore* store = [[CNContactStore alloc] init];

        contactStore = store;
    }
    if(!contactStore.defaultContainerIdentifier) {
        RCTLog(@"warn - no contact store container id");

        CNAuthorizationStatus authStatus = [CNContactStore authorizationStatusForEntityType:CNEntityTypeContacts];
        if (authStatus == CNAuthorizationStatusDenied || authStatus == CNAuthorizationStatusRestricted){
            reject(@"Error", @"denied", nil);
        } else {
            reject(@"Error", @"undefined", nil);
        }

        return nil;
    }

    return contactStore;
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

// Thanks to this guard, we won't compile this code when we build for the old architecture.
#ifdef RCT_NEW_ARCH_ENABLED
- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeContactsSpecJSI>(params);
}

#endif

- (void)getAll:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    [self getAllContacts:resolve reject:reject withThumbnails:true];
}

- (void)checkPermission:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    CNAuthorizationStatus authStatus = [CNContactStore authorizationStatusForEntityType:CNEntityTypeContacts];

    if (authStatus == CNAuthorizationStatusDenied || authStatus == CNAuthorizationStatusRestricted) {
        resolve(@"denied");
    } else if (authStatus == CNAuthorizationStatusAuthorized) {
        resolve(@"authorized");
    } else if (@available(iOS 18, *)) {
        if (authStatus == CNAuthorizationStatusRestricted) {
            resolve(@"limited");
        }
    } else {
        resolve(@"undefined");
    }
}


 - (void)deleteContact:(NSDictionary *)contactData resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
     if(!contactStore) {
         contactStore = [[CNContactStore alloc] init];
     }

     NSString* recordID = [contactData valueForKey:@"recordID"];

     NSArray *keys = @[CNContactIdentifierKey];


     @try {

         CNMutableContact *contact = [[contactStore unifiedContactWithIdentifier:recordID keysToFetch:keys error:nil] mutableCopy];
         NSError *error;
         CNSaveRequest *saveRequest = [[CNSaveRequest alloc] init];
         [saveRequest deleteContact:contact];
         [contactStore executeSaveRequest:saveRequest error:&error];

         resolve(recordID);
     }
     @catch (NSException *exception) {
         reject(@"Error", [exception reason], nil);
     }
 }


 - (void)editExistingContact:(NSDictionary *)contactData resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
     CNContactStore* contactStore = [self contactsStore:reject];
     if(!contactStore)
         return;

     NSError* contactError;
     selectedContact = nil;
     NSString* recordID = [contactData valueForKey:@"recordID"];
     NSArray * keysToFetch =@[
                              CNContactEmailAddressesKey,
                              CNContactPhoneNumbersKey,
                              CNContactFamilyNameKey,
                              CNContactGivenNameKey,
                              CNContactMiddleNameKey,
                              CNContactPostalAddressesKey,
                              CNContactOrganizationNameKey,
                              CNContactJobTitleKey,
                              CNContactImageDataAvailableKey,
                              CNContactThumbnailImageDataKey,
                              CNContactImageDataKey,
                              CNContactUrlAddressesKey,
                              CNContactBirthdayKey,
                              CNContactIdentifierKey,
                              [CNContactFormatter descriptorForRequiredKeysForStyle:CNContactFormatterStyleFullName],
                              [CNContactViewController descriptorForRequiredKeys]];

     @try {
         CNMutableContact* record = [[contactStore unifiedContactWithIdentifier:recordID keysToFetch:keysToFetch error:&contactError] mutableCopy];

         NSMutableArray *phoneNumbers = [[NSMutableArray alloc]init];
         phoneNumbers = [NSMutableArray arrayWithArray:record.phoneNumbers];

         for (id phoneData in [contactData valueForKey:@"phoneNumbers"]) {
             NSString *number = [phoneData valueForKey:@"number"];

             CNLabeledValue *contactPhoneNumber = [CNLabeledValue labeledValueWithLabel:CNLabelOther value:[CNPhoneNumber phoneNumberWithStringValue:number]];
             //record.phoneNumbers = @[contactPhoneNumber];
             [phoneNumbers addObject:contactPhoneNumber];
         }

         NSArray *phoneNumbersNew = [[NSArray alloc]init];
         phoneNumbersNew = [NSArray arrayWithArray:phoneNumbers];


         record.phoneNumbers = phoneNumbersNew;

         CNSaveRequest *request = [[CNSaveRequest alloc] init];
         [request updateContact:record];
         
         selectedContact = record;

         [contactStore executeSaveRequest:request error:nil];

         CNContactViewController *controller = [CNContactViewController viewControllerForContact:record];
         //controller.title = @"Saved!";
         UIAlertController *alert=   [UIAlertController
             alertControllerWithTitle:@"Saved!"
             message:@"Number added to contact"
             preferredStyle:UIAlertControllerStyleAlert];
         //[controller presentViewController:alert animated:YES completion:nil];
         
         // Add a cancel button which will close the view
         controller.navigationItem.leftBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Done"  style:UIBarButtonItemStylePlain target:self action:@selector(doneContactForm)];
         
         controller.delegate = self;
         controller.allowsEditing = true;
         controller.allowsActions = true;

         dispatch_async(dispatch_get_main_queue(), ^{
             UINavigationController* navigation = [[UINavigationController alloc] initWithRootViewController:controller];
             UIViewController *presentingViewController = RCTPresentedViewController();
             [presentingViewController presentViewController:navigation animated:YES completion:nil];
             [controller presentViewController:alert animated:YES completion:nil];

             self->updateContactPromise = resolve;
         });
         
         dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(3.0 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{

                 [alert dismissViewControllerAnimated:YES completion:^{

                     //Dismissed
                 }];

         });
     }
     @catch (NSException *exception) {
         reject(@"Error", [exception reason], nil);
     }
 }


 - (void)getAllWithoutPhotos:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
     [self getAllContacts:resolve reject:reject withThumbnails:false];
 }


- (void)getContactById:(nonnull NSString *)recordID resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    CNContactStore *contactStore = [self contactsStore:reject];
    if (!contactStore)
        return;

    CNEntityType entityType = CNEntityTypeContacts;
    CNAuthorizationStatus authorizationStatus = [CNContactStore authorizationStatusForEntityType:entityType];

    if (authorizationStatus == CNAuthorizationStatusNotDetermined) {
        [contactStore requestAccessForEntityType:entityType completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if (granted) {
                resolve([self getContact:recordID addressBook:contactStore withThumbnails:NO]);
            }
        }];
    } else if (authorizationStatus == CNAuthorizationStatusAuthorized) {
        resolve([self getContact:recordID addressBook:contactStore withThumbnails:NO]);
    } else if (@available(iOS 18, *)) {
        if (authorizationStatus == CNAuthorizationStatusRestricted) {
            resolve([self getContact:recordID addressBook:contactStore withThumbnails:NO]);
        }
    }
}


 - (void)getContactsByEmailAddress:(NSString *)string resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
     CNContactStore *contactStore = [[CNContactStore alloc] init];
        if (!contactStore)
            return;
        [self getContactsFromAddressBook:contactStore byEmailAddress:string resolve:resolve];
 }


 - (void)getContactsByPhoneNumber:(NSString *)string resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
     CNContactStore *contactStore = [[CNContactStore alloc] init];
         if (!contactStore)
             return;
         [self getContactsFromAddressBook:contactStore byPhoneNumber:string resolve:resolve];
 }


 - (void)getContactsMatchingString:(NSString *)string resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
     CNContactStore *contactStore = [[CNContactStore alloc] init];
         if (!contactStore)
             return;
         [self getContactsFromAddressBook:contactStore matchingString:string resolve:resolve];
 }


 - (void)getCount:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
     [self getAllContactsCount:resolve reject:reject];
 }


- (void)getPhotoForId:(nonnull NSString *)recordID resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    CNContactStore *contactStore = [self contactsStore:reject];
    if (!contactStore)
        return;

    CNEntityType entityType = CNEntityTypeContacts;
    CNAuthorizationStatus authStatus = [CNContactStore authorizationStatusForEntityType:entityType];

    if (authStatus == CNAuthorizationStatusNotDetermined) {
        [contactStore requestAccessForEntityType:entityType completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if (granted) {
                resolve([self getFilePathForThumbnailImage:recordID addressBook:contactStore]);
            }
        }];
    } else if (authStatus == CNAuthorizationStatusAuthorized) {
        resolve([self getFilePathForThumbnailImage:recordID addressBook:contactStore]);
    } else if (@available(iOS 18, *)) {
        if (authStatus == CNAuthorizationStatusRestricted) {
            resolve([self getFilePathForThumbnailImage:recordID addressBook:contactStore]);
        }
    } else {
        reject(@"CONTACT_ACCESS_DENIED", @"Contact access is not authorized.", nil);
    }
}

- (void)openContactForm:(NSDictionary *)contactData resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    CNMutableContact * contact = [[CNMutableContact alloc] init];
    
    [self updateRecord:contact withData:contactData];
    
    CNContactViewController *controller = [CNContactViewController viewControllerForNewContact:contact];
    
    
    controller.delegate = self;
    
    dispatch_async(dispatch_get_main_queue(), ^{
        UINavigationController* navigation = [[UINavigationController alloc] initWithRootViewController:controller];
        UIViewController *presentingViewController = RCTPresentedViewController();
        [presentingViewController presentViewController:navigation animated:YES completion:nil];
        
        self->updateContactPromise = resolve;
    });
}


 - (void)openExistingContact:(NSDictionary *)contactData resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
     if(!contactStore) {
         contactStore = [[CNContactStore alloc] init];
     }

     NSString* recordID = [contactData valueForKey:@"recordID"];
     NSString* backTitle = [contactData valueForKey:@"backTitle"];

     NSArray *keys = @[CNContactIdentifierKey,
                       CNContactEmailAddressesKey,
                       CNContactBirthdayKey,
                       CNContactImageDataKey,
                       CNContactPhoneNumbersKey,
                       [CNContactFormatter descriptorForRequiredKeysForStyle:CNContactFormatterStyleFullName],
                       [CNContactViewController descriptorForRequiredKeys]];

     @try {

         CNContact *contact = [contactStore unifiedContactWithIdentifier:recordID keysToFetch:keys error:nil];

         CNContactViewController *contactViewController = [CNContactViewController viewControllerForContact:contact];

         // Add a cancel button which will close the view
         contactViewController.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:backTitle == nil ? @"Cancel" : backTitle style:UIBarButtonItemStyleDone target:self action:@selector(cancelContactForm)];
         contactViewController.delegate = self;


         dispatch_async(dispatch_get_main_queue(), ^{
             UINavigationController* navigation = [[UINavigationController alloc] initWithRootViewController:contactViewController];

             UIViewController *currentViewController = [UIApplication sharedApplication].keyWindow.rootViewController;

             while (currentViewController.presentedViewController)
             {
                 currentViewController = currentViewController.presentedViewController;
             }

             UIActivityIndicatorViewStyle activityIndicatorStyle;
             UIActivityIndicatorView *activityIndicator = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleMedium];
             UIColor *activityIndicatorBackgroundColor;
             if (@available(iOS 13, *)) {
                 activityIndicatorStyle = UIActivityIndicatorViewStyleMedium;
                 activityIndicatorBackgroundColor = [UIColor secondarySystemGroupedBackgroundColor];
             } else {
                 activityIndicatorStyle = UIActivityIndicatorViewStyleGray;
                 activityIndicatorBackgroundColor = [UIColor whiteColor];;
             }

             // Cover the contact view with an activity indicator so we can put it in edit mode without user seeing the transition
             UIActivityIndicatorView *activityIndicatorView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:activityIndicatorStyle];
             activityIndicatorView.frame = UIApplication.sharedApplication.keyWindow.frame;
             [activityIndicatorView startAnimating];
             activityIndicatorView.backgroundColor = activityIndicatorBackgroundColor;
             [navigation.view addSubview:activityIndicatorView];

             [currentViewController presentViewController:navigation animated:YES completion:nil];


             // TODO should this 'fake click' method be used? For a brief instance
             // Fake click edit button to enter edit mode
             //                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
             //                    SEL selector = contactViewController.navigationItem.rightBarButtonItem.action;
             //                    NSLog(@"!!!!!!!!!!!!!!!!!! FAKE CLICK!!!  %@", NSStringFromSelector(selector));
             //                    id  target = contactViewController.navigationItem.rightBarButtonItem.target;
             //                    [target performSelector:selector];
             //                });


             // We need to wait for a short while otherwise contactViewController will not respond to the selector (it has not initialized)
             [contactViewController performSelector:@selector(toggleEditing:) withObject:nil afterDelay:0.1];

             // remove the activity indicator after a delay so the underlying transition will have time to complete
             dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                 [activityIndicatorView removeFromSuperview];
             });

             updateContactPromise = resolve;
         });

     }
     @catch (NSException *exception) {
         reject(@"Error", [exception reason], nil);
     }
 }


 - (void)requestPermission:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
     CNContactStore* contactStore = [[CNContactStore alloc] init];

        [contactStore requestAccessForEntityType:CNEntityTypeContacts completionHandler:^(BOOL granted, NSError * _Nullable error) {
            [self checkPermission:resolve rejecter:reject];
        }];
 }


 - (void)updateContact:(NSDictionary *)contactData resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
     CNContactStore* contactStore = [self contactsStore:reject];
     if(!contactStore)
         return;

     NSError* contactError;
     NSString* recordID = [contactData valueForKey:@"recordID"];
     NSMutableArray * keysToFetch = [NSMutableArray arrayWithArray: @[
         CNContactEmailAddressesKey,
         CNContactPhoneNumbersKey,
         CNContactFamilyNameKey,
         CNContactGivenNameKey,
         CNContactMiddleNameKey,
         CNContactPostalAddressesKey,
         CNContactOrganizationNameKey,
         CNContactJobTitleKey,
         CNContactImageDataAvailableKey,
         CNContactThumbnailImageDataKey,
         CNContactImageDataKey,
         CNContactUrlAddressesKey,
         CNContactBirthdayKey,
         CNContactInstantMessageAddressesKey
     ]];
     if(notesUsageEnabled) {
         [keysToFetch addObject: CNContactNoteKey];
     }

     @try {
         CNMutableContact* record = [[contactStore unifiedContactWithIdentifier:recordID keysToFetch:keysToFetch error:&contactError] mutableCopy];
         [self updateRecord:record withData:contactData];
         CNSaveRequest *request = [[CNSaveRequest alloc] init];
         [request updateContact:record];

         [contactStore executeSaveRequest:request error:nil];

         NSDictionary *contactDict = [self contactToDictionary:record withThumbnails:false];

         resolve(contactDict);
     }
     @catch (NSException *exception) {
         reject(@"Error", [exception reason], nil);
     }
 }


 - (void)viewExistingContact:(NSDictionary *)contactData resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
     if(!contactStore) {
         contactStore = [[CNContactStore alloc] init];
     }

     NSString* recordID = [contactData valueForKey:@"recordID"];
     NSString* backTitle = [contactData valueForKey:@"backTitle"];

     NSArray *keys = @[CNContactIdentifierKey,
                       CNContactEmailAddressesKey,
                       CNContactBirthdayKey,
                       CNContactImageDataKey,
                       CNContactPhoneNumbersKey,
                       [CNContactFormatter descriptorForRequiredKeysForStyle:CNContactFormatterStyleFullName],
                       [CNContactViewController descriptorForRequiredKeys]];

     @try {

         CNContact *contact = [contactStore unifiedContactWithIdentifier:recordID keysToFetch:keys error:nil];

         CNContactViewController *contactViewController = [CNContactViewController viewControllerForContact:contact];

         // Add a cancel button which will close the view
         contactViewController.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:backTitle == nil ? @"Cancel" : backTitle style:UIBarButtonItemStylePlain target:self action:@selector(cancelContactForm)];
         contactViewController.delegate = self;


         dispatch_async(dispatch_get_main_queue(), ^{
             UINavigationController* navigation = [[UINavigationController alloc] initWithRootViewController:contactViewController];
             UIViewController *presentingViewController = RCTPresentedViewController();
             [presentingViewController presentViewController:navigation animated:YES completion:nil];

             updateContactPromise = resolve;
         });

     }
     @catch (NSException *exception) {
         reject(@"Error", [exception reason], nil);
     }
 }


 - (void)writePhotoToPath:(NSString *)contactId file:(NSString *)file resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
     reject(@"Error", @"not implemented", nil);
 }

- (void)addContact:(NSDictionary *)contactData resolve:(RCTPromiseResolveBlock)resolve reject:(RCTPromiseRejectBlock)reject {
    CNContactStore* contactStore = [self contactsStore:reject];
        if(!contactStore)
            return;

        CNMutableContact * contact = [[CNMutableContact alloc] init];

        [self updateRecord:contact withData:contactData];

        @try {
            CNSaveRequest *request = [[CNSaveRequest alloc] init];
            [request addContact:contact toContainerWithIdentifier:nil];

            [contactStore executeSaveRequest:request error:nil];

            NSDictionary *contactDict = [self contactToDictionary:contact withThumbnails:false];

            resolve(contactDict);
        }
        @catch (NSException *exception) {
            reject(@"Error", [exception reason], nil);
        }
}

@end
