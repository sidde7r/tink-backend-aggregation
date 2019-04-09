package se.tink.backend.aggregation.agents.banks.seb.mortgage.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Objects;

public enum MortgageStatus {
    APPLICATION_CREATED("0"), // SEB har tagit emot din ansökan
    SEB_WILL_CONTACT_CUSTOMER("1"), // SEB kommer att kontakta  dig
    INCOMPLETE_APPLICATION(
            "2"), // Du behöver komplettera alt. Du behöver komplettera några uppgifter alt. Du
                  // behöver komplettera dina uppgifter
    APPLICATION_APPROVED("3"), // Bolånet är beviljat
    APPLICATION_REJECTED("4"), // Bolånet är avslaget
    MORTGAGE_TRANSFERRED("5"), // Bolånet är flyttat
    CUSTOMER_DECLINED("6"); // Kunden har avböjt erbjudande

    private final String serialized;

    MortgageStatus(String serialized) {
        this.serialized = serialized;
    }

    @JsonValue
    public String getSerialized() {
        return serialized;
    }

    @JsonCreator
    public static MortgageStatus create(String serialized) {
        for (MortgageStatus status : MortgageStatus.values()) {
            if (Objects.equal(status.getSerialized(), serialized)) {
                return status;
            }
        }

        throw new IllegalArgumentException(
                String.format(
                        "Invalid serialized value for Status: %s. Missing a mapping?", serialized));
    }
}
