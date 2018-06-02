package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class AbstractBankIdResponse {
    public enum BankIdResponseStatus {
        CLIENT_NOT_STARTED, USER_SIGN, COMPLETE;
    }

    private BankIdResponseStatus status;
    private LinksEntity links;

    public BankIdResponseStatus getStatus() {
        return status;
    }

    public LinksEntity getLinks() {
        return Optional.ofNullable(links).orElseThrow(IllegalStateException::new);
    }
}
