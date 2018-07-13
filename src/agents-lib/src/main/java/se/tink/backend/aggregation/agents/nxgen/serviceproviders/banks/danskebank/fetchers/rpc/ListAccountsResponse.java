package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.rpc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.authenticator.bankid.rpc.AbstractBankIdResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankPredicates;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;

@JsonObject
public class ListAccountsResponse extends AbstractBankIdResponse {

    private String lastUpdated;
    private String languageCode;
    private List<AccountEntity> accounts;

    public String getLastUpdated() {
        return this.lastUpdated;
    }

    public String getLanguageCode() {
        return this.languageCode;
    }

    public List<AccountEntity> getAccounts() {
        return this.accounts != null ? this.accounts : Collections.emptyList();
    }

    public List<TransactionalAccount> toTinkCheckingAccounts(List<String> knownCheckingAccountProducts) {
        return this.accounts.stream()
                .filter(DanskeBankPredicates.CREDIT_CARDS.negate())
                .filter(DanskeBankPredicates.knownCheckingAccountProducts(knownCheckingAccountProducts))
                .map(AccountEntity::toCheckingAccount)
                .collect(Collectors.toList());
    }

    public List<TransactionalAccount> toTinkSavingsAccounts(List<String> knownSavingsAccountProducts) {
        return this.accounts.stream()
                .filter(DanskeBankPredicates.CREDIT_CARDS.negate())
                .filter(DanskeBankPredicates.knownSavingsAccountProducts(knownSavingsAccountProducts))
                .map(AccountEntity::toSavingsAccount)
                .collect(Collectors.toList());
    }

    public List<CreditCardAccount> toTinkCreditCardAccounts() {
        return this.accounts.stream()
                .filter(DanskeBankPredicates.CREDIT_CARDS)
                .map(AccountEntity::toCreditCardAccount)
                .collect(Collectors.toList());
    }
}
