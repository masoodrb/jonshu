package jonshu.utils;

import java.util.ArrayDeque;
import java.util.Deque;

public class StringBuilderCache {
    private static final int _defaultCapacity = 16;
    private static int _maxBuilderSize = 360;

    private static final ThreadLocal<Deque<StringBuilder>> _cache = ThreadLocal.withInitial(ArrayDeque::new);

    public static StringBuilder Acquire(int capacity) {
        if (capacity <= _maxBuilderSize) {
            Deque<StringBuilder> cache = _cache.get();
            if (cache != null) {
                synchronized (cache) {
                    if (!cache.isEmpty()) {
                        StringBuilder sb = cache.pop();
                        if (sb != null && sb.capacity() >= capacity) {
                            sb.setLength(0);
                            return sb;
                        }
                    }
                }
            }
        }
        return new StringBuilder(capacity);
    }

    public static StringBuilder Acquire() {
        return Acquire(_defaultCapacity);
    }

    public static void Release(StringBuilder sb) {
        if (sb.capacity() <= _maxBuilderSize) {
            Deque<StringBuilder> cache = _cache.get();
            if (cache != null) {
                synchronized (cache) {
                    if (cache.size() < 1024) {
                        cache.push(sb);
                    }
                }
            }
        }
    }

    public static void SetMaxBuilderSize(int size) {
        if (size > 0) {
            _maxBuilderSize = size;
        }
    }
}