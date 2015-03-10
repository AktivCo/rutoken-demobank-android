/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.Pkcs11;
/* Derived from pkcs11f.h include file for PKCS #11. */

/* License to copy and use this software is granted provided that it is
 * identified as "RSA Security Inc. PKCS #11 Cryptographic Token Interface
 * (Cryptoki)" in all material mentioning or referencing this software.

 * License is also granted to make and use derivative works provided that
 * such works are identified as "derived from the RSA Security Inc. PKCS #11
 * Cryptographic Token Interface (Cryptoki)" in all material mentioning or 
 * referencing the derived work.

 * RSA Security Inc. makes no representations concerning either the 
 * merchantability of this software or the suitability of this software for
 * any particular purpose. It is provided "as is" without express or implied
 * warranty of any kind.
 */

/* This header file contains pretty much everything about all the */
/* Cryptoki function prototypes.  Because this information is */
/* used for more than just declaring function prototypes, the */
/* order of the functions appearing herein is important, and */
/* should not be altered. */

/*
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Callback;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.NativeLongByReference;

public interface Pkcs11 extends Library {
/* General-purpose */

    /* C_Initialize initializes the Cryptoki library. */
    NativeLong C_Initialize
    (
            CK_C_INITIALIZE_ARGS   pInitArgs  /* if this is not NULL_PTR, it gets
                            * cast to CK_C_INITIALIZE_ARGS_PTR
                            * and dereferenced */
    );
    /* C_Finalize indicates that an application is done with the
     * Cryptoki library. */
    NativeLong C_Finalize
    (
            Pointer   pReserved  /* reserved.  Should be NULL_PTR */
    );
    /* C_GetInfo returns general information about Cryptoki. */
    NativeLong C_GetInfo
    (
            CK_INFO   pInfo  /* location that receives information */
    );
    /* C_GetFunctionList returns the function list. */
    NativeLong C_GetFunctionList
    (
            Pointer[] ppFunctionList  /* receives pointer to
                                            * function list */
    );

/* Slot and token management */

    /* C_GetSlotList obtains a list of slots in the system. */
    NativeLong C_GetSlotList
    (
            boolean       tokenPresent,  /* only slots with tokens? */
            NativeLong[]         pSlotList,     /* receives array of slot IDs */
            NativeLongByReference   pulCount       /* receives number of slots */
    );
    /* C_GetSlotInfo obtains information about a particular slot in
     * the system. */
    NativeLong C_GetSlotInfo
    (
            NativeLong       slotID,  /* the ID of the slot */
            CK_SLOT_INFO pInfo    /* receives the slot information */
    );
    /* C_GetTokenInfo obtains information about a particular token
     * in the system. */
    NativeLong C_GetTokenInfo
    (
            NativeLong        slotID,  /* ID of the token's slot */
            CK_TOKEN_INFO pInfo    /* receives the token information */
    );
    /* C_GetMechanismList obtains a list of mechanism types
     * supported by a token. */
    NativeLong C_GetMechanismList
    (
            NativeLong            slotID,          /* ID of token's slot */
            NativeLong[] pMechanismList,  /* gets mech. array */
            NativeLongByReference          pulCount         /* gets # of mechs. */
    );
    /* C_GetMechanismInfo obtains information about a particular
     * mechanism possibly supported by a token. */
    NativeLong C_GetMechanismInfo
    (
            NativeLong            slotID,  /* ID of the token's slot */
            NativeLong            type,    /* type of mechanism */
            CK_MECHANISM_INFO pInfo    /* receives mechanism info */
    );
    /* C_InitToken initializes a token. */
/* pLabel changed from CK_CHAR_PTR to byte[] for v2.10 */
    NativeLong C_InitToken
    (
            NativeLong      slotID,    /* ID of the token's slot */
            byte[] pPin,      /* the SO's initial PIN */
            NativeLong        ulPinLen,  /* length in bytes of the PIN */
            byte[] pLabel     /* 32-byte token label (blank padded) */
    );
    /* C_InitPIN initializes the normal user's PIN. */
    NativeLong C_InitPIN
    (
            NativeLong hSession,  /* the session's handle */
            byte[]   pPin,      /* the normal user's PIN */
            NativeLong          ulPinLen   /* length in bytes of the PIN */
    );
    /* C_SetPIN modifies the PIN of the user who is logged in. */
    NativeLong C_SetPIN
    (
            NativeLong hSession,  /* the session's handle */
            byte[]   pOldPin,   /* the old PIN */
            NativeLong          ulOldLen,  /* length of the old PIN */
            byte[]   pNewPin,   /* the new PIN */
            NativeLong          ulNewLen   /* length of the new PIN */
    );

/* Session management */

    /* C_OpenSession opens a session between an application and a
     * token. */
    NativeLong C_OpenSession
    (
            NativeLong            slotID,        /* the slot's ID */
            NativeLong              flags,         /* from CK_SESSION_INFO */
            Pointer           pApplication,  /* passed to callback */
            Callback             Notify,        /* callback function */
            NativeLongByReference phSession      /* gets session handle */
    );
    /* C_CloseSession closes a session between an application and a
     * token. */
    NativeLong C_CloseSession
    (
            NativeLong hSession  /* the session's handle */
    );
    /* C_CloseAllSessions closes all sessions with a token. */
    NativeLong C_CloseAllSessions
    (
            NativeLong     slotID  /* the token's slot */
    );
    /* C_GetSessionInfo obtains information about the session. */
    NativeLong C_GetSessionInfo
    (
            NativeLong   hSession,  /* the session's handle */
            CK_SESSION_INFO pInfo      /* receives session info */
    );
    /* C_GetOperationState obtains the state of the cryptographic operation
     * in a session. */
    NativeLong C_GetOperationState
    (
            NativeLong hSession,             /* session's handle */
            byte[] pOperationState,      /* gets state */
            NativeLongByReference      pulOperationStateLen  /* gets state length */
    );
    /* C_SetOperationState restores the state of the cryptographic
     * operation in a session. */
    NativeLong C_SetOperationState
    (
            NativeLong hSession,            /* session's handle */
            byte[]      pOperationState,      /* holds state */
            NativeLong         ulOperationStateLen,  /* holds state length */
            NativeLong hEncryptionKey,       /* en/decryption key */
            NativeLong hAuthenticationKey    /* sign/verify key */
    );
    /* C_Login logs a user into a token. */
    NativeLong C_Login
    (
            NativeLong hSession,  /* the session's handle */
            NativeLong      userType,  /* the user type */
            byte[]   pPin,      /* the user's PIN */
            NativeLong          ulPinLen   /* the length of the PIN */
    );
    /* C_Logout logs a user out from a token. */
    NativeLong C_Logout
    (
            NativeLong hSession  /* the session's handle */
    );

/* Object management */

    /* C_CreateObject creates a new object. */
    NativeLong C_CreateObject
    (
            NativeLong hSession,    /* the session's handle */
            CK_ATTRIBUTE  pTemplate,   /* the object's template */
            NativeLong          ulCount,     /* attributes in template */
            NativeLongByReference phObject  /* gets new object's handle. */
    );
    /* C_CopyObject copies an object, creating a new object for the
     * copy. */
    NativeLong C_CopyObject
    (
            NativeLong    hSession,    /* the session's handle */
            NativeLong     hObject,     /* the object's handle */
            CK_ATTRIBUTE     pTemplate,   /* template for new object */
            NativeLong             ulCount,     /* attributes in template */
            NativeLongByReference phNewObject  /* receives handle of copy */
    );
    /* C_DestroyObject destroys an object. */
    NativeLong C_DestroyObject
    (
            NativeLong hSession,  /* the session's handle */
            NativeLong  hObject    /* the object's handle */
    );
    /* C_GetObjectSize gets the size of an object in bytes. */
    NativeLong C_GetObjectSize
    (
            NativeLong hSession,  /* the session's handle */
            NativeLong  hObject,   /* the object's handle */
            NativeLongByReference      pulSize    /* receives size of object */
    );
    /* C_GetAttributeValue obtains the value of one or more object
     * attributes. */
    NativeLong C_GetAttributeValue
    (
            NativeLong hSession,   /* the session's handle */
            NativeLong  hObject,    /* the object's handle */
            CK_ATTRIBUTE[]  pTemplate,  /* specifies attrs; gets vals */
            NativeLong          ulCount     /* attributes in template */
    );
    /* C_SetAttributeValue modifies the value of one or more object
     * attributes */
    NativeLong C_SetAttributeValue
    (
            NativeLong hSession,   /* the session's handle */
            NativeLong  hObject,    /* the object's handle */
            CK_ATTRIBUTE[]  pTemplate,  /* specifies attrs and values */
            NativeLong          ulCount     /* attributes in template */
    );
    /* C_FindObjectsInit initializes a search for token and session
     * objects that match a template. */
    NativeLong C_FindObjectsInit
    (
            NativeLong hSession,   /* the session's handle */
            CK_ATTRIBUTE[]  pTemplate,  /* attribute values to match */
            NativeLong          ulCount     /* attrs in search template */
    );
    /* C_FindObjects continues a search for token and session
     * objects that match a template, obtaining additional object
     * handles. */
    NativeLong C_FindObjects
    (
            NativeLong    hSession,          /* session's handle */
            NativeLong[] phObject,          /* gets obj. handles */
            NativeLong             ulMaxObjectCount,  /* max handles to get */
            NativeLongByReference         pulObjectCount     /* actual # returned */
    );
    /* C_FindObjectsFinal finishes a search for token and session
     * objects. */
    NativeLong C_FindObjectsFinal
    (
            NativeLong hSession  /* the session's handle */
    );

/* Encryption and decryption */

    /* C_EncryptInit initializes an encryption operation. */
    NativeLong C_EncryptInit
    (
            NativeLong hSession,    /* the session's handle */
            CK_MECHANISM  pMechanism,  /* the encryption mechanism */
            NativeLong  hKey         /* handle of encryption key */
    );
    /* C_Encrypt encrypts single-part data. */
    NativeLong C_Encrypt
    (
            NativeLong hSession,            /* session's handle */
            byte[]       pData,               /* the plaintext data */
            NativeLong          ulDataLen,           /* bytes of plaintext */
            byte[]       pEncryptedData,      /* gets ciphertext */
            NativeLongByReference      pulEncryptedDataLen  /* gets c-text size */
    );
    /* C_EncryptUpdate continues a multiple-part encryption
     * operation. */
    NativeLong C_EncryptUpdate
    (
            NativeLong hSession,           /* session's handle */
            byte[]       pPart,              /* the plaintext data */
            NativeLong          ulPartLen,          /* plaintext data len */
            byte[]       pEncryptedPart,     /* gets ciphertext */
            NativeLongByReference      pulEncryptedPartLen /* gets c-text size */
    );
    /* C_EncryptFinal finishes a multiple-part encryption
     * operation. */
    NativeLong C_EncryptFinal
    (
            NativeLong hSession,                /* session handle */
            byte[]       pLastEncryptedPart,      /* last c-text */
            NativeLongByReference      pulLastEncryptedPartLen  /* gets last size */
    );
    /* C_DecryptInit initializes a decryption operation. */
    NativeLong C_DecryptInit
    (
            NativeLong hSession,    /* the session's handle */
            CK_MECHANISM  pMechanism,  /* the decryption mechanism */
            NativeLong  hKey         /* handle of decryption key */
    );
    /* C_Decrypt decrypts encrypted data in a single part. */
    NativeLong C_Decrypt
    (
            NativeLong hSession,           /* session's handle */
            byte[]       pEncryptedData,     /* ciphertext */
            NativeLong          ulEncryptedDataLen, /* ciphertext length */
            byte[]       pData,              /* gets plaintext */
            NativeLongByReference      pulDataLen          /* gets p-text size */
    );
    /* C_DecryptUpdate continues a multiple-part decryption
     * operation. */
    NativeLong C_DecryptUpdate
    (
            NativeLong hSession,            /* session's handle */
            byte[]       pEncryptedPart,      /* encrypted data */
            NativeLong          ulEncryptedPartLen,  /* input length */
            byte[]       pPart,               /* gets plaintext */
            NativeLongByReference      pulPartLen           /* p-text size */
    );
    /* C_DecryptFinal finishes a multiple-part decryption
     * operation. */
    NativeLong C_DecryptFinal
    (
            NativeLong hSession,       /* the session's handle */
            byte[]       pLastPart,      /* gets plaintext */
            NativeLongByReference      pulLastPartLen  /* p-text size */
    );

/* Message digesting */

    /* C_DigestInit initializes a message-digesting operation. */
    NativeLong C_DigestInit
    (
            NativeLong hSession,   /* the session's handle */
            CK_MECHANISM  pMechanism  /* the digesting mechanism */
    );
    /* C_Digest digests data in a single part. */
    NativeLong C_Digest
    (
            NativeLong hSession,     /* the session's handle */
            byte[]       pData,        /* data to be digested */
            NativeLong          ulDataLen,    /* bytes of data to digest */
            byte[]       pDigest,      /* gets the message digest */
            NativeLongByReference      pulDigestLen  /* gets digest length */
    );
    /* C_DigestUpdate continues a multiple-part message-digesting
     * operation. */
    NativeLong C_DigestUpdate
    (
            NativeLong hSession,  /* the session's handle */
            byte[]       pPart,     /* data to be digested */
            NativeLong          ulPartLen  /* bytes of data to be digested */
    );
    /* C_DigestKey continues a multi-part message-digesting
     * operation, by digesting the value of a secret key as part of
     * the data already digested. */
    NativeLong C_DigestKey
    (
            NativeLong hSession,  /* the session's handle */
            NativeLong  hKey       /* secret key to digest */
    );
    /* C_DigestFinal finishes a multiple-part message-digesting
     * operation. */
    NativeLong C_DigestFinal
    (
            NativeLong hSession,     /* the session's handle */
            byte[]       pDigest,      /* gets the message digest */
            NativeLongByReference      pulDigestLen  /* gets byte count of digest */
    );

/* Signing and MACing */

    /* C_SignInit initializes a signature (private key encryption)
     * operation, where the signature is (will be) an appendix to
     * the data, and plaintext cannot be recovered from the
     *signature. */
    NativeLong C_SignInit
    (
            NativeLong hSession,    /* the session's handle */
            CK_MECHANISM  pMechanism,  /* the signature mechanism */
            NativeLong  hKey         /* handle of signature key */
    );
    /* C_Sign signs (encrypts with private key) data in a single
     * part, where the signature is (will be) an appendix to the
     * data, and plaintext cannot be recovered from the signature. */
    NativeLong C_Sign
    (
            NativeLong hSession,        /* the session's handle */
            byte[]       pData,           /* the data to sign */
            NativeLong          ulDataLen,       /* count of bytes to sign */
            byte[]       pSignature,      /* gets the signature */
            NativeLongByReference      pulSignatureLen  /* gets signature length */
    );
    /* C_SignUpdate continues a multiple-part signature operation,
     * where the signature is (will be) an appendix to the data, 
     * and plaintext cannot be recovered from the signature. */
    NativeLong C_SignUpdate
    (
            NativeLong hSession,  /* the session's handle */
            byte[]       pPart,     /* the data to sign */
            NativeLong          ulPartLen  /* count of bytes to sign */
    );
    /* C_SignFinal finishes a multiple-part signature operation, 
     * returning the signature. */
    NativeLong C_SignFinal
    (
            NativeLong hSession,        /* the session's handle */
            byte[]       pSignature,      /* gets the signature */
            NativeLongByReference      pulSignatureLen  /* gets signature length */
    );
    /* C_SignRecoverInit initializes a signature operation, where
     * the data can be recovered from the signature. */
    NativeLong C_SignRecoverInit
    (
            NativeLong hSession,   /* the session's handle */
            CK_MECHANISM  pMechanism, /* the signature mechanism */
            NativeLong  hKey        /* handle of the signature key */
    );
    /* C_SignRecover signs data in a single operation, where the
     * data can be recovered from the signature. */
    NativeLong C_SignRecover
    (
            NativeLong hSession,        /* the session's handle */
            byte[]       pData,           /* the data to sign */
            NativeLong          ulDataLen,       /* count of bytes to sign */
            byte[]       pSignature,      /* gets the signature */
            NativeLongByReference      pulSignatureLen  /* gets signature length */
    );

/* Verifying signatures and MACs */

    /* C_VerifyInit initializes a verification operation, where the
     * signature is an appendix to the data, and plaintext cannot
     *  cannot be recovered from the signature (e.g. DSA). */
    NativeLong C_VerifyInit
    (
            NativeLong hSession,    /* the session's handle */
            CK_MECHANISM  pMechanism,  /* the verification mechanism */
            NativeLong  hKey         /* verification key */
    );
    /* C_Verify verifies a signature in a single-part operation, 
     * where the signature is an appendix to the data, and plaintext
     * cannot be recovered from the signature. */
    NativeLong C_Verify
    (
            NativeLong hSession,       /* the session's handle */
            byte[]       pData,          /* signed data */
            NativeLong          ulDataLen,      /* length of signed data */
            byte[]       pSignature,     /* signature */
            NativeLong          ulSignatureLen  /* signature length*/
    );
    /* C_VerifyUpdate continues a multiple-part verification
     * operation, where the signature is an appendix to the data, 
     * and plaintext cannot be recovered from the signature. */
    NativeLong C_VerifyUpdate
    (
            NativeLong hSession,  /* the session's handle */
            byte[]       pPart,     /* signed data */
            NativeLong          ulPartLen  /* length of signed data */
    );
    /* C_VerifyFinal finishes a multiple-part verification
     * operation, checking the signature. */
    NativeLong C_VerifyFinal
    (
            NativeLong hSession,       /* the session's handle */
            byte[]       pSignature,     /* signature to verify */
            NativeLong          ulSignatureLen  /* signature length */
    );
    /* C_VerifyRecoverInit initializes a signature verification
     * operation, where the data is recovered from the signature. */
    NativeLong C_VerifyRecoverInit
    (
            NativeLong hSession,    /* the session's handle */
            CK_MECHANISM  pMechanism,  /* the verification mechanism */
            NativeLong  hKey         /* verification key */
    );
    /* C_VerifyRecover verifies a signature in a single-part
     * operation, where the data is recovered from the signature. */
    NativeLong C_VerifyRecover
    (
            NativeLong hSession,        /* the session's handle */
            byte[]       pSignature,      /* signature to verify */
            NativeLong          ulSignatureLen,  /* signature length */
            byte[]       pData,           /* gets signed data */
            NativeLongByReference      pulDataLen       /* gets signed data len */
    );

/* Dual-function cryptographic operations */

    /* C_DigestEncryptUpdate continues a multiple-part digesting
     * and encryption operation. */
    NativeLong C_DigestEncryptUpdate
    (
            NativeLong hSession,            /* session's handle */
            byte[]       pPart,               /* the plaintext data */
            NativeLong          ulPartLen,           /* plaintext length */
            byte[]       pEncryptedPart,      /* gets ciphertext */
            NativeLongByReference      pulEncryptedPartLen  /* gets c-text length */
    );
    /* C_DecryptDigestUpdate continues a multiple-part decryption and
     * digesting operation. */
    NativeLong C_DecryptDigestUpdate
    (
            NativeLong hSession,            /* session's handle */
            byte[]       pEncryptedPart,      /* ciphertext */
            NativeLong          ulEncryptedPartLen,  /* ciphertext length */
            byte[]       pPart,               /* gets plaintext */
            NativeLongByReference      pulPartLen           /* gets plaintext len */
    );
    /* C_SignEncryptUpdate continues a multiple-part signing and
     * encryption operation. */
    NativeLong C_SignEncryptUpdate
    (
            NativeLong hSession,            /* session's handle */
            byte[]       pPart,               /* the plaintext data */
            NativeLong          ulPartLen,           /* plaintext length */
            byte[]       pEncryptedPart,      /* gets ciphertext */
            NativeLongByReference      pulEncryptedPartLen  /* gets c-text length */
    );
    /* C_DecryptVerifyUpdate continues a multiple-part decryption and
     * verify operation. */
    NativeLong C_DecryptVerifyUpdate
    (
            NativeLong hSession,            /* session's handle */
            byte[]       pEncryptedPart,      /* ciphertext */
            NativeLong          ulEncryptedPartLen,  /* ciphertext length */
            byte[]       pPart,               /* gets plaintext */
            NativeLongByReference      pulPartLen           /* gets p-text length */
    );

/* Key management */

    /* C_GenerateKey generates a secret key, creating a new key
     * object. */
    NativeLong C_GenerateKey
    (
            NativeLong    hSession,    /* the session's handle */
            CK_MECHANISM     pMechanism,  /* key generation mech. */
            CK_ATTRIBUTE     pTemplate,   /* template for new key */
            NativeLong             ulCount,     /* # of attrs in template */
            NativeLongByReference phKey        /* gets handle of new key */
    );
    /* C_GenerateKeyPair generates a public-key/private-key pair, 
     * creating new key objects. */
    NativeLong C_GenerateKeyPair
    (
            NativeLong    hSession,                    /* session
                                                     * handle */
            CK_MECHANISM     pMechanism,                  /* key-gen
                                                     * mech. */
            CK_ATTRIBUTE     pPublicKeyTemplate,          /* template
                                                     * for pub.
                                                     * key */
            NativeLong             ulPublicKeyAttributeCount,   /* # pub.
                                                     * attrs. */
            CK_ATTRIBUTE     pPrivateKeyTemplate,         /* template
                                                     * for priv.
                                                     * key */
            NativeLong             ulPrivateKeyAttributeCount,  /* # priv.
                                                     * attrs. */
            NativeLongByReference phPublicKey,                 /* gets pub.
                                                     * key
                                                     * handle */
            NativeLongByReference phPrivateKey                 /* gets
                                                     * priv. key
                                                     * handle */
    );
    /* C_WrapKey wraps (i.e., encrypts) a key. */
    NativeLong C_WrapKey
    (
            NativeLong hSession,        /* the session's handle */
            CK_MECHANISM  pMechanism,      /* the wrapping mechanism */
            NativeLong  hWrappingKey,    /* wrapping key */
            NativeLong  hKey,            /* key to be wrapped */
            byte[]       pWrappedKey,     /* gets wrapped key */
            NativeLongByReference      pulWrappedKeyLen /* gets wrapped key size */
    );
    /* C_UnwrapKey unwraps (decrypts) a wrapped key, creating a new
     * key object. */
    NativeLong C_UnwrapKey
    (
            NativeLong    hSession,          /* session's handle */
            CK_MECHANISM     pMechanism,        /* unwrapping mech. */
            NativeLong     hUnwrappingKey,    /* unwrapping key */
            byte[]          pWrappedKey,       /* the wrapped key */
            NativeLong             ulWrappedKeyLen,   /* wrapped key len */
            CK_ATTRIBUTE     pTemplate,         /* new key template */
            NativeLong             ulAttributeCount,  /* template length */
            NativeLongByReference phKey              /* gets new handle */
    );
    /* C_DeriveKey derives a key from a base key, creating a new key
     * object. */
    NativeLong C_DeriveKey
    (
            NativeLong    hSession,          /* session's handle */
            CK_MECHANISM     pMechanism,        /* key deriv. mech. */
            NativeLong     hBaseKey,          /* base key */
            CK_ATTRIBUTE     pTemplate,         /* new key template */
            NativeLong             ulAttributeCount,  /* template length */
            NativeLongByReference phKey              /* gets new handle */
    );

/* Random number generation */

    /* C_SeedRandom mixes additional seed material into the token's
     * random number generator. */
    NativeLong C_SeedRandom
    (
            NativeLong hSession,  /* the session's handle */
            byte[] pSeed,     /* the seed material */
            NativeLong          ulSeedLen  /* length of seed material */
    );
    /* C_GenerateRandom generates random data. */
    NativeLong C_GenerateRandom
    (
            NativeLong hSession,    /* the session's handle */
            byte[]       RandomData,  /* receives the random data */
            NativeLong          ulRandomLen  /* # of bytes to generate */
    );

/* Parallel function management */

    /* C_GetFunctionStatus is a legacy function; it obtains an
     * updated status of a function running in parallel with an
     * application. */
    NativeLong C_GetFunctionStatus
    (
            NativeLong hSession  /* the session's handle */
    );
    /* C_CancelFunction is a legacy function; it cancels a function
     * running in parallel. */
    NativeLong C_CancelFunction
    (
            NativeLong hSession  /* the session's handle */
    );

/* Functions added in for Cryptoki Version 2.01 or later */

    /* C_WaitForSlotEvent waits for a slot event (token insertion,
     * removal, etc.) to occur. */
    NativeLong C_WaitForSlotEvent
    (
            NativeLong flags,        /* blocking/nonblocking flag */
            NativeLongByReference pSlot,  /* location that receives the slot ID */
            Pointer pReserved   /* reserved.  Should be NULL_PTR */
    );
}

