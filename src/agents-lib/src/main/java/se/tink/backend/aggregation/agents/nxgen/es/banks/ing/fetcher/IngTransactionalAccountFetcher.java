package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher;

import java.util.function.Function;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc.Element;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc.Product;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public class IngTransactionalAccountFetcher extends ProductMovementsFetcher<TransactionalAccount, Transaction> {

    private static boolean isTransactionalAccount(Product product) {
        return mapToKnownType(product).filter(type -> (type == AccountTypes.CHECKING || type == AccountTypes.SAVINGS))
                .isPresent();
    }

    private static TransactionalAccount toTransactionalAccount(Product product) {

        TransactionalAccount.Builder<? extends Account, ?> builder = TransactionalAccount
                .builder(mapToKnownType(product).get(), product.getUniqueIdentifier());

        copyCommonAttributes(product, builder);

        String bic = product.getBic();
        String iban = product.getIban();
        if (bic != null && iban != null) {
            builder.addIdentifier(new IbanIdentifier(bic, iban.replaceAll(" ", "")));
        }

        return builder.build();
    }

    protected Transaction toTinkTransaction(TransactionalAccount account, Element movement) {

        Transaction.Builder builder = new Transaction.Builder();

        copyCommonAttributes(account, movement, builder);

        return builder.build();
    }

    public IngTransactionalAccountFetcher(IngApiClient apiClient) {
        super(apiClient);
    }

    protected Predicate<Product> productType() {
        return IngTransactionalAccountFetcher::isTransactionalAccount;
    }

    @Override
    protected Function<Product, TransactionalAccount> toTinkAccount() {
        return IngTransactionalAccountFetcher::toTransactionalAccount;
    }
}
