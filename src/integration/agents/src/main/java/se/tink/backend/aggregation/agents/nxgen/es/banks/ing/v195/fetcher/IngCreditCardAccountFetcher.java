package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.Product;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.Amount;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IngCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final IngApiClient ingApiClient;

    public IngCreditCardAccountFetcher(IngApiClient ingApiClient) {
        this.ingApiClient = ingApiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<Product> products = this.ingApiClient.getApiRestProducts().getProducts();

        List<Product> creditCards = products
                .stream()
                .filter(Product::isActiveCreditCardAccount)
                .collect(Collectors.toList());

        // Credit cards do not have a currency field specified, so we need to look up the associated account and use
        // the currency from that.
        return creditCards.stream()
                .map(creditCard -> mapCreditCardAccount(creditCard, lookupAssociatedAccount(creditCard, products)))
                .collect(Collectors.toList());
    }

    private Optional<Product> lookupAssociatedAccount(Product creditCard, List<Product> products) {
        if (creditCard.getAssociatedAccount() == null) {
            return Optional.empty();
        }

        return products.stream()
                .filter(product -> product.getUuid().equals(creditCard.getAssociatedAccount().getUuid()))
                .findFirst();
    }

    private CreditCardAccount mapCreditCardAccount(Product product, Optional<Product> associatedAccount) {

        String currency = associatedAccount.isPresent() ? associatedAccount.get().getCurrency() : IngConstants.CURRENCY;

        return CreditCardAccount.builderFromFullNumber(product.getProductNumber())
                .setAvailableCredit(new Amount(currency, product.getAvailableBalance()))
                .setBalance(new Amount(currency, product.getSpentAmount()))
                .setBankIdentifier(product.getUuid())
                .setHolderName(new HolderName(product.getHolder().getAnyName()))
                .setName(product.getName())
                .build();
    }
}
