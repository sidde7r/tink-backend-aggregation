package se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.ErstebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.erstebank.authenticator.rpc.ConsentSignResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.BerlinGroupAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc.ConsentBaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountBaseEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.rpc.AccountsBaseResponse;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class ErstebankAuthenticator extends BerlinGroupAuthenticator {
    private final ErstebankApiClient apiClient;
    private final SessionStorage sessionStorage;

    public ErstebankAuthenticator(ErstebankApiClient apiClient, SessionStorage sessionStorage) {
        super(apiClient);
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        final OAuth2Token token = apiClient.getToken(code);
        sessionStorage.put(StorageKeys.OAUTH_TOKEN, token);

        final AccountsBaseResponse accounts = apiClient.fetchAccounts();
        List<String> ibans =
                accounts.getAccounts().stream()
                        .map(AccountBaseEntity::getIban)
                        .collect(Collectors.toList());

        final ConsentBaseResponse consent = apiClient.getConsent(ibans);
        final ConsentSignResponse signedConsent = apiClient.signConsent(consent.getConsentId());
        sessionStorage.put(StorageKeys.CONSENT_ID, signedConsent.getAuthorisationId());
        return token;
    }
}
