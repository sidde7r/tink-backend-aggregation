package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.entities.AccountsData;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.entities.TransactionsData;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class SocieteGeneraleTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionPagePaginator<TransactionalAccount> {

    private static final int PAGE_SIZE = 100;
    private static final Logger logger =
            LoggerFactory.getLogger(SocieteGeneraleTransactionalAccountFetcher.class);

    private final SocieteGeneraleApiClient apiClient;

    public SocieteGeneraleTransactionalAccountFetcher(SocieteGeneraleApiClient apiClient) {
        this.apiClient = apiClient;
    }

    private static AccountTypes toTinkAccountType(AccountEntity entity) {

        String productCode = entity.getProductCode();

        Optional<AccountTypes> translated =
                SocieteGeneraleConstants.AccountType.translate(productCode);

        if (!translated.isPresent()) {
            logger.info(
                    "{} Unknown account type: {}",
                    SocieteGeneraleConstants.Logging.UNKNOWN_ACCOUNT_TYPE,
                    productCode);
        }

        return translated.orElse(AccountTypes.OTHER);
    }

    private static TransactionalAccount toTinkAccount(AccountEntity entity) {

        AccountTypes accountType = toTinkAccountType(entity);

        TransactionalAccount.Builder<? extends Account, ?> builder =
                TransactionalAccount.builder(accountType, entity.getNumber());

        builder.setName(entity.getLabel());
        builder.setAccountNumber(entity.getNumber());
        builder.setBankIdentifier(entity.getTechnicalId());

        builder.setBalance(entity.getBalance().toTinkAmount());

        builder.putInTemporaryStorage(
                SocieteGeneraleConstants.StorageKey.TECHNICAL_ID, entity.getTechnicalId());
        builder.putInTemporaryStorage(
                SocieteGeneraleConstants.StorageKey.TECHNICAL_CARD_ID, entity.getTechnicalCardId());

        return builder.build();
    }

    private static boolean isTransactionalAccount(AccountEntity entity) {
        switch (toTinkAccountType(entity)) {
            case CHECKING:
            case SAVINGS:
                return true;
            default:
                return false;
        }
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {

        Optional<AccountsData> response = apiClient.getAccounts();

        if (response.isPresent()) {
            return response.get()
                    .getBenefits()
                    .filter(SocieteGeneraleTransactionalAccountFetcher::isTransactionalAccount)
                    .map(SocieteGeneraleTransactionalAccountFetcher::toTinkAccount)
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public PaginatorResponse getTransactionsFor(TransactionalAccount account, int page) {

        String technicalId =
                account.getFromTemporaryStorage(SocieteGeneraleConstants.StorageKey.TECHNICAL_ID);
        String technicalCardId =
                account.getFromTemporaryStorage(
                        SocieteGeneraleConstants.StorageKey.TECHNICAL_CARD_ID);

        Optional<TransactionsData> response =
                apiClient.getTransactions(technicalId, technicalCardId, page, PAGE_SIZE);

        List<Transaction> transactions =
                response.isPresent()
                        ? response.get()
                                .getTransactions()
                                .map(TransactionEntity::toTinkTransaction)
                                .collect(Collectors.toList())
                        : Collections.emptyList();

        return PaginatorResponseImpl.create(transactions, transactions.size() == PAGE_SIZE);
    }
}
