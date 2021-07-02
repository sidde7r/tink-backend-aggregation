package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.authenticator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationFlow;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class LaBanquePostaleOAuth2AuthenticationController extends OAuth2AuthenticationController {

    private static final Map<String, AuthenticationException> errorMappings = new HashMap<>();

    static {
        errorMappings.put("ECHEC_VALIDATION", ThirdPartyAppError.CANCELLED.exception());
        errorMappings.put("TIMEOUT", ThirdPartyAppError.CANCELLED.exception());
        errorMappings.put("ANNULATION_CLIENT", ThirdPartyAppError.CANCELLED.exception());
    }

    public LaBanquePostaleOAuth2AuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OAuth2Authenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState) {
        super(
                persistentStorage,
                supplementalInformationHelper,
                authenticator,
                credentials,
                strongAuthenticationState);
    }

    protected void handleCallbackErrors(Map<String, String> callbackData) {
        Optional<String> error =
                OAuth2AuthenticationFlow.getCallbackElement(
                        callbackData, OAuth2Constants.CallbackParams.ERROR);
        if (error.isPresent() && errorMappings.containsKey(error.get())) {
            throw errorMappings.get(error.get());
        }
        super.handleCallbackErrors(callbackData);
    }
}
