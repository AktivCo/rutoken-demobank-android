package ru.rutoken.Pkcs11;

/* rtpkcs11f.h include file for PKCS #11. */

/*
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

public interface RtPkcs11 extends Pkcs11 {

    /* C_EX_GetFunctionListExtended returns the extended function list. */
    int C_EX_GetFunctionListExtended
    (
        Pointer[]   ppFunctionList /* receives pointer to extended function list */
    );

    /* C_EX_InitToken initializes a token with full format. */
    int C_EX_InitToken
    (
        int                     slotID,    /* ID of the token's slot */
        byte[]                  pPin,      /* the SO's initial PIN */
        int                     ulPinLen,  /* length in bytes of the PIN */
        CK_RUTOKEN_INIT_PARAM   pInitInfo  /* init parameters */
    );

    /* C_EX_GetTokenInfoExtended obtains information about a particular
    * token in the system. */
    int C_EX_GetTokenInfoExtended
    (
        int                     slotID,  /* ID of the token's slot */
        CK_TOKEN_INFO_EXTENDED  pInfo    /* receives the token information */
    );

    /* C_EX_UnblockUserPIN unblock the blocked user's PIN.
     * C_EX_UnblockUserPIN requires same conditions as a
     * C_InitPIN */
    int C_EX_UnblockUserPIN
    (
        int     hSession   /* the session's handle */
    );


    /* C_EX_SetTokenName modifies the token symbol name (label) if
     * User is logged in. C_EX_SetTokenName can only be called in
     * the "R/W User Functions" state.
     */
    int C_EX_SetTokenName
    (
        int     hSession,  /* the session's handle */
        byte[]  pLabel,    /* the new label */
        int     ulLabelLen /* length of the new label */
    );

    /* C_EX_SetLicense modifies the token license if User or SO
     * is logged in. C_EX_SetLicense can only be called in the
     * "R/W User Functions" state or "R/W SO Functions" state.
     */
    int C_EX_SetLicense
    (
        int     hSession,     /* the session's handle */
        int     ulLicenseNum, /* the number of the new license, can only be 1 or 2 */
        byte[]  pLicense,     /* byte buffer with the data of new license */
        int     ulLicenseLen  /* length of the new license, can only be 72 */
    );


    /* C_EX_GetLicense read the token license. C_EX_GetLicense
     * can be called in the every state.
     * pulLicenseLen [in/out] - [in]- sets license length, can only be 72
     *                          [out] - gets license length (if pLicense is NULL_PTR)
     */
    int C_EX_GetLicense
    (
        int             hSession,     /* the session's handle */
        int             ulLicenseNum, /* the number of the license, can only be 1 or 2 */
        byte[]          pLicense,     /* receives the license */
        IntByReference  pulLicenseLen /* length of the license */
    );


    /* C_EX_GetCertificateInfoText get text information about
     * certificate. C_EX_GetCertificateInfoText can be called
     * in the every state.
     */
    int C_EX_GetCertificateInfoText
    (
        int             hSession,  /* the session's handle */
        int             hCert,     /* the object's handle */
        Pointer         pInfo,     /* return address of allocated buffer with text information */
        IntByReference  pulInfoLen /* length of the allocated buffer */
    );

    /* C_EX_PKCS7Sign sign data and pack it to PKCS#7 format
     * certificate. C_EX_PKCS7Sign can only be called in the
     * "R/W User Functions" or "R User Functions" state.
     */
    int C_EX_PKCS7Sign
    (
        int             hSession,
        byte[]          pData,
        int             ulDataLen,
        int             hCert,
        Pointer         ppEnvelope,
        IntByReference  pEnvelopeLen,
        int             hPrivKey,
        int             phCertificates,
        int             ulCertificatesLen,
        int             flags
    );


    /* C_EX_CreateCSR create request by certificate and pack it in
     * PKCS#10 format. C_EX_CreateCSR can only be called in the
     * "R/W User Functions" or "R User Functions" state.
     */
    int C_EX_CreateCSR
    (
        int             hSession,
        int             hPublicKey,
        String[]        dn,
        int             dnLength,
        Pointer         pCsr,
        IntByReference  pulCsrLength,
        int             hPrivKey,
        int             pAttributes,
        int             ulAttributesLength,
        String[]        pExtensions,
        int             ulExtensionsLength
    );


    /* C_EX_FreeBuffer free buffer, allocated in extended functions.
     */
    int C_EX_FreeBuffer
    (
        Pointer          pBuffer /* pointer to the buffer */
    );


    /* C_EX_GetTokenName return the token symbol name (label).
     */
    int C_EX_GetTokenName
    (
        int             hSession,   /* the session's handle */
        byte[]          pLabel,     /* byte buffer for label */
        IntByReference  pulLabelLen /* length of the label */
    );


    /* C_EX_SetLocalPIN modifies the local PIN for devices which supported it.
     */
    int C_EX_SetLocalPIN
    (
        int     slotID,           /* ID of the token's slot */
        byte[]  pUserPin,         /* the current User PIN */
        int     ulUserPinLen,     /* length of current User PIN */
        byte[]  pNewLocalPin,     /* the new local PIN */
        int     ulNewLocalPinLen, /* length of the new local PIN */
        int     ulLocalID         /* ID of the local PIN */
    );


    /* C_EX_LoadActivationKey */
    int C_EX_LoadActivationKey
    (
        int     hSession, /* the session's handle */
        byte[]  key,
        int     keySize
    );

    /* C_EX_SetActivationPassword */
    int C_EX_SetActivationPassword
    (
        int     slotID,  /* ID of the token's slot */
        byte[]  password
    );
}
