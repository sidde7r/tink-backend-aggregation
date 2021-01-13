package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.totalkredit;

import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@EqualsAndHashCode
class TotalKreditLoanAmount {
    private String localizedValue;
    private String localizedValueWithCurrency;
    private String localizedValueWithoutCurrency;
    private int value;
    private byte scale;
    private String currency;
}
