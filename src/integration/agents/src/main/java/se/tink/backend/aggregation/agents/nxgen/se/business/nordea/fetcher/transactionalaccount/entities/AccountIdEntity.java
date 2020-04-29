package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountIdEntity {
    @JsonProperty("@deposit")
    private boolean deposit;

    @JsonProperty("@id")
    private String id;

    @JsonProperty("@ownTransferFrom")
    private boolean ownTransferFrom;

    @JsonProperty("@ownTransferTo")
    private boolean ownTransferTo;

    @JsonProperty("@pay")
    private boolean pay;

    @JsonProperty("@productIdentity")
    private String productIdentity;

    @JsonProperty("@requireFourEyesConfirmation")
    private boolean requireFourEyesConfirmation;

    @JsonProperty("@signPayment")
    private boolean signPayment;

    @JsonProperty("@thirdParty")
    private boolean thirdParty;

    @JsonProperty("@view")
    private boolean view;

    public String getId() {
        return id;
    }
}
