package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
public class PaymentRequestLinksEntity {
    @JsonProperty("request")
    private GenericLinkEntity request = null;

    @JsonProperty("confirmation")
    private GenericLinkEntity confirmation = null;
}
