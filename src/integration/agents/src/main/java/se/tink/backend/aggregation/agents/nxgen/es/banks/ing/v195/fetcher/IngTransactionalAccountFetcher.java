package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.Holder;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.Product;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.transactional.TransactionalBuildStep;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
                .map(this::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTinkAccount(Product product) {
        if (!product.isActiveTransactionalAccount()) {
            return Optional.empty();
        }

        final String alias =
                Optional.ofNullable(product.getAliasOrProductName())
                        .filter(StringUtils::isNotEmpty)
                        .orElse(product.getIban());

        final TransactionalBuildStep buildStep =
                TransactionalAccount.nxBuilder()
                        .withType(product.getTransactionalAccountType())
                        .withInferredAccountFlags()
                        .withBalance(
                                BalanceModule.of(
                                        ExactCurrencyAmount.of(
                                                product.getBalance(), product.getCurrency())))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(
                                                product.getUniqueIdentifierForTransactionAccount())
                                        .withAccountNumber(product.getIban())
                                        .withAccountName(alias)
                                        .addIdentifier(
                                                new IbanIdentifier(
                                                        product.getBic(),
                                                        product.getIbanCanonical()))
                                        .setProductName(product.getName())
                                        .build())
                        .setApiIdentifier(product.getUuid());

        product.getHolders().stream().map(Holder::getAnyName).forEach(buildStep::addHolderName);

        return buildStep.build();
    }
}
