package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExpenseControlEntity {
    private String status;
    private boolean viewCategorizations;
    private LinksEntity links;

    public String getStatus() {
        return status;
    }

    public boolean isViewCategorizations() {
        return viewCategorizations;
    }

    public LinksEntity getLinks() {
        return links;
    }
}
