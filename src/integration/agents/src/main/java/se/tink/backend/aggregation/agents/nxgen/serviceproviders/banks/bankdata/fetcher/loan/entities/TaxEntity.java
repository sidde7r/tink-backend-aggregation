package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.loan.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class TaxEntity {
    private String municipalName;
    private String taxRate;
}
