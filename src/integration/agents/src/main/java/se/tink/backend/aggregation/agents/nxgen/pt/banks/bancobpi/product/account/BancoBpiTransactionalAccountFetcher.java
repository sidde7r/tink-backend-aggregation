package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.BancoBpiClientApi;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.TransactionalAccountBaseInfo;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BancoBpiTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final BancoBpiClientApi clientApi;
    private final BancoBpiEntityManager entityManager;

    public BancoBpiTransactionalAccountFetcher(
            BancoBpiClientApi clientApi, BancoBpiEntityManager entityManager) {
        this.clientApi = clientApi;
        this.entityManager = entityManager;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<TransactionalAccount> transactionalAccounts = new LinkedList<>();
        for (TransactionalAccountBaseInfo accountBaseInfo :
                entityManager.getAccountsContext().getAccountInfo()) {
            try {
                BigDecimal balance = clientApi.fetchAccountBalance(accountBaseInfo);
                transactionalAccounts.add(buildTransactionalAccount(accountBaseInfo, balance));
            } catch (RequestException ex) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception(ex.getMessage());
            }
        }
        return transactionalAccounts;
    }

    private TransactionalAccount buildTransactionalAccount(
            TransactionalAccountBaseInfo accountBaseInfo, BigDecimal balance) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(balance, accountBaseInfo.getCurrency())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountBaseInfo.getIban())
                                .withAccountNumber(accountBaseInfo.getAccountName())
                                .withAccountName(accountBaseInfo.getAccountName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.IBAN,
                                                accountBaseInfo.getIban()))
                                .build())
                .build()
                .get();
    }
}
