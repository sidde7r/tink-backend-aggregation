package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TermsAndConditionsEntity {
    private String id;
    private String termsDate;
    private String productCode;
    private String statisticalCode;
    private String validFrom;
    private String validTo;
    private String type;
    private String url;
}
