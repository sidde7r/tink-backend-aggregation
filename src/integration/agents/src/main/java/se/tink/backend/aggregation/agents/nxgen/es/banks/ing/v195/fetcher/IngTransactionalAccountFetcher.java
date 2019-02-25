package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.AccountCategories;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.responses.ProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.responses.entities.Product;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.BuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.UniqueIdentifierStep;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class IngTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final IngApiClient ingApiClient;
    private final SessionStorage sessionStorage;

    public IngTransactionalAccountFetcher(IngApiClient ingApiClient, SessionStorage sessionStorage) {
        this.ingApiClient = ingApiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<Product> products = this.sessionStorage
                .get(IngConstants.Tags.PRODUCT_LIST, ProductsResponse.class)
                .orElseGet(this.ingApiClient::getApiRestProducts)
                .getProducts();

        return products
                .stream()
                .filter(IngTransactionalAccountFetcher::transactionalAccountFilter)
                .map(IngTransactionalAccountFetcher::mapTransactionalAccount)
                .collect(Collectors.toList());
    }


    private static TransactionalAccount mapTransactionalAccount(Product product) {
        UniqueIdentifierStep<? extends BuildStep> builder;
        if (AccountCategories.TRANSACTION_ACCOUNTS.contains(product.getType())) {
            builder = CheckingAccount.builder();
        } else if (AccountCategories.SAVINGS_ACCOUNTS.contains(product.getType())) {
            builder = SavingsAccount.builder();
        } else {
            throw new IllegalStateException("Unknown account type");
        }

        BuildStep buildStep = builder.setUniqueIdentifier(product.getIban())
                .setAccountNumber(product.getIban())
                .setBalance(new Amount(product.getCurrency(), product.getBalance()))
                .addAccountIdentifier(new IbanIdentifier(product.getBic(), product.getIbanCanonical()))
                .setApiIdentifier(product.getUuid())
                .setProductName(product.getName());

        if (!Strings.isNullOrEmpty(product.getAlias())) {
            buildStep.setAlias(product.getAlias());
        }

        product.getHolders().forEach(holder -> {
            buildStep.addHolderName(holder.getAnyName());
        });

        return (TransactionalAccount) buildStep.build();
    }

    private static boolean transactionalAccountFilter(Product product) {
        boolean isTransactionalAccount = AccountCategories.TRANSACTION_ACCOUNTS.contains(product.getType());

        boolean isOperative = IngConstants.AccountStatus.OPERATIVE.equals(product.getStatus().getCod());

        return isTransactionalAccount && isOperative;
    }
}
