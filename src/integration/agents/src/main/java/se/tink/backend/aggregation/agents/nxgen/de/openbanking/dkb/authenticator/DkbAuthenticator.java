package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.xml.bind.DatatypeConverter;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.DkbConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.entities.AccessItemEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.rpc.GetConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.rpc.GetConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator.rpc.GetTokenForm;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.configuration.DkbConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class DkbAuthenticator implements PasswordAuthenticator {
    private final DkbApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final DkbConfiguration configuration;
    private final String iban;

    public DkbAuthenticator(
            DkbApiClient apiClient,
            PersistentStorage persistentStorage,
            DkbConfiguration configuration,
            String iban) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
        this.iban = iban;
    }

    private DkbConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public void authenticate(String username, String password)
            throws AuthenticationException, AuthorizationException {
        final GetTokenForm getTokenForm = createGetTokenForm(username, password);
        final String clientId = configuration.getClientId();
        final String clientSecret = configuration.getClientSecret();

        final OAuth2Token token =
                apiClient.authenticate(clientId, clientSecret, getTokenForm).toTinkToken();

        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);

        final GetConsentRequest getConsentRequest = createGetConsentRequest();
        final GetConsentResponse getConsentResponse = apiClient.getConsent(getConsentRequest);

        persistentStorage.put(StorageKeys.CONSENT_ID, getConsentResponse.getConsentId());
    }

    private GetConsentRequest createGetConsentRequest() {
        final List<AccessItemEntity> accessItemEntityList =
                Collections.singletonList(
                        new AccessItemEntity(
                                iban,
                                FormValues.BBAN,
                                FormValues.PAN,
                                FormValues.MASKED_PAN,
                                FormValues.MSISDN,
                                FormValues.EUR));
        final AccessEntity accessEntity =
                new AccessEntity(
                        accessItemEntityList,
                        accessItemEntityList,
                        accessItemEntityList,
                        FormValues.ALL_ACCOUNTS,
                        FormValues.ALL_ACCOUNTS);

        return new GetConsentRequest(
                accessEntity,
                FormValues.FALSE,
                FormValues.FREQUENCY_PER_DAY,
                FormValues.FALSE,
                FormValues.VALID_UNTIL);
    }

    private GetTokenForm createGetTokenForm(String username, String password) {
        return GetTokenForm.builder()
                .setGrantType(FormValues.PASSWORD)
                .setUsername(username)
                .setPassword(password)
                .build();
    }

    private String toBase64String(byte[] bytes) {
        return DatatypeConverter.printBase64Binary(bytes);
    }
}
