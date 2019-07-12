package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ExecutionRuleEntity {
    FWNG("FWNG"),
    PREC("PREC");

    private String value;

    ExecutionRuleEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static ExecutionRuleEntity fromValue(String text) {
        for (ExecutionRuleEntity b : ExecutionRuleEntity.values()) {
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
