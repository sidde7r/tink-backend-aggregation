package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.IngHolder;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.IngProduct;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
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
                .map(this::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Optional<TransactionalAccount> toTransactionalAccount(IngProduct product) {
        if (!product.isActiveTransactionalAccount()) {
            log.warn(
                    "Unknown product type for name and type: {}, {}",
                    product.getName(),
                    product.getType());
            return Optional.empty();
        }

        final TransactionalAccountType type = product.getTransactionalAccountType();
        final BalanceModule balance =
                BalanceModule.of(
                        ExactCurrencyAmount.of(product.getBalance(), product.getCurrency()));
        final IdModule id = getId(product);
        final String apiIdentifier = product.getUuid();
        final List<Party> parties = IngHolder.getParties(product);

        return TransactionalAccount.nxBuilder()
                .withType(type)
                .withInferredAccountFlags()
                .withBalance(balance)
                .withId(id)
                .setApiIdentifier(apiIdentifier)
                .addParties(parties)
                .build();
    }

    private IdModule getId(IngProduct product) {
        String alias = getAlias(product);

        return IdModule.builder()
                .withUniqueIdentifier(product.getUniqueIdentifierForTransactionAccount())
                .withAccountNumber(product.getIban())
                .withAccountName(alias)
                .addIdentifier(new IbanIdentifier(product.getBic(), product.getIbanCanonical()))
                .build();
    }

    private String getAlias(IngProduct product) {
        return Optional.ofNullable(product.getAliasOrProductName())
                .filter(StringUtils::isNotEmpty)
                .orElse(product.getIban());
    }
}
