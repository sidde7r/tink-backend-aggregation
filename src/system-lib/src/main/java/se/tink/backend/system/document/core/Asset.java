package se.tink.backend.system.document.core;

public class Asset {
    private final String name;
    private final int value;

    public Asset(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name.toUpperCase() + ": " + value ;
    }
}
