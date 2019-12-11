package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.transaction;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.BancoBpiEntityManager;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.TransactionalAccountBaseInfo;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BancoBpiTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final TinkHttpClient httpClient;
    private final BancoBpiEntityManager entityManager;

    public BancoBpiTransactionalAccountFetcher(
            TinkHttpClient httpClient, BancoBpiEntityManager entityManager) {
        this.httpClient = httpClient;
        this.entityManager = entityManager;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<TransactionalAccount> transactionalAccounts = new LinkedList<>();
        for (TransactionalAccountBaseInfo accountBaseInfo :
                entityManager.getTransactionalAccounts().getAccountInfo()) {
            try {
                BigDecimal balance =
                        new TransactionalAccountBalanceRequest(
                                        entityManager.getAuthContext(), accountBaseInfo)
                                .call(httpClient);
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
                                .withAccountNumber(accountBaseInfo.getInternalAccountId())
                                .withAccountName(accountBaseInfo.getAccountName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.IBAN,
                                                accountBaseInfo.getIban()))
                                .build())
                .build()
                .get();
    }
}
