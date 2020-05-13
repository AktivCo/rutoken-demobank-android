package ru.rutoken.demobank.pkcs11caller;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ru.rutoken.demobank.utils.KeyExecutors;

public class TokenExecutors extends KeyExecutors<Token> {
    private static final TokenExecutors INSTANCE = new TokenExecutors();

    private TokenExecutors() {
        super(Executors::newSingleThreadExecutor);
    }

    public static TokenExecutors getInstance() {
        return INSTANCE;
    }

    public ExecutorService get(String tokenSerial) {
        return get(Objects.requireNonNull(TokenManager.getInstance().getTokenBySerial(tokenSerial)));
    }
}
