package ru.rutoken.demobank.pkcs11caller;

public final class GostOids {
    public static final byte[] OID_3411_2012_512 = {0x06, 0x08, 0x2a, (byte) 0x85, 0x03, 0x07, 0x01, 0x01, 0x02, 0x03};
    public static final byte[] OID_3411_2012_256 = {0x06, 0x08, 0x2a, (byte) 0x85, 0x03, 0x07, 0x01, 0x01, 0x02, 0x02};
    public static final byte[] OID_3411_1994 = {0x06, 0x07, 0x2a, (byte) 0x85, 0x03, 0x02, 0x02, 0x1e, 0x01};

    public static final String STRING_OID_3410_2012_512 = "1.2.643.7.1.1.1.2";
    public static final String STRING_OID_3411_2012_256 = "1.2.643.7.1.1.2.2";
    public static final String STRING_OID_3411_2012_512 = "1.2.643.7.1.1.2.3";

    private GostOids() {
    }
}
