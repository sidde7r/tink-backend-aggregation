package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;

@Slf4j
public class AccountConsentManager {

    private final CreditAgricoleBaseApiClient apiClient;
    private final AtomicBoolean wasConsentSent = new AtomicBoolean();

    public AccountConsentManager(CreditAgricoleBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    boolean prepareConsentsIfNeeded(@NonNull GetAccountsResponse accountsResponse) {
        if (wasConsentSent.get()) {
            log.info("[ACCOUNT CONSENT MANAGER] Consents were already prepared before");
            return false;
        }

        List<AccountIdEntity> accountsWithoutConsent =
                accountsResponse.getAccountsListForNecessaryConsents();
        if (accountsWithoutConsent.isEmpty()) {
            log.info("[ACCOUNT CONSENT MANAGER] No accounts without consents");
            return false;
        }

        log.info(
                "[ACCOUNT CONSENT MANAGER] Sending consents for {} accounts",
                accountsWithoutConsent.size());
        apiClient.putConsents(accountsWithoutConsent);
        wasConsentSent.set(true);
        return true;
    }
}
