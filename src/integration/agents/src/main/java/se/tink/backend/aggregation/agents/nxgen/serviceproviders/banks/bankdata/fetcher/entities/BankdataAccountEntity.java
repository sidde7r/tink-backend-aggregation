package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.entities;

import java.util.Optional;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankdataPaymentAccountCapabilities;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Getter
@JsonObject
public class BankdataAccountEntity {

    public static final String REGISTRATION_NUMBER_TEMP_STORAGE_KEY = "regNo";
    public static final String ACCOUNT_NUMBER_TEMP_STORAGE_KEY = "accountNo";

    private String regNo;
    private String accountNo;
    private double balance;
    private String bicSwift;
    private String currencyCode;
    private double drawingRight;
    private String iban;
    private String name;
    private Boolean transfersToAllowed;
    private Boolean transfersFromAllowed;
    private String accountOwner;
    private boolean mastercard;

    public Optional<TransactionalAccount> toTinkAccount() {
        AccountTypes type = getType();
        TransactionalAccountType transType = TransactionalAccountType.from(type).orElse(null);
        if (!isProperTransactionalAccount(transType)) {
            return Optional.empty();
        }
        return TransactionalAccount.nxBuilder()
                .withType(transType)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.of(balance, currencyCode)))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(accountNo)
                                .withAccountName(name)
                                .addIdentifier(new IbanIdentifier(bicSwift, iban))
                                .build())
                .addHolderName(accountOwner)
                .setApiIdentifier(iban)
                .putInTemporaryStorage(REGISTRATION_NUMBER_TEMP_STORAGE_KEY, regNo)
                .putInTemporaryStorage(ACCOUNT_NUMBER_TEMP_STORAGE_KEY, accountNo)
                .canExecuteExternalTransfer(
                        BankdataPaymentAccountCapabilities.canExecuteExternalTransfer(
                                name, type, this))
                .canReceiveExternalTransfer(
                        BankdataPaymentAccountCapabilities.canReceiveExternalTransfer(
                                name, type, this))
                .canWithdrawCash(BankdataPaymentAccountCapabilities.canWithdrawCash(name, type))
                .canPlaceFunds(BankdataPaymentAccountCapabilities.canPlaceFunds(name, type, this))
                .build();
    }

    private boolean isProperTransactionalAccount(TransactionalAccountType type) {
        return type != null && type != TransactionalAccountType.OTHER;
    }

    public AccountTypes getType() {
        final String savingsPartialName = "Opsparing";
        AccountTypes accountTypes;
        if (StringUtils.containsIgnoreCase(name, savingsPartialName)) {
            accountTypes = AccountTypes.SAVINGS;
        } else if (drawingRight > 0) {
            accountTypes = AccountTypes.LOAN;
        } else {
            accountTypes = AccountTypes.CHECKING;
        }
        return accountTypes;
    }
}
