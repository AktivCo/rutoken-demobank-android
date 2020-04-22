package ru.rutoken.utils;

import androidx.annotation.AnyThread;
import androidx.core.util.Supplier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

/**
 * Provides executor for key
 *
 * @param <K> key
 */
@AnyThread
public class KeyExecutors<K> {
    private final Supplier<ExecutorService> mExecutorServiceSupplier;
    private final Map<K, ExecutorService> mExecutors = new HashMap<>();

    public KeyExecutors(Supplier<ExecutorService> executorServiceSupplier) {
        mExecutorServiceSupplier = Objects.requireNonNull(executorServiceSupplier);
    }

    public ExecutorService get(K key) {
        synchronized (mExecutors) {
            ExecutorService executor = mExecutors.get(key);
            if (executor == null) {
                executor = mExecutorServiceSupplier.get();
                mExecutors.put(key, executor);
            }
            return executor;
        }
    }
}
