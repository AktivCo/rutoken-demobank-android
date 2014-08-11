package ru.rutoken.Pkcs11;
/* pkcs11t.h include file for PKCS #11. */
/* $Revision: 1.10 $ */

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

public interface Pkcs11Constants {
    public static final int CRYPTOKI_VERSION_MAJOR = 2;
    public static final int CRYPTOKI_VERSION_MINOR = 20;
    public static final int CRYPTOKI_VERSION_AMENDMENT = 3;

    public static final byte CK_TRUE = 1;
    public static final byte CK_FALSE = 0;

    /* some special values for certain CK_ULONG variables */
    public static final int CK_UNAVAILABLE_INFORMATION = (~0);
    public static final int CK_EFFECTIVELY_INFINITE = 0;

    /* The following value is always invalid if used as a session */
    /* handle or object handle */
    public static final int CK_INVALID_HANDLE = 0;

    public static final int CKN_SURRENDER = 0;

    /* The following notification is new for PKCS #11 v2.20 amendment 3 */
    public static final int CKN_OTP_CHANGED = 1;

    /* flags: bit flags that provide capabilities of the slot
     *      Bit Flag              Mask        Meaning
     */
    public static final int CKF_TOKEN_PRESENT = 0x00000001;  /* a token is there */
    public static final int CKF_REMOVABLE_DEVICE = 0x00000002;  /* removable devices*/
    public static final int CKF_HW_SLOT = 0x00000004;  /* hardware slot */

    /* The flags parameter is defined as follows:
     *      Bit Flag                    Mask        Meaning
     */
    public static final int CKF_RNG = 0x00000001;  /* has random #
                                                 * generator */
    public static final int CKF_WRITE_PROTECTED = 0x00000002;  /* token is
                                                 * write-
                                                 * protected */
    public static final int CKF_LOGIN_REQUIRED = 0x00000004;  /* user must
                                                 * login */
    public static final int CKF_USER_PIN_INITIALIZED = 0x00000008;  /* normal user's
                                                 * PIN is set */

    /* CKF_RESTORE_KEY_NOT_NEEDED is new for v2.0.  If it is set,
     * that means that *every* time the state of cryptographic
     * operations of a session is successfully saved, all keys
     * needed to continue those operations are stored in the state */
    public static final int CKF_RESTORE_KEY_NOT_NEEDED = 0x00000020;

    /* CKF_CLOCK_ON_TOKEN is new for v2.0.  If it is set, that means
     * that the token has some sort of clock.  The time on that
     * clock is returned in the token info structure */
    public static final int CKF_CLOCK_ON_TOKEN = 0x00000040;

    /* CKF_PROTECTED_AUTHENTICATION_PATH is new for v2.0.  If it is
     * set, that means that there is some way for the user to login
     * without sending a PIN through the Cryptoki library itself */
    public static final int CKF_PROTECTED_AUTHENTICATION_PATH = 0x00000100;

    /* CKF_DUAL_CRYPTO_OPERATIONS is new for v2.0.  If it is true,
     * that means that a single session with the token can perform
     * dual simultaneous cryptographic operations (digest and
     * encrypt; decrypt and digest; sign and encrypt; and decrypt
     * and sign) */
    public static final int CKF_DUAL_CRYPTO_OPERATIONS = 0x00000200;

    /* CKF_TOKEN_INITIALIZED if new for v2.10. If it is true, the
     * token has been initialized using C_InitializeToken or an
     * equivalent mechanism outside the scope of PKCS #11.
     * Calling C_InitializeToken when this flag is set will cause
     * the token to be reinitialized. */
    public static final int CKF_TOKEN_INITIALIZED = 0x00000400;

    /* CKF_SECONDARY_AUTHENTICATION if new for v2.10. If it is
     * true, the token supports secondary authentication for
     * private key objects. This flag is deprecated in v2.11 and
       onwards. */
    public static final int CKF_SECONDARY_AUTHENTICATION = 0x00000800;

    /* CKF_USER_PIN_COUNT_LOW if new for v2.10. If it is true, an
     * incorrect user login PIN has been entered at least once
     * since the last successful authentication. */
    public static final int CKF_USER_PIN_COUNT_LOW = 0x00010000;

    /* CKF_USER_PIN_FINAL_TRY if new for v2.10. If it is true,
     * supplying an incorrect user PIN will it to become locked. */
    public static final int CKF_USER_PIN_FINAL_TRY = 0x00020000;

    /* CKF_USER_PIN_LOCKED if new for v2.10. If it is true, the
     * user PIN has been locked. User login to the token is not
     * possible. */
    public static final int CKF_USER_PIN_LOCKED = 0x00040000;

    /* CKF_USER_PIN_TO_BE_CHANGED if new for v2.10. If it is true,
     * the user PIN value is the default value set by token
     * initialization or manufacturing, or the PIN has been
     * expired by the card. */
    public static final int CKF_USER_PIN_TO_BE_CHANGED = 0x00080000;

    /* CKF_SO_PIN_COUNT_LOW if new for v2.10. If it is true, an
     * incorrect SO login PIN has been entered at least once since
     * the last successful authentication. */
    public static final int CKF_SO_PIN_COUNT_LOW = 0x00100000;

    /* CKF_SO_PIN_FINAL_TRY if new for v2.10. If it is true,
     * supplying an incorrect SO PIN will it to become locked. */
    public static final int CKF_SO_PIN_FINAL_TRY = 0x00200000;

    /* CKF_SO_PIN_LOCKED if new for v2.10. If it is true, the SO
     * PIN has been locked. SO login to the token is not possible.
     */
    public static final int CKF_SO_PIN_LOCKED = 0x00400000;

    /* CKF_SO_PIN_TO_BE_CHANGED if new for v2.10. If it is true,
     * the SO PIN value is the default value set by token
     * initialization or manufacturing, or the PIN has been
     * expired by the card. */
    public static final int CKF_SO_PIN_TO_BE_CHANGED = 0x00800000;

    /* Security Officer */
    public static final int CKU_SO = 0;
    /* Normal user */
    public static final int CKU_USER = 1;
    /* Context specific (added in v2.20) */
    public static final int CKU_CONTEXT_SPECIFIC = 2;

    public static final int CKS_RO_PUBLIC_SESSION = 0;
    public static final int CKS_RO_USER_FUNCTIONS = 1;
    public static final int CKS_RW_PUBLIC_SESSION = 2;
    public static final int CKS_RW_USER_FUNCTIONS = 3;
    public static final int CKS_RW_SO_FUNCTIONS = 4;

    /* The flags are defined in the following table:
     *      Bit Flag                Mask        Meaning
     */
    public static final int CKF_RW_SESSION = 0x00000002;  /* session is r/w */
    public static final int CKF_SERIAL_SESSION = 0x00000004;  /* no parallel */

    /* The following classes of objects are defined: */
/* CKO_HW_FEATURE is new for v2.10 */
/* CKO_DOMAIN_PARAMETERS is new for v2.11 */
/* CKO_MECHANISM is new for v2.20 */
    public static final int CKO_DATA = 0x00000000;
    public static final int CKO_CERTIFICATE = 0x00000001;
    public static final int CKO_PUBLIC_KEY = 0x00000002;
    public static final int CKO_PRIVATE_KEY = 0x00000003;
    public static final int CKO_SECRET_KEY = 0x00000004;
    public static final int CKO_HW_FEATURE = 0x00000005;
    public static final int CKO_DOMAIN_PARAMETERS = 0x00000006;
    public static final int CKO_MECHANISM = 0x00000007;

    /* CKO_OTP_KEY is new for PKCS #11 v2.20 amendment 1 */
    public static final int CKO_OTP_KEY = 0x00000008;

    public static final int CKO_VENDOR_DEFINED = 0x80000000;

    /* The following hardware feature types are defined */
/* CKH_USER_INTERFACE is new for v2.20 */
    public static final int CKH_MONOTONIC_COUNTER = 0x00000001;
    public static final int CKH_CLOCK = 0x00000002;
    public static final int CKH_USER_INTERFACE = 0x00000003;
    public static final int CKH_VENDOR_DEFINED = 0x80000000;

    /* the following key types are defined: */
    public static final int CKK_RSA = 0x00000000;
    public static final int CKK_DSA = 0x00000001;
    public static final int CKK_DH = 0x00000002;

    /* CKK_ECDSA and CKK_KEA are new for v2.0 */
/* CKK_ECDSA is deprecated in v2.11, CKK_EC is preferred. */
    public static final int CKK_ECDSA = 0x00000003;
    public static final int CKK_EC = 0x00000003;
    public static final int CKK_X9_42_DH = 0x00000004;
    public static final int CKK_KEA = 0x00000005;

    public static final int CKK_GENERIC_SECRET = 0x00000010;
    public static final int CKK_RC2 = 0x00000011;
    public static final int CKK_RC4 = 0x00000012;
    public static final int CKK_DES = 0x00000013;
    public static final int CKK_DES2 = 0x00000014;
    public static final int CKK_DES3 = 0x00000015;

    /* all these key types are new for v2.0 */
    public static final int CKK_CAST = 0x00000016;
    public static final int CKK_CAST3 = 0x00000017;
    /* CKK_CAST5 is deprecated in v2.11, CKK_CAST128 is preferred. */
    public static final int CKK_CAST5 = 0x00000018;
    public static final int CKK_CAST128 = 0x00000018;
    public static final int CKK_RC5 = 0x00000019;
    public static final int CKK_IDEA = 0x0000001A;
    public static final int CKK_SKIPJACK = 0x0000001B;
    public static final int CKK_BATON = 0x0000001C;
    public static final int CKK_JUNIPER = 0x0000001D;
    public static final int CKK_CDMF = 0x0000001E;
    public static final int CKK_AES = 0x0000001F;

    /* BlowFish and TwoFish are new for v2.20 */
    public static final int CKK_BLOWFISH = 0x00000020;
    public static final int CKK_TWOFISH = 0x00000021;

    /* SecurID, HOTP, and ACTI are new for PKCS #11 v2.20 amendment 1 */
    public static final int CKK_SECURID = 0x00000022;
    public static final int CKK_HOTP = 0x00000023;
    public static final int CKK_ACTI = 0x00000024;

    /* Camellia is new for PKCS #11 v2.20 amendment 3 */
    public static final int CKK_CAMELLIA = 0x00000025;
    /* ARIA is new for PKCS #11 v2.20 amendment 3 */
    public static final int CKK_ARIA = 0x00000026;


    public static final int CKK_VENDOR_DEFINED = 0x80000000;

    /* The following certificate types are defined: */
/* CKC_X_509_ATTR_CERT is new for v2.10 */
/* CKC_WTLS is new for v2.20 */
    public static final int CKC_X_509 = 0x00000000;
    public static final int CKC_X_509_ATTR_CERT = 0x00000001;
    public static final int CKC_WTLS = 0x00000002;
    public static final int CKC_VENDOR_DEFINED = 0x80000000;

    /* The CKF_ARRAY_ATTRIBUTE flag identifies an attribute which
       consists of an array of values. */
    public static final int CKF_ARRAY_ATTRIBUTE = 0x40000000;

    /* The following OTP-related defines are new for PKCS #11 v2.20 amendment 1
       and relates to the CKA_OTP_FORMAT attribute */
    public static final int CK_OTP_FORMAT_DECIMAL = 0;
    public static final int CK_OTP_FORMAT_HEXADECIMAL = 1;
    public static final int CK_OTP_FORMAT_ALPHANUMERIC = 2;
    public static final int CK_OTP_FORMAT_BINARY = 3;

    /* The following OTP-related defines are new for PKCS #11 v2.20 amendment 1
       and relates to the CKA_OTP_..._REQUIREMENT attributes */
    public static final int CK_OTP_PARAM_IGNORED = 0;
    public static final int CK_OTP_PARAM_OPTIONAL = 1;
    public static final int CK_OTP_PARAM_MANDATORY = 2;

    /* The following attribute types are defined: */
    public static final int CKA_CLASS = 0x00000000;
    public static final int CKA_TOKEN = 0x00000001;
    public static final int CKA_PRIVATE = 0x00000002;
    public static final int CKA_LABEL = 0x00000003;
    public static final int CKA_APPLICATION = 0x00000010;
    public static final int CKA_VALUE = 0x00000011;

    /* CKA_OBJECT_ID is new for v2.10 */
    public static final int CKA_OBJECT_ID = 0x00000012;

    public static final int CKA_CERTIFICATE_TYPE = 0x00000080;
    public static final int CKA_ISSUER = 0x00000081;
    public static final int CKA_SERIAL_NUMBER = 0x00000082;

    /* CKA_AC_ISSUER, CKA_OWNER, and CKA_ATTR_TYPES are new
     * for v2.10 */
    public static final int CKA_AC_ISSUER = 0x00000083;
    public static final int CKA_OWNER = 0x00000084;
    public static final int CKA_ATTR_TYPES = 0x00000085;

    /* CKA_TRUSTED is new for v2.11 */
    public static final int CKA_TRUSTED = 0x00000086;

    /* CKA_CERTIFICATE_CATEGORY ...
     * CKA_CHECK_VALUE are new for v2.20 */
    public static final int CKA_CERTIFICATE_CATEGORY = 0x00000087;
    public static final int CKA_JAVA_MIDP_SECURITY_DOMAIN = 0x00000088;
    public static final int CKA_URL = 0x00000089;
    public static final int CKA_HASH_OF_SUBJECT_PUBLIC_KEY = 0x0000008A;
    public static final int CKA_HASH_OF_ISSUER_PUBLIC_KEY = 0x0000008B;
    public static final int CKA_CHECK_VALUE = 0x00000090;

    public static final int CKA_KEY_TYPE = 0x00000100;
    public static final int CKA_SUBJECT = 0x00000101;
    public static final int CKA_ID = 0x00000102;
    public static final int CKA_SENSITIVE = 0x00000103;
    public static final int CKA_ENCRYPT = 0x00000104;
    public static final int CKA_DECRYPT = 0x00000105;
    public static final int CKA_WRAP = 0x00000106;
    public static final int CKA_UNWRAP = 0x00000107;
    public static final int CKA_SIGN = 0x00000108;
    public static final int CKA_SIGN_RECOVER = 0x00000109;
    public static final int CKA_VERIFY = 0x0000010A;
    public static final int CKA_VERIFY_RECOVER = 0x0000010B;
    public static final int CKA_DERIVE = 0x0000010C;
    public static final int CKA_START_DATE = 0x00000110;
    public static final int CKA_END_DATE = 0x00000111;
    public static final int CKA_MODULUS = 0x00000120;
    public static final int CKA_MODULUS_BITS = 0x00000121;
    public static final int CKA_PUBLIC_EXPONENT = 0x00000122;
    public static final int CKA_PRIVATE_EXPONENT = 0x00000123;
    public static final int CKA_PRIME_1 = 0x00000124;
    public static final int CKA_PRIME_2 = 0x00000125;
    public static final int CKA_EXPONENT_1 = 0x00000126;
    public static final int CKA_EXPONENT_2 = 0x00000127;
    public static final int CKA_COEFFICIENT = 0x00000128;
    public static final int CKA_PRIME = 0x00000130;
    public static final int CKA_SUBPRIME = 0x00000131;
    public static final int CKA_BASE = 0x00000132;

    /* CKA_PRIME_BITS and CKA_SUB_PRIME_BITS are new for v2.11 */
    public static final int CKA_PRIME_BITS = 0x00000133;
    public static final int CKA_SUBPRIME_BITS = 0x00000134;
    public static final int CKA_SUB_PRIME_BITS = CKA_SUBPRIME_BITS;
/* (To retain backwards-compatibility) */

    public static final int CKA_VALUE_BITS = 0x00000160;
    public static final int CKA_VALUE_LEN = 0x00000161;

    /* CKA_EXTRACTABLE, CKA_LOCAL, CKA_NEVER_EXTRACTABLE,
     * CKA_ALWAYS_SENSITIVE, CKA_MODIFIABLE, CKA_ECDSA_PARAMS,
     * and CKA_EC_POINT are new for v2.0 */
    public static final int CKA_EXTRACTABLE = 0x00000162;
    public static final int CKA_LOCAL = 0x00000163;
    public static final int CKA_NEVER_EXTRACTABLE = 0x00000164;
    public static final int CKA_ALWAYS_SENSITIVE = 0x00000165;

    /* CKA_KEY_GEN_MECHANISM is new for v2.11 */
    public static final int CKA_KEY_GEN_MECHANISM = 0x00000166;

    public static final int CKA_MODIFIABLE = 0x00000170;

    /* CKA_ECDSA_PARAMS is deprecated in v2.11,
     * CKA_EC_PARAMS is preferred. */
    public static final int CKA_ECDSA_PARAMS = 0x00000180;
    public static final int CKA_EC_PARAMS = 0x00000180;

    public static final int CKA_EC_POINT = 0x00000181;

    /* CKA_SECONDARY_AUTH, CKA_AUTH_PIN_FLAGS,
     * are new for v2.10. Deprecated in v2.11 and onwards. */
    public static final int CKA_SECONDARY_AUTH = 0x00000200;
    public static final int CKA_AUTH_PIN_FLAGS = 0x00000201;

    /* CKA_ALWAYS_AUTHENTICATE ...
     * CKA_UNWRAP_TEMPLATE are new for v2.20 */
    public static final int CKA_ALWAYS_AUTHENTICATE = 0x00000202;

    public static final int CKA_WRAP_WITH_TRUSTED = 0x00000210;
    public static final int CKA_WRAP_TEMPLATE = (CKF_ARRAY_ATTRIBUTE|0x00000211);
    public static final int CKA_UNWRAP_TEMPLATE = (CKF_ARRAY_ATTRIBUTE|0x00000212);

    /* CKA_OTP... atttributes are new for PKCS #11 v2.20 amendment 3. */
    public static final int CKA_OTP_FORMAT = 0x00000220;
    public static final int CKA_OTP_LENGTH = 0x00000221;
    public static final int CKA_OTP_TIME_INTERVAL = 0x00000222;
    public static final int CKA_OTP_USER_FRIENDLY_MODE = 0x00000223;
    public static final int CKA_OTP_CHALLENGE_REQUIREMENT = 0x00000224;
    public static final int CKA_OTP_TIME_REQUIREMENT = 0x00000225;
    public static final int CKA_OTP_COUNTER_REQUIREMENT = 0x00000226;
    public static final int CKA_OTP_PIN_REQUIREMENT = 0x00000227;
    public static final int CKA_OTP_COUNTER = 0x0000022E;
    public static final int CKA_OTP_TIME = 0x0000022F;
    public static final int CKA_OTP_USER_IDENTIFIER = 0x0000022A;
    public static final int CKA_OTP_SERVICE_IDENTIFIER = 0x0000022B;
    public static final int CKA_OTP_SERVICE_LOGO = 0x0000022C;
    public static final int CKA_OTP_SERVICE_LOGO_TYPE = 0x0000022D;


    /* CKA_HW_FEATURE_TYPE, CKA_RESET_ON_INIT, and CKA_HAS_RESET
     * are new for v2.10 */
    public static final int CKA_HW_FEATURE_TYPE = 0x00000300;
    public static final int CKA_RESET_ON_INIT = 0x00000301;
    public static final int CKA_HAS_RESET = 0x00000302;

    /* The following attributes are new for v2.20 */
    public static final int CKA_PIXEL_X = 0x00000400;
    public static final int CKA_PIXEL_Y = 0x00000401;
    public static final int CKA_RESOLUTION = 0x00000402;
    public static final int CKA_CHAR_ROWS = 0x00000403;
    public static final int CKA_CHAR_COLUMNS = 0x00000404;
    public static final int CKA_COLOR = 0x00000405;
    public static final int CKA_BITS_PER_PIXEL = 0x00000406;
    public static final int CKA_CHAR_SETS = 0x00000480;
    public static final int CKA_ENCODING_METHODS = 0x00000481;
    public static final int CKA_MIME_TYPES = 0x00000482;
    public static final int CKA_MECHANISM_TYPE = 0x00000500;
    public static final int CKA_REQUIRED_CMS_ATTRIBUTES = 0x00000501;
    public static final int CKA_DEFAULT_CMS_ATTRIBUTES = 0x00000502;
    public static final int CKA_SUPPORTED_CMS_ATTRIBUTES = 0x00000503;
    public static final int CKA_ALLOWED_MECHANISMS = (CKF_ARRAY_ATTRIBUTE|0x00000600);

    public static final int CKA_VENDOR_DEFINED = 0x80000000;

    /* The flags are defined as follows:
     *      Bit Flag               Mask        Meaning */
    public static final int CKF_HW = 0x00000001;  /* performed by HW */

    /* The flags CKF_ENCRYPT, CKF_DECRYPT, CKF_DIGEST, CKF_SIGN,
     * CKG_SIGN_RECOVER, CKF_VERIFY, CKF_VERIFY_RECOVER,
     * CKF_GENERATE, CKF_GENERATE_KEY_PAIR, CKF_WRAP, CKF_UNWRAP,
     * and CKF_DERIVE are new for v2.0.  They specify whether or not
     * a mechanism can be used for a particular task */
    public static final int CKF_ENCRYPT = 0x00000100;
    public static final int CKF_DECRYPT = 0x00000200;
    public static final int CKF_DIGEST = 0x00000400;
    public static final int CKF_SIGN = 0x00000800;
    public static final int CKF_SIGN_RECOVER = 0x00001000;
    public static final int CKF_VERIFY = 0x00002000;
    public static final int CKF_VERIFY_RECOVER = 0x00004000;
    public static final int CKF_GENERATE = 0x00008000;
    public static final int CKF_GENERATE_KEY_PAIR = 0x00010000;
    public static final int CKF_WRAP = 0x00020000;
    public static final int CKF_UNWRAP = 0x00040000;
    public static final int CKF_DERIVE = 0x00080000;

    /* CKF_EC_F_P, CKF_EC_F_2M, CKF_EC_ECPARAMETERS, CKF_EC_NAMEDCURVE,
     * CKF_EC_UNCOMPRESS, and CKF_EC_COMPRESS are new for v2.11. They
     * describe a token's EC capabilities not available in mechanism
     * information. */
    public static final int CKF_EC_F_P = 0x00100000;
    public static final int CKF_EC_F_2M = 0x00200000;
    public static final int CKF_EC_ECPARAMETERS = 0x00400000;
    public static final int CKF_EC_NAMEDCURVE = 0x00800000;
    public static final int CKF_EC_UNCOMPRESS = 0x01000000;
    public static final int CKF_EC_COMPRESS = 0x02000000;

    public static final int CKF_EXTENSION = 0x80000000; /* FALSE for this version */

    public static final int CKR_OK = 0x00000000;
    public static final int CKR_CANCEL = 0x00000001;
    public static final int CKR_HOST_MEMORY = 0x00000002;
    public static final int CKR_SLOT_ID_INVALID = 0x00000003;

/* CKR_FLAGS_INVALID was removed for v2.0 */

    /* CKR_GENERAL_ERROR and CKR_FUNCTION_FAILED are new for v2.0 */
    public static final int CKR_GENERAL_ERROR = 0x00000005;
    public static final int CKR_FUNCTION_FAILED = 0x00000006;

    /* CKR_ARGUMENTS_BAD, CKR_NO_EVENT, CKR_NEED_TO_CREATE_THREADS,
     * and CKR_CANT_LOCK are new for v2.01 */
    public static final int CKR_ARGUMENTS_BAD = 0x00000007;
    public static final int CKR_NO_EVENT = 0x00000008;
    public static final int CKR_NEED_TO_CREATE_THREADS = 0x00000009;
    public static final int CKR_CANT_LOCK = 0x0000000A;

    public static final int CKR_ATTRIBUTE_READ_ONLY = 0x00000010;
    public static final int CKR_ATTRIBUTE_SENSITIVE = 0x00000011;
    public static final int CKR_ATTRIBUTE_TYPE_INVALID = 0x00000012;
    public static final int CKR_ATTRIBUTE_VALUE_INVALID = 0x00000013;
    public static final int CKR_DATA_INVALID = 0x00000020;
    public static final int CKR_DATA_LEN_RANGE = 0x00000021;
    public static final int CKR_DEVICE_ERROR = 0x00000030;
    public static final int CKR_DEVICE_MEMORY = 0x00000031;
    public static final int CKR_DEVICE_REMOVED = 0x00000032;
    public static final int CKR_ENCRYPTED_DATA_INVALID = 0x00000040;
    public static final int CKR_ENCRYPTED_DATA_LEN_RANGE = 0x00000041;
    public static final int CKR_FUNCTION_CANCELED = 0x00000050;
    public static final int CKR_FUNCTION_NOT_PARALLEL = 0x00000051;

    /* CKR_FUNCTION_NOT_SUPPORTED is new for v2.0 */
    public static final int CKR_FUNCTION_NOT_SUPPORTED = 0x00000054;

    public static final int CKR_KEY_HANDLE_INVALID = 0x00000060;

/* CKR_KEY_SENSITIVE was removed for v2.0 */

    public static final int CKR_KEY_SIZE_RANGE = 0x00000062;
    public static final int CKR_KEY_TYPE_INCONSISTENT = 0x00000063;

    /* CKR_KEY_NOT_NEEDED, CKR_KEY_CHANGED, CKR_KEY_NEEDED,
     * CKR_KEY_INDIGESTIBLE, CKR_KEY_FUNCTION_NOT_PERMITTED,
     * CKR_KEY_NOT_WRAPPABLE, and CKR_KEY_UNEXTRACTABLE are new for
     * v2.0 */
    public static final int CKR_KEY_NOT_NEEDED = 0x00000064;
    public static final int CKR_KEY_CHANGED = 0x00000065;
    public static final int CKR_KEY_NEEDED = 0x00000066;
    public static final int CKR_KEY_INDIGESTIBLE = 0x00000067;
    public static final int CKR_KEY_FUNCTION_NOT_PERMITTED = 0x00000068;
    public static final int CKR_KEY_NOT_WRAPPABLE = 0x00000069;
    public static final int CKR_KEY_UNEXTRACTABLE = 0x0000006A;

    public static final int CKR_MECHANISM_INVALID = 0x00000070;
    public static final int CKR_MECHANISM_PARAM_INVALID = 0x00000071;

    /* CKR_OBJECT_CLASS_INCONSISTENT and CKR_OBJECT_CLASS_INVALID
     * were removed for v2.0 */
    public static final int CKR_OBJECT_HANDLE_INVALID = 0x00000082;
    public static final int CKR_OPERATION_ACTIVE = 0x00000090;
    public static final int CKR_OPERATION_NOT_INITIALIZED = 0x00000091;
    public static final int CKR_PIN_INCORRECT = 0x000000A0;
    public static final int CKR_PIN_INVALID = 0x000000A1;
    public static final int CKR_PIN_LEN_RANGE = 0x000000A2;

    /* CKR_PIN_EXPIRED and CKR_PIN_LOCKED are new for v2.0 */
    public static final int CKR_PIN_EXPIRED = 0x000000A3;
    public static final int CKR_PIN_LOCKED = 0x000000A4;

    public static final int CKR_SESSION_CLOSED = 0x000000B0;
    public static final int CKR_SESSION_COUNT = 0x000000B1;
    public static final int CKR_SESSION_HANDLE_INVALID = 0x000000B3;
    public static final int CKR_SESSION_PARALLEL_NOT_SUPPORTED = 0x000000B4;
    public static final int CKR_SESSION_READ_ONLY = 0x000000B5;
    public static final int CKR_SESSION_EXISTS = 0x000000B6;

    /* CKR_SESSION_READ_ONLY_EXISTS and
     * CKR_SESSION_READ_WRITE_SO_EXISTS are new for v2.0 */
    public static final int CKR_SESSION_READ_ONLY_EXISTS = 0x000000B7;
    public static final int CKR_SESSION_READ_WRITE_SO_EXISTS = 0x000000B8;

    public static final int CKR_SIGNATURE_INVALID = 0x000000C0;
    public static final int CKR_SIGNATURE_LEN_RANGE = 0x000000C1;
    public static final int CKR_TEMPLATE_INCOMPLETE = 0x000000D0;
    public static final int CKR_TEMPLATE_INCONSISTENT = 0x000000D1;
    public static final int CKR_TOKEN_NOT_PRESENT = 0x000000E0;
    public static final int CKR_TOKEN_NOT_RECOGNIZED = 0x000000E1;
    public static final int CKR_TOKEN_WRITE_PROTECTED = 0x000000E2;
    public static final int CKR_UNWRAPPING_KEY_HANDLE_INVALID = 0x000000F0;
    public static final int CKR_UNWRAPPING_KEY_SIZE_RANGE = 0x000000F1;
    public static final int CKR_UNWRAPPING_KEY_TYPE_INCONSISTENT = 0x000000F2;
    public static final int CKR_USER_ALREADY_LOGGED_IN = 0x00000100;
    public static final int CKR_USER_NOT_LOGGED_IN = 0x00000101;
    public static final int CKR_USER_PIN_NOT_INITIALIZED = 0x00000102;
    public static final int CKR_USER_TYPE_INVALID = 0x00000103;

    /* CKR_USER_ANOTHER_ALREADY_LOGGED_IN and CKR_USER_TOO_MANY_TYPES
     * are new to v2.01 */
    public static final int CKR_USER_ANOTHER_ALREADY_LOGGED_IN = 0x00000104;
    public static final int CKR_USER_TOO_MANY_TYPES = 0x00000105;

    public static final int CKR_WRAPPED_KEY_INVALID = 0x00000110;
    public static final int CKR_WRAPPED_KEY_LEN_RANGE = 0x00000112;
    public static final int CKR_WRAPPING_KEY_HANDLE_INVALID = 0x00000113;
    public static final int CKR_WRAPPING_KEY_SIZE_RANGE = 0x00000114;
    public static final int CKR_WRAPPING_KEY_TYPE_INCONSISTENT = 0x00000115;
    public static final int CKR_RANDOM_SEED_NOT_SUPPORTED = 0x00000120;

    /* These are new to v2.0 */
    public static final int CKR_RANDOM_NO_RNG = 0x00000121;

    /* These are new to v2.11 */
    public static final int CKR_DOMAIN_PARAMS_INVALID = 0x00000130;

    /* These are new to v2.0 */
    public static final int CKR_BUFFER_TOO_SMALL = 0x00000150;
    public static final int CKR_SAVED_STATE_INVALID = 0x00000160;
    public static final int CKR_INFORMATION_SENSITIVE = 0x00000170;
    public static final int CKR_STATE_UNSAVEABLE = 0x00000180;

    /* These are new to v2.01 */
    public static final int CKR_CRYPTOKI_NOT_INITIALIZED = 0x00000190;
    public static final int CKR_CRYPTOKI_ALREADY_INITIALIZED = 0x00000191;
    public static final int CKR_MUTEX_BAD = 0x000001A0;
    public static final int CKR_MUTEX_NOT_LOCKED = 0x000001A1;

    /* The following return values are new for PKCS #11 v2.20 amendment 3 */
    public static final int CKR_NEW_PIN_MODE = 0x000001B0;
    public static final int CKR_NEXT_OTP = 0x000001B1;

    /* This is new to v2.20 */
    public static final int CKR_FUNCTION_REJECTED = 0x00000200;

    public static final int CKR_VENDOR_DEFINED = 0x80000000;

    /* flags: bit flags that provide capabilities of the slot
     *      Bit Flag                           Mask       Meaning
     */
    public static final int CKF_LIBRARY_CANT_CREATE_OS_THREADS = 0x00000001;
    public static final int CKF_OS_LOCKING_OK = 0x00000002;



    /* CKF_DONT_BLOCK is for the function C_WaitForSlotEvent */
    public static final int CKF_DONT_BLOCK = 1;

    /* The following MGFs are defined */
/* CKG_MGF1_SHA256, CKG_MGF1_SHA384, and CKG_MGF1_SHA512
 * are new for v2.20 */
    public static final int CKG_MGF1_SHA1 = 0x00000001;
    public static final int CKG_MGF1_SHA256 = 0x00000002;
    public static final int CKG_MGF1_SHA384 = 0x00000003;
    public static final int CKG_MGF1_SHA512 = 0x00000004;
    /* SHA-224 is new for PKCS #11 v2.20 amendment 3 */
    public static final int CKG_MGF1_SHA224 = 0x00000005;

    /* The following encoding parameter sources are defined */
    public static final int CKZ_DATA_SPECIFIED = 0x00000001;

    /* The following EC Key Derivation Functions are defined */
    public static final int CKD_NULL = 0x00000001;
    public static final int CKD_SHA1_KDF = 0x00000002;

    /* The following X9.42 DH key derivation functions are defined
       (besides CKD_NULL already defined : */
    public static final int CKD_SHA1_KDF_ASN1 = 0x00000003;
    public static final int CKD_SHA1_KDF_CONCATENATE = 0x00000004;

    /* The following PRFs are defined in PKCS #5 v2.0. */
    public static final int CKP_PKCS5_PBKD2_HMAC_SHA1 = 0x00000001;

    /* The following salt value sources are defined in PKCS #5 v2.0. */
    public static final int CKZ_SALT_SPECIFIED = 0x00000001;

    /* The following OTP-related defines are new for PKCS #11 v2.20 amendment 1 */
    public static final int CK_OTP_VALUE = 0;
    public static final int CK_OTP_PIN = 1;
    public static final int CK_OTP_CHALLENGE = 2;
    public static final int CK_OTP_TIME = 3;
    public static final int CK_OTP_COUNTER = 4;
    public static final int CK_OTP_FLAGS = 5;
    public static final int CK_OTP_OUTPUT_LENGTH = 6;
    public static final int CK_OTP_OUTPUT_FORMAT = 7;

    /* The following OTP-related defines are new for PKCS #11 v2.20 amendment 1 */
    public static final int CKF_NEXT_OTP = 0x00000001;
    public static final int CKF_EXCLUDE_TIME = 0x00000002;
    public static final int CKF_EXCLUDE_COUNTER = 0x00000004;
    public static final int CKF_EXCLUDE_CHALLENGE = 0x00000008;
    public static final int CKF_EXCLUDE_PIN = 0x00000010;
    public static final int CKF_USER_FRIENDLY_OTP = 0x00000020;
}
