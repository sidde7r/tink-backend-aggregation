package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities.UserEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.entities.WalletEntity;
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
