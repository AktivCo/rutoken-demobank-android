/*
 * Copyright (c) 2016, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.Pkcs11;
/* Derived from rtpkcs11f.h include file for PKCS #11. */

/*
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.NativeLongByReference;

public interface RtPkcs11 extends Pkcs11 {

    /* C_EX_GetFunctionListExtended returns the extended function list. */
    NativeLong C_EX_GetFunctionListExtended
    (
        Pointer[]   ppFunctionList /* receives pointer to extended function list */
    );

    /* C_EX_InitToken initializes a token with full format. */
    NativeLong C_EX_InitToken
    (
        NativeLong            slotID,    /* ID of the token's slot */
        byte[]                pPin,      /* the SO's initial PIN */
        NativeLong            ulPinLen,  /* length in bytes of the PIN */
        CK_RUTOKEN_INIT_PARAM pInitInfo  /* init parameters */
    );

    /* C_EX_GetTokenInfoExtended obtains information about the particular
    * token in the system. */
    NativeLong C_EX_GetTokenInfoExtended
    (
        NativeLong              slotID,  /* ID of the token's slot */
        CK_TOKEN_INFO_EXTENDED  pInfo    /* receives the token information */
    );

    /* C_EX_UnblockUserPIN unblocks the blocked User's PIN.
     * C_EX_UnblockUserPIN requires same conditions as a
     * C_InitPIN */
    NativeLong C_EX_UnblockUserPIN
    (
        NativeLong hSession   /* the session's handle */
    );


    /* C_EX_SetTokenName modifies the token symbol name (label) if
     * User is logged in. C_EX_SetTokenName can only be called in
     * the "R/W User Functions" state.
     */
    NativeLong C_EX_SetTokenName
    (
        NativeLong hSession,  /* the session's handle */
        byte[]     pLabel,    /* the new label */
        NativeLong ulLabelLen /* length of the new label */
    );

    /* C_EX_SetLicense modifies the token license if User or SO
     * is logged in. C_EX_SetLicense can only be called in the
     * "R/W User Functions" state or "R/W SO Functions" state.
     */
    NativeLong C_EX_SetLicense
    (
        NativeLong hSession,     /* the session's handle */
        NativeLong ulLicenseNum, /* the number of the new license, can only be 1 or 2 */
        byte[]     pLicense,     /* byte buffer with the data of new license */
        NativeLong ulLicenseLen  /* length of the new license, can only be 72 */
    );


    /* C_EX_GetLicense reads the token license. C_EX_GetLicense
     * can be called in any state.
     * pulLicenseLen [in/out] - [in]- sets license length, can only be 72
     *                          [out] - gets license length (if pLicense is null)
     */
    NativeLong C_EX_GetLicense
    (
        NativeLong            hSession,     /* the session's handle */
        NativeLong            ulLicenseNum, /* the number of the license, can only be 1 or 2 */
        byte[]                pLicense,     /* receives the license */
        NativeLongByReference pulLicenseLen /* length of the license */
    );


    /* C_EX_GetCertificateInfoText get text information about
     * certificate. C_EX_GetCertificateInfoText can be called
     * in any state.
     */
    NativeLong C_EX_GetCertificateInfoText
    (
        NativeLong            hSession,  /* the session's handle */
        NativeLong            hCert,     /* the object's handle */
        Pointer               pInfo,     /* returns address of allocated buffer with text information */
        NativeLongByReference pulInfoLen /* length of the allocated buffer */
    );

    /* C_EX_PKCS7Sign signs data and packs it to PKCS#7 format
     * certificate. C_EX_PKCS7Sign can only be called in the
     * "R/W User Functions" or "R User Functions" state.
     */
    NativeLong C_EX_PKCS7Sign
    (
        NativeLong            hSession,
        byte[]                pData,
        NativeLong            ulDataLen,
        NativeLong            hCert,
        Pointer               ppEnvelope,
        NativeLongByReference pEnvelopeLen,
        NativeLong            hPrivKey,
        NativeLong[]          phCertificates,
        NativeLong            ulCertificatesLen,
        NativeLong            flags
    );


    /* C_EX_CreateCSR creates a certification request and packs it in
     * PKCS#10 format. C_EX_CreateCSR can only be called in the
     * "R/W User Functions" or "R User Functions" state.
     */
    NativeLong C_EX_CreateCSR
    (
        NativeLong             hSession,
        NativeLong             hPublicKey,
        String[]               dn,
        NativeLong             dnLength,
        Pointer                pCsr,
        NativeLongByReference  pulCsrLength,
        NativeLong             hPrivKey,
        NativeLong[]           pAttributes,
        NativeLong             ulAttributesLength,
        String[]               pExtensions,
        NativeLong             ulExtensionsLength
    );


    /* C_EX_FreeBuffer frees buffer, allocated in extended functions.
     */
    NativeLong C_EX_FreeBuffer
    (
        Pointer pBuffer /* pointer to the buffer */
    );


    /* C_EX_GetTokenName returns the token symbol name (label).
     */
    NativeLong C_EX_GetTokenName
    (
        NativeLong            hSession,   /* the session's handle */
        byte[]                pLabel,     /* byte buffer for label */
        NativeLongByReference pulLabelLen /* length of the label */
    );


    /* C_EX_SetLocalPIN modifies the local PIN for devices which supported it.
     */
    NativeLong C_EX_SetLocalPIN
    (
        NativeLong slotID,           /* ID of the token's slot */
        byte[]     pUserPin,         /* the current User PIN */
        NativeLong ulUserPinLen,     /* length of current User PIN */
        byte[]     pNewLocalPin,     /* the new local PIN */
        NativeLong ulNewLocalPinLen, /* length of the new local PIN */
        NativeLong ulLocalID         /* ID of the local PIN */
    );


    /* C_EX_LoadActivationKey */
    NativeLong C_EX_LoadActivationKey
    (
        NativeLong hSession, /* the session's handle */
        byte[]     key,
        NativeLong keySize
    );

    /* C_EX_SetActivationPassword */
    NativeLong C_EX_SetActivationPassword
    (
        NativeLong slotID,  /* ID of the token's slot */
        byte[]     password
    );
}
