package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

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

    private static Optional<TransactionalAccountType> toTinkAccountType(AccountEntity entity) {

        String productCode = entity.getProductCode();

        Optional<TransactionalAccountType> translated =
                SocieteGeneraleConstants.AccountType.translate(productCode);

        if (!translated.isPresent()) {
            logger.info(
                    "{} Unknown account type: [{}], with product name: [{}] and product description [{}]",
                    SocieteGeneraleConstants.Logging.UNKNOWN_ACCOUNT_TYPE,
                    productCode,
                    entity.getLabel(),
                    entity.getDescriptiveLabel());
        }

        return translated;
    }

    private static Optional<TransactionalAccount> toTinkAccount(AccountEntity entity) {

        Optional<TransactionalAccountType> accountType = toTinkAccountType(entity);

        if (!accountType.isPresent()) {
            return Optional.empty();
        }

        return TransactionalAccount.nxBuilder()
                .withType(accountType.get())
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(entity.getBalance().toTinkAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(entity.getNumber())
                                .withAccountNumber(entity.getNumber())
                                .withAccountName(entity.getLabel())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.IBAN, entity.getNumber()))
                                .build())
                .setApiIdentifier(entity.getTechnicalId())
                .putInTemporaryStorage(
                        SocieteGeneraleConstants.StorageKey.TECHNICAL_ID, entity.getTechnicalId())
                .putInTemporaryStorage(
                        SocieteGeneraleConstants.StorageKey.TECHNICAL_CARD_ID,
                        entity.getTechnicalCardId())
                .build();
    }

    private static boolean isTransactionalAccount(AccountEntity entity) {
        switch (toTinkAccountType(entity).orElse(TransactionalAccountType.OTHER)) {
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

        return response.map(
                        accountsData ->
                                accountsData
                                        .getBenefits()
                                        .filter(
                                                SocieteGeneraleTransactionalAccountFetcher
                                                        ::isTransactionalAccount)
                                        .map(
                                                SocieteGeneraleTransactionalAccountFetcher
                                                        ::toTinkAccount)
                                        .filter(Optional::isPresent)
                                        .map(Optional::get)
                                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
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
                response.map(
                                transactionsData ->
                                        transactionsData
                                                .getTransactions()
                                                .map(TransactionEntity::toTinkTransaction)
                                                .collect(Collectors.toList()))
                        .orElse(Collections.emptyList());

        return PaginatorResponseImpl.create(transactions, transactions.size() == PAGE_SIZE);
    }
}
