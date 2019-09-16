package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2;

import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Authentication;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.SpankkiAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.SpankkiKeyCardAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.SpankkiSmsOtpAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.entities.CustomerEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.sessionhandler.SpankkiSessionHandler;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.keycardandsmsotp.KeyCardAndSmsOtpAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SpankkiAgent extends NextGenerationAgent implements RefreshIdentityDataExecutor {
    private final SpankkiApiClient apiClient;

    public SpankkiAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        this.apiClient = new SpankkiApiClient(client, persistentStorage, sessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final KeyCardAndSmsOtpAuthenticationController mfaCtrl =
                new KeyCardAndSmsOtpAuthenticationController(
                        catalog,
                        supplementalInformationHelper,
                        new SpankkiKeyCardAuthenticator(
                                apiClient, persistentStorage, sessionStorage),
                        Authentication.KEY_CARD_VALUE_LENGTH,
                        new SpankkiSmsOtpAuthenticator(
                                apiClient, persistentStorage, sessionStorage),
                        Authentication.SMS_OTP_VALUE_LENGTH);

        return new AutoAuthenticationController(
                request,
                context,
                mfaCtrl,
                new SpankkiAutoAuthenticator(
                        apiClient, credentials, persistentStorage, sessionStorage));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new SpankkiSessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        // There is also the endpoint "/v2/core/customer/profile" that can also fetch identity data
        return sessionStorage
                .get(Storage.CUSTOMER_ENTITY, CustomerEntity.class)
                .map(CustomerEntity::toTinkIdentity)
                .map(FetchIdentityDataResponse::new)
                .orElseThrow(NoSuchElementException::new);
    }
}
