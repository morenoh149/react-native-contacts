#import <AddressBook/AddressBook.h>
#import <UIKit/UIKit.h>
#import "RCTContacts.h"
#import <AssetsLibrary/AssetsLibrary.h>
#import <React/RCTLog.h>

@implementation RCTContacts {
    CNContactStore * contactStore;

    RCTResponseSenderBlock updateContactCallback;
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
             @"PERMISSION_UNDEFINED": @"undefined"
             };
}

RCT_EXPORT_METHOD(checkPermission:(RCTResponseSenderBlock) callback)
{
    CNAuthorizationStatus authStatus = [CNContactStore authorizationStatusForEntityType:CNEntityTypeContacts];
    if (authStatus == CNAuthorizationStatusDenied || authStatus == CNAuthorizationStatusRestricted){
        callback(@[[NSNull null], @"denied"]);
    } else if (authStatus == CNAuthorizationStatusAuthorized){
        callback(@[[NSNull null], @"authorized"]);
    } else {
        callback(@[[NSNull null], @"undefined"]);
    }
}

RCT_EXPORT_METHOD(requestPermission:(RCTResponseSenderBlock) callback)
{
    CNContactStore* contactStore = [[CNContactStore alloc] init];

    [contactStore requestAccessForEntityType:CNEntityTypeContacts completionHandler:^(BOOL granted, NSError * _Nullable error) {
        [self checkPermission:callback];
    }];
}

RCT_EXPORT_METHOD(getContactsMatchingString:(NSString *)string callback:(RCTResponseSenderBlock) callback)
{
    CNContactStore *contactStore = [[CNContactStore alloc] init];
    if (!contactStore)
        return;
    [self getContactsFromAddressBook:contactStore matchingString:string callback:callback];
}

-(void) getContactsFromAddressBook:(CNContactStore *)store
                    matchingString:(NSString *)searchString
                          callback:(RCTResponseSenderBlock)callback
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
                      CNContactBirthdayKey
                      ];
    NSArray *arrayOfContacts = [store unifiedContactsMatchingPredicate:[CNContact predicateForContactsMatchingName:searchString]
                                                           keysToFetch:keys
                                                                 error:&contactError];
    [arrayOfContacts enumerateObjectsUsingBlock:^(CNContact * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        NSDictionary *contactDictionary = [self contactToDictionary:obj withThumbnails:true];
        [contacts addObject:contactDictionary];
    }];
    callback(@[[NSNull null], contacts]);
}

RCT_EXPORT_METHOD(getContactsByPhoneNumber:(NSString *)string callback:(RCTResponseSenderBlock) callback)
{
    CNContactStore *contactStore = [[CNContactStore alloc] init];
    if (!contactStore)
        return;
    [self getContactsFromAddressBook:contactStore byPhoneNumber:string callback:callback];
}

-(void) getContactsFromAddressBook:(CNContactStore *)store
                    byPhoneNumber:(NSString *)phoneNumber
                          callback:(RCTResponseSenderBlock)callback
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
                      CNContactBirthdayKey
                      ];
    CNPhoneNumber *cnPhoneNumber = [[CNPhoneNumber alloc] initWithStringValue:phoneNumber];
    NSArray *arrayOfContacts = [store unifiedContactsMatchingPredicate:[CNContact predicateForContactsMatchingPhoneNumber:cnPhoneNumber]
                                                           keysToFetch:keys
                                                                 error:&contactError];
    [arrayOfContacts enumerateObjectsUsingBlock:^(CNContact * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        NSDictionary *contactDictionary = [self contactToDictionary:obj withThumbnails:true];
        [contacts addObject:contactDictionary];
    }];
    callback(@[[NSNull null], contacts]);
}

-(void) getAllContacts:(RCTResponseSenderBlock) callback
        withThumbnails:(BOOL) withThumbnails
{
    CNContactStore* contactStore = [self contactsStore:callback];
    if(!contactStore)
        return;

    [self retrieveContactsFromAddressBook:contactStore withThumbnails:withThumbnails withCallback:callback];
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
                                       CNContactImageDataAvailableKey,
                                       CNContactUrlAddressesKey,
                                       CNContactBirthdayKey
                                       ]];

    if(withThumbnails) {
        [keysToFetch addObject:CNContactThumbnailImageDataKey];
    }

    CNContactFetchRequest * request = [[CNContactFetchRequest alloc]initWithKeysToFetch:keysToFetch];
    BOOL success = [contactStore enumerateContactsWithFetchRequest:request error:&contactError usingBlock:^(CNContact * __nonnull contact, BOOL * __nonnull stop){
        NSDictionary *contactDict = [self contactToDictionary: contact withThumbnails:withThumbnails];
        [contacts addObject:contactDict];
    }];

    callback(@[[NSNull null], contacts]);
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

    if (birthday) {
        if (birthday.month != NSDateComponentUndefined && birthday.day != NSDateComponentUndefined) {
            //months are indexed to 0 in JavaScript (0 = January) so we subtract 1 from NSDateComponents.month
            if (birthday.year != NSDateComponentUndefined) {
                [output setObject:@{@"year": @(birthday.year), @"month": @(birthday.month - 1), @"day": @(birthday.day)} forKey:@"birthday"];
            } else {
                [output setObject:@{@"month": @(birthday.month - 1), @"day":@(birthday.day)} forKey:@"birthday"];
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

    [output setValue:[NSNumber numberWithBool:person.imageDataAvailable] forKey:@"hasThumbnail"];
    if (withThumbnails) {
        [output setObject:[self getFilePathForThumbnailImage:person recordID:recordID] forKey:@"thumbnailPath"];
    }
    
    //handle social profiles
    NSMutableArray *socialProfiles = [[NSMutableArray alloc] init];
    
    for (CNLabeledValue<CNSocialProfile*>* labeledValue in person.socialProfiles) {
        NSString * label = [CNLabeledValue localizedStringForLabel:[labeledValue label]];
        CNSocialProfile* socialProfile = labeledValue.value;
        NSMutableDictionary* profile = [NSMutableDictionary dictionary];
        
        if(socialProfile) {
            if(!label) {
                label = [CNLabeledValue localizedStringForLabel:@"other"];
            }
            if (socialProfile.service) {
                [profile setObject: socialProfile.service forKey:@"service"];
            }
            if (socialProfile.username) {
                [profile setObject: socialProfile.username forKey:@"username"];
            }
            if (socialProfile.urlString) {
                [profile setObject: socialProfile.urlString forKey:@"url"];
            }
            if (socialProfile.userIdentifier) {
                [profile setObject: socialProfile.urlString forKey:@"userIdentifier"];
            }
            [socialProfiles addObject:profile];
        }
    }
    
    [output setObject: socialProfiles forKey:@"socialProfiles"];
    //end social profiles
    
    //handle instant messaging addresses
    NSMutableArray *addresses = [[NSMutableArray alloc] init];
    
    for (CNLabeledValue<CNInstantMessageAddress*>* labeledValue in person.instantMessageAddresses) {
        NSString * label = [CNLabeledValue localizedStringForLabel:[labeledValue label]];
        CNInstantMessageAddress* instantMessageAddress = labeledValue.value;
        NSMutableDictionary* address = [NSMutableDictionary dictionary];
        
        if(instantMessageAddress) {
            if(!label) {
                label = [CNLabeledValue localizedStringForLabel:@"other"];
            }
            if (instantMessageAddress.service) {
                [address setObject: instantMessageAddress.service forKey:@"service"];
            }
            if (instantMessageAddress.username) {
                [address setObject: instantMessageAddress.username forKey:@"username"];
            }
            [addresses addObject:address];
        }
    }
    
    [output setObject: addresses forKey:@"instantMessageAddresses"];
    //end instant messaging addresses

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
    NSArray *paths = NSSearchPathForDirectoriesInDomains(directory, NSUserDomainMask, YES);
    return [paths firstObject];
}

RCT_EXPORT_METHOD(getPhotoForId:(nonnull NSString *)recordID callback:(RCTResponseSenderBlock)callback)
{
    CNContactStore* contactStore = [self contactsStore:callback];
    if(!contactStore)
        return;

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
    NSArray * keysToFetch =@[CNContactThumbnailImageDataKey, CNContactImageDataAvailableKey];
    CNContact* contact = [addressBook unifiedContactWithIdentifier:recordID keysToFetch:keysToFetch error:&contactError];

    return [self getFilePathForThumbnailImage:contact recordID:recordID];
}

RCT_EXPORT_METHOD(getContactById:(nonnull NSString *)recordID callback:(RCTResponseSenderBlock)callback)
{
    CNContactStore* contactStore = [self contactsStore:callback];
    if(!contactStore)
        return;

    CNEntityType entityType = CNEntityTypeContacts;
    if([CNContactStore authorizationStatusForEntityType:entityType] == CNAuthorizationStatusNotDetermined)
    {
        [contactStore requestAccessForEntityType:entityType completionHandler:^(BOOL granted, NSError * _Nullable error) {
            if(granted){
                callback(@[[NSNull null], [self getContact:recordID addressBook:contactStore withThumbnails:false]]);
            }
        }];
    }
    else if( [CNContactStore authorizationStatusForEntityType:entityType]== CNAuthorizationStatusAuthorized)
    {
        callback(@[[NSNull null], [self getContact:recordID addressBook:contactStore withThumbnails:false]]);
    }
}

-(NSString *) getContact:(NSString *)recordID
                               addressBook:(CNContactStore*)addressBook
                               withThumbnails:(BOOL) withThumbnails
{
    NSError* contactError;
    NSArray *keysToFetch = @[
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
                      CNContactBirthdayKey
                      ];
    CNContact* contact = [addressBook unifiedContactWithIdentifier:recordID keysToFetch:keysToFetch error:&contactError];

    return [self contactToDictionary: contact withThumbnails:withThumbnails];
}


RCT_EXPORT_METHOD(addContact:(NSDictionary *)contactData callback:(RCTResponseSenderBlock)callback)
{
    CNContactStore* contactStore = [self contactsStore:callback];
    if(!contactStore)
        return;

    CNMutableContact * contact = [[CNMutableContact alloc] init];

    [self updateRecord:contact withData:contactData];

    @try {
        CNSaveRequest *request = [[CNSaveRequest alloc] init];
        [request addContact:contact toContainerWithIdentifier:nil];

        [contactStore executeSaveRequest:request error:nil];

        NSDictionary *contactDict = [self contactToDictionary:contact withThumbnails:false];

        callback(@[[NSNull null], contactDict]);
    }
    @catch (NSException *exception) {
        callback(@[[exception description], [NSNull null]]);
    }
}

RCT_EXPORT_METHOD(openContactForm:(NSDictionary *)contactData callback:(RCTResponseSenderBlock)callback)
{
    CNContactStore* contactStore = [self contactsStore:callback];
    if(!contactStore)
        return;

    CNMutableContact * contact = [[CNMutableContact alloc] init];

    [self updateRecord:contact withData:contactData];

    CNContactViewController *controller = [CNContactViewController viewControllerForNewContact:contact];
    
    controller.delegate = self;

    dispatch_async(dispatch_get_main_queue(), ^{
        UINavigationController* navigation = [[UINavigationController alloc] initWithRootViewController:controller];
        UIViewController *viewController = (UIViewController*)[[[[UIApplication sharedApplication] delegate] window] rootViewController];
        [viewController presentViewController:navigation animated:YES completion:nil];

        updateContactCallback = callback;
    });
}

RCT_EXPORT_METHOD(openExistingContact:(NSDictionary *)contactData callback:(RCTResponseSenderBlock)callback)
{
    if(!contactStore) {
        contactStore = [[CNContactStore alloc] init];
    }

    NSString* recordID = [contactData valueForKey:@"recordID"];

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
        // TODO localize cancel button title (either through creating a localized strings file, or passing in the title)
        contactViewController.navigationItem.backBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Cancel" style:UIBarButtonItemStylePlain target:self action:@selector(cancelContactForm)];
        contactViewController.delegate = self;


        dispatch_async(dispatch_get_main_queue(), ^{
            UINavigationController* navigation = [[UINavigationController alloc] initWithRootViewController:contactViewController];
            UIViewController *rooViewController = (UIViewController*)[[[[UIApplication sharedApplication] delegate] window] rootViewController];

            // Cover the contact view with an activity indicator so we can put it in edit mode without user seeing the transition
            UIActivityIndicatorView *activityIndicatorView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
            activityIndicatorView.frame = UIScreen.mainScreen.applicationFrame;
            [activityIndicatorView startAnimating];
            activityIndicatorView.backgroundColor = [UIColor whiteColor];
            [navigation.view addSubview:activityIndicatorView];

            [rooViewController presentViewController:navigation animated:YES completion:nil];

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

            updateContactCallback = callback;
        });

    }
    @catch (NSException *exception) {
        callback(@[[exception description], [NSNull null]]);
    }
}

- (void)cancelContactForm
{
    if (updateContactCallback != nil) {
        UIViewController *rootViewController = (UIViewController*)[[[[UIApplication sharedApplication] delegate] window] rootViewController];
        [rootViewController dismissViewControllerAnimated:YES completion:nil];

        updateContactCallback(@[[NSNull null]]);
        updateContactCallback = nil;
    }
}

//dismiss open contact page after done or cancel is clicked
- (void)contactViewController:(CNContactViewController *)viewController didCompleteWithContact:(CNContact *)contact {
    [viewController dismissViewControllerAnimated:YES completion:nil];

    if(updateContactCallback) {

        if (contact) {
            NSDictionary *contactDict = [self contactToDictionary:contact withThumbnails:true];
            updateContactCallback(@[[NSNull null], contactDict]);
        } else {
            updateContactCallback(@[[NSNull null]]);
        }

        updateContactCallback = nil;
    }
}

RCT_EXPORT_METHOD(updateContact:(NSDictionary *)contactData callback:(RCTResponseSenderBlock)callback)
{
    CNContactStore* contactStore = [self contactsStore:callback];
    if(!contactStore)
        return;

    NSError* contactError;
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
                             CNContactBirthdayKey
                             ];

    @try {
        CNMutableContact* record = [[contactStore unifiedContactWithIdentifier:recordID keysToFetch:keysToFetch error:&contactError] mutableCopy];
        [self updateRecord:record withData:contactData];
        CNSaveRequest *request = [[CNSaveRequest alloc] init];
        [request updateContact:record];

        [contactStore executeSaveRequest:request error:nil];

        NSDictionary *contactDict = [self contactToDictionary:record withThumbnails:false];

        callback(@[[NSNull null], contactDict]);
    }
    @catch (NSException *exception) {
        callback(@[[exception description], [NSNull null]]);
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
            //months are indexed to 0 in JavaScript so we add 1 when assigning the month to DateComponent
            components.month = [birthday[@"month"] intValue] + 1;
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

+ (NSData*) loadImageAsset:(NSURL*)assetURL {
    //thanks to http://www.codercowboy.com/code-synchronous-alassetlibrary-asset-existence-check/

    __block NSData *data = nil;
    __block NSConditionLock * albumReadLock = [[NSConditionLock alloc] initWithCondition:WDASSETURL_PENDINGREADS];
    //this *MUST* execute on a background thread, ALAssetLibrary tries to use the main thread and will hang if you're on the main thread.
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        ALAssetsLibrary * assetLibrary = [[ALAssetsLibrary alloc] init];
        [assetLibrary assetForURL:assetURL
                      resultBlock:^(ALAsset *asset) {
                          ALAssetRepresentation *rep = [asset defaultRepresentation];

                          Byte *buffer = (Byte*)malloc(rep.size);
                          NSUInteger buffered = [rep getBytes:buffer fromOffset:0.0 length:rep.size error:nil];
                          data = [NSData dataWithBytesNoCopy:buffer length:buffered freeWhenDone:YES];

                          [albumReadLock lock];
                          [albumReadLock unlockWithCondition:WDASSETURL_ALLFINISHED];
                      } failureBlock:^(NSError *error) {
                          RCTLog(@"asset error: %@", [error localizedDescription]);

                          [albumReadLock lock];
                          [albumReadLock unlockWithCondition:WDASSETURL_ALLFINISHED];
                      }];
    });

    [albumReadLock lockWhenCondition:WDASSETURL_ALLFINISHED];
    [albumReadLock unlock];

    RCTLog(@"asset lookup finished: %@ %@", [assetURL absoluteString], (data ? @"exists" : @"does not exist"));

    return data;
}

RCT_EXPORT_METHOD(deleteContact:(NSDictionary *)contactData callback:(RCTResponseSenderBlock)callback)
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

        callback(@[[NSNull null], recordID]);
    }
    @catch (NSException *exception) {
        callback(@[[exception description], [NSNull null]]);
    }
}

RCT_EXPORT_METHOD(writePhotoToPath:(RCTResponseSenderBlock) callback)
{
    callback(@[@"not implemented", [NSNull null]]);
}

-(CNContactStore*) contactsStore: (RCTResponseSenderBlock)callback {
    if(!contactStore) {
        CNContactStore* store = [[CNContactStore alloc] init];

        if(!store.defaultContainerIdentifier) {
            RCTLog(@"warn - no contact store container id");

            CNAuthorizationStatus authStatus = [CNContactStore authorizationStatusForEntityType:CNEntityTypeContacts];
            if (authStatus == CNAuthorizationStatusDenied || authStatus == CNAuthorizationStatusRestricted){
                callback(@[@"denied", [NSNull null]]);
            } else {
                callback(@[@"undefined", [NSNull null]]);
            }

            return nil;
        }

        contactStore = store;
    }

    return contactStore;
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

@end
