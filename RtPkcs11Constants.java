package ru.rutoken.Pkcs11;
/*******************************************************************
 * Rutoken ECP                                                      *
 * Copyright (C) Aktiv Co. 2003-11                                  *
 * rtpkcs11t.h                                                      *
 * Файл, включающий все символы для работы с библиотекой PKCS#11,   *
 * а также расширения для Rutoken ECP.                              *
 ********************************************************************/
/*
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

public interface RtPkcs11Constants {
/*-----------------------------------------------------------------*/
/* Расширенные коды ошибок                                         */
/*-----------------------------------------------------------------*/

    public static final int CKR_CORRUPTED_MAPFILE = (Pkcs11Constants.CKR_VENDOR_DEFINED+1);
    public static final int CKR_WRONG_VERSION_FIELD = (Pkcs11Constants.CKR_VENDOR_DEFINED+2);
    public static final int CKR_WRONG_PKCS1_ENCODING = (Pkcs11Constants.CKR_VENDOR_DEFINED+3);

    /* Неверный формат данных, переданных на подпись в PINPad,
     * или пользователь отказался от подписи данных */
    public static final int CKR_PINPAD_DATA_INCORRECT = (Pkcs11Constants.CKR_VENDOR_DEFINED+0x6FB1); // 0x80006FB1
    /* Размер данных, переданных на подпись в PINPad, больше допустимого */
    public static final int CKR_PINPAD_WRONG_DATALEN = (Pkcs11Constants.CKR_VENDOR_DEFINED+0x6FB6); // 0x80006FB6

/*-----------------------------------------------------------------*/
/* Необходимые определения для работы с расширениями PKCS для ГОСТ */
/*-----------------------------------------------------------------*/

    /* GOST KEY TYPES */
    public static final int CKK_GOSTR3410 = 0x00000030;
    public static final int CKK_GOSTR3411 = 0x00000031;
    public static final int CKK_GOST28147 = 0x00000032;

    /* GOST OBJECT ATTRIBUTES */
    public static final int CKA_GOSTR3410_PARAMS = 0x00000250;
    public static final int CKA_GOSTR3411_PARAMS = 0x00000251;
    public static final int CKA_GOST28147_PARAMS = 0x00000252;

    /* GOST MECHANISMS */
    public static final int CKM_GOSTR3410_KEY_PAIR_GEN = 0x00001200;
    public static final int CKM_GOSTR3410 = 0x00001201;
    public static final int CKM_GOSTR3410_WITH_GOSTR3411 = 0x00001202;
    public static final int CKM_GOSTR3410_KEY_WRAP = 0x00001203;
    public static final int CKM_GOSTR3410_DERIVE = 0x00001204;
    public static final int CKM_GOSTR3411 = 0x00001210;
    public static final int CKM_GOSTR3411_HMAC = 0x00001211;
    public static final int CKM_GOST28147_KEY_GEN = 0x00001220;
    public static final int CKM_GOST28147_ECB = 0x00001221;
    public static final int CKM_GOST28147 = 0x00001222;
    public static final int CKM_GOST28147_MAC = 0x00001223;
    public static final int CKM_GOST28147_KEY_WRAP = 0x00001224;

    public static final int CKD_CPDIVERSIFY_KDF = 0x00000009;
    public static final int CKP_PKCS5_PBKD2_HMAC_GOSTR3411 = 0x00000002;
    
/* Token flags (field "flags" from CK_TOKEN_INFO_EXTENDED +
 * field "ChangeUserPINPolicy" from CK_RUTOKEN_INIT_PARAM) */
/* TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN - if it is set, that
 * means that administrator (SO) can change user PIN
 */
    public static final int TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN = 0x00000001;

/* TOKEN_FLAGS_USER_CHANGE_USER_PIN - if it is set, that
 * means that user can change user PIN
 */
    public static final int TOKEN_FLAGS_USER_CHANGE_USER_PIN  = 0x00000002;

/* TOKEN_FLAGS_ADMIN_PIN_NOT_DEFAULT - if it is set, that
 * means that current administrator (SO) PIN is not default
 */
    public static final int TOKEN_FLAGS_ADMIN_PIN_NOT_DEFAULT = 0x00000004;

/* TOKEN_FLAGS_USER_PIN_NOT_DEFAULT - if it is set, that
 * means that current user PIN not default
 */
    public static final int TOKEN_FLAGS_USER_PIN_NOT_DEFAULT  = 0x00000008;

/* TOKEN_FLAGS_SUPPORT_FKN - if it is set, that
 * means that token support CryptoPro FKN
 */
    public static final int TOKEN_FLAGS_SUPPORT_FKN           = 0x00000010;

/* TOKEN_FLAGS_SUPPORT_SM - if it is set, that
 * means that token supports Secure Messaging
 */
    public static final int TOKEN_FLAGS_SUPPORT_SM            = 0x00000040;

    /* Body color of the token */
    public static final int TOKEN_BODY_COLOR_UNKNOWN          = 0;
    public static final int TOKEN_BODY_COLOR_WHITE            = 1;
    public static final int TOKEN_BODY_COLOR_BLACK            = 2;
}
