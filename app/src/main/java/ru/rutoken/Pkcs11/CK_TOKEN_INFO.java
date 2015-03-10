/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

/*
 * reserved comment block
 * DO NOT REMOVE OR ALTER!
 */
/* Copyright  (c) 2002 Graz University of Technology. All rights reserved.
 *
 * Redistribution and use in  source and binary forms, with or without
 * modification, are permitted  provided that the following conditions are met:
 *
 * 1. Redistributions of  source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in  binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The end-user documentation included with the redistribution, if any, must
 *    include the following acknowledgment:
 *
 *    "This product includes software developed by IAIK of Graz University of
 *     Technology."
 *
 *    Alternately, this acknowledgment may appear in the software itself, if
 *    and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Graz University of Technology" and "IAIK of Graz University of
 *    Technology" must not be used to endorse or promote products derived from
 *    this software without prior written permission.
 *
 * 5. Products derived from this software may not be called
 *    "IAIK PKCS Wrapper", nor may "IAIK" appear in their name, without prior
 *    written permission of Graz University of Technology.
 *
 *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE LICENSOR BE
 *  LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 *  OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 *  OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 *  OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 *  OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *  POSSIBILITY  OF SUCH DAMAGE.
 */

package ru.rutoken.Pkcs11;

/**
 * @author Karl Scheibelhofer <Karl.Scheibelhofer@iaik.at>
 * @author Martin Schlaeffer <schlaeff@sbox.tugraz.at>
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

import com.sun.jna.NativeLong;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

public class CK_TOKEN_INFO extends Structure {

    /*
     * label, manufacturerID, and model have been changed from CK_CHAR to CK_UTF8CHAR for v2.11.
     */
    public byte[] label = new byte[32]; /* blank padded */// 32 bytes

    public byte[] manufacturerID = new byte[32]; /* blank padded */

    public byte[] model = new byte[16]; /* blank padded */

    public byte[] serialNumber = new byte[16]; /* blank padded */

    public NativeLong flags; /* see below */

    /*
     * ulMaxSessionCount, ulSessionCount, ulMaxRwSessionCount, ulRwSessionCount, ulMaxPinLen, and
     * ulMinPinLen have all been changed from CK_USHORT to CK_ULONG for v2.0
     */
    public NativeLong ulMaxSessionCount; /* max open sessions */

    public NativeLong ulSessionCount; /* sess. now open */

    public NativeLong ulMaxRwSessionCount; /* max R/W sessions */

    public NativeLong ulRwSessionCount; /* R/W sess. now open */

    public NativeLong ulMaxPinLen; /* in bytes */

    public NativeLong ulMinPinLen; /* in bytes */

    public NativeLong ulTotalPublicMemory; /* in bytes */

    public NativeLong ulFreePublicMemory; /* in bytes */

    public NativeLong ulTotalPrivateMemory; /* in bytes */

    public NativeLong ulFreePrivateMemory; /* in bytes */

    /*
     * hardwareVersion, firmwareVersion, and time are new for v2.0
     */
    public CK_VERSION hardwareVersion; /* version of hardware */

    public CK_VERSION firmwareVersion; /* version of firmware */

    public byte[] utcTime = new byte[16]; /* time */

    public CK_TOKEN_INFO() {
        super();
    }

    public CK_TOKEN_INFO(byte[] label, byte[] vendor, byte[] model,
            byte[] serialNo, NativeLong flags,
            NativeLong sessionMax, NativeLong session,
            NativeLong rwSessionMax, NativeLong rwSession,
            NativeLong pinLenMax, NativeLong pinLenMin,
            NativeLong totalPubMem, NativeLong freePubMem,
            NativeLong totalPrivMem, NativeLong freePrivMem,
            CK_VERSION hwVer, CK_VERSION fwVer, byte[] utcTime) {
        this.label = label;
        this.manufacturerID = vendor;
        this.model = model;
        this.serialNumber = serialNo;
        this.flags = flags;
        this.ulMaxSessionCount = sessionMax;
        this.ulSessionCount = session;
        this.ulMaxRwSessionCount = rwSessionMax;
        this.ulRwSessionCount = rwSession;
        this.ulMaxPinLen = pinLenMax;
        this.ulMinPinLen = pinLenMin;
        this.ulTotalPublicMemory = totalPubMem;
        this.ulFreePublicMemory = freePubMem;
        this.ulTotalPrivateMemory = totalPrivMem;
        this.ulFreePrivateMemory = freePrivMem;
        this.hardwareVersion = hwVer;
        this.firmwareVersion = fwVer;
        this.utcTime = utcTime;
    }

    protected List<String> getFieldOrder() {
        return Arrays.asList(new String[] {
                "label",
                "manufacturerID",
                "model",
                "serialNumber",
                "flags",
                "ulMaxSessionCount",
                "ulSessionCount",
                "ulMaxRwSessionCount",
                "ulRwSessionCount",
                "ulMaxPinLen",
                "ulMinPinLen",
                "ulTotalPublicMemory",
                "ulFreePublicMemory",
                "ulTotalPrivateMemory",
                "ulFreePrivateMemory",
                "hardwareVersion",
                "firmwareVersion",
                "utcTime",
        });
    }
}
