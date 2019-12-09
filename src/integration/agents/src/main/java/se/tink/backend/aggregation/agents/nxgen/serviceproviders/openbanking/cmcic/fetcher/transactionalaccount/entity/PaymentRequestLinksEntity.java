package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentRequestLinksEntity {
    @JsonProperty("request")
    private GenericLinkEntity request = null;

    @JsonProperty("confirmation")
    private GenericLinkEntity confirmation = null;

    public GenericLinkEntity getRequest() {
        return request;
    }

    public void setRequest(GenericLinkEntity request) {
        this.request = request;
    }

    public GenericLinkEntity getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(GenericLinkEntity confirmation) {
        this.confirmation = confirmation;
    }
}
