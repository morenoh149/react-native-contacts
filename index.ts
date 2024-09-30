import { NativeModules } from "react-native";
import NativeContacts from "./src/NativeContacts";
import { Contact, PermissionType } from "./type";

const isTurboModuleEnabled = global.__turboModuleProxy != null;
const Contacts = isTurboModuleEnabled ? NativeContacts : NativeModules.Contacts;

async function getAll(): Promise<Contact[]> {
  return Contacts.getAll();
}

async function getAllWithoutPhotos(): Promise<Contact[]> {
  return Contacts.getAllWithoutPhotos();
}

async function getContactById(contactId: string): Promise<Contact | null> {
  return Contacts.getContactById(contactId);
}

async function getCount(): Promise<number> {
  return Contacts.getCount();
}

async function getPhotoForId(contactId: string): Promise<string> {
  return Contacts.getPhotoForId(contactId);
}

async function addContact(contact: Partial<Contact>): Promise<Contact> {
  return Contacts.addContact(contact);
}

async function openContactForm(contact: Partial<Contact>): Promise<Contact> {
  return Contacts.openContactForm(contact);
}

async function openExistingContact(contact: Contact): Promise<Contact> {
  return Contacts.openExistingContact(contact);
}

async function viewExistingContact(contact: {
  recordID: string;
}): Promise<Contact | void> {
  return Contacts.viewExistingContact(contact);
}

async function editExistingContact(contact: Contact): Promise<Contact> {
  return Contacts.editExistingContact(contact);
}

async function updateContact(
  contact: Partial<Contact> & { recordID: string }
): Promise<void> {
  return Contacts.updateContact(contact);
}

async function deleteContact(contact: Contact): Promise<void> {
  return Contacts.deleteContact(contact);
}

async function getContactsMatchingString(str: string): Promise<Contact[]> {
  return Contacts.getContactsMatchingString(str);
}

async function getContactsByPhoneNumber(
  phoneNumber: string
): Promise<Contact[]> {
  return Contacts.getContactsByPhoneNumber(phoneNumber);
}

async function getContactsByEmailAddress(
  emailAddress: string
): Promise<Contact[]> {
  return Contacts.getContactsByEmailAddress(emailAddress);
}

async function checkPermission(): Promise<PermissionType> {
  return Contacts.checkPermission();
}

async function requestPermission(): Promise<PermissionType> {
  return Contacts.requestPermission();
}

async function writePhotoToPath(
  contactId: string,
  file: string
): Promise<boolean> {
  return Contacts.writePhotoToPath(contactId, file);
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
};
