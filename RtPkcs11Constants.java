/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.Pkcs11;
/* Derived from rtpkcs11t.h include file for PKCS #11. */

/*
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

import com.sun.jna.NativeLong;

public interface RtPkcs11Constants {
/*-----------------------------------------------------------------*/
/* Расширенные коды ошибок                                         */
/*-----------------------------------------------------------------*/

    public static final NativeLong CKR_CORRUPTED_MAPFILE = new NativeLong(Pkcs11Constants.CKR_VENDOR_DEFINED.intValue()+1);
    public static final NativeLong CKR_WRONG_VERSION_FIELD = new NativeLong(Pkcs11Constants.CKR_VENDOR_DEFINED.intValue()+2);
    public static final NativeLong CKR_WRONG_PKCS1_ENCODING = new NativeLong(Pkcs11Constants.CKR_VENDOR_DEFINED.intValue()+3);

    /* Неверный формат данных, переданных на подпись в PINPad,
     * или пользователь отказался от подписи данных */
    public static final NativeLong CKR_PINPAD_DATA_INCORRECT = new NativeLong(Pkcs11Constants.CKR_VENDOR_DEFINED.intValue()+0x6FB1); // 0x80006FB1
    /* Размер данных, переданных на подпись в PINPad, больше допустимого */
    public static final NativeLong CKR_PINPAD_WRONG_DATALEN = new NativeLong(Pkcs11Constants.CKR_VENDOR_DEFINED.intValue()+0x6FB6); // 0x80006FB6

/*-----------------------------------------------------------------*/
/* Необходимые определения для работы с расширениями PKCS для ГОСТ */
/*-----------------------------------------------------------------*/

    /* GOST KEY TYPES */
    public static final NativeLong CKK_GOSTR3410 = new NativeLong(0x00000030);
    public static final NativeLong CKK_GOSTR3411 = new NativeLong(0x00000031);
    public static final NativeLong CKK_GOST28147 = new NativeLong(0x00000032);

    /* GOST OBJECT ATTRIBUTES */
    public static final NativeLong CKA_GOSTR3410_PARAMS = new NativeLong(0x00000250);
    public static final NativeLong CKA_GOSTR3411_PARAMS = new NativeLong(0x00000251);
    public static final NativeLong CKA_GOST28147_PARAMS = new NativeLong(0x00000252);

    /* GOST MECHANISMS */
    public static final NativeLong CKM_GOSTR3410_KEY_PAIR_GEN = new NativeLong(0x00001200);
    public static final NativeLong CKM_GOSTR3410 = new NativeLong(0x00001201);
    public static final NativeLong CKM_GOSTR3410_WITH_GOSTR3411 = new NativeLong(0x00001202);
    public static final NativeLong CKM_GOSTR3410_KEY_WRAP = new NativeLong(0x00001203);
    public static final NativeLong CKM_GOSTR3410_DERIVE = new NativeLong(0x00001204);
    public static final NativeLong CKM_GOSTR3411 = new NativeLong(0x00001210);
    public static final NativeLong CKM_GOSTR3411_HMAC = new NativeLong(0x00001211);
    public static final NativeLong CKM_GOST28147_KEY_GEN = new NativeLong(0x00001220);
    public static final NativeLong CKM_GOST28147_ECB = new NativeLong(0x00001221);
    public static final NativeLong CKM_GOST28147 = new NativeLong(0x00001222);
    public static final NativeLong CKM_GOST28147_MAC = new NativeLong(0x00001223);
    public static final NativeLong CKM_GOST28147_KEY_WRAP = new NativeLong(0x00001224);

    public static final NativeLong CKD_CPDIVERSIFY_KDF = new NativeLong(0x00000009);
    public static final NativeLong CKP_PKCS5_PBKD2_HMAC_GOSTR3411 = new NativeLong(0x00000002);
    
/* Token flags (field "flags" from CK_TOKEN_INFO_EXTENDED +
 * field "ChangeUserPINPolicy" from CK_RUTOKEN_INIT_PARAM) */
/* TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN - if it is set, that
 * means that administrator (SO) can change user PIN
 */
    public static final NativeLong TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN = new NativeLong(0x00000001);

/* TOKEN_FLAGS_USER_CHANGE_USER_PIN - if it is set, that
 * means that user can change user PIN
 */
    public static final NativeLong TOKEN_FLAGS_USER_CHANGE_USER_PIN  = new NativeLong(0x00000002);

/* TOKEN_FLAGS_ADMIN_PIN_NOT_DEFAULT - if it is set, that
 * means that current administrator (SO) PIN is not default
 */
    public static final NativeLong TOKEN_FLAGS_ADMIN_PIN_NOT_DEFAULT = new NativeLong(0x00000004);

/* TOKEN_FLAGS_USER_PIN_NOT_DEFAULT - if it is set, that
 * means that current user PIN not default
 */
    public static final NativeLong TOKEN_FLAGS_USER_PIN_NOT_DEFAULT  = new NativeLong(0x00000008);

/* TOKEN_FLAGS_SUPPORT_FKN - if it is set, that
 * means that token support CryptoPro FKN
 */
    public static final NativeLong TOKEN_FLAGS_SUPPORT_FKN           = new NativeLong(0x00000010);

/* TOKEN_FLAGS_SUPPORT_SM - if it is set, that
 * means that token supports Secure Messaging
 */
    public static final NativeLong TOKEN_FLAGS_SUPPORT_SM            = new NativeLong(0x00000040);

    /* Body color of the token */
    public static final NativeLong TOKEN_BODY_COLOR_UNKNOWN          = new NativeLong(0);
    public static final NativeLong TOKEN_BODY_COLOR_WHITE            = new NativeLong(1);
    public static final NativeLong TOKEN_BODY_COLOR_BLACK            = new NativeLong(2);
}
