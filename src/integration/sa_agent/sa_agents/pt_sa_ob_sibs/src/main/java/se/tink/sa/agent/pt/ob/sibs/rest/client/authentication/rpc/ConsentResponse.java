package se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import se.tink.sa.agent.pt.ob.sibs.rest.client.authentication.entity.ConsentLinksEntity;

@Getter
@Setter
public class ConsentResponse {

    private String transactionStatus;

    @JsonProperty("_links")
    private ConsentLinksEntity links;

    private String consentId;
    private String psuMessage;
}
