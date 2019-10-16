package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    @JsonProperty("tinDescription")
    private String tinDescription;

    @JsonProperty("terms-of-business")
    private String termsOfBusiness;

    @JsonProperty("order-handling")
    private String orderHandling;

    @JsonProperty("agreement-prefix")
    private String agreementPrefix;

    @JsonProperty("icons")
    private String icons;

    @JsonProperty("onboarding-risk-warning")
    private String onboardingRiskWarning;

    @JsonProperty("riskWarning")
    private String riskWarning;

    @JsonProperty("usPerson")
    private String usPerson;

    @JsonProperty("professional")
    private String professional;
}
