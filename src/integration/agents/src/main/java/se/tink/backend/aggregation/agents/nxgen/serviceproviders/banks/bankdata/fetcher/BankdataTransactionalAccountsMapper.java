package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.BankdataMapperUtils.getAccountNumberToDisplay;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountEntity.ACCOUNT_NUMBER_TEMP_STORAGE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountEntity.REGISTRATION_NUMBER_TEMP_STORAGE_KEY;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataPaymentAccountCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities.BankdataAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BankdataTransactionalAccountsMapper {

    public static List<TransactionalAccount> getTransactionalAccounts(
            GetAccountsResponse getAccountsResponse) {
        return getAccountsResponse.getAccounts().stream()
                .map(BankdataTransactionalAccountsMapper::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static Optional<TransactionalAccount> toTransactionalAccount(
            BankdataAccountEntity entity) {
        AccountTypes type = entity.getAccountType();
        TransactionalAccountType transType = TransactionalAccountType.from(type).orElse(null);

        if (!isTransactionalAccount(transType)) {
            return Optional.empty();
        }

        return TransactionalAccount.nxBuilder()
                .withType(transType)
                .withPaymentAccountFlag()
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(
                                        entity.getBalance(), entity.getCurrencyCode())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(entity.getIban())
                                .withAccountNumber(
                                        getAccountNumberToDisplay(
                                                entity.getRegNo(), entity.getAccountNo()))
                                .withAccountName(entity.getName())
                                .addIdentifiers(
                                        new IbanIdentifier(entity.getBicSwift(), entity.getIban()),
                                        new BbanIdentifier(entity.getIban().substring(4)))
                                .build())
                .addHolderName(entity.getAccountOwner())
                .setApiIdentifier(entity.getIban())
                .putInTemporaryStorage(REGISTRATION_NUMBER_TEMP_STORAGE_KEY, entity.getRegNo())
                .putInTemporaryStorage(ACCOUNT_NUMBER_TEMP_STORAGE_KEY, entity.getAccountNo())
                .canExecuteExternalTransfer(
                        BankdataPaymentAccountCapabilities.canExecuteExternalTransfer(
                                entity.getName(), type, entity))
                .canReceiveExternalTransfer(
                        BankdataPaymentAccountCapabilities.canReceiveExternalTransfer(
                                entity.getName(), type, entity))
                .canWithdrawCash(
                        BankdataPaymentAccountCapabilities.canWithdrawCash(entity.getName(), type))
                .canPlaceFunds(
                        BankdataPaymentAccountCapabilities.canPlaceFunds(
                                entity.getName(), type, entity))
                .build();
    }

    private static boolean isTransactionalAccount(TransactionalAccountType accountType) {
        return accountType == TransactionalAccountType.CHECKING
                || accountType == TransactionalAccountType.SAVINGS;
    }
}
