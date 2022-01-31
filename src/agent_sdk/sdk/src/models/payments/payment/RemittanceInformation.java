package se.tink.agent.sdk.models.payments.payment;

public class RemittanceInformation {

    private final RemittanceInformationType type;
    private final String value;

    public RemittanceInformation(RemittanceInformationType type, String value) {
        this.type = type;
        this.value = value;
    }

    public boolean isOfType(RemittanceInformationType type) {
        return type.equals(this.type);
    }

    public RemittanceInformationType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
