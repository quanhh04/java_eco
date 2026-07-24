package com.demo.securities.util;

import java.util.Collection;
import java.util.function.Function;

public final class IdGenerator {

    private IdGenerator() { }

    public static <T> String nextId(Collection<T> items, Function<T, String> idExtractor,
                                     String prefix, int digits) {
        int max = 0;
        for (T item : items) {
            String id = idExtractor.apply(item);
            if (id != null && id.startsWith(prefix)) {
                try {
                    int num = Integer.parseInt(id.substring(prefix.length()));
                    max = Math.max(max, num);
                } catch (NumberFormatException ignored) {
                    // id không theo định dạng số, bỏ qua khi tính max
                }
            }
        }
        return prefix + String.format("%0" + digits + "d", max + 1);
    }
}
