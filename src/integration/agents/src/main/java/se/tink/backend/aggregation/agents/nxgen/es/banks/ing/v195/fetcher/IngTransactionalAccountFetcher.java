package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.AccountCategories;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.Product;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.BuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.builder.UniqueIdentifierStep;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

public class IngTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final IngApiClient ingApiClient;
    private final SessionStorage sessionStorage;

    public IngTransactionalAccountFetcher(
            IngApiClient ingApiClient, SessionStorage sessionStorage) {
        this.ingApiClient = ingApiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return this.ingApiClient.getApiRestProducts().getProducts().stream()
                .filter(Product::isActiveTransactionalAccount)
                .map(product -> mapTransactionalAccount(product))
                .collect(Collectors.toList());
    }

    private TransactionalAccount mapTransactionalAccount(Product product) {
        UniqueIdentifierStep<? extends BuildStep> builder;
        if (AccountCategories.TRANSACTION_ACCOUNTS.contains(product.getType())) {
            builder = CheckingAccount.builder();
        } else if (AccountCategories.SAVINGS_ACCOUNTS.contains(product.getType())) {
            builder = SavingsAccount.builder();
        } else {
            throw new IllegalStateException("Unknown account type");
        }

        String alias =
                Optional.ofNullable(product.getAliasOrProductName())
                        .filter(StringUtils::isNotEmpty)
                        .orElse(product.getIban());

        BuildStep buildStep =
                builder.setUniqueIdentifier(product.getUniqueIdentifierForTransactionAccount())
                        .setAccountNumber(product.getIban())
                        .setBalance(new Amount(product.getCurrency(), product.getBalance()))
                        .setAlias(alias)
                        .addAccountIdentifier(
                                new IbanIdentifier(product.getBic(), product.getIbanCanonical()))
                        .setApiIdentifier(product.getUuid())
                        .setProductName(product.getName());

        product.getHolders()
                .forEach(
                        holder -> {
                            buildStep.addHolderName(holder.getAnyName());
                        });

        return (TransactionalAccount) buildStep.build();
    }
}
