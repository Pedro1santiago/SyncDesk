package com.syncdesk.shared.util;

public final class EnumUtils {

    private EnumUtils() {}

    /**
     * Parses a string value into the given enum type, case-insensitively.
     * Throws {@link IllegalArgumentException} with a descriptive message if the value is invalid.
     */
    public static <T extends Enum<T>> T parse(Class<T> enumClass, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(enumClass.getSimpleName() + " value cannot be blank");
        }
        try {
            return Enum.valueOf(enumClass, value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid value '" + value + "' for " + enumClass.getSimpleName()
            );
        }
    }
}
