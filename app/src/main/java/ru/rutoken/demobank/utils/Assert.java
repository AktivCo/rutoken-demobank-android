package ru.rutoken.demobank.utils;

import ru.rutoken.demobank.BuildConfig;

class Assert {
    static void assertTrue(String message, boolean condition) {
        if (BuildConfig.DEBUG && !condition)
            throw new AssertionError(message);
    }
}
