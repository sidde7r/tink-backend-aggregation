package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost.authenticator;

import com.google.common.base.Strings;
import java.util.Map;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aDevelopersAuthenticatorHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BPostAuthenticatorHelper extends Xs2aDevelopersAuthenticatorHelper {

    public BPostAuthenticatorHelper(
            Xs2aDevelopersApiClient apiClient,
            PersistentStorage persistentStorage,
            Xs2aDevelopersProviderConfiguration configuration,
            LocalDateTimeSource localDateTimeSource,
            Credentials credentials) {
        super(apiClient, persistentStorage, configuration, localDateTimeSource, credentials);
    }

    @Override
    public void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {
        String value = callbackData.getOrDefault(CallbackParams.ERROR, null);
        if (!Strings.isNullOrEmpty(value)
                && value.contains(
                        "The Strong Customer Authentication solution encountered an error")) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        }
    }
}
