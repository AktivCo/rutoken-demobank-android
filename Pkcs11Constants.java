/*
 * Copyright (c) 2015, CJSC Aktiv-Soft. See the LICENSE file at the top-level directory of this distribution.
 * All Rights Reserved.
 */

package ru.rutoken.Pkcs11;
/* Derived from pkcs11t.h include file for PKCS #11. */

/* License to copy and use this software is granted provided that it is
 * identified as "RSA Security Inc. PKCS #11 Cryptographic Token Interface
 * (Cryptoki)" in all material mentioning or referencing this software.

 * License is also granted to make and use derivative works provided that
 * such works are identified as "derived from the RSA Security Inc. PKCS #11
 * Cryptographic Token Interface (Cryptoki)" in all material mentioning or
 * referencing the derived work.

 * RSA Security Inc. makes no representations concerning either the
 * merchantability of this software or the suitability of this software for
 * any particular purpose. It is provided "as is" without express or implied
 * warranty of any kind.
 */
/*
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

import com.sun.jna.NativeLong;

public interface Pkcs11Constants {
    public static final NativeLong  CRYPTOKI_VERSION_MAJOR = new NativeLong(2);
    public static final NativeLong  CRYPTOKI_VERSION_MINOR = new NativeLong(20);
    public static final NativeLong  CRYPTOKI_VERSION_AMENDMENT = new NativeLong(3);

    public static final byte CK_TRUE = 1;
    public static final byte CK_FALSE = 0;

    /* some special values for certain CK_ULONG variables */
    public static final NativeLong CK_UNAVAILABLE_INFORMATION = new NativeLong(~0);
    public static final NativeLong  CK_EFFECTIVELY_INFINITE = new NativeLong(0);

    /* The following value is always invalid if used as a session */
    /* handle or object handle */
    public static final NativeLong  CK_INVALID_HANDLE = new NativeLong(0);

    public static final NativeLong  CKN_SURRENDER = new NativeLong(0);

    /* The following notification is new for PKCS #11 v2.20 amendment 3 */
    public static final NativeLong  CKN_OTP_CHANGED = new NativeLong(1);

    /* flags: bit flags that provide capabilities of the slot
     *      Bit Flag              Mask        Meaning
     */
    public static final NativeLong  CKF_TOKEN_PRESENT = new NativeLong(0x00000001);  /* a token is there */
    public static final NativeLong  CKF_REMOVABLE_DEVICE = new NativeLong(0x00000002);  /* removable devices*/
    public static final NativeLong  CKF_HW_SLOT = new NativeLong(0x00000004);  /* hardware slot */

    /* The flags parameter is defined as follows:
     *      Bit Flag                    Mask        Meaning
     */
    public static final NativeLong  CKF_RNG = new NativeLong(0x00000001);  /* has random #
                                                 * generator */
    public static final NativeLong  CKF_WRITE_PROTECTED = new NativeLong(0x00000002);  /* token is
                                                 * write-
                                                 * protected */
    public static final NativeLong  CKF_LOGIN_REQUIRED = new NativeLong(0x00000004);  /* user must
                                                 * login */
    public static final NativeLong  CKF_USER_PIN_INITIALIZED = new NativeLong(0x00000008);  /* normal user's
                                                 * PIN is set */

    /* CKF_RESTORE_KEY_NOT_NEEDED is new for v2.0.  If it is set,
     * that means that *every* time the state of cryptographic
     * operations of a session is successfully saved, all keys
     * needed to continue those operations are stored in the state */
    public static final NativeLong  CKF_RESTORE_KEY_NOT_NEEDED = new NativeLong(0x00000020);

    /* CKF_CLOCK_ON_TOKEN is new for v2.0.  If it is set, that means
     * that the token has some sort of clock.  The time on that
     * clock is returned in the token info structure */
    public static final NativeLong  CKF_CLOCK_ON_TOKEN = new NativeLong(0x00000040);

    /* CKF_PROTECTED_AUTHENTICATION_PATH is new for v2.0.  If it is
     * set, that means that there is some way for the user to login
     * without sending a PIN through the Cryptoki library itself */
    public static final NativeLong  CKF_PROTECTED_AUTHENTICATION_PATH = new NativeLong(0x00000100);

    /* CKF_DUAL_CRYPTO_OPERATIONS is new for v2.0.  If it is true,
     * that means that a single session with the token can perform
     * dual simultaneous cryptographic operations (digest and
     * encrypt; decrypt and digest; sign and encrypt; and decrypt
     * and sign) */
    public static final NativeLong  CKF_DUAL_CRYPTO_OPERATIONS = new NativeLong(0x00000200);

    /* CKF_TOKEN_INITIALIZED if new for v2.10. If it is true, the
     * token has been initialized using C_InitializeToken or an
     * equivalent mechanism outside the scope of PKCS #11.
     * Calling C_InitializeToken when this flag is set will cause
     * the token to be reinitialized. */
    public static final NativeLong  CKF_TOKEN_INITIALIZED = new NativeLong(0x00000400);

    /* CKF_SECONDARY_AUTHENTICATION if new for v2.10. If it is
     * true, the token supports secondary authentication for
     * private key objects. This flag is deprecated in v2.11 and
       onwards. */
    public static final NativeLong  CKF_SECONDARY_AUTHENTICATION = new NativeLong(0x00000800);

    /* CKF_USER_PIN_COUNT_LOW if new for v2.10. If it is true, an
     * incorrect user login PIN has been entered at least once
     * since the last successful authentication. */
    public static final NativeLong  CKF_USER_PIN_COUNT_LOW = new NativeLong(0x00010000);

    /* CKF_USER_PIN_FINAL_TRY if new for v2.10. If it is true,
     * supplying an incorrect user PIN will it to become locked. */
    public static final NativeLong  CKF_USER_PIN_FINAL_TRY = new NativeLong(0x00020000);

    /* CKF_USER_PIN_LOCKED if new for v2.10. If it is true, the
     * user PIN has been locked. User login to the token is not
     * possible. */
    public static final NativeLong  CKF_USER_PIN_LOCKED = new NativeLong(0x00040000);

    /* CKF_USER_PIN_TO_BE_CHANGED if new for v2.10. If it is true,
     * the user PIN value is the default value set by token
     * initialization or manufacturing, or the PIN has been
     * expired by the card. */
    public static final NativeLong  CKF_USER_PIN_TO_BE_CHANGED = new NativeLong(0x00080000);

    /* CKF_SO_PIN_COUNT_LOW if new for v2.10. If it is true, an
     * incorrect SO login PIN has been entered at least once since
     * the last successful authentication. */
    public static final NativeLong  CKF_SO_PIN_COUNT_LOW = new NativeLong(0x00100000);

    /* CKF_SO_PIN_FINAL_TRY if new for v2.10. If it is true,
     * supplying an incorrect SO PIN will it to become locked. */
    public static final NativeLong  CKF_SO_PIN_FINAL_TRY = new NativeLong(0x00200000);

    /* CKF_SO_PIN_LOCKED if new for v2.10. If it is true, the SO
     * PIN has been locked. SO login to the token is not possible.
     */
    public static final NativeLong  CKF_SO_PIN_LOCKED = new NativeLong(0x00400000);

    /* CKF_SO_PIN_TO_BE_CHANGED if new for v2.10. If it is true,
     * the SO PIN value is the default value set by token
     * initialization or manufacturing, or the PIN has been
     * expired by the card. */
    public static final NativeLong  CKF_SO_PIN_TO_BE_CHANGED = new NativeLong(0x00800000);

    /* Security Officer */
    public static final NativeLong  CKU_SO = new NativeLong(0);
    /* Normal user */
    public static final NativeLong  CKU_USER = new NativeLong(1);
    /* Context specific (added in v2.20) */
    public static final NativeLong  CKU_CONTEXT_SPECIFIC = new NativeLong(2);

    public static final NativeLong  CKS_RO_PUBLIC_SESSION = new NativeLong(0);
    public static final NativeLong  CKS_RO_USER_FUNCTIONS = new NativeLong(1);
    public static final NativeLong  CKS_RW_PUBLIC_SESSION = new NativeLong(2);
    public static final NativeLong  CKS_RW_USER_FUNCTIONS = new NativeLong(3);
    public static final NativeLong  CKS_RW_SO_FUNCTIONS = new NativeLong(4);

    /* The flags are defined in the following table:
     *      Bit Flag                Mask        Meaning
     */
    public static final NativeLong  CKF_RW_SESSION = new NativeLong(0x00000002);  /* session is r/w */
    public static final NativeLong  CKF_SERIAL_SESSION = new NativeLong(0x00000004);  /* no parallel */

    /* The following classes of objects are defined: */
/* CKO_HW_FEATURE is new for v2.10 */
/* CKO_DOMAIN_PARAMETERS is new for v2.11 */
/* CKO_MECHANISM is new for v2.20 */
    public static final NativeLong  CKO_DATA = new NativeLong(0x00000000);
    public static final NativeLong  CKO_CERTIFICATE = new NativeLong(0x00000001);
    public static final NativeLong  CKO_PUBLIC_KEY = new NativeLong(0x00000002);
    public static final NativeLong  CKO_PRIVATE_KEY = new NativeLong(0x00000003);
    public static final NativeLong  CKO_SECRET_KEY = new NativeLong(0x00000004);
    public static final NativeLong  CKO_HW_FEATURE = new NativeLong(0x00000005);
    public static final NativeLong  CKO_DOMAIN_PARAMETERS = new NativeLong(0x00000006);
    public static final NativeLong  CKO_MECHANISM = new NativeLong(0x00000007);

    /* CKO_OTP_KEY is new for PKCS #11 v2.20 amendment 1 */
    public static final NativeLong  CKO_OTP_KEY = new NativeLong(0x00000008);

    public static final NativeLong  CKO_VENDOR_DEFINED = new NativeLong(0x80000000);

    /* The following hardware feature types are defined */
/* CKH_USER_INTERFACE is new for v2.20 */
    public static final NativeLong  CKH_MONOTONIC_COUNTER = new NativeLong(0x00000001);
    public static final NativeLong  CKH_CLOCK = new NativeLong(0x00000002);
    public static final NativeLong  CKH_USER_INTERFACE = new NativeLong(0x00000003);
    public static final NativeLong  CKH_VENDOR_DEFINED = new NativeLong(0x80000000);

    /* the following key types are defined: */
    public static final NativeLong  CKK_RSA = new NativeLong(0x00000000);
    public static final NativeLong  CKK_DSA = new NativeLong(0x00000001);
    public static final NativeLong  CKK_DH = new NativeLong(0x00000002);

    /* CKK_ECDSA and CKK_KEA are new for v2.0 */
/* CKK_ECDSA is deprecated in v2.11, CKK_EC is preferred. */
    public static final NativeLong  CKK_ECDSA = new NativeLong(0x00000003);
    public static final NativeLong  CKK_EC = new NativeLong(0x00000003);
    public static final NativeLong  CKK_X9_42_DH = new NativeLong(0x00000004);
    public static final NativeLong  CKK_KEA = new NativeLong(0x00000005);

    public static final NativeLong  CKK_GENERIC_SECRET = new NativeLong(0x00000010);
    public static final NativeLong  CKK_RC2 = new NativeLong(0x00000011);
    public static final NativeLong  CKK_RC4 = new NativeLong(0x00000012);
    public static final NativeLong  CKK_DES = new NativeLong(0x00000013);
    public static final NativeLong  CKK_DES2 = new NativeLong(0x00000014);
    public static final NativeLong  CKK_DES3 = new NativeLong(0x00000015);

    /* all these key types are new for v2.0 */
    public static final NativeLong  CKK_CAST = new NativeLong(0x00000016);
    public static final NativeLong  CKK_CAST3 = new NativeLong(0x00000017);
    /* CKK_CAST5 is deprecated in v2.11, CKK_CAST128 is preferred. */
    public static final NativeLong  CKK_CAST5 = new NativeLong(0x00000018);
    public static final NativeLong  CKK_CAST128 = new NativeLong(0x00000018);
    public static final NativeLong  CKK_RC5 = new NativeLong(0x00000019);
    public static final NativeLong  CKK_IDEA = new NativeLong(0x0000001A);
    public static final NativeLong  CKK_SKIPJACK = new NativeLong(0x0000001B);
    public static final NativeLong  CKK_BATON = new NativeLong(0x0000001C);
    public static final NativeLong  CKK_JUNIPER = new NativeLong(0x0000001D);
    public static final NativeLong  CKK_CDMF = new NativeLong(0x0000001E);
    public static final NativeLong  CKK_AES = new NativeLong(0x0000001F);

    /* BlowFish and TwoFish are new for v2.20 */
    public static final NativeLong  CKK_BLOWFISH = new NativeLong(0x00000020);
    public static final NativeLong  CKK_TWOFISH = new NativeLong(0x00000021);

    /* SecurID, HOTP, and ACTI are new for PKCS #11 v2.20 amendment 1 */
    public static final NativeLong  CKK_SECURID = new NativeLong(0x00000022);
    public static final NativeLong  CKK_HOTP = new NativeLong(0x00000023);
    public static final NativeLong  CKK_ACTI = new NativeLong(0x00000024);

    /* Camellia is new for PKCS #11 v2.20 amendment 3 */
    public static final NativeLong  CKK_CAMELLIA = new NativeLong(0x00000025);
    /* ARIA is new for PKCS #11 v2.20 amendment 3 */
    public static final NativeLong  CKK_ARIA = new NativeLong(0x00000026);


    public static final NativeLong  CKK_VENDOR_DEFINED = new NativeLong(0x80000000);

    /* The following certificate types are defined: */
/* CKC_X_509_ATTR_CERT is new for v2.10 */
/* CKC_WTLS is new for v2.20 */
    public static final NativeLong  CKC_X_509 = new NativeLong(0x00000000);
    public static final NativeLong  CKC_X_509_ATTR_CERT = new NativeLong(0x00000001);
    public static final NativeLong  CKC_WTLS = new NativeLong(0x00000002);
    public static final NativeLong  CKC_VENDOR_DEFINED = new NativeLong(0x80000000);

    /* The CKF_ARRAY_ATTRIBUTE flag identifies an attribute which
       consists of an array of values. */
    public static final NativeLong  CKF_ARRAY_ATTRIBUTE = new NativeLong(0x40000000);

    /* The following OTP-related defines are new for PKCS #11 v2.20 amendment 1
       and relates to the CKA_OTP_FORMAT attribute */
    public static final NativeLong  CK_OTP_FORMAT_DECIMAL = new NativeLong(0);
    public static final NativeLong  CK_OTP_FORMAT_HEXADECIMAL = new NativeLong(1);
    public static final NativeLong  CK_OTP_FORMAT_ALPHANUMERIC = new NativeLong(2);
    public static final NativeLong  CK_OTP_FORMAT_BINARY = new NativeLong(3);

    /* The following OTP-related defines are new for PKCS #11 v2.20 amendment 1
       and relates to the CKA_OTP_..._REQUIREMENT attributes */
    public static final NativeLong  CK_OTP_PARAM_IGNORED = new NativeLong(0);
    public static final NativeLong  CK_OTP_PARAM_OPTIONAL = new NativeLong(1);
    public static final NativeLong  CK_OTP_PARAM_MANDATORY = new NativeLong(2);

    /* The following attribute types are defined: */
    public static final NativeLong  CKA_CLASS = new NativeLong(0x00000000);
    public static final NativeLong  CKA_TOKEN = new NativeLong(0x00000001);
    public static final NativeLong  CKA_PRIVATE = new NativeLong(0x00000002);
    public static final NativeLong  CKA_LABEL = new NativeLong(0x00000003);
    public static final NativeLong  CKA_APPLICATION = new NativeLong(0x00000010);
    public static final NativeLong  CKA_VALUE = new NativeLong(0x00000011);

    /* CKA_OBJECT_ID is new for v2.10 */
    public static final NativeLong  CKA_OBJECT_ID = new NativeLong(0x00000012);

    public static final NativeLong  CKA_CERTIFICATE_TYPE = new NativeLong(0x00000080);
    public static final NativeLong  CKA_ISSUER = new NativeLong(0x00000081);
    public static final NativeLong  CKA_SERIAL_NUMBER = new NativeLong(0x00000082);

    /* CKA_AC_ISSUER, CKA_OWNER, and CKA_ATTR_TYPES are new
     * for v2.10 */
    public static final NativeLong  CKA_AC_ISSUER = new NativeLong(0x00000083);
    public static final NativeLong  CKA_OWNER = new NativeLong(0x00000084);
    public static final NativeLong  CKA_ATTR_TYPES = new NativeLong(0x00000085);

    /* CKA_TRUSTED is new for v2.11 */
    public static final NativeLong  CKA_TRUSTED = new NativeLong(0x00000086);

    /* CKA_CERTIFICATE_CATEGORY ...
     * CKA_CHECK_VALUE are new for v2.20 */
    public static final NativeLong  CKA_CERTIFICATE_CATEGORY = new NativeLong(0x00000087);
    public static final NativeLong  CKA_JAVA_MIDP_SECURITY_DOMAIN = new NativeLong(0x00000088);
    public static final NativeLong  CKA_URL = new NativeLong(0x00000089);
    public static final NativeLong  CKA_HASH_OF_SUBJECT_PUBLIC_KEY = new NativeLong(0x0000008A);
    public static final NativeLong  CKA_HASH_OF_ISSUER_PUBLIC_KEY = new NativeLong(0x0000008B);
    public static final NativeLong  CKA_CHECK_VALUE = new NativeLong(0x00000090);

    public static final NativeLong  CKA_KEY_TYPE = new NativeLong(0x00000100);
    public static final NativeLong  CKA_SUBJECT = new NativeLong(0x00000101);
    public static final NativeLong  CKA_ID = new NativeLong(0x00000102);
    public static final NativeLong  CKA_SENSITIVE = new NativeLong(0x00000103);
    public static final NativeLong  CKA_ENCRYPT = new NativeLong(0x00000104);
    public static final NativeLong  CKA_DECRYPT = new NativeLong(0x00000105);
    public static final NativeLong  CKA_WRAP = new NativeLong(0x00000106);
    public static final NativeLong  CKA_UNWRAP = new NativeLong(0x00000107);
    public static final NativeLong  CKA_SIGN = new NativeLong(0x00000108);
    public static final NativeLong  CKA_SIGN_RECOVER = new NativeLong(0x00000109);
    public static final NativeLong  CKA_VERIFY = new NativeLong(0x0000010A);
    public static final NativeLong  CKA_VERIFY_RECOVER = new NativeLong(0x0000010B);
    public static final NativeLong  CKA_DERIVE = new NativeLong(0x0000010C);
    public static final NativeLong  CKA_START_DATE = new NativeLong(0x00000110);
    public static final NativeLong  CKA_END_DATE = new NativeLong(0x00000111);
    public static final NativeLong  CKA_MODULUS = new NativeLong(0x00000120);
    public static final NativeLong  CKA_MODULUS_BITS = new NativeLong(0x00000121);
    public static final NativeLong  CKA_PUBLIC_EXPONENT = new NativeLong(0x00000122);
    public static final NativeLong  CKA_PRIVATE_EXPONENT = new NativeLong(0x00000123);
    public static final NativeLong  CKA_PRIME_1 = new NativeLong(0x00000124);
    public static final NativeLong  CKA_PRIME_2 = new NativeLong(0x00000125);
    public static final NativeLong  CKA_EXPONENT_1 = new NativeLong(0x00000126);
    public static final NativeLong  CKA_EXPONENT_2 = new NativeLong(0x00000127);
    public static final NativeLong  CKA_COEFFICIENT = new NativeLong(0x00000128);
    public static final NativeLong  CKA_PRIME = new NativeLong(0x00000130);
    public static final NativeLong  CKA_SUBPRIME = new NativeLong(0x00000131);
    public static final NativeLong  CKA_BASE = new NativeLong(0x00000132);

    /* CKA_PRIME_BITS and CKA_SUB_PRIME_BITS are new for v2.11 */
    public static final NativeLong  CKA_PRIME_BITS = new NativeLong(0x00000133);
    public static final NativeLong  CKA_SUBPRIME_BITS = new NativeLong(0x00000134);
    public static final NativeLong  CKA_SUB_PRIME_BITS = CKA_SUBPRIME_BITS;
/* (To retain backwards-compatibility) */

    public static final NativeLong  CKA_VALUE_BITS = new NativeLong(0x00000160);
    public static final NativeLong  CKA_VALUE_LEN = new NativeLong(0x00000161);

    /* CKA_EXTRACTABLE, CKA_LOCAL, CKA_NEVER_EXTRACTABLE,
     * CKA_ALWAYS_SENSITIVE, CKA_MODIFIABLE, CKA_ECDSA_PARAMS,
     * and CKA_EC_POINT are new for v2.0 */
    public static final NativeLong  CKA_EXTRACTABLE = new NativeLong(0x00000162);
    public static final NativeLong  CKA_LOCAL = new NativeLong(0x00000163);
    public static final NativeLong  CKA_NEVER_EXTRACTABLE = new NativeLong(0x00000164);
    public static final NativeLong  CKA_ALWAYS_SENSITIVE = new NativeLong(0x00000165);

    /* CKA_KEY_GEN_MECHANISM is new for v2.11 */
    public static final NativeLong  CKA_KEY_GEN_MECHANISM = new NativeLong(0x00000166);

    public static final NativeLong  CKA_MODIFIABLE = new NativeLong(0x00000170);

    /* CKA_ECDSA_PARAMS is deprecated in v2.11,
     * CKA_EC_PARAMS is preferred. */
    public static final NativeLong  CKA_ECDSA_PARAMS = new NativeLong(0x00000180);
    public static final NativeLong  CKA_EC_PARAMS = new NativeLong(0x00000180);

    public static final NativeLong  CKA_EC_POINT = new NativeLong(0x00000181);

    /* CKA_SECONDARY_AUTH, CKA_AUTH_PIN_FLAGS,
     * are new for v2.10. Deprecated in v2.11 and onwards. */
    public static final NativeLong  CKA_SECONDARY_AUTH = new NativeLong(0x00000200);
    public static final NativeLong  CKA_AUTH_PIN_FLAGS = new NativeLong(0x00000201);

    /* CKA_ALWAYS_AUTHENTICATE ...
     * CKA_UNWRAP_TEMPLATE are new for v2.20 */
    public static final NativeLong  CKA_ALWAYS_AUTHENTICATE = new NativeLong(0x00000202);

    public static final NativeLong  CKA_WRAP_WITH_TRUSTED = new NativeLong(0x00000210);
    public static final NativeLong CKA_WRAP_TEMPLATE = new NativeLong(CKF_ARRAY_ATTRIBUTE.intValue()|0x00000211);
    public static final NativeLong CKA_UNWRAP_TEMPLATE = new NativeLong(CKF_ARRAY_ATTRIBUTE.intValue()|0x00000212);

    /* CKA_OTP... atttributes are new for PKCS #11 v2.20 amendment 3. */
    public static final NativeLong  CKA_OTP_FORMAT = new NativeLong(0x00000220);
    public static final NativeLong  CKA_OTP_LENGTH = new NativeLong(0x00000221);
    public static final NativeLong  CKA_OTP_TIME_INTERVAL = new NativeLong(0x00000222);
    public static final NativeLong  CKA_OTP_USER_FRIENDLY_MODE = new NativeLong(0x00000223);
    public static final NativeLong  CKA_OTP_CHALLENGE_REQUIREMENT = new NativeLong(0x00000224);
    public static final NativeLong  CKA_OTP_TIME_REQUIREMENT = new NativeLong(0x00000225);
    public static final NativeLong  CKA_OTP_COUNTER_REQUIREMENT = new NativeLong(0x00000226);
    public static final NativeLong  CKA_OTP_PIN_REQUIREMENT = new NativeLong(0x00000227);
    public static final NativeLong  CKA_OTP_COUNTER = new NativeLong(0x0000022E);
    public static final NativeLong  CKA_OTP_TIME = new NativeLong(0x0000022F);
    public static final NativeLong  CKA_OTP_USER_IDENTIFIER = new NativeLong(0x0000022A);
    public static final NativeLong  CKA_OTP_SERVICE_IDENTIFIER = new NativeLong(0x0000022B);
    public static final NativeLong  CKA_OTP_SERVICE_LOGO = new NativeLong(0x0000022C);
    public static final NativeLong  CKA_OTP_SERVICE_LOGO_TYPE = new NativeLong(0x0000022D);


    /* CKA_HW_FEATURE_TYPE, CKA_RESET_ON_INIT, and CKA_HAS_RESET
     * are new for v2.10 */
    public static final NativeLong  CKA_HW_FEATURE_TYPE = new NativeLong(0x00000300);
    public static final NativeLong  CKA_RESET_ON_INIT = new NativeLong(0x00000301);
    public static final NativeLong  CKA_HAS_RESET = new NativeLong(0x00000302);

    /* The following attributes are new for v2.20 */
    public static final NativeLong  CKA_PIXEL_X = new NativeLong(0x00000400);
    public static final NativeLong  CKA_PIXEL_Y = new NativeLong(0x00000401);
    public static final NativeLong  CKA_RESOLUTION = new NativeLong(0x00000402);
    public static final NativeLong  CKA_CHAR_ROWS = new NativeLong(0x00000403);
    public static final NativeLong  CKA_CHAR_COLUMNS = new NativeLong(0x00000404);
    public static final NativeLong  CKA_COLOR = new NativeLong(0x00000405);
    public static final NativeLong  CKA_BITS_PER_PIXEL = new NativeLong(0x00000406);
    public static final NativeLong  CKA_CHAR_SETS = new NativeLong(0x00000480);
    public static final NativeLong  CKA_ENCODING_METHODS = new NativeLong(0x00000481);
    public static final NativeLong  CKA_MIME_TYPES = new NativeLong(0x00000482);
    public static final NativeLong  CKA_MECHANISM_TYPE = new NativeLong(0x00000500);
    public static final NativeLong  CKA_REQUIRED_CMS_ATTRIBUTES = new NativeLong(0x00000501);
    public static final NativeLong  CKA_DEFAULT_CMS_ATTRIBUTES = new NativeLong(0x00000502);
    public static final NativeLong  CKA_SUPPORTED_CMS_ATTRIBUTES = new NativeLong(0x00000503);
    public static final NativeLong  CKA_ALLOWED_MECHANISMS = new NativeLong(CKF_ARRAY_ATTRIBUTE.intValue()|0x00000600);

    public static final NativeLong  CKA_VENDOR_DEFINED = new NativeLong(0x80000000);

    /* The flags are defined as follows:
     *      Bit Flag               Mask        Meaning */
    public static final NativeLong  CKF_HW = new NativeLong(0x00000001);  /* performed by HW */

    /* The flags CKF_ENCRYPT, CKF_DECRYPT, CKF_DIGEST, CKF_SIGN,
     * CKG_SIGN_RECOVER, CKF_VERIFY, CKF_VERIFY_RECOVER,
     * CKF_GENERATE, CKF_GENERATE_KEY_PAIR, CKF_WRAP, CKF_UNWRAP,
     * and CKF_DERIVE are new for v2.0.  They specify whether or not
     * a mechanism can be used for a particular task */
    public static final NativeLong  CKF_ENCRYPT = new NativeLong(0x00000100);
    public static final NativeLong  CKF_DECRYPT = new NativeLong(0x00000200);
    public static final NativeLong  CKF_DIGEST = new NativeLong(0x00000400);
    public static final NativeLong  CKF_SIGN = new NativeLong(0x00000800);
    public static final NativeLong  CKF_SIGN_RECOVER = new NativeLong(0x00001000);
    public static final NativeLong  CKF_VERIFY = new NativeLong(0x00002000);
    public static final NativeLong  CKF_VERIFY_RECOVER = new NativeLong(0x00004000);
    public static final NativeLong  CKF_GENERATE = new NativeLong(0x00008000);
    public static final NativeLong  CKF_GENERATE_KEY_PAIR = new NativeLong(0x00010000);
    public static final NativeLong  CKF_WRAP = new NativeLong(0x00020000);
    public static final NativeLong  CKF_UNWRAP = new NativeLong(0x00040000);
    public static final NativeLong  CKF_DERIVE = new NativeLong(0x00080000);

    /* CKF_EC_F_P, CKF_EC_F_2M, CKF_EC_ECPARAMETERS, CKF_EC_NAMEDCURVE,
     * CKF_EC_UNCOMPRESS, and CKF_EC_COMPRESS are new for v2.11. They
     * describe a token's EC capabilities not available in mechanism
     * information. */
    public static final NativeLong  CKF_EC_F_P = new NativeLong(0x00100000);
    public static final NativeLong  CKF_EC_F_2M = new NativeLong(0x00200000);
    public static final NativeLong  CKF_EC_ECPARAMETERS = new NativeLong(0x00400000);
    public static final NativeLong  CKF_EC_NAMEDCURVE = new NativeLong(0x00800000);
    public static final NativeLong  CKF_EC_UNCOMPRESS = new NativeLong(0x01000000);
    public static final NativeLong  CKF_EC_COMPRESS = new NativeLong(0x02000000);

    public static final NativeLong  CKF_EXTENSION = new NativeLong(0x80000000); /* FALSE for this version */

    public static final NativeLong  CKR_OK = new NativeLong(0x00000000);
    public static final NativeLong  CKR_CANCEL = new NativeLong(0x00000001);
    public static final NativeLong  CKR_HOST_MEMORY = new NativeLong(0x00000002);
    public static final NativeLong  CKR_SLOT_ID_INVALID = new NativeLong(0x00000003);

/* CKR_FLAGS_INVALID was removed for v2.0 */

    /* CKR_GENERAL_ERROR and CKR_FUNCTION_FAILED are new for v2.0 */
    public static final NativeLong  CKR_GENERAL_ERROR = new NativeLong(0x00000005);
    public static final NativeLong  CKR_FUNCTION_FAILED = new NativeLong(0x00000006);

    /* CKR_ARGUMENTS_BAD, CKR_NO_EVENT, CKR_NEED_TO_CREATE_THREADS,
     * and CKR_CANT_LOCK are new for v2.01 */
    public static final NativeLong  CKR_ARGUMENTS_BAD = new NativeLong(0x00000007);
    public static final NativeLong  CKR_NO_EVENT = new NativeLong(0x00000008);
    public static final NativeLong  CKR_NEED_TO_CREATE_THREADS = new NativeLong(0x00000009);
    public static final NativeLong  CKR_CANT_LOCK = new NativeLong(0x0000000A);

    public static final NativeLong  CKR_ATTRIBUTE_READ_ONLY = new NativeLong(0x00000010);
    public static final NativeLong  CKR_ATTRIBUTE_SENSITIVE = new NativeLong(0x00000011);
    public static final NativeLong  CKR_ATTRIBUTE_TYPE_INVALID = new NativeLong(0x00000012);
    public static final NativeLong  CKR_ATTRIBUTE_VALUE_INVALID = new NativeLong(0x00000013);
    public static final NativeLong  CKR_DATA_INVALID = new NativeLong(0x00000020);
    public static final NativeLong  CKR_DATA_LEN_RANGE = new NativeLong(0x00000021);
    public static final NativeLong  CKR_DEVICE_ERROR = new NativeLong(0x00000030);
    public static final NativeLong  CKR_DEVICE_MEMORY = new NativeLong(0x00000031);
    public static final NativeLong  CKR_DEVICE_REMOVED = new NativeLong(0x00000032);
    public static final NativeLong  CKR_ENCRYPTED_DATA_INVALID = new NativeLong(0x00000040);
    public static final NativeLong  CKR_ENCRYPTED_DATA_LEN_RANGE = new NativeLong(0x00000041);
    public static final NativeLong  CKR_FUNCTION_CANCELED = new NativeLong(0x00000050);
    public static final NativeLong  CKR_FUNCTION_NOT_PARALLEL = new NativeLong(0x00000051);

    /* CKR_FUNCTION_NOT_SUPPORTED is new for v2.0 */
    public static final NativeLong  CKR_FUNCTION_NOT_SUPPORTED = new NativeLong(0x00000054);

    public static final NativeLong  CKR_KEY_HANDLE_INVALID = new NativeLong(0x00000060);

/* CKR_KEY_SENSITIVE was removed for v2.0 */

    public static final NativeLong  CKR_KEY_SIZE_RANGE = new NativeLong(0x00000062);
    public static final NativeLong  CKR_KEY_TYPE_INCONSISTENT = new NativeLong(0x00000063);

    /* CKR_KEY_NOT_NEEDED, CKR_KEY_CHANGED, CKR_KEY_NEEDED,
     * CKR_KEY_INDIGESTIBLE, CKR_KEY_FUNCTION_NOT_PERMITTED,
     * CKR_KEY_NOT_WRAPPABLE, and CKR_KEY_UNEXTRACTABLE are new for
     * v2.0 */
    public static final NativeLong  CKR_KEY_NOT_NEEDED = new NativeLong(0x00000064);
    public static final NativeLong  CKR_KEY_CHANGED = new NativeLong(0x00000065);
    public static final NativeLong  CKR_KEY_NEEDED = new NativeLong(0x00000066);
    public static final NativeLong  CKR_KEY_INDIGESTIBLE = new NativeLong(0x00000067);
    public static final NativeLong  CKR_KEY_FUNCTION_NOT_PERMITTED = new NativeLong(0x00000068);
    public static final NativeLong  CKR_KEY_NOT_WRAPPABLE = new NativeLong(0x00000069);
    public static final NativeLong  CKR_KEY_UNEXTRACTABLE = new NativeLong(0x0000006A);

    public static final NativeLong  CKR_MECHANISM_INVALID = new NativeLong(0x00000070);
    public static final NativeLong  CKR_MECHANISM_PARAM_INVALID = new NativeLong(0x00000071);

    /* CKR_OBJECT_CLASS_INCONSISTENT and CKR_OBJECT_CLASS_INVALID
     * were removed for v2.0 */
    public static final NativeLong  CKR_OBJECT_HANDLE_INVALID = new NativeLong(0x00000082);
    public static final NativeLong  CKR_OPERATION_ACTIVE = new NativeLong(0x00000090);
    public static final NativeLong  CKR_OPERATION_NOT_INITIALIZED = new NativeLong(0x00000091);
    public static final NativeLong  CKR_PIN_INCORRECT = new NativeLong(0x000000A0);
    public static final NativeLong  CKR_PIN_INVALID = new NativeLong(0x000000A1);
    public static final NativeLong  CKR_PIN_LEN_RANGE = new NativeLong(0x000000A2);

    /* CKR_PIN_EXPIRED and CKR_PIN_LOCKED are new for v2.0 */
    public static final NativeLong  CKR_PIN_EXPIRED = new NativeLong(0x000000A3);
    public static final NativeLong  CKR_PIN_LOCKED = new NativeLong(0x000000A4);

    public static final NativeLong  CKR_SESSION_CLOSED = new NativeLong(0x000000B0);
    public static final NativeLong  CKR_SESSION_COUNT = new NativeLong(0x000000B1);
    public static final NativeLong  CKR_SESSION_HANDLE_INVALID = new NativeLong(0x000000B3);
    public static final NativeLong  CKR_SESSION_PARALLEL_NOT_SUPPORTED = new NativeLong(0x000000B4);
    public static final NativeLong  CKR_SESSION_READ_ONLY = new NativeLong(0x000000B5);
    public static final NativeLong  CKR_SESSION_EXISTS = new NativeLong(0x000000B6);

    /* CKR_SESSION_READ_ONLY_EXISTS and
     * CKR_SESSION_READ_WRITE_SO_EXISTS are new for v2.0 */
    public static final NativeLong  CKR_SESSION_READ_ONLY_EXISTS = new NativeLong(0x000000B7);
    public static final NativeLong  CKR_SESSION_READ_WRITE_SO_EXISTS = new NativeLong(0x000000B8);

    public static final NativeLong  CKR_SIGNATURE_INVALID = new NativeLong(0x000000C0);
    public static final NativeLong  CKR_SIGNATURE_LEN_RANGE = new NativeLong(0x000000C1);
    public static final NativeLong  CKR_TEMPLATE_INCOMPLETE = new NativeLong(0x000000D0);
    public static final NativeLong  CKR_TEMPLATE_INCONSISTENT = new NativeLong(0x000000D1);
    public static final NativeLong  CKR_TOKEN_NOT_PRESENT = new NativeLong(0x000000E0);
    public static final NativeLong  CKR_TOKEN_NOT_RECOGNIZED = new NativeLong(0x000000E1);
    public static final NativeLong  CKR_TOKEN_WRITE_PROTECTED = new NativeLong(0x000000E2);
    public static final NativeLong  CKR_UNWRAPPING_KEY_HANDLE_INVALID = new NativeLong(0x000000F0);
    public static final NativeLong  CKR_UNWRAPPING_KEY_SIZE_RANGE = new NativeLong(0x000000F1);
    public static final NativeLong  CKR_UNWRAPPING_KEY_TYPE_INCONSISTENT = new NativeLong(0x000000F2);
    public static final NativeLong  CKR_USER_ALREADY_LOGGED_IN = new NativeLong(0x00000100);
    public static final NativeLong  CKR_USER_NOT_LOGGED_IN = new NativeLong(0x00000101);
    public static final NativeLong  CKR_USER_PIN_NOT_INITIALIZED = new NativeLong(0x00000102);
    public static final NativeLong  CKR_USER_TYPE_INVALID = new NativeLong(0x00000103);

    /* CKR_USER_ANOTHER_ALREADY_LOGGED_IN and CKR_USER_TOO_MANY_TYPES
     * are new to v2.01 */
    public static final NativeLong  CKR_USER_ANOTHER_ALREADY_LOGGED_IN = new NativeLong(0x00000104);
    public static final NativeLong  CKR_USER_TOO_MANY_TYPES = new NativeLong(0x00000105);

    public static final NativeLong  CKR_WRAPPED_KEY_INVALID = new NativeLong(0x00000110);
    public static final NativeLong  CKR_WRAPPED_KEY_LEN_RANGE = new NativeLong(0x00000112);
    public static final NativeLong  CKR_WRAPPING_KEY_HANDLE_INVALID = new NativeLong(0x00000113);
    public static final NativeLong  CKR_WRAPPING_KEY_SIZE_RANGE = new NativeLong(0x00000114);
    public static final NativeLong  CKR_WRAPPING_KEY_TYPE_INCONSISTENT = new NativeLong(0x00000115);
    public static final NativeLong  CKR_RANDOM_SEED_NOT_SUPPORTED = new NativeLong(0x00000120);

    /* These are new to v2.0 */
    public static final NativeLong  CKR_RANDOM_NO_RNG = new NativeLong(0x00000121);

    /* These are new to v2.11 */
    public static final NativeLong  CKR_DOMAIN_PARAMS_INVALID = new NativeLong(0x00000130);

    /* These are new to v2.0 */
    public static final NativeLong  CKR_BUFFER_TOO_SMALL = new NativeLong(0x00000150);
    public static final NativeLong  CKR_SAVED_STATE_INVALID = new NativeLong(0x00000160);
    public static final NativeLong  CKR_INFORMATION_SENSITIVE = new NativeLong(0x00000170);
    public static final NativeLong  CKR_STATE_UNSAVEABLE = new NativeLong(0x00000180);

    /* These are new to v2.01 */
    public static final NativeLong  CKR_CRYPTOKI_NOT_INITIALIZED = new NativeLong(0x00000190);
    public static final NativeLong  CKR_CRYPTOKI_ALREADY_INITIALIZED = new NativeLong(0x00000191);
    public static final NativeLong  CKR_MUTEX_BAD = new NativeLong(0x000001A0);
    public static final NativeLong  CKR_MUTEX_NOT_LOCKED = new NativeLong(0x000001A1);

    /* The following return values are new for PKCS #11 v2.20 amendment 3 */
    public static final NativeLong  CKR_NEW_PIN_MODE = new NativeLong(0x000001B0);
    public static final NativeLong  CKR_NEXT_OTP = new NativeLong(0x000001B1);

    /* This is new to v2.20 */
    public static final NativeLong  CKR_FUNCTION_REJECTED = new NativeLong(0x00000200);

    public static final NativeLong  CKR_VENDOR_DEFINED = new NativeLong(0x80000000);

    /* flags: bit flags that provide capabilities of the slot
     *      Bit Flag                           Mask       Meaning
     */
    public static final NativeLong  CKF_LIBRARY_CANT_CREATE_OS_THREADS = new NativeLong(0x00000001);
    public static final NativeLong  CKF_OS_LOCKING_OK = new NativeLong(0x00000002);



    /* CKF_DONT_BLOCK is for the function C_WaitForSlotEvent */
    public static final NativeLong  CKF_DONT_BLOCK = new NativeLong(1);

    /* The following MGFs are defined */
/* CKG_MGF1_SHA256, CKG_MGF1_SHA384, and CKG_MGF1_SHA512
 * are new for v2.20 */
    public static final NativeLong  CKG_MGF1_SHA1 = new NativeLong(0x00000001);
    public static final NativeLong  CKG_MGF1_SHA256 = new NativeLong(0x00000002);
    public static final NativeLong  CKG_MGF1_SHA384 = new NativeLong(0x00000003);
    public static final NativeLong  CKG_MGF1_SHA512 = new NativeLong(0x00000004);
    /* SHA-224 is new for PKCS #11 v2.20 amendment 3 */
    public static final NativeLong  CKG_MGF1_SHA224 = new NativeLong(0x00000005);

    /* The following encoding parameter sources are defined */
    public static final NativeLong  CKZ_DATA_SPECIFIED = new NativeLong(0x00000001);

    /* The following EC Key Derivation Functions are defined */
    public static final NativeLong  CKD_NULL = new NativeLong(0x00000001);
    public static final NativeLong  CKD_SHA1_KDF = new NativeLong(0x00000002);

    /* The following X9.42 DH key derivation functions are defined
       (besides CKD_NULL already defined : */
    public static final NativeLong  CKD_SHA1_KDF_ASN1 = new NativeLong(0x00000003);
    public static final NativeLong  CKD_SHA1_KDF_CONCATENATE = new NativeLong(0x00000004);

    /* The following PRFs are defined in PKCS #5 v2.0. */
    public static final NativeLong  CKP_PKCS5_PBKD2_HMAC_SHA1 = new NativeLong(0x00000001);

    /* The following salt value sources are defined in PKCS #5 v2.0. */
    public static final NativeLong  CKZ_SALT_SPECIFIED = new NativeLong(0x00000001);

    /* The following OTP-related defines are new for PKCS #11 v2.20 amendment 1 */
    public static final NativeLong  CK_OTP_VALUE = new NativeLong(0);
    public static final NativeLong  CK_OTP_PIN = new NativeLong(1);
    public static final NativeLong  CK_OTP_CHALLENGE = new NativeLong(2);
    public static final NativeLong  CK_OTP_TIME = new NativeLong(3);
    public static final NativeLong  CK_OTP_COUNTER = new NativeLong(4);
    public static final NativeLong  CK_OTP_FLAGS = new NativeLong(5);
    public static final NativeLong  CK_OTP_OUTPUT_LENGTH = new NativeLong(6);
    public static final NativeLong  CK_OTP_OUTPUT_FORMAT = new NativeLong(7);

    /* The following OTP-related defines are new for PKCS #11 v2.20 amendment 1 */
    public static final NativeLong  CKF_NEXT_OTP = new NativeLong(0x00000001);
    public static final NativeLong  CKF_EXCLUDE_TIME = new NativeLong(0x00000002);
    public static final NativeLong  CKF_EXCLUDE_COUNTER = new NativeLong(0x00000004);
    public static final NativeLong  CKF_EXCLUDE_CHALLENGE = new NativeLong(0x00000008);
    public static final NativeLong  CKF_EXCLUDE_PIN = new NativeLong(0x00000010);
    public static final NativeLong  CKF_USER_FRIENDLY_OTP = new NativeLong(0x00000020);
}
