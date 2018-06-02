package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.investmentfetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ProductDetailsEntity {
    private String name;
    private String description;
    private String shortName;
    private String productSheetURI;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getShortName() {
        return shortName;
    }

    public String getProductSheetURI() {
        return productSheetURI;
    }
}