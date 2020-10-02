package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator;

import java.util.Collections;
import java.util.List;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc.GetConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.BerlinGroupAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ErstebankAuthenticator extends BerlinGroupAuthenticator {
    private final ErstebankApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final Credentials credentials;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final SupplementalInformationFormer supplementalInformationFormer;

    public ErstebankAuthenticator(
            ErstebankApiClient apiClient,
            PersistentStorage persistentStorage,
            Credentials credentials,
            SupplementalInformationHelper supplementalInformationHelper,
            SupplementalInformationFormer supplementalInformationFormer) {
        super(apiClient);
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.credentials = credentials;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.supplementalInformationFormer = supplementalInformationFormer;
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        final OAuth2Token token = apiClient.getToken(code);
        persistentStorage.put(StorageKeys.OAUTH_TOKEN, token);

        List<String> ibans = Collections.singletonList(credentials.getField(CredentialKeys.IBAN));
        final ConsentBaseResponse consent = apiClient.getConsent(ibans);
        String otp =
                supplementalInformationHelper
                        .askSupplementalInformation(
                                supplementalInformationFormer.getField(Key.OTP_INPUT))
                        .get(Key.OTP_INPUT.getFieldKey());
        final GetConsentResponse signedConsent = apiClient.signConsent(consent.getConsentId());
        persistentStorage.put(StorageKeys.CONSENT_ID, consent.getConsentId());
        apiClient.authorizeConsent(
                consent.getConsentId(), signedConsent.getAuthorisationIds().get(0), otp);
        return token;
    }
}
