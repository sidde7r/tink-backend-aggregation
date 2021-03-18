package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.fetcher;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.EdenredApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.EdenredConstants;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.EdenredConstants.Cards;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities.CardEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.entities.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.storage.EdenredStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class EdenredAccountsFetcher implements AccountFetcher<TransactionalAccount> {

    private final EdenredApiClient edenredApiClient;

    private final EdenredStorage edenredStorage;

    public EdenredAccountsFetcher(
            EdenredApiClient edenredApiClient, EdenredStorage edenredStorage) {
        this.edenredApiClient = edenredApiClient;
        this.edenredStorage = edenredStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        List<CardEntity> cards = edenredApiClient.getCards().getData();
        if (cards == null) {
            return Collections.emptyList();
        }
        return cards.stream()
                .filter(this::isActive)
                .map(this::mapAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private boolean isActive(CardEntity cardEntity) {
        return Cards.ACTIVE.equals(cardEntity.getStatus());
    }

    private Optional<TransactionalAccount> mapAccount(CardEntity cardEntity) {
        String id = cardEntity.getNumber();
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(id)
                        .withAccountNumber(id)
                        .withAccountName(cardEntity.getProduct().getName())
                        .addIdentifier(AccountIdentifier.create(AccountIdentifierType.IBAN, id))
                        .build();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(buildBalanceModule(cardEntity))
                .withId(idModule)
                .setApiIdentifier(String.valueOf(cardEntity.getId()))
                .build();
    }

    private BalanceModule buildBalanceModule(CardEntity cardEntity) {
        AccountEntity accountEntity = fetchAccountWithBalance(cardEntity.getId());
        ExactCurrencyAmount amount =
                ExactCurrencyAmount.of(
                        accountEntity.getAvailableBalance(), EdenredConstants.CURRENCY);
        return BalanceModule.builder().withBalance(amount).setAvailableBalance(amount).build();
    }

    private AccountEntity fetchAccountWithBalance(Long id) {
        TransactionsEntity data = edenredApiClient.getTransactions(id).getData();
        edenredStorage.storeTransactions(id, data);
        return data.getAccount();
    }
}
