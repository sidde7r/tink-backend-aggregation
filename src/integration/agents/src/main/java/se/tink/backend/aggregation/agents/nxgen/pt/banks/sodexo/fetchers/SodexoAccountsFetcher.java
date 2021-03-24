package se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.fetchers;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.SodexoStorage;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.sodexo.rpc.BalanceResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SodexoAccountsFetcher implements AccountFetcher<TransactionalAccount> {

    private final SodexoApiClient sodexoApiClient;
    private final SodexoStorage sodexoStorage;

    public SodexoAccountsFetcher(
            final SodexoApiClient sodexoApiClient, final SodexoStorage sodexoStorage) {
        this.sodexoApiClient = sodexoApiClient;
        this.sodexoStorage = sodexoStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return Collections.singletonList(mapAccount(sodexoApiClient.getBalanceResponse()).get());
    }

    private Optional<TransactionalAccount> mapAccount(BalanceResponse balanceResponse) {

        ExactCurrencyAmount amount =
                ExactCurrencyAmount.of(balanceResponse.getBalance(), SodexoConstants.CURRENCY);

        BalanceModule balanceModule =
                BalanceModule.builder().withBalance(amount).setAvailableBalance(amount).build();

        String id =
                sodexoStorage
                        .getName()
                        .concat(sodexoStorage.getSurname())
                        .concat(sodexoStorage.getCardNumber());

        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(id)
                        .withAccountNumber(id)
                        .withAccountName(id)
                        .addIdentifier(AccountIdentifier.create(AccountIdentifierType.TINK, id))
                        .build();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(balanceModule)
                .withId(idModule)
                .setApiIdentifier(id)
                .build();
    }
}
