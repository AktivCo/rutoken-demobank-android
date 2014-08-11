package ru.rutoken.Pkcs11;

/**
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

import com.sun.jna.Structure;
import java.util.Arrays;
import java.util.List;

/* CK_TOKEN_INFO_EXTENDED provides extended information about a token */
public class CK_TOKEN_INFO_EXTENDED  extends Structure {

  /* init this field by size of this structure
   * [in] - size of input structure
   * [out] - return size of filled structure
   */
  public int ulSizeofThisStructure;
  /* type of token: */
  public int ulTokenType;       /* see below */
  /* exchange protocol number */
  public int ulProtocolNumber;
  /* microcode number */
  public int ulMicrocodeNumber;
  /* order number */
  public int ulOrderNumber;
  /* information flags */
  /* TOKEN_FLAGS_ADMIN_CHANGE_USER_PIN - administrator can change user PIN
   * TOKEN_FLAGS_USER_CHANGE_USER_PIN  - user can change user PIN
   * TOKEN_FLAGS_ADMIN_PIN_NOT_DEFAULT - administrator PIN not default
   * TOKEN_FLAGS_USER_PIN_NOT_DEFAULT  - user PIN not default
   * TOKEN_FLAGS_SUPPORT_FKN           - token support CryptoPro FKN
   */
  public int flags;            /* see below */
  /* maximum and minimum PIN length */
  public int ulMaxAdminPinLen;
  public int ulMinAdminPinLen;
  public int ulMaxUserPinLen;
  public int ulMinUserPinLen;
  /* max count of unsuccessful login attempts */
  public int ulMaxAdminRetryCount;
  /* count of unsuccessful attempts left (for administrator PIN)
   * if field equal 0 - that means that PIN is blocked */
  public int ulAdminRetryCountLeft;
  /* min counts of unsuccessful login attempts */
  public int ulMaxUserRetryCount;
  /* count of unsuccessful attempts left (for user PIN)
   * if field equal 0 - that means that PIN is blocked */
  public int ulUserRetryCountLeft;
  /* token serial number in Big Endian format */
  public byte[] serialNumber = new byte[8];
  /* size of all memory */
  public int ulTotalMemory;    /* in bytes */
  /* size of free memory */
  public int ulFreeMemory;     /* in bytes */
  /* atr of the token */
  public byte[] ATR = new byte[64];
  /* size of atr */
  public int ulATRLen;
  /* class of token */
  public int ulTokenClass;     /* see below */
  /* Battery Voltage */
  public int ulBatteryVoltage; /* microvolts */

  public CK_TOKEN_INFO_EXTENDED(){super();}

  public CK_TOKEN_INFO_EXTENDED(int ulSizeofThisStructure, int ulTokenType, int ulProtocolNumber,
                                int ulMicrocodeNumber, int ulOrderNumber, int flags, int ulMaxAdminPinLen,
                                int ulMinAdminPinLen, int ulMaxUserPinLen, int ulMinUserPinLen,
                                int ulMaxAdminRetryCount, int ulAdminRetryCountLeft, int ulMaxUserRetryCount,
                                int ulUserRetryCountLeft, byte[] serialNumber, int ulTotalMemory, int ulFreeMemory,
                                byte[] ATR, int ulATRLen, int ulTokenClass, int ulBatteryVoltage) {
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
    }

    protected List<String> getFieldOrder() {
        return Arrays.asList(new String[]{
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
                "ulBatteryVoltage"
        });
    }
}
