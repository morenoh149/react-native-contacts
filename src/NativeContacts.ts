import type { TurboModule } from "react-native/Libraries/TurboModule/RCTExport";
import { TurboModuleRegistry } from "react-native";
import { Contact, Group, PermissionType } from "../type";

export interface Spec extends TurboModule {
  getAll: () => Promise<any>;
  getAllWithoutPhotos: () => Promise<Contact[]>;
  getContactById: (contactId: string) => Promise<Contact>;
  getCount: () => Promise<number>;
  getPhotoForId: (contactId: string) => Promise<string>;
  addContact: (contact: Object) => Promise<any>;
  openContactForm: (contact: Object) => Promise<Contact>;
  openExistingContact: (contact: Object) => Promise<Contact>;
  viewExistingContact: (contact: { recordID: string }) => Promise<Contact>;
  editExistingContact: (contact: Object) => Promise<Contact>;
  updateContact: (contact: Object) => Promise<void>;
  deleteContact: (contact: Object) => Promise<void>;
  getContactsMatchingString: (str: string) => Promise<Contact[]>;
  getContactsByPhoneNumber: (phoneNumber: string) => Promise<Contact[]>;
  getContactsByEmailAddress: (emailAddress: string) => Promise<Contact[]>;
  checkPermission: () => Promise<PermissionType>;
  requestPermission: () => Promise<PermissionType>;
  writePhotoToPath: (contactId: string, file: string) => Promise<boolean>;
  iosEnableNotesUsage: (enabled: boolean) => void;
  getGroups(): Promise<Group[]>;
  getGroup: (identifier: string) => Promise<Group | null>;
  deleteGroup(identifier: string): Promise<boolean>;
  updateGroup(identifier: string, groupData: Object): Promise<Group>;
  addGroup(group: Object): Promise<Group>;
  contactsInGroup(identifier: string): Promise<Contact[]>;
  addContactsToGroup(
    groupIdentifier: string,
    contactIdentifiers: string[]
  ): Promise<boolean>;
  removeContactsFromGroup(
    groupIdentifier: string,
    contactIdentifiers: string[]
  ): Promise<boolean>;
}

export default TurboModuleRegistry.get<Spec>("RCTContacts");
