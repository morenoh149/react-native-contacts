export function getAll(): Promise<Contact[]>;
export function getAllWithoutPhotos(): Promise<Contact[]>;
export function getContactById(contactId: string): Promise<Contact>;
export function getCount(): Promise<number>;
export function getPhotoForId(contactId: string): Promise<string>;
export function addContact(contact: Contact): Promise<void>;
export function openContactForm(contact: Partial<Contact>): Promise<Contact>;
export function openExistingContact(contact: Contact): Promise<Contact>;
export function editExistingContact(contact: Contact): Promise<Contact>;
export function updateContact(contact: Contact): Promise<void>;
export function deleteContact(contact: Contact): Promise<void>;
export function getContactsMatchingString(str: string): Promise<Contact[]>;
export function getContactsByPhoneNumber(phoneNumber: string): Promise<Contact[]>;
export function checkPermission(): Promise<'authorized' | 'denied' | 'undefined'>;
export function requestPermission(): Promise<'authorized' | 'denied' | 'undefined'>;
export function writePhotoToPath(contactId: string, file: string): Promise<boolean>;
export function iosEnableNotesUsage(enabled: boolean): void;

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
    year: number;
}

export interface Contact {
    recordID: string;
    backTitle: string;
    company: string;
    emailAddresses: EmailAddress[];
    familyName: string;
    givenName: string;
    middleName: string;
    jobTitle: string;
    phoneNumbers: PhoneNumber[];
    hasThumbnail: boolean;
    thumbnailPath: string;
    postalAddresses: PostalAddress[];
    prefix: string;
    suffix: string;
    department: string;
    birthday: Birthday;
    imAddresses: InstantMessageAddress[]
    note: string;
}
