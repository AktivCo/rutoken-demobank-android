/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */


package ru.rutoken.Pkcs11;

/**
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

/* CK_RUTOKEN_INIT_PARAM uses in C_EX_InitToken - extended function
 for all token reformat (C_InitToken will format only PKCS#11) */
public class CK_RUTOKEN_INIT_PARAM extends Structure {

    /*
     * init this field by size of this structure [in] - size of input structure [out] - return size
     * of filled structure
     */
    public NativeLong ulSizeofThisStructure;
    /*
     * if field equal 0 - format procedure requires authentication as administrator if field not
     * equal 0 - format procedure executes without authentication as administrator
     */
    public NativeLong UseRepairMode;
    /* pointer to byte array with new administrator PIN */
    public Pointer pNewAdminPin;
    /* length of new administrator PIN: minimum bMinAdminPinLength bytes, maximum 32 bytes */
    public NativeLong ulNewAdminPinLen;
    /* pointer to byte array with new user PIN */
    public Pointer pNewUserPin;
    /* length of new user PIN: minimum bMinUserPinLength bytes, maximum 32 bytes */
    public NativeLong ulNewUserPinLen;
    /* policy of change user PIN */
    /*
     * TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN (0x1) - administrator can change user PIN
     * TOKEN_FLAGS_USER_CHANGE_USER_PIN (0x2) - user can change user PIN
     * TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN | TOKEN_FLAGS_USER_CHANGE_USER_PIN (0x3) - administrator
     * and user can change user PIN In another cases - error
     */
    public NativeLong ChangeUserPINPolicy;
    /*
     * minimal size of administrator PIN minimum 6 byte, maximum 32 bytes
     */
    public NativeLong ulMinAdminPinLen;
    /*
     * minimal size of user PIN minimum 6 byte, maximum 32 bytes
     */
    public NativeLong ulMinUserPinLen;
    /* minimum 3, maximum 10 */
    public NativeLong ulMaxAdminRetryCount;
    /* minimum 1, maximum 10 */
    public NativeLong ulMaxUserRetryCount;
    /*
     * pointer to byte array with new token symbol name, if pTokenLabel == NULL - token symbol name
     * will not set
     */
    public Pointer pTokenLabel;
    /* length of new token symbol name */
    public NativeLong ulLabelLen;

    public CK_RUTOKEN_INIT_PARAM() {
        super();
    }

    public CK_RUTOKEN_INIT_PARAM(NativeLong ulSizeofThisStructure, NativeLong UseRepairMode, Pointer pNewAdminPin,
            NativeLong ulNewAdminPinLen, Pointer pNewUserPin, NativeLong ulNewUserPinLen,
            NativeLong ChangeUserPINPolicy, NativeLong ulMinAdminPinLen, NativeLong ulMinUserPinLen,
            NativeLong ulMaxAdminRetryCount, NativeLong ulMaxUserRetryCount, Pointer pTokenLabel,
            NativeLong ulLabelLen) {
        this.ulSizeofThisStructure = ulSizeofThisStructure;
        this.UseRepairMode = UseRepairMode;
        this.pNewAdminPin = pNewAdminPin;
        this.ulNewAdminPinLen = ulNewAdminPinLen;
        this.pNewUserPin = pNewUserPin;
        this.ulNewUserPinLen = ulNewUserPinLen;
        this.ChangeUserPINPolicy = ChangeUserPINPolicy;
        this.ulMinAdminPinLen = ulMinAdminPinLen;
        this.ulMinUserPinLen = ulMinUserPinLen;
        this.ulMaxAdminRetryCount = ulMaxAdminRetryCount;
        this.ulMaxUserRetryCount = ulMaxUserRetryCount;
        this.pTokenLabel = pTokenLabel;
        this.ulLabelLen = ulLabelLen;
    }

    protected List<String> getFieldOrder() {
        return Arrays.asList(new String[] {
                "ulSizeofThisStructure",
                "UseRepairMode",
                "pNewAdminPin",
                "ulNewAdminPinLen",
                "pNewUserPin",
                "ulNewUserPinLen",
                "ChangeUserPINPolicy",
                "ulMinAdminPinLen",
                "ulMinUserPinLen",
                "ulMaxAdminRetryCount",
                "ulMaxUserRetryCount",
                "pTokenLabel",
                "ulLabelLen"
        });
    }

}
