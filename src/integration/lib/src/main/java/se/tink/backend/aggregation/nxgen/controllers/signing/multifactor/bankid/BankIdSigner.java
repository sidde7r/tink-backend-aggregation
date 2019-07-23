package se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid;

import se.tink.backend.aggregation.agents.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;

public interface BankIdSigner<T> {
    BankIdStatus collect(T toCollect) throws AuthenticationException;
}
