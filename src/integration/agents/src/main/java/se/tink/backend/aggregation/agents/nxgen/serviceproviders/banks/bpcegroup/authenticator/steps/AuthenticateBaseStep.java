package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize.AuthTransactionResponseDto;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.entities.AuthResponseStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;

@Slf4j
public abstract class AuthenticateBaseStep extends AbstractAuthenticationStep {

    static void validateAuthenticationSucceeded(
            AuthTransactionResponseDto authTransactionResponseDto) throws AuthenticationException {
        if (Objects.isNull(authTransactionResponseDto.getResponse())
                || Objects.isNull(authTransactionResponseDto.getResponse().getStatus())) {
            throw AuthorizationError.UNAUTHORIZED.exception();
        }

        final String status = authTransactionResponseDto.getResponse().getStatus();

        if (!AuthResponseStatus.AUTHENTICATION_SUCCESS.getName().equalsIgnoreCase(status)) {
            log.error("Authentication failed with status: " + status);

            if (AuthResponseStatus.AUTHENTICATION_LOCKED.getName().equalsIgnoreCase(status)) {
                throw AuthorizationError.ACCOUNT_BLOCKED.exception();
            } else {
                throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception();
            }
        }
    }
}
