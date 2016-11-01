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

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

import java.util.Arrays;
import java.util.List;


public class CK_C_INITIALIZE_ARGS extends Pkcs11Structure {
    public CK_C_INITIALIZE_ARGS() {}

    public CK_C_INITIALIZE_ARGS(Pointer CreateMutex, Pointer DestroyMutex, Pointer LockMutex,
            Pointer UnlockMutex, NativeLong flags, Pointer pReserved) {
        this.CreateMutex = CreateMutex;
        this.DestroyMutex = DestroyMutex;
        this.LockMutex = LockMutex;
        this.UnlockMutex = UnlockMutex;
        this.flags = flags;
        this.pReserved = pReserved;

    }

    public Pointer CreateMutex;
    public Pointer DestroyMutex;
    public Pointer LockMutex;
    public Pointer UnlockMutex;
    public NativeLong flags;
    public Pointer pReserved;

    protected List<String> getFieldOrder() {
        return Arrays.asList(new String[] {
                "CreateMutex",
                "DestroyMutex",
                "LockMutex",
                "UnlockMutex",
                "flags",
                "pReserved"
        });
    }
}
