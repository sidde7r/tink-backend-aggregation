package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@JsonObject
public class FetchAccountResponse {

    @JsonProperty("Productos")
    private List<AccountEntity> accounts;

    @JsonIgnore
    public List<TransactionalAccount> getAccounts(IberCajaSessionStorage sessionStorage) {
        return accounts.stream()
                .filter(AccountEntity::isTransactionalAccount)
                .map(accountEntity -> accountEntity.toTinkAccount(sessionStorage))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<InvestmentAccount> getInvestmentAccounts() {
        return accounts.stream()
                .filter(AccountEntity::isInvestmentAccount)
                .map(AccountEntity::toTinkInvestmentAccount)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public List<CreditCardAccount> getCreditCardAccounts() {
        return accounts.stream()
                .filter(AccountEntity::isCreditCardAccount)
                .map(AccountEntity::toTinkCreditCardAccount)
                .collect(Collectors.toList());
    }
}
