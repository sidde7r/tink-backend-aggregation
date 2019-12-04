package se.tink.backend.aggregation.nxgen.controllers.authentication;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;

/**
 * Given an authentication request, provides an authentication response. A step can be followed by
 * another step, forming a linked list.
 */
@FunctionalInterface
public interface AuthenticationStep {

    /**
     * @param request
     * @return has to return Optional.empty() when all data are in place to execute logic otherwise
     *     Optional.of(SupplementInformationRequester) when need ask for some supplement information
     * @throws AuthenticationException
     * @throws AuthorizationException
     */
    Optional<SupplementInformationRequester> execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException;

    default String getIdentifier() {
        return this.getClass().getName();
    }
}
