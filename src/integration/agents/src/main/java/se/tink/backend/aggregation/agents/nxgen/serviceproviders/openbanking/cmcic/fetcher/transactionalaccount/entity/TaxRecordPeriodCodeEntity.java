package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaxRecordPeriodCodeEntity {
    MM01("MM01"),
    MM02("MM02"),
    MM03("MM03"),
    MM04("MM04"),
    MM05("MM05"),
    MM06("MM06"),
    MM07("MM07"),
    MM08("MM08"),
    MM09("MM09"),
    MM10("MM10"),
    MM11("MM11"),
    MM12("MM12"),
    QTR1("QTR1"),
    QTR2("QTR2"),
    QTR3("QTR3"),
    QTR4("QTR4"),
    HLF1("HLF1"),
    HLF2("HLF2");

    private String value;

    TaxRecordPeriodCodeEntity(String value) {
        this.value = value;
    }

    @JsonCreator
    public static TaxRecordPeriodCodeEntity fromValue(String text) {
        for (TaxRecordPeriodCodeEntity b : TaxRecordPeriodCodeEntity.values()) {
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
