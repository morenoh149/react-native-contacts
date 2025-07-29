#import <React/RCTBridgeModule.h>
#import <Contacts/Contacts.h>
#import <ContactsUI/ContactsUI.h>
#ifdef RCT_NEW_ARCH_ENABLED
#import <RNContactsSpec/RNContactsSpec.h>
#endif

@interface RCTContacts : NSObject <RCTBridgeModule, CNContactViewControllerDelegate>

@end

#ifdef RCT_NEW_ARCH_ENABLED
@interface RCTContacts: NSObject <NativeContactsSpec, CNContactViewControllerDelegate>

@end
#endif
