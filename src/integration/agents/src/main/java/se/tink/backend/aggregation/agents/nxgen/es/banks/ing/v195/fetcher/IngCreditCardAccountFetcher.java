package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.AccountStatus;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.responses.ProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.responses.entities.Product;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.amount.Amount;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IngCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final IngApiClient ingApiClient;
    private final SessionStorage sessionStorage;


    public IngCreditCardAccountFetcher(IngApiClient ingApiClient, SessionStorage sessionStorage) {
        this.ingApiClient = ingApiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<Product> products = this.sessionStorage
                .get(IngConstants.Tags.PRODUCT_LIST, ProductsResponse.class)
                .orElseGet(this.ingApiClient::getApiRestProducts)
                .getProducts();

        List<Product> creditCards = products
                .stream()
                .filter( product ->
                        AccountTypes.CREDIT_CARD.equals(product.getType()) &&
                                AccountStatus.ACTIVE.equals(product.getStatus().getCod())
                ).collect(Collectors.toList());


        // Credit cards do not have a currency field specified, so we need to look up the associated account and use
        // the currency from that.
        return creditCards.stream()
                .map(creditCard -> {
                    Optional<Product> associatedAccount = products.stream()
                            .filter(product ->
                                            creditCard.getAssociatedAccount() != null &&
                                            product.getUuid().equals(creditCard.getAssociatedAccount().getUuid()))
                            .findFirst();
                    return mapCreditCardAccount(creditCard, associatedAccount);
                }).collect(Collectors.toList());
    }

    private static CreditCardAccount mapCreditCardAccount(Product product, Optional<Product> associatedAccount) {

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
