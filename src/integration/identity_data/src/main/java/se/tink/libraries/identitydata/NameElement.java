package se.tink.libraries.identitydata;

public class NameElement {

    public enum Type {
        FIRST_NAME,
        SURNAME,
        FULLNAME
    }

    private final Type type;
    private final String value;

    public NameElement(Type type, String value) {
        this.type = type;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "NameElement{" + "type=" + type + ", value='" + value + '\'' + '}';
    }
}
