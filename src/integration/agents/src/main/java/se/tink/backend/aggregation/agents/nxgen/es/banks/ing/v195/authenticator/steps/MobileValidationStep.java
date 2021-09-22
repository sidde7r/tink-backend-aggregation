package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.DeviceAction;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.BasicResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.DeviceDataRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.DismissScaRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.InsecureMobileLoginError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc.TicketResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

@RequiredArgsConstructor
@Slf4j
public class MobileValidationStep extends AbstractAuthenticationStep {
    private final IngApiClient apiClient;
    private final SessionStorage sessionStorage;

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        String personId =
                sessionStorage
                        .get(Storage.PERSON_ID, String.class)
                        .orElseThrow(
                                () ->
                                        LoginError.DEFAULT_MESSAGE.exception(
                                                "Missing neccessary personId in storage"));

        BasicResponse registerInsecurePhoneResponse =
                apiClient.registerInsecureMobileError(InsecureMobileLoginError.create(personId));
        checkBasicResponse(registerInsecurePhoneResponse);

        BasicResponse dismissScaResponse = apiClient.dismissSca(DismissScaRequest.create(personId));
        checkBasicResponse(dismissScaResponse);

        TicketResponse ticketResponse = apiClient.requestSsoTicket(new DeviceDataRequest());

        if (ticketResponse.getTicket() == null) {
            throw LoginError.DEFAULT_MESSAGE.exception(
                    "SSO Ticket for authentication missing in response ");
        }

        apiClient.postLoginAuthResponse(ticketResponse.getTicket(), DeviceAction.MOBILE_PHONE);

        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private void checkBasicResponse(BasicResponse response) {
        if (response.isFail()) {
            throw LoginError.DEFAULT_MESSAGE.exception("Error when registering rooted mobile");
        }
    }
}
