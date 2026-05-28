import { NativeModules } from "react-native";
import NativeContacts from "./src/NativeContacts";
import { Contact, Group, PermissionType } from "./type";

const Contacts = NativeModules.Contacts ?? NativeContacts;

export async function getAll(): Promise<Contact[]> {
  return Contacts.getAll();
}

export async function getAllWithoutPhotos(): Promise<Contact[]> {
  return Contacts.getAllWithoutPhotos();
}

export async function getContactById(contactId: string): Promise<Contact | null> {
  return Contacts.getContactById(contactId);
}

export async function getCount(): Promise<number> {
  return Contacts.getCount();
}

export async function getPhotoForId(contactId: string): Promise<string> {
  return Contacts.getPhotoForId(contactId);
}

export async function addContact(contact: Partial<Contact>): Promise<Contact> {
  return Contacts.addContact(contact);
}

export async function openContactForm(contact: Partial<Contact>): Promise<Contact> {
  return Contacts.openContactForm(contact);
}

export async function openExistingContact(contact: Contact): Promise<Contact> {
  return Contacts.openExistingContact(contact);
}

export async function viewExistingContact(contact: {
  recordID: string;
}): Promise<Contact | void> {
  return Contacts.viewExistingContact(contact);
}

export async function editExistingContact(contact: Contact): Promise<Contact> {
  return Contacts.editExistingContact(contact);
}

export async function updateContact(
  contact: Partial<Contact> & { recordID: string }
): Promise<void> {
  return Contacts.updateContact(contact);
}

export async function deleteContact(contact: Contact): Promise<void> {
  return Contacts.deleteContact(contact);
}

export async function getContactsMatchingString(str: string): Promise<Contact[]> {
  return Contacts.getContactsMatchingString(str);
}

export async function getContactsByPhoneNumber(
  phoneNumber: string
): Promise<Contact[]> {
  return Contacts.getContactsByPhoneNumber(phoneNumber);
}

export async function getContactsByEmailAddress(
  emailAddress: string
): Promise<Contact[]> {
  return Contacts.getContactsByEmailAddress(emailAddress);
}

export async function checkPermission(): Promise<PermissionType> {
  return Contacts.checkPermission();
}

export async function requestPermission(): Promise<PermissionType> {
  return Contacts.requestPermission();
}

export async function writePhotoToPath(
  contactId: string,
  file: string
): Promise<boolean> {
  return Contacts.writePhotoToPath(contactId, file);
}

export function iosEnableNotesUsage(enabled: boolean): void {
  return Contacts.iosEnableNotesUsage(enabled);
}

export async function getGroups(): Promise<Group[]> {
  return Contacts.getGroups();
}
export async function getGroup(identifier: string): Promise<Group | null> {
  return Contacts.getGroup(identifier);
}
export async function deleteGroup(identifier: string): Promise<boolean> {
  return Contacts.deleteGroup(identifier);
}
export async function updateGroup(identifier: string, groupData: Pick<Group, 'name'>): Promise<Group> {
  return Contacts.updateGroup(identifier, groupData);
}
export async function addGroup(group: Pick<Group, 'name'>): Promise<Group>{
  return Contacts.addGroup(group);
}
export async function contactsInGroup(identifier: string): Promise<Contact[]> {
  return Contacts.contactsInGroup(identifier);
}
export async function addContactsToGroup(groupIdentifier: string, contactIdentifiers: string[]): Promise<boolean> {
  return Contacts.addContactsToGroup(groupIdentifier, contactIdentifiers);
}
export async function removeContactsFromGroup(groupIdentifier: string, contactIdentifiers: string[]): Promise<boolean> {
  return Contacts.removeContactsFromGroup(groupIdentifier, contactIdentifiers);
}
export default {
  getAll,
  getAllWithoutPhotos,
  getContactById,
  getCount,
  getPhotoForId,
  addContact,
  openContactForm,
  openExistingContact,
  viewExistingContact,
  editExistingContact,
  updateContact,
  deleteContact,
  getContactsMatchingString,
  getContactsByPhoneNumber,
  getContactsByEmailAddress,
  checkPermission,
  requestPermission,
  writePhotoToPath,
  iosEnableNotesUsage,
  getGroups,
  getGroup,
  deleteGroup,
  updateGroup,
  addGroup,
  contactsInGroup,
  addContactsToGroup,
  removeContactsFromGroup
};
