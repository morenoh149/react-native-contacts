export function getAll(): Promise<Contact[]>;
export function getAllWithoutPhotos(): Promise<Contact[]>;
export function getContactById(contactId: string): Promise<Contact | null>;
export function getCount(): Promise<number>;
export function getPhotoForId(contactId: string): Promise<string>;
export function addContact(contact: Partial<Contact>): Promise<Contact>;
export function openContactForm(
  contact: Partial<Contact>
): Promise<Contact | null>;
export function openExistingContact(contact: Contact): Promise<Contact>;
export function viewExistingContact(contact: {
  recordID: string;
}): Promise<Contact | void>;
export function editExistingContact(contact: Contact): Promise<Contact>;
export function updateContact(
  contact: Partial<Contact> & { recordID: string }
): Promise<void>;
export function deleteContact(contact: Contact): Promise<void>;
export function getContactsMatchingString(str: string): Promise<Contact[]>;
export function getContactsByPhoneNumber(
  phoneNumber: string
): Promise<Contact[]>;
export function getContactsByEmailAddress(
  emailAddress: string
): Promise<Contact[]>;
export function checkPermission(): Promise<
  "authorized" | "denied" | "undefined"
>;
export function requestPermission(): Promise<
  "authorized" | "denied" | "undefined"
>;
export function writePhotoToPath(
  contactId: string,
  file: string
): Promise<boolean>;
export function iosEnableNotesUsage(enabled: boolean): void;

export function getGroups(): Promise<Group[]>;
export function getGroup(identifier: string): Promise<Group | null>;
export function deleteGroup(identifier: string): Promise<boolean>;
export function updateGroup(
  identifier: string,
  groupData: Pick<Group, "name">
): Promise<Group>;
export function addGroup(group: Pick<Group, "name">): Promise<Group>;
export function contactsInGroup(identifier: string): Promise<Contact[]>;
export function addContactsToGroup(
  groupIdentifier: string,
  contactIdentifiers: string[]
): Promise<boolean>;
export function removeContactsFromGroup(
  groupIdentifier: string,
  contactIdentifiers: string[]
): Promise<boolean>;
export interface Group {
  identifier: string;
  name: string;
}
export interface EmailAddress {
  label: string;
  email: string;
}

export interface PhoneNumber {
  label: string;
  number: string;
}

export interface PostalAddress {
  label: string;
  formattedAddress: string;
  street: string;
  pobox: string;
  neighborhood: string;
  city: string;
  region: string;
  state: string;
  postCode: string;
  country: string;
}

export interface InstantMessageAddress {
  username: string;
  service: string;
}

export interface Birthday {
  day: number;
  month: number;
  year?: number;
}

export interface UrlAddress {
  url: string;
  label: string;
}

export interface Contact {
  recordID: string;
  backTitle: string;
  company: string | null;
  emailAddresses: EmailAddress[];
  displayName: string | null;
  familyName: string;
  givenName: string | null;
  middleName: string;
  jobTitle: string | null;
  phoneNumbers: PhoneNumber[];
  hasThumbnail: boolean;
  thumbnailPath: string;
  isStarred: boolean;
  postalAddresses: PostalAddress[];
  prefix: string | null;
  suffix: string | null;
  department: string | null;
  birthday?: Birthday;
  imAddresses: InstantMessageAddress[];
  urlAddresses: UrlAddress[];
  note: string | null;
}
