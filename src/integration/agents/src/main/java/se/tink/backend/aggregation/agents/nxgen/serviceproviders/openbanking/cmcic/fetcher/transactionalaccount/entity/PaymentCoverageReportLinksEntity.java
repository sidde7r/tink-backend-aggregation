package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentCoverageReportLinksEntity {
    @JsonProperty("self")
    private GenericLinkEntity self = null;

    public GenericLinkEntity getSelf() {
        return self;
    }

    public void setSelf(GenericLinkEntity self) {
        this.self = self;
    }
}
