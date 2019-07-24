package se.tink.backend.aggregation.nxgen.controllers.signing;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;

public interface Signer<T> {
    void sign(T toSign) throws AuthenticationException;
}
