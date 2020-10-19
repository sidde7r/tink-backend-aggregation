package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.exceptions.refresh.TransactionRefreshException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngProxyApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities.TransactionsResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.helper.IngMiscUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class IngTransactionFetcher
        implements TransactionKeyPaginator<TransactionalAccount, String> {

    private final IngProxyApiClient ingProxyApiClient;

    public IngTransactionFetcher(IngProxyApiClient ingProxyApiClient) {
        this.ingProxyApiClient = ingProxyApiClient;
    }

    @Override
    public TransactionKeyPaginatorResponse<String> getTransactionsFor(
            TransactionalAccount account, String key) {
        String transactionsHref = account.getFromTemporaryStorage(Storage.TRANSACTIONS_HREF);

        TransactionsResponseEntity response;
        if (key == null) {
            response =
                    ingProxyApiClient.getTransactionsFirstPage(
                            transactionsHref, map(account.getType()));
        } else {
            String[] split = key.split("\\?");
            String path = split[0];
            String query = split[1];
            response = ingProxyApiClient.getTransactionsNextPages(path, query);
        }

        return new IngTransactionPageResponse(map(response.getTransactions()), getNext(response));
    }

    private List<Transaction> map(List<TransactionEntity> transactions) {
        return transactions.stream().map(this::map).collect(Collectors.toList());
    }

    private Transaction map(TransactionEntity transactionEntity) {
        return Transaction.builder()
                .setAmount(
                        ExactCurrencyAmount.of(
                                transactionEntity.getAmount().getValue(),
                                transactionEntity.getAmount().getCurrency()))
                .setDate(transactionEntity.getExecutionDate())
                .setRawDetails(mapRawDetails(transactionEntity))
                .setDescription(extractDescription(transactionEntity))
                .build();
    }

    private String map(AccountTypes type) {
        if (AccountTypes.CHECKING.equals(type)) {
            return "CURRENT";
        } else if (AccountTypes.SAVINGS.equals(type)) {
            return "SAVINGS";
        }
        throw new TransactionRefreshException("Unknown account type " + type);
    }

    private String extractDescription(TransactionEntity entity) {
        String subject = entity.getSubject();
        if (subject != null) {
            int lastLineSep = subject.lastIndexOf('\n');
            return IngMiscUtils.cleanDescription(subject.substring(lastLineSep + 1));
        }
        List<String> subjectLines = entity.getSubjectLines();
        if (subjectLines != null && !subjectLines.isEmpty()) {
            if (subjectLines.size() > 1) {
                return subjectLines.get(1);
            }
            return subjectLines.get(0);
        }
        return "";
    }

    private IngRawDetails mapRawDetails(TransactionEntity entity) {
        if (entity.getSubjectLines() == null) {
            return null;
        }

        List<String> details = new ArrayList<>();
        List<String> extraDetails = new ArrayList<>();

        boolean extra = false;
        for (String subjectLine : entity.getSubjectLines()) {
            if (subjectLine.equals("\n")) {
                extra = true;
                continue;
            }
            if (extra) {
                extraDetails.add(subjectLine.trim());
            } else {
                details.add(subjectLine.trim());
            }
        }

        return new IngRawDetails(details, extraDetails);
    }

    private String getNext(TransactionsResponseEntity response) {
        return response.getLinks().stream()
                .filter(link -> "next".equals(link.getRel()))
                .map(LinkEntity::getHref)
                .map(IngMiscUtils::decodeUrl)
                .findFirst()
                .orElse(null);
    }
}
