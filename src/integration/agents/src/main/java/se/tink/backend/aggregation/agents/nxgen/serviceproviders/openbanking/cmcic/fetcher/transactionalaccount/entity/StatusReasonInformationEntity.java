package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum StatusReasonInformationEntity {
    AC01("AC01"),
    AC04("AC04"),
    AC06("AC06"),
    AG01("AG01"),
    AM18("AM18"),
    CH03("CH03"),
    CUST("CUST"),
    DS02("DS02"),
    FF01("FF01"),
    FRAD("FRAD"),
    MS03("MS03"),
    NOAS("NOAS"),
    RR01("RR01"),
    RR03("RR03"),
    RR04("RR04"),
    RR12("RR12");

    private String value;

    StatusReasonInformationEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static StatusReasonInformationEntity fromValue(String text) {
        for (StatusReasonInformationEntity b : StatusReasonInformationEntity.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}
