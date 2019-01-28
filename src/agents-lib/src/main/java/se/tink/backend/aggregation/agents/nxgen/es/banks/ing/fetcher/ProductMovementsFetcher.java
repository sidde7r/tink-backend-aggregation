package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher;

import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc.Element;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc.Movements;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc.Product;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;

abstract class ProductMovementsFetcher<A extends Account, T extends Transaction> implements AccountFetcher<A>,
        TransactionMonthPaginator<A> {

    protected static final AggregationLogger LOGGER = new AggregationLogger(ProductMovementsFetcher.class);

    private final IngApiClient apiClient;

    public ProductMovementsFetcher(IngApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public final Collection<A> fetchAccounts() {

        List<Product> products = apiClient.getApiRestProducts();

        if (products.size() <= 0) {
            return Collections.emptyList();
        }

        return products.stream().filter(productType()).map(toTinkAccount()).collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(A account, Year year, Month month) {

        Product product = account.getFromTemporaryStorage(IngConstants.ORIGINAL_ENTITY, Product.class)
                .orElseThrow(() -> new IllegalStateException("No product supplied"));

        List<T> transactions = new ArrayList<>();

        // First day of the month
        LocalDate fromDate = LocalDate.of(year.getValue(), month, 1);
        // Last day of the month
        LocalDate toDate = fromDate.with(TemporalAdjusters.lastDayOfMonth());
        int page = 0;
        Movements movements = null;

        do {

            movements = apiClient.getApiRestProductsMovements(product.getUuid(), fromDate, toDate, page);

            if (movements != null && movements.getElements() != null) {
                movements.getElements().forEach(movement -> transactions.add(toTinkTransaction(account, movement)));
            }

            page++;
        } while (movements.getTotal() > movements.getOffset() + movements.getCount());

        return PaginatorResponseImpl.create(transactions);
    }

    protected abstract Predicate<Product> productType();

    protected abstract Function<Product, A> toTinkAccount();

    protected abstract T toTinkTransaction(A account, Element movement);

    protected static Optional<AccountTypes> mapToKnownType(Product product) {
        return IngConstants.ProductType.translate(product);
    }

    protected static void copyCommonAttributes(Product product, Account.Builder builder) {
        builder.setAccountNumber(product.getIban())
                .setName(product.getName())
                .setBankIdentifier(product.getBank())
                .setBalance(new Amount(product.getCurrency(), product.getBalance()))
                .setHolderName(new HolderName(product.getHolder().getCompleteName()))
                .putInTemporaryStorage(IngConstants.ORIGINAL_ENTITY, product);
    }

    protected static void copyCommonAttributes(Account account, Element movement,
            Transaction.Builder builder) {
        builder.setAmount(new Amount(account.getBalance().getCurrency(), movement.getAmount()))
                .setDate(IngUtils.toJavaLangDate(movement.getEffectiveDate()))
                .setDescription(movement.getDescription())
                .setPending(false);
    }
}
