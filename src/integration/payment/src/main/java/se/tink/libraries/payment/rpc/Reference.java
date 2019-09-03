package se.tink.libraries.payment.rpc;

public class Reference {
    private String type;
    private String value;

    public Reference(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
