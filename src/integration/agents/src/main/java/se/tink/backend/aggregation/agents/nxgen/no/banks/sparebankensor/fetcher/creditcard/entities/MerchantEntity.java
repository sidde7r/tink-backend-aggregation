package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.fetcher.creditcard.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class MerchantEntity {
    private String categoryCode;
    private String city;
    private String name;
}
