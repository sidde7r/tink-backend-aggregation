package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc.collect;

import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.entities.collect.PollBankIDSIMAuthenticationInEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BankIdCollectRequestBody {
    private PollBankIDSIMAuthenticationInEntity pollBankIDSIMAuthenticationInEntity = new PollBankIDSIMAuthenticationInEntity();

    public BankIdCollectRequestBody(String sessionId) {
        pollBankIDSIMAuthenticationInEntity.setId(sessionId);
    }

    public PollBankIDSIMAuthenticationInEntity getPollBankIDSIMAuthenticationInEntity() {
        return pollBankIDSIMAuthenticationInEntity;
    }
}
