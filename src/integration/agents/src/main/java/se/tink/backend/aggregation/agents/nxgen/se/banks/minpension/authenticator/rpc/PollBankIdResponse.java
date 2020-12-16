package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.rpc;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.authenticator.entities.BankIdUserEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class PollBankIdResponse {
    private String authenticationStatus;
    private BankIdUserEntity bankIdUser;
    private String errorMessage;
}
