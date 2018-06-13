package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.amex.v62.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BillingInfoDetailsEntity {
    private String billingIndex;
    private String title;
    private String label;
    private String startDate;
    private String endDate;
}
