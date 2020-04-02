package se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid;

import java.util.Optional;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;

public interface BankIdSigner<T> {
    BankIdStatus collect(T toCollect) throws AuthenticationException;

    default Optional<String> getAutostartToken() {
        return Optional.empty();
    }
}
