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

export interface UrlAddress {
  url: string;
  label: string;
}

export interface Contact {
  recordID: string;
  backTitle: string;
  company: string | null;
  emailAddresses: EmailAddress[];
  displayName: string;
  familyName: string;
  givenName: string;
  middleName: string;
  jobTitle: string;
  phoneNumbers: PhoneNumber[];
  hasThumbnail: boolean;
  thumbnailPath: string;
  isStarred: boolean;
  postalAddresses: PostalAddress[];
  prefix: string;
  suffix: string;
  department: string;
  birthday: Birthday;
  imAddresses: InstantMessageAddress[];
  urlAddresses: UrlAddress[];
  note: string;
}

export interface Group {
  identifier: string;
  name: string;
}

export type PermissionType = "authorized" | "limited" | "denied" | "undefined";
