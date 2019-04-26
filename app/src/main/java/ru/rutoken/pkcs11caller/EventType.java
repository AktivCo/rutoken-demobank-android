/*
 * Copyright (c) 2018, JSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.pkcs11caller;

public enum EventType {
    SLOT_ADDED,
    SLOT_REMOVED,
    TOKEN_INFO_LOADED,
    TOKEN_INFO_FAILED,
    ENUMERATION_FINISHED,
    EVENT_HANDLER_FAILED
}
