package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.base;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient.BredBanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.ConsentDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.transactionalaccount.entity.CustomerConsent;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@RequiredArgsConstructor
public abstract class BredBanquePopulaireBaseAccountFetcher<T extends Account>
        implements AccountFetcher<T> {

    protected final BredBanquePopulaireApiClient apiClient;

    protected abstract boolean filterAccountType(AccountEntity accountEntity);

    protected abstract Optional<T> mapToAccount(AccountEntity accountEntity);

    @Override
    public Collection<T> fetchAccounts() {
        final List<AccountEntity> accountEntitiesFirstCallResult = getAccounts();

        if (accountEntitiesFirstCallResult.isEmpty()) {
            return Collections.emptyList();
        }

        final List<AccountEntity> accountEntities =
                accountEntitiesFirstCallResult.stream().anyMatch(this::doesAccountLackConsentedData)
                        ? recordCustomerConsentAndRefetchAccounts(accountEntitiesFirstCallResult)
                        : accountEntitiesFirstCallResult;

        return accountEntities.stream()
                .map(this::mapToAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<AccountEntity> getAccounts() {
        return apiClient.fetchAccounts().getAccounts().stream()
                .filter(this::filterAccountType)
                .collect(Collectors.toList());
    }

    private List<AccountEntity> recordCustomerConsentAndRefetchAccounts(
            List<AccountEntity> accountEntities) {

        apiClient.recordCustomerConsent(mapToCustomerConsent(accountEntities));
        return getAccounts();
    }

    private CustomerConsent mapToCustomerConsent(List<AccountEntity> accountEntities) {
        final List<ConsentDataEntity> consentEntities =
                accountEntities.stream()
                        .map(
                                entity ->
                                        new ConsentDataEntity(
                                                entity.getAccountId().getIban(),
                                                entity.getResourceId()))
                        .collect(Collectors.toList());

        return CustomerConsent.builder()
                .balances(consentEntities)
                .transactions(consentEntities)
                .psuIdentity(true)
                .trustedBeneficiaries(true)
                .build();
    }

    private boolean doesAccountLackConsentedData(AccountEntity accountEntity) {
        return Objects.isNull(accountEntity.getResourceId())
                || Objects.isNull(accountEntity.getLinks())
                || Objects.isNull(accountEntity.getLinks().getBalances())
                || Objects.isNull(accountEntity.getLinks().getTransactions());
    }
}
