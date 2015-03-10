/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.Pkcs11;

/**
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

import com.sun.jna.NativeLong;
import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

/* CK_TOKEN_INFO_EXTENDED provides extended information about a token */
public class CK_TOKEN_INFO_EXTENDED extends Structure {

    /*
     * init this field by size of this structure [in] - size of input structure [out] - return size
     * of filled structure
     */
    public NativeLong ulSizeofThisStructure;
    /* type of token: */
    public NativeLong ulTokenType; /* see below */
    /* exchange protocol number */
    public NativeLong ulProtocolNumber;
    /* microcode number */
    public NativeLong ulMicrocodeNumber;
    /* order number */
    public NativeLong ulOrderNumber;
    /* information flags */
    /*
     * TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN - administrator can change user PIN
     * TOKEN_FLAGS_USER_CHANGE_USER_PIN - user can change user PIN TOKEN_FLAGS_ADMIN_PIN_NOT_DEFAULT
     * - administrator PIN not default TOKEN_FLAGS_USER_PIN_NOT_DEFAULT - user PIN not default
     * TOKEN_FLAGS_SUPPORT_FKN - token support CryptoPro FKN
     */
    public NativeLong flags; /* see below */
    /* maximum and minimum PIN length */
    public NativeLong ulMaxAdminPinLen;
    public NativeLong ulMinAdminPinLen;
    public NativeLong ulMaxUserPinLen;
    public NativeLong ulMinUserPinLen;
    /* max count of unsuccessful login attempts */
    public NativeLong ulMaxAdminRetryCount;
    /*
     * count of unsuccessful attempts left (for administrator PIN) if field equal 0 - that means
     * that PIN is blocked
     */
    public NativeLong ulAdminRetryCountLeft;
    /* min counts of unsuccessful login attempts */
    public NativeLong ulMaxUserRetryCount;
    /*
     * count of unsuccessful attempts left (for user PIN) if field equal 0 - that means that PIN is
     * blocked
     */
    public NativeLong ulUserRetryCountLeft;
    /* token serial number in Big Endian format */
    public byte[] serialNumber = new byte[8];
    /* size of all memory */
    public NativeLong ulTotalMemory; /* in bytes */
    /* size of free memory */
    public NativeLong ulFreeMemory; /* in bytes */
    /* atr of the token */
    public byte[] ATR = new byte[64];
    /* size of atr */
    public NativeLong ulATRLen;
    /* class of token */
    public NativeLong ulTokenClass; /* see below */
    /* Battery Voltage */
    public NativeLong ulBatteryVoltage; /* microvolts */

    public NativeLong ulBodyColor;

    public CK_TOKEN_INFO_EXTENDED() {
        super();
    }

    public CK_TOKEN_INFO_EXTENDED(NativeLong ulSizeofThisStructure, NativeLong ulTokenType, NativeLong ulProtocolNumber,
            NativeLong ulMicrocodeNumber, NativeLong ulOrderNumber, NativeLong flags, NativeLong ulMaxAdminPinLen,
            NativeLong ulMinAdminPinLen, NativeLong ulMaxUserPinLen, NativeLong ulMinUserPinLen,
            NativeLong ulMaxAdminRetryCount, NativeLong ulAdminRetryCountLeft, NativeLong ulMaxUserRetryCount,
            NativeLong ulUserRetryCountLeft, byte[] serialNumber, NativeLong ulTotalMemory, NativeLong ulFreeMemory,
            byte[] ATR, NativeLong ulATRLen, NativeLong ulTokenClass, NativeLong ulBatteryVoltage, NativeLong ulBodyColor) {
        this.ulSizeofThisStructure = ulSizeofThisStructure;
        this.ulTokenType = ulTokenType;
        this.ulProtocolNumber = ulProtocolNumber;
        this.ulMicrocodeNumber = ulMicrocodeNumber;
        this.ulOrderNumber = ulOrderNumber;
        this.flags = flags;
        this.ulMaxAdminPinLen = ulMaxAdminPinLen;
        this.ulMinAdminPinLen = ulMinAdminPinLen;
        this.ulMaxUserPinLen = ulMaxUserPinLen;
        this.ulMinUserPinLen = ulMinUserPinLen;
        this.ulMaxAdminRetryCount = ulMaxAdminRetryCount;
        this.ulAdminRetryCountLeft = ulAdminRetryCountLeft;
        this.ulMaxUserRetryCount = ulMaxUserRetryCount;
        this.ulUserRetryCountLeft = ulUserRetryCountLeft;
        this.serialNumber = serialNumber;
        this.ulTotalMemory = ulTotalMemory;
        this.ulFreeMemory = ulFreeMemory;
        this.ATR = ATR;
        this.ulATRLen = ulATRLen;
        this.ulTokenClass = ulTokenClass;
        this.ulBatteryVoltage = ulBatteryVoltage;
        this.ulBodyColor = ulBodyColor;
    }

    protected List<String> getFieldOrder() {
        return Arrays.asList(new String[] {
                "ulSizeofThisStructure",
                "ulTokenType",
                "ulProtocolNumber",
                "ulMicrocodeNumber",
                "ulOrderNumber",
                "flags",
                "ulMaxAdminPinLen",
                "ulMinAdminPinLen",
                "ulMaxUserPinLen",
                "ulMinUserPinLen",
                "ulMaxAdminRetryCount",
                "ulAdminRetryCountLeft",
                "ulMaxUserRetryCount",
                "ulUserRetryCountLeft",
                "serialNumber",
                "ulTotalMemory",
                "ulFreeMemory",
                "ATR",
                "ulATRLen",
                "ulTokenClass",
                "ulBatteryVoltage",
                "ulBodyColor"
        });
    }
}
