package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InvestmentProfileEntity {
    private String riskProfile;
    private String timeHorizon;
}
