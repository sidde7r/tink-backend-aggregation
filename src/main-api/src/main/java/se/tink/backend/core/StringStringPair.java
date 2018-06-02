package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import io.protostuff.Tag;

public class StringStringPair {
    @Tag(1)
    private String key;
    @Tag(2)    
    private String value;

    public StringStringPair() {

    }

    public StringStringPair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("key", key).add("value", value).toString();
    }
}
