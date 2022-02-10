package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdPollTimeoutException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecStorage;
import se.tink.backend.aggregation.agents.utils.supplementalfields.DanishFields;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.i18n_aggregation.Catalog;
import se.tink.libraries.retrypolicy.RetryCallback;
import se.tink.libraries.retrypolicy.RetryExecutor;
import se.tink.libraries.retrypolicy.RetryPolicy;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BecAuthWithCodeAppStep {

    private static final int POLL_NEM_ID_MAX_ATTEMPTS = 10;

    private final BecApiClient apiClient;
    private final Credentials credentials;
    private final BecStorage storage;
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public void authenticate() {
        String nemIdToken = sendNemIdRequest();

        displayPromptSync();
        pollNemId(nemIdToken);

        String scaToken = exchangeNemIdTokenForScaToken(nemIdToken);
        storage.saveScaToken(scaToken);
    }

    private String sendNemIdRequest() {
        return apiClient
                .getNemIdToken(
                        credentials.getField(Field.Key.USERNAME),
                        credentials.getField(Field.Key.PASSWORD),
                        storage.getDeviceId())
                .getCodeappTokenDetails()
                .getToken();
    }

    private void displayPromptSync() {
        Field field = DanishFields.NemIdInfo.build(catalog);

        try {
            supplementalInformationController.askSupplementalInformationSync(field);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }

    private void pollNemId(String nemIdToken) {
        RetryExecutor retryExecutor = new RetryExecutor();
        retryExecutor.setRetryPolicy(
                new RetryPolicy(POLL_NEM_ID_MAX_ATTEMPTS, NemIdPollTimeoutException.class));
        retryExecutor.execute(
                (RetryCallback<Void, AuthenticationException>)
                        () -> {
                            apiClient.pollNemId(nemIdToken);
                            return null;
                        });
    }

    private String exchangeNemIdTokenForScaToken(String nemIdToken) {
        return apiClient
                .authCodeApp(
                        credentials.getField(Key.USERNAME),
                        credentials.getField(Key.PASSWORD),
                        nemIdToken,
                        storage.getDeviceId())
                .getScaToken();
    }
}
