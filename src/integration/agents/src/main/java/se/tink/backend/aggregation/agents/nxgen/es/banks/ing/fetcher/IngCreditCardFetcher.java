package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher;

import java.util.function.Function;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc.Element;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc.Product;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;

public class IngCreditCardFetcher extends ProductMovementsFetcher<CreditCardAccount, CreditCardTransaction> {

    private static boolean isCreditCardAccount(Product product) {
        return mapToKnownType(product).filter(type -> type == AccountTypes.CREDIT_CARD).isPresent();
    }

    private static CreditCardAccount toCreditCardAccount(Product product) {

        CreditCardAccount.Builder<? extends Account, ?> builder = CreditCardAccount
                .builder(product.getUniqueIdentifier());

        copyCommonAttributes(product, builder);

        builder.setAvailableCredit(new Amount(product.getCurrency(), product.getAvailableCreditAmount()));

        return builder.build();
    }

    protected CreditCardTransaction toTinkTransaction(CreditCardAccount account, Element movement) {

        CreditCardTransaction.Builder builder = new CreditCardTransaction.Builder();

        copyCommonAttributes(account, movement, builder);

        builder.setCreditAccount(account);

        return builder.build();
    }

    public IngCreditCardFetcher(IngApiClient apiClient) {
        super(apiClient);
    }

    protected Predicate<Product> productType() {
        return IngCreditCardFetcher::isCreditCardAccount;
    }

    @Override
    protected Function<Product, CreditCardAccount> toTinkAccount() {
        return IngCreditCardFetcher::toCreditCardAccount;
    }
}
