package ru.rutoken.Pkcs11;
/* pkcs11f.h include file for PKCS #11. */
/* $Revision: 1.4 $ */

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
import com.sun.jna.ptr.IntByReference;

public interface Pkcs11 extends Library {
/* General-purpose */

    /* C_Initialize initializes the Cryptoki library. */
    int C_Initialize
    (
            Pointer   pInitArgs  /* if this is not NULL_PTR, it gets
                            * cast to CK_C_INITIALIZE_ARGS_PTR
                            * and dereferenced */
    );
    /* C_Finalize indicates that an application is done with the
     * Cryptoki library. */
    int C_Finalize
    (
            Pointer   pReserved  /* reserved.  Should be NULL_PTR */
    );
    /* C_GetInfo returns general information about Cryptoki. */
    int C_GetInfo
    (
            CK_INFO   pInfo  /* location that receives information */
    );
    /* C_GetFunctionList returns the function list. */
    int C_GetFunctionList
    (
            Pointer[] ppFunctionList  /* receives pointer to
                                            * function list */
    );

/* Slot and token management */

    /* C_GetSlotList obtains a list of slots in the system. */
    int C_GetSlotList
    (
            boolean       tokenPresent,  /* only slots with tokens? */
            int[]         pSlotList,     /* receives array of slot IDs */
            IntByReference   pulCount       /* receives number of slots */
    );
    /* C_GetSlotInfo obtains information about a particular slot in
     * the system. */
    int C_GetSlotInfo
    (
            int       slotID,  /* the ID of the slot */
            CK_SLOT_INFO pInfo    /* receives the slot information */
    );
    /* C_GetTokenInfo obtains information about a particular token
     * in the system. */
    int C_GetTokenInfo
    (
            int        slotID,  /* ID of the token's slot */
            CK_TOKEN_INFO pInfo    /* receives the token information */
    );
    /* C_GetMechanismList obtains a list of mechanism types
     * supported by a token. */
    int C_GetMechanismList
    (
            int            slotID,          /* ID of token's slot */
            int[] pMechanismList,  /* gets mech. array */
            IntByReference          pulCount         /* gets # of mechs. */
    );
    /* C_GetMechanismInfo obtains information about a particular
     * mechanism possibly supported by a token. */
    int C_GetMechanismInfo
    (
            int            slotID,  /* ID of the token's slot */
            int            type,    /* type of mechanism */
            CK_MECHANISM_INFO pInfo    /* receives mechanism info */
    );
    /* C_InitToken initializes a token. */
/* pLabel changed from CK_CHAR_PTR to byte[] for v2.10 */
    int C_InitToken
    (
            int      slotID,    /* ID of the token's slot */
            byte[] pPin,      /* the SO's initial PIN */
            int        ulPinLen,  /* length in bytes of the PIN */
            byte[] pLabel     /* 32-byte token label (blank padded) */
    );
    /* C_InitPIN initializes the normal user's PIN. */
    int C_InitPIN
    (
            int hSession,  /* the session's handle */
            byte[]   pPin,      /* the normal user's PIN */
            int          ulPinLen   /* length in bytes of the PIN */
    );
    /* C_SetPIN modifies the PIN of the user who is logged in. */
    int C_SetPIN
    (
            int hSession,  /* the session's handle */
            byte[]   pOldPin,   /* the old PIN */
            int          ulOldLen,  /* length of the old PIN */
            byte[]   pNewPin,   /* the new PIN */
            int          ulNewLen   /* length of the new PIN */
    );

/* Session management */

    /* C_OpenSession opens a session between an application and a
     * token. */
    int C_OpenSession
    (
            int            slotID,        /* the slot's ID */
            int              flags,         /* from CK_SESSION_INFO */
            Pointer           pApplication,  /* passed to callback */
            Callback             Notify,        /* callback function */
            IntByReference phSession      /* gets session handle */
    );
    /* C_CloseSession closes a session between an application and a
     * token. */
    int C_CloseSession
    (
            int hSession  /* the session's handle */
    );
    /* C_CloseAllSessions closes all sessions with a token. */
    int C_CloseAllSessions
    (
            int     slotID  /* the token's slot */
    );
    /* C_GetSessionInfo obtains information about the session. */
    int C_GetSessionInfo
    (
            int   hSession,  /* the session's handle */
            CK_SESSION_INFO pInfo      /* receives session info */
    );
    /* C_GetOperationState obtains the state of the cryptographic operation
     * in a session. */
    int C_GetOperationState
    (
            int hSession,             /* session's handle */
            byte[] pOperationState,      /* gets state */
            IntByReference      pulOperationStateLen  /* gets state length */
    );
    /* C_SetOperationState restores the state of the cryptographic
     * operation in a session. */
    int C_SetOperationState
    (
            int hSession,            /* session's handle */
            byte[]      pOperationState,      /* holds state */
            int         ulOperationStateLen,  /* holds state length */
            int hEncryptionKey,       /* en/decryption key */
            int hAuthenticationKey    /* sign/verify key */
    );
    /* C_Login logs a user into a token. */
    int C_Login
    (
            int hSession,  /* the session's handle */
            int      userType,  /* the user type */
            byte[]   pPin,      /* the user's PIN */
            int          ulPinLen   /* the length of the PIN */
    );
    /* C_Logout logs a user out from a token. */
    int C_Logout
    (
            int hSession  /* the session's handle */
    );

/* Object management */

    /* C_CreateObject creates a new object. */
    int C_CreateObject
    (
            int hSession,    /* the session's handle */
            CK_ATTRIBUTE  pTemplate,   /* the object's template */
            int          ulCount,     /* attributes in template */
            IntByReference phObject  /* gets new object's handle. */
    );
    /* C_CopyObject copies an object, creating a new object for the
     * copy. */
    int C_CopyObject
    (
            int    hSession,    /* the session's handle */
            int     hObject,     /* the object's handle */
            CK_ATTRIBUTE     pTemplate,   /* template for new object */
            int             ulCount,     /* attributes in template */
            IntByReference phNewObject  /* receives handle of copy */
    );
    /* C_DestroyObject destroys an object. */
    int C_DestroyObject
    (
            int hSession,  /* the session's handle */
            int  hObject    /* the object's handle */
    );
    /* C_GetObjectSize gets the size of an object in bytes. */
    int C_GetObjectSize
    (
            int hSession,  /* the session's handle */
            int  hObject,   /* the object's handle */
            IntByReference      pulSize    /* receives size of object */
    );
    /* C_GetAttributeValue obtains the value of one or more object
     * attributes. */
    int C_GetAttributeValue
    (
            int hSession,   /* the session's handle */
            int  hObject,    /* the object's handle */
            CK_ATTRIBUTE[]  pTemplate,  /* specifies attrs; gets vals */
            int          ulCount     /* attributes in template */
    );
    /* C_SetAttributeValue modifies the value of one or more object
     * attributes */
    int C_SetAttributeValue
    (
            int hSession,   /* the session's handle */
            int  hObject,    /* the object's handle */
            CK_ATTRIBUTE[]  pTemplate,  /* specifies attrs and values */
            int          ulCount     /* attributes in template */
    );
    /* C_FindObjectsInit initializes a search for token and session
     * objects that match a template. */
    int C_FindObjectsInit
    (
            int hSession,   /* the session's handle */
            CK_ATTRIBUTE[]  pTemplate,  /* attribute values to match */
            int          ulCount     /* attrs in search template */
    );
    /* C_FindObjects continues a search for token and session
     * objects that match a template, obtaining additional object
     * handles. */
    int C_FindObjects
    (
            int    hSession,          /* session's handle */
            int[] phObject,          /* gets obj. handles */
            int             ulMaxObjectCount,  /* max handles to get */
            IntByReference         pulObjectCount     /* actual # returned */
    );
    /* C_FindObjectsFinal finishes a search for token and session
     * objects. */
    int C_FindObjectsFinal
    (
            int hSession  /* the session's handle */
    );

/* Encryption and decryption */

    /* C_EncryptInit initializes an encryption operation. */
    int C_EncryptInit
    (
            int hSession,    /* the session's handle */
            CK_MECHANISM  pMechanism,  /* the encryption mechanism */
            int  hKey         /* handle of encryption key */
    );
    /* C_Encrypt encrypts single-part data. */
    int C_Encrypt
    (
            int hSession,            /* session's handle */
            byte[]       pData,               /* the plaintext data */
            int          ulDataLen,           /* bytes of plaintext */
            byte[]       pEncryptedData,      /* gets ciphertext */
            IntByReference      pulEncryptedDataLen  /* gets c-text size */
    );
    /* C_EncryptUpdate continues a multiple-part encryption
     * operation. */
    int C_EncryptUpdate
    (
            int hSession,           /* session's handle */
            byte[]       pPart,              /* the plaintext data */
            int          ulPartLen,          /* plaintext data len */
            byte[]       pEncryptedPart,     /* gets ciphertext */
            IntByReference      pulEncryptedPartLen /* gets c-text size */
    );
    /* C_EncryptFinal finishes a multiple-part encryption
     * operation. */
    int C_EncryptFinal
    (
            int hSession,                /* session handle */
            byte[]       pLastEncryptedPart,      /* last c-text */
            IntByReference      pulLastEncryptedPartLen  /* gets last size */
    );
    /* C_DecryptInit initializes a decryption operation. */
    int C_DecryptInit
    (
            int hSession,    /* the session's handle */
            CK_MECHANISM  pMechanism,  /* the decryption mechanism */
            int  hKey         /* handle of decryption key */
    );
    /* C_Decrypt decrypts encrypted data in a single part. */
    int C_Decrypt
    (
            int hSession,           /* session's handle */
            byte[]       pEncryptedData,     /* ciphertext */
            int          ulEncryptedDataLen, /* ciphertext length */
            byte[]       pData,              /* gets plaintext */
            IntByReference      pulDataLen          /* gets p-text size */
    );
    /* C_DecryptUpdate continues a multiple-part decryption
     * operation. */
    int C_DecryptUpdate
    (
            int hSession,            /* session's handle */
            byte[]       pEncryptedPart,      /* encrypted data */
            int          ulEncryptedPartLen,  /* input length */
            byte[]       pPart,               /* gets plaintext */
            IntByReference      pulPartLen           /* p-text size */
    );
    /* C_DecryptFinal finishes a multiple-part decryption
     * operation. */
    int C_DecryptFinal
    (
            int hSession,       /* the session's handle */
            byte[]       pLastPart,      /* gets plaintext */
            IntByReference      pulLastPartLen  /* p-text size */
    );

/* Message digesting */

    /* C_DigestInit initializes a message-digesting operation. */
    int C_DigestInit
    (
            int hSession,   /* the session's handle */
            CK_MECHANISM  pMechanism  /* the digesting mechanism */
    );
    /* C_Digest digests data in a single part. */
    int C_Digest
    (
            int hSession,     /* the session's handle */
            byte[]       pData,        /* data to be digested */
            int          ulDataLen,    /* bytes of data to digest */
            byte[]       pDigest,      /* gets the message digest */
            IntByReference      pulDigestLen  /* gets digest length */
    );
    /* C_DigestUpdate continues a multiple-part message-digesting
     * operation. */
    int C_DigestUpdate
    (
            int hSession,  /* the session's handle */
            byte[]       pPart,     /* data to be digested */
            int          ulPartLen  /* bytes of data to be digested */
    );
    /* C_DigestKey continues a multi-part message-digesting
     * operation, by digesting the value of a secret key as part of
     * the data already digested. */
    int C_DigestKey
    (
            int hSession,  /* the session's handle */
            int  hKey       /* secret key to digest */
    );
    /* C_DigestFinal finishes a multiple-part message-digesting
     * operation. */
    int C_DigestFinal
    (
            int hSession,     /* the session's handle */
            byte[]       pDigest,      /* gets the message digest */
            IntByReference      pulDigestLen  /* gets byte count of digest */
    );

/* Signing and MACing */

    /* C_SignInit initializes a signature (private key encryption)
     * operation, where the signature is (will be) an appendix to
     * the data, and plaintext cannot be recovered from the
     *signature. */
    int C_SignInit
    (
            int hSession,    /* the session's handle */
            CK_MECHANISM  pMechanism,  /* the signature mechanism */
            int  hKey         /* handle of signature key */
    );
    /* C_Sign signs (encrypts with private key) data in a single
     * part, where the signature is (will be) an appendix to the
     * data, and plaintext cannot be recovered from the signature. */
    int C_Sign
    (
            int hSession,        /* the session's handle */
            byte[]       pData,           /* the data to sign */
            int          ulDataLen,       /* count of bytes to sign */
            byte[]       pSignature,      /* gets the signature */
            IntByReference      pulSignatureLen  /* gets signature length */
    );
    /* C_SignUpdate continues a multiple-part signature operation,
     * where the signature is (will be) an appendix to the data, 
     * and plaintext cannot be recovered from the signature. */
    int C_SignUpdate
    (
            int hSession,  /* the session's handle */
            byte[]       pPart,     /* the data to sign */
            int          ulPartLen  /* count of bytes to sign */
    );
    /* C_SignFinal finishes a multiple-part signature operation, 
     * returning the signature. */
    int C_SignFinal
    (
            int hSession,        /* the session's handle */
            byte[]       pSignature,      /* gets the signature */
            IntByReference      pulSignatureLen  /* gets signature length */
    );
    /* C_SignRecoverInit initializes a signature operation, where
     * the data can be recovered from the signature. */
    int C_SignRecoverInit
    (
            int hSession,   /* the session's handle */
            CK_MECHANISM  pMechanism, /* the signature mechanism */
            int  hKey        /* handle of the signature key */
    );
    /* C_SignRecover signs data in a single operation, where the
     * data can be recovered from the signature. */
    int C_SignRecover
    (
            int hSession,        /* the session's handle */
            byte[]       pData,           /* the data to sign */
            int          ulDataLen,       /* count of bytes to sign */
            byte[]       pSignature,      /* gets the signature */
            IntByReference      pulSignatureLen  /* gets signature length */
    );

/* Verifying signatures and MACs */

    /* C_VerifyInit initializes a verification operation, where the
     * signature is an appendix to the data, and plaintext cannot
     *  cannot be recovered from the signature (e.g. DSA). */
    int C_VerifyInit
    (
            int hSession,    /* the session's handle */
            CK_MECHANISM  pMechanism,  /* the verification mechanism */
            int  hKey         /* verification key */
    );
    /* C_Verify verifies a signature in a single-part operation, 
     * where the signature is an appendix to the data, and plaintext
     * cannot be recovered from the signature. */
    int C_Verify
    (
            int hSession,       /* the session's handle */
            byte[]       pData,          /* signed data */
            int          ulDataLen,      /* length of signed data */
            byte[]       pSignature,     /* signature */
            int          ulSignatureLen  /* signature length*/
    );
    /* C_VerifyUpdate continues a multiple-part verification
     * operation, where the signature is an appendix to the data, 
     * and plaintext cannot be recovered from the signature. */
    int C_VerifyUpdate
    (
            int hSession,  /* the session's handle */
            byte[]       pPart,     /* signed data */
            int          ulPartLen  /* length of signed data */
    );
    /* C_VerifyFinal finishes a multiple-part verification
     * operation, checking the signature. */
    int C_VerifyFinal
    (
            int hSession,       /* the session's handle */
            byte[]       pSignature,     /* signature to verify */
            int          ulSignatureLen  /* signature length */
    );
    /* C_VerifyRecoverInit initializes a signature verification
     * operation, where the data is recovered from the signature. */
    int C_VerifyRecoverInit
    (
            int hSession,    /* the session's handle */
            CK_MECHANISM  pMechanism,  /* the verification mechanism */
            int  hKey         /* verification key */
    );
    /* C_VerifyRecover verifies a signature in a single-part
     * operation, where the data is recovered from the signature. */
    int C_VerifyRecover
    (
            int hSession,        /* the session's handle */
            byte[]       pSignature,      /* signature to verify */
            int          ulSignatureLen,  /* signature length */
            byte[]       pData,           /* gets signed data */
            IntByReference      pulDataLen       /* gets signed data len */
    );

/* Dual-function cryptographic operations */

    /* C_DigestEncryptUpdate continues a multiple-part digesting
     * and encryption operation. */
    int C_DigestEncryptUpdate
    (
            int hSession,            /* session's handle */
            byte[]       pPart,               /* the plaintext data */
            int          ulPartLen,           /* plaintext length */
            byte[]       pEncryptedPart,      /* gets ciphertext */
            IntByReference      pulEncryptedPartLen  /* gets c-text length */
    );
    /* C_DecryptDigestUpdate continues a multiple-part decryption and
     * digesting operation. */
    int C_DecryptDigestUpdate
    (
            int hSession,            /* session's handle */
            byte[]       pEncryptedPart,      /* ciphertext */
            int          ulEncryptedPartLen,  /* ciphertext length */
            byte[]       pPart,               /* gets plaintext */
            IntByReference      pulPartLen           /* gets plaintext len */
    );
    /* C_SignEncryptUpdate continues a multiple-part signing and
     * encryption operation. */
    int C_SignEncryptUpdate
    (
            int hSession,            /* session's handle */
            byte[]       pPart,               /* the plaintext data */
            int          ulPartLen,           /* plaintext length */
            byte[]       pEncryptedPart,      /* gets ciphertext */
            IntByReference      pulEncryptedPartLen  /* gets c-text length */
    );
    /* C_DecryptVerifyUpdate continues a multiple-part decryption and
     * verify operation. */
    int C_DecryptVerifyUpdate
    (
            int hSession,            /* session's handle */
            byte[]       pEncryptedPart,      /* ciphertext */
            int          ulEncryptedPartLen,  /* ciphertext length */
            byte[]       pPart,               /* gets plaintext */
            IntByReference      pulPartLen           /* gets p-text length */
    );

/* Key management */

    /* C_GenerateKey generates a secret key, creating a new key
     * object. */
    int C_GenerateKey
    (
            int    hSession,    /* the session's handle */
            CK_MECHANISM     pMechanism,  /* key generation mech. */
            CK_ATTRIBUTE     pTemplate,   /* template for new key */
            int             ulCount,     /* # of attrs in template */
            IntByReference phKey        /* gets handle of new key */
    );
    /* C_GenerateKeyPair generates a public-key/private-key pair, 
     * creating new key objects. */
    int C_GenerateKeyPair
    (
            int    hSession,                    /* session
                                                     * handle */
            CK_MECHANISM     pMechanism,                  /* key-gen
                                                     * mech. */
            CK_ATTRIBUTE     pPublicKeyTemplate,          /* template
                                                     * for pub.
                                                     * key */
            int             ulPublicKeyAttributeCount,   /* # pub.
                                                     * attrs. */
            CK_ATTRIBUTE     pPrivateKeyTemplate,         /* template
                                                     * for priv.
                                                     * key */
            int             ulPrivateKeyAttributeCount,  /* # priv.
                                                     * attrs. */
            IntByReference phPublicKey,                 /* gets pub.
                                                     * key
                                                     * handle */
            IntByReference phPrivateKey                 /* gets
                                                     * priv. key
                                                     * handle */
    );
    /* C_WrapKey wraps (i.e., encrypts) a key. */
    int C_WrapKey
    (
            int hSession,        /* the session's handle */
            CK_MECHANISM  pMechanism,      /* the wrapping mechanism */
            int  hWrappingKey,    /* wrapping key */
            int  hKey,            /* key to be wrapped */
            byte[]       pWrappedKey,     /* gets wrapped key */
            IntByReference      pulWrappedKeyLen /* gets wrapped key size */
    );
    /* C_UnwrapKey unwraps (decrypts) a wrapped key, creating a new
     * key object. */
    int C_UnwrapKey
    (
            int    hSession,          /* session's handle */
            CK_MECHANISM     pMechanism,        /* unwrapping mech. */
            int     hUnwrappingKey,    /* unwrapping key */
            byte[]          pWrappedKey,       /* the wrapped key */
            int             ulWrappedKeyLen,   /* wrapped key len */
            CK_ATTRIBUTE     pTemplate,         /* new key template */
            int             ulAttributeCount,  /* template length */
            IntByReference phKey              /* gets new handle */
    );
    /* C_DeriveKey derives a key from a base key, creating a new key
     * object. */
    int C_DeriveKey
    (
            int    hSession,          /* session's handle */
            CK_MECHANISM     pMechanism,        /* key deriv. mech. */
            int     hBaseKey,          /* base key */
            CK_ATTRIBUTE     pTemplate,         /* new key template */
            int             ulAttributeCount,  /* template length */
            IntByReference phKey              /* gets new handle */
    );

/* Random number generation */

    /* C_SeedRandom mixes additional seed material into the token's
     * random number generator. */
    int C_SeedRandom
    (
            int hSession,  /* the session's handle */
            byte[] pSeed,     /* the seed material */
            int          ulSeedLen  /* length of seed material */
    );
    /* C_GenerateRandom generates random data. */
    int C_GenerateRandom
    (
            int hSession,    /* the session's handle */
            byte[]       RandomData,  /* receives the random data */
            int          ulRandomLen  /* # of bytes to generate */
    );

/* Parallel function management */

    /* C_GetFunctionStatus is a legacy function; it obtains an
     * updated status of a function running in parallel with an
     * application. */
    int C_GetFunctionStatus
    (
            int hSession  /* the session's handle */
    );
    /* C_CancelFunction is a legacy function; it cancels a function
     * running in parallel. */
    int C_CancelFunction
    (
            int hSession  /* the session's handle */
    );

/* Functions added in for Cryptoki Version 2.01 or later */

    /* C_WaitForSlotEvent waits for a slot event (token insertion,
     * removal, etc.) to occur. */
    int C_WaitForSlotEvent
    (
            int flags,        /* blocking/nonblocking flag */
            IntByReference pSlot,  /* location that receives the slot ID */
            Pointer pReserved   /* reserved.  Should be NULL_PTR */
    );
}
