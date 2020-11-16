package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.NordeaNoConstants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.fetcher.transactionalaccount.entity.AccountEntity;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.NorwegianIdentifier;

@AllArgsConstructor
public class TransactionalAccountFetcher
        implements se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher<
                TransactionalAccount> {

    private FetcherClient fetcherClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return fetcherClient.fetchAccounts().getAccounts().stream()
                .map(this::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> toTinkAccount(AccountEntity accountEntity) {

        Optional<TransactionalAccountType> transactionalAccountType =
                getTransactionalAccountType(accountEntity);
        if (!transactionalAccountType.isPresent()) {
            return Optional.empty();
        }
        return TransactionalAccount.nxBuilder()
                .withType(transactionalAccountType.get())
                .withInferredAccountFlags()
                .withBalance(getBalances(accountEntity))
                .withId(getIdModule(accountEntity))
                .canPlaceFunds(accountEntity.canPlaceFunds())
                .canWithdrawCash(accountEntity.canWithdrawCash())
                .canExecuteExternalTransfer(accountEntity.canExecuteExternalTransfer())
                .canReceiveExternalTransfer(accountEntity.canReceiveExternalTransfer())
                .setApiIdentifier(accountEntity.getAccountId())
                .addHolderName(accountEntity.getOwner())
                .putInTemporaryStorage(
                        NordeaNoConstants.PRODUCT_CODE, accountEntity.getProductCode())
                .putInTemporaryStorage(
                        NordeaNoConstants.CAN_FETCH_TRANSACTION,
                        accountEntity.getPermissions().getCanViewTransactions())
                .build();
    }

    private Optional<TransactionalAccountType> getTransactionalAccountType(
            AccountEntity accountEntity) {
        return accountEntity.getTinkAccountType().flatMap(TransactionalAccountType::from);
    }

    private BalanceModule getBalances(AccountEntity accountEntity) {
        return BalanceModule.builder()
                .withBalance(accountEntity.getBookedBalance())
                .setAvailableBalance(accountEntity.getAvailableBalance())
                .setCreditLimit(accountEntity.getCreditLimit())
                .build();
    }

    private IdModule getIdModule(AccountEntity accountEntity) {
        return IdModule.builder()
                .withUniqueIdentifier(accountEntity.getIban())
                .withAccountNumber(accountEntity.getIban())
                .withAccountName(accountEntity.getNickname())
                .addIdentifier(new IbanIdentifier(accountEntity.getBic(), accountEntity.getIban()))
                .addIdentifier(new NorwegianIdentifier(accountEntity.getAccountId()))
                .setProductName(accountEntity.getProductName())
                .build();
    }
}
