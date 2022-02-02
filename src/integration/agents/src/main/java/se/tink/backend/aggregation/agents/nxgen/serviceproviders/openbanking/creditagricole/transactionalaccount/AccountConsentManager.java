package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount;

import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.apiclient.CreditAgricoleBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.AccountIdEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.rpc.GetAccountsResponse;

/**
 * Whenever sending consents, we should always put ALL accounts in the request. New consent is
 * overriding old one and not putting all can result in lack of resources.
 */
@Slf4j
public class AccountConsentManager {

    private final CreditAgricoleBaseApiClient apiClient;
    private boolean wasConsentSent;

    public AccountConsentManager(CreditAgricoleBaseApiClient apiClient) {
        this.apiClient = apiClient;
    }

    boolean prepareConsentsIfNeeded(@NonNull GetAccountsResponse accountsResponse) {
        if (wasConsentSent) {
            log.info("[ACCOUNT CONSENT MANAGER] Consents were already prepared before");
            return false;
        }

        List<AccountIdEntity> accountsWithoutConsent =
                accountsResponse.getAccountsListForNecessaryConsents();
        if (accountsWithoutConsent.isEmpty()) {
            log.info("[ACCOUNT CONSENT MANAGER] No accounts without consents");
            return false;
        }

        List<AccountIdEntity> allAccountIds =
                accountsResponse.getAccounts().stream()
                        .map(AccountEntity::getAccountId)
                        .collect(Collectors.toList());
        log.info("[ACCOUNT CONSENT MANAGER] Sending consent for {} accounts", allAccountIds.size());
        apiClient.putConsents(allAccountIds);
        wasConsentSent = true;
        return true;
    }
}
