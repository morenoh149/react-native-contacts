#import <React/RCTBridgeModule.h>
#import <Contacts/Contacts.h>
#import <ContactsUI/ContactsUI.h>
#import <Foundation/Foundation.h>

#ifdef RCT_NEW_ARCH_ENABLED

#import <RNContactsSpec/RNContactsSpec.h>
@interface RCTContacts: NSObject <NativeContactsSpec, CNContactViewControllerDelegate>

#else

#import <React/RCTBridgeModule.h>
@interface RCTContacts : NSObject <RCTBridgeModule, CNContactViewControllerDelegate>

#endif

@end

