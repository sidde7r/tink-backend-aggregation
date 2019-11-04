package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc;

import java.util.List;
import java.util.Optional;
import org.apache.commons.collections4.CollectionUtils;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.AccountTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountDetailsResponse {

    private String accountCurrency;

    private String accountNumber;

    private String iban;

    private String bicswift;

    private List<BalanceEntity> accountBalancesList;

    private AccountTransactionEntity accountTransactions;

    public BalanceEntity getBalance() {
        if (CollectionUtils.isNotEmpty(accountBalancesList)) {
            return accountBalancesList.get(0);
        }

        return null;
    }

    public List<TransactionEntity> getTransactions() {
        return Optional.ofNullable(accountTransactions)
                .orElse(new AccountTransactionEntity())
                .getTransactions();
    }

    public String getAccountCurrency() {
        return accountCurrency;
    }
}
