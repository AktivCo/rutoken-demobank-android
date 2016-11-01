/*
 * Copyright (c) 2016, CJSC Aktiv-Soft.
 * All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package ru.rutoken.Pkcs11;

/**
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

import com.sun.jna.NativeLong;

import java.util.Arrays;
import java.util.List;

/* CK_TOKEN_INFO_EXTENDED provides extended information about a token */
public class CK_TOKEN_INFO_EXTENDED extends Pkcs11Structure {

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
     * TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN - Administrator can change User PIN
     * TOKEN_FLAGS_USER_CHANGE_USER_PIN - User can change User PIN
     * TOKEN_FLAGS_ADMIN_PIN_NOT_DEFAULT - Administrator PIN is not default
     * TOKEN_FLAGS_USER_PIN_NOT_DEFAULT - User PIN is not default
     * TOKEN_FLAGS_SUPPORT_FKN - token supports CryptoPro FKN
     */
    public NativeLong flags; /* see below */
    /* maximum and minimum PIN length */
    public NativeLong ulMaxAdminPinLen;
    public NativeLong ulMinAdminPinLen;
    public NativeLong ulMaxUserPinLen;
    public NativeLong ulMinUserPinLen;
    /* max count of unsuccessful Administrator login attempts */
    public NativeLong ulMaxAdminRetryCount;
    /*
     * count of unsuccessful login attempts left (for Administrator PIN) if field equals 0 - that means
     * that PIN is blocked
     */
    public NativeLong ulAdminRetryCountLeft;
    /* min counts of unsuccessful User login attempts */
    public NativeLong ulMaxUserRetryCount;
    /*
     * count of unsuccessful login attempts left (for User PIN) if field equals 0 - that means that PIN is
     * blocked
     */
    public NativeLong ulUserRetryCountLeft;
    /* token serial number in Big Endian format */
    public byte[] serialNumber = new byte[8];
    /* size of all memory */
    public NativeLong ulTotalMemory; /* in bytes */
    /* size of free memory */
    public NativeLong ulFreeMemory; /* in bytes */
    /* ATR of the token */
    public byte[] ATR = new byte[64];
    /* size of ATR */
    public NativeLong ulATRLen;
    /* class of token */
    public NativeLong ulTokenClass; /* see below */
    /* Battery voltage */
    public NativeLong ulBatteryVoltage; /* microvolts */

    public NativeLong ulBodyColor;

    public CK_TOKEN_INFO_EXTENDED() {}

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
