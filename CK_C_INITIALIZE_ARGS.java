package ru.rutoken.Pkcs11;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * Created by mironenko on 19.02.2015.
 */
public class CK_C_INITIALIZE_ARGS  extends Structure {
    public CK_C_INITIALIZE_ARGS() {super();}

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
        return Arrays.asList(new String[]{"CreateMutex"
                , "DestroyMutex"
                , "LockMutex"
                , "UnlockMutex"
                , "flags"
                , "pReserved"
        });
    }
}