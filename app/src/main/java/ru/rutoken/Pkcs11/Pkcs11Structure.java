package ru.rutoken.Pkcs11;

import com.sun.jna.Platform;
import com.sun.jna.Structure;

/**
 * @author Aktiv Co. <hotline@rutoken.ru>
 */

public abstract class Pkcs11Structure extends Structure {
    public Pkcs11Structure() {
        super(Platform.isWindows() ? Structure.ALIGN_NONE : Structure.ALIGN_DEFAULT);
    }
}
