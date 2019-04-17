package se.tink.libraries.payment.rpc;

import javafx.util.Pair;

public class Reference {
    private Pair<String, String> internalRef;

    public Reference(String type, String value) {
        this.internalRef = new Pair<>(type, value);
    }

    public String getType() {
        return internalRef.getKey();
    }

    public String getValue() {
        return internalRef.getValue();
    }
}
