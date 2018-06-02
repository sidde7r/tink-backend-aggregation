package se.tink.backend.common.utils.giro.lookup;

public class LookupGiroException extends Exception {
    private Type type;

    public LookupGiroException(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        INVALID_FORMAT, NOT_FOUND
    }
}
