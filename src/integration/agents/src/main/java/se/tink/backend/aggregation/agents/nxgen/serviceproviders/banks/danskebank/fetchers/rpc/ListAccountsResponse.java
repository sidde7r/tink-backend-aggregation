package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankPredicates;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractBankIdResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class ListAccountsResponse<T extends AccountEntity> extends AbstractBankIdResponse {

    private String lastUpdated;
    private String languageCode;
    private List<T> accounts;

    public String getLastUpdated() {
        return this.lastUpdated;
    }

    public String getLanguageCode() {
        return this.languageCode;
    }

    public List<T> getAccounts() {
        return this.accounts != null ? this.accounts : Collections.emptyList();
    }

    public List<TransactionalAccount> toTinkCheckingAccounts(
            List<String> knownCheckingAccountProducts) {
        return this.accounts.stream()
                .filter(DanskeBankPredicates.CREDIT_CARDS.negate())
                .filter(
                        DanskeBankPredicates.knownCheckingAccountProducts(
                                knownCheckingAccountProducts))
                .map(AccountEntity::toCheckingAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<TransactionalAccount> toTinkSavingsAccounts(
            List<String> knownSavingsAccountProducts) {
        return this.accounts.stream()
                .filter(DanskeBankPredicates.CREDIT_CARDS.negate())
                .filter(
                        DanskeBankPredicates.knownSavingsAccountProducts(
                                knownSavingsAccountProducts))
                .map(AccountEntity::toSavingAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<CreditCardAccount> toTinkCreditCardAccounts(DanskeBankConfiguration configuration) {
        return this.accounts.stream()
                .filter(DanskeBankPredicates.CREDIT_CARDS)
                .map(account -> account.toCreditCardAccount(configuration))
                .collect(Collectors.toList());
    }

    public boolean isOwnAccount(String identifier) {
        return accounts.stream()
                .anyMatch(
                        accountEntity ->
                                accountEntity.getAccountNoExt().equalsIgnoreCase(identifier));
    }

    public AccountEntity findAccount(String identifier) {
        return accounts.stream()
                .filter(
                        accountEntity ->
                                accountEntity.getAccountNoExt().equalsIgnoreCase(identifier))
                .findFirst()
                .orElse(null);
    }
}
