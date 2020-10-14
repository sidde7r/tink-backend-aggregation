package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps;

import org.assertj.core.util.Strings;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngComponents;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngProxyApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.AuthenticateRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.AuthenticateResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngRequestFactory;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;

public class AuthenticateStep extends AbstractAuthenticationStep {

    private static final String CARD_ID_FIELD = "cardId";

    private final IngProxyApiClient ingProxyApiClient;
    private final IngStorage ingStorage;
    private final IngRequestFactory ingRequestFactory;

    public AuthenticateStep(IngComponents ingComponents) {
        super("AUTHENTICATE");
        this.ingProxyApiClient = ingComponents.getIngProxyApiClient();
        this.ingStorage = ingComponents.getIngStorage();
        this.ingRequestFactory = ingComponents.getIngRequestFactory();
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        String username = request.getCredentials().getField(Field.Key.USERNAME);
        String cardNumber = request.getCredentials().getField(CARD_ID_FIELD);
        String identifyOtp = ingStorage.getForSession(Storage.OTP);

        if (Strings.isNullOrEmpty(username)
                || Strings.isNullOrEmpty(cardNumber)
                || Strings.isNullOrEmpty(identifyOtp)) {
            throw LoginError.INCORRECT_CREDENTIALS.exception(
                    "Did not receive input for authentication");
        }

        authenticate(username, cardNumber, identifyOtp);

        return AuthenticationStepResponse.executeNextStep();
    }

    public void authenticate(String ingId, String cardNr, String identifyOtp) {
        AuthenticateRequestEntity request =
                ingRequestFactory.createAuthenticateRequestEntity(identifyOtp, ingId, cardNr);

        AuthenticateResponseEntity authenticate = ingProxyApiClient.authenticate(request);

        String accessToken = authenticate.getAccessToken().findAccessToken();

        ingStorage.storeAccessToken(accessToken);
    }
}
