export function getAll(callback: (error: any, contacts: Contact[]) => void): void;
export function getAllWithoutPhotos(callback: (error: any, contacts: Contact[]) => void): void;
export function getContactById(contactId: string, callback: (error: any, contact: Contact) => void): void;
export function getCount(callback: (count: number) => void): void;
export function getPhotoForId(contactId: string, callback: (error: any, photoUri: string) => void): void;
export function addContact(contact: Contact, callback: (error?: any) => void): void;
export function openContactForm(contact: Contact, callback: (error: any, contact: Contact) => void): void;
export function openExistingContact(contact: Contact, callback: (error: any, contact: Contact) => void): void;
export function updateContact(contact: Contact, callback: (error?: any) => void): void;
export function deleteContact(contact: Contact, callback: (error?: any) => void): void;
export function getContactsMatchingString(str: string, callback: (error: any, contacts: Contact[]) => void): void;
export function getContactsByPhoneNumber(phoneNumber: string, callback: (error: any, contacts: Contact[]) => void): void;
export function checkPermission(callback: (error: any, result: 'authorized' | 'denied' | 'undefined') => void): void;
export function requestPermission(callback: (error: any, result: 'authorized' | 'denied' | 'undefined') => void): void;
export function writePhotoToPath(contactId: string, file: string, callback: (error: any, result: boolean) => void): void;
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
    note: string;
}
