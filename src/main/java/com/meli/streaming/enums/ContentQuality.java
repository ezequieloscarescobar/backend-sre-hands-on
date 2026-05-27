package com.meli.streaming.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ContentQuality {
    SD("SD"),
    HD("HD"),
    FOUR_K("4K");

    private final String value;

    ContentQuality(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ContentQuality fromValue(String value) {
        for (ContentQuality q : ContentQuality.values()) {
            if (q.value.equals(value)) {
                return q;
            }
        }
        throw new IllegalArgumentException("Unknown quality: " + value);
    }
}
