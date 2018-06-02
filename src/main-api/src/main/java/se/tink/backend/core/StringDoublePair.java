package se.tink.backend.core;

import com.google.common.base.MoreObjects;
import io.protostuff.Tag;

public class StringDoublePair {
    @Tag(1)
    private String key;
    @Tag(2)    
    private double value;

    public StringDoublePair() {

    }

    public StringDoublePair(String key, double value) {
        this.key = key;
        this.value = value;
    }

    public StringDoublePair(String key, Double value) {
        this.key = key;
        this.value = value.doubleValue();
    }
    
    public String getKey() {
        return key;
    }

    public double getValue() {
        return value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("key", key).add("value", value).toString();
    }
}
