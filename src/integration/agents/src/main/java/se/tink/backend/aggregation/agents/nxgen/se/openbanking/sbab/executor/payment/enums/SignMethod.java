package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.executor.payment.enums;

public enum SignMethod {
    MOBILE("mobile"),
    DESKTOP("desktop");

    private String value;

    SignMethod(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
