package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.investment.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AmountEntity {
    private Double amount;
    private String currencyCode;
}
