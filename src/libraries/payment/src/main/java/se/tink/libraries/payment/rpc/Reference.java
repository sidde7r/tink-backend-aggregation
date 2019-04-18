package se.tink.libraries.payment.rpc;

import se.tink.libraries.pair.Pair;

public class Reference {
    private Pair<String, String> internalRef;

    public Reference(String type, String value) {
        this.internalRef = new Pair<>(type, value);
    }

    public String getType() {
        return internalRef.first;
    }

    public String getValue() {
        return internalRef.second;
    }
}
