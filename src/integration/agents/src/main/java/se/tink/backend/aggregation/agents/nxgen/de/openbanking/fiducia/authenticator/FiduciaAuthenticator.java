package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator;

import java.security.interfaces.RSAPrivateKey;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.SignatureValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.Access;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.AccessDetails;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.entities.PsuData;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.AuthorizeConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.CreateConsentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.rpc.CreateConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.configuration.FiduciaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils.JWTUtils;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils.SignatureUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FiduciaAuthenticator implements Authenticator {

    private final FiduciaApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final FiduciaConfiguration configuration;
    private final String iban;
    private final String psuId;
    private final String password;
    private final String certificate;
    private final String keyId;
    private final RSAPrivateKey privateKey;

    public FiduciaAuthenticator(
            FiduciaApiClient apiClient,
            PersistentStorage persistentStorage,
            FiduciaConfiguration configuration,
            String iban,
            String psuId,
            String password) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.configuration = configuration;
        this.iban = iban;
        this.psuId = psuId;
        this.password = password;

        certificate = JWTUtils.readFile(configuration.getCertificatePath());
        keyId = configuration.getKeyId();
        privateKey = JWTUtils.getKey(configuration.getKeyPath());
    }

    private FiduciaConfiguration getConfiguration() {
        return Optional.ofNullable(configuration)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        CreateConsentResponse createConsentResponse = createConsent();
        persistentStorage.put(StorageKeys.CONSENT_ID, createConsentResponse.getConsentId());
        authorizeConsent(createConsentResponse);
    }

    private CreateConsentResponse createConsent() {
        CreateConsentRequest createConsentRequest =
                new CreateConsentRequest(
                        new Access(
                                Collections.singletonList(new AccessDetails(iban)),
                                Collections.singletonList(new AccessDetails(iban))),
                        FormValues.FREQUENCY_PER_DAY,
                        FormValues.TRUE,
                        FormValues.VALID_UNTIL,
                        FormValues.FALSE);

        String digest =
                SignatureUtils.createDigest(
                        SerializationUtils.serializeToString(createConsentRequest));
        String date = SignatureUtils.getCurrentDateFormatted();

        String reqId = String.valueOf(UUID.randomUUID());
        String signature =
                SignatureUtils.createSignature(
                        privateKey,
                        keyId,
                        SignatureValues.HEADERS_WITH_PSU_ID,
                        digest,
                        reqId,
                        date,
                        psuId);

        return apiClient.createConsent(
                createConsentRequest, digest, certificate, signature, reqId, date, psuId);
    }

    private void authorizeConsent(CreateConsentResponse createConsentResponse) {

        AuthorizeConsentRequest authorizeConsentRequest =
                new AuthorizeConsentRequest(new PsuData(password));
        String digest2 =
                SignatureUtils.createDigest(
                        SerializationUtils.serializeToString(authorizeConsentRequest));
        String date2 = SignatureUtils.getCurrentDateFormatted();
        String reqId2 = String.valueOf(UUID.randomUUID());
        String signature2 =
                SignatureUtils.createSignature(
                        privateKey, keyId, SignatureValues.HEADERS, digest2, reqId2, date2, null);

        apiClient.authorizeConsent(
                createConsentResponse,
                digest2,
                certificate,
                signature2,
                reqId2,
                date2,
                authorizeConsentRequest);
    }
}
