package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.IngHolder;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.IngProduct;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class IngCreditCardAccountFetcher implements AccountFetcher<CreditCardAccount> {

    private final IngApiClient ingApiClient;

    public IngCreditCardAccountFetcher(IngApiClient ingApiClient) {
        this.ingApiClient = ingApiClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        List<IngProduct> products = this.ingApiClient.getApiRestProducts().getProducts();
        return products.stream()
                .filter(IngProduct::isActiveCreditCardAccount)
                .map(toCreditCardAccount(products))
                .collect(Collectors.toList());
    }

    private Function<IngProduct, CreditCardAccount> toCreditCardAccount(List<IngProduct> products) {
        return creditCard ->
                toCreditCardAccount(
                        creditCard, getCurrencyOfAssociatedAccount(products, creditCard));
    }

    private CreditCardAccount toCreditCardAccount(IngProduct product, String currency) {
        CreditCardModule cardDetails = getCreditCardModule(product, currency);
        IdModule id = getId(product);
        String apiIdentifier = product.getUuid();
        Holder holder = IngHolder.toHolder(product.getHolder());

        return CreditCardAccount.nxBuilder()
                .withCardDetails(cardDetails)
                .withoutFlags()
                .withId(id)
                .setApiIdentifier(apiIdentifier)
                .addHolders(holder)
                .build();
    }

    private CreditCardModule getCreditCardModule(IngProduct product, String currency) {
        ExactCurrencyAmount balance =
                ExactCurrencyAmount.of(product.getAvailableBalance(), currency);
        ExactCurrencyAmount availableCredit =
                ExactCurrencyAmount.of(product.getAvailableCreditAmount(), currency);
        return CreditCardModule.builder()
                .withCardNumber(product.getProductNumber())
                .withBalance(balance)
                .withAvailableCredit(availableCredit)
                .withCardAlias(product.getAliasOrProductName())
                .build();
    }

    private IdModule getId(IngProduct product) {
        return IdModule.builder()
                .withUniqueIdentifier(product.getProductNumber())
                .withAccountNumber(product.getProductNumber())
                .withAccountName(product.getName())
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifier.Type.PAYMENT_CARD_NUMBER,
                                product.getProductNumber()))
                .build();
    }

    // Credit cards do not have a currency field specified, so we need to look up the associated
    // account and use the currency from that.
    private String getCurrencyOfAssociatedAccount(
            List<IngProduct> products, IngProduct creditCard) {
        return getAssociatedAccount(creditCard, products)
                .map(IngProduct::getCurrency)
                .orElse(IngConstants.CURRENCY);
    }

    private Optional<IngProduct> getAssociatedAccount(
            IngProduct creditCard, List<IngProduct> products) {
        if (creditCard.getAssociatedAccount() == null) {
            return Optional.empty();
        }

        return products.stream()
                .filter(
                        product ->
                                product.getUuid()
                                        .equals(creditCard.getAssociatedAccount().getUuid()))
                .findFirst();
    }
}
