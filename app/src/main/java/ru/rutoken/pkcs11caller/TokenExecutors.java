package ru.rutoken.pkcs11caller;

import com.sun.jna.NativeLong;

import java.util.concurrent.Executors;

import ru.rutoken.utils.KeyExecutors;

public class TokenExecutors extends KeyExecutors<NativeLong> {
    private static final TokenExecutors INSTANCE = new TokenExecutors();

    private TokenExecutors() {
        super(Executors::newSingleThreadExecutor);
    }

    public static TokenExecutors getInstance() {
        return INSTANCE;
    }
}
