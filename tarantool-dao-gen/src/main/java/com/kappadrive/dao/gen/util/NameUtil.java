package com.kappadrive.dao.gen.util;

import javax.annotation.Nonnull;

public final class NameUtil {

    private NameUtil() {
    }

    @Nonnull
    public static String removeGetIfPresent(@Nonnull final String name) {
        String get = "get";
        if (name.startsWith(get)) {
            return name.substring(get.length(), get.length() + 1).toLowerCase().concat(name.substring(get.length() + 1));
        }
        return name;
    }
}
