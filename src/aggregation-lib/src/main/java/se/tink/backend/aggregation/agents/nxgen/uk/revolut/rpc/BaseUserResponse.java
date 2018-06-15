package se.tink.backend.aggregation.agents.nxgen.uk.revolut.rpc;

import se.tink.backend.aggregation.agents.nxgen.uk.revolut.entities.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.uk.revolut.entities.WalletEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BaseUserResponse {
    private UserEntity user;
    private WalletEntity wallet;

    public UserEntity getUser() {
        return user;
    }

    public WalletEntity getWallet() {
        return wallet;
    }
}
