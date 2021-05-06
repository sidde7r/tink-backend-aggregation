package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.entities;

import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.Storage;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Getter
public class AccountsEntity {
    private AccountNumberEntity accountNumber;
    private String name;
    private String ownerName;
    private boolean isOverdraft;
    private BalanceEntity balance;
    private BalanceEntity drawRight;
    private BalanceEntity disposalAmount;
    private BalanceEntity reservedAmount;
    private boolean showAmountAtDisposal;
    private boolean isOwnAccount;
    private boolean transfersFromAllowed;
    private boolean transfersToAllowed;
    private boolean isVisible;
    private boolean editNameAllowed;
    private String iban;
    private String swift;
    private String bankName;
    private boolean externalAccount;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(getTinkAccountType())
                .withInferredAccountFlags()
                .withBalance(getBalanceModule())
                .withId(getIdModule())
                .addHolderName(ownerName)
                .putInTemporaryStorage(Storage.PUBLIC_ID, accountNumber.getPublicId())
                .build();
    }

    private TransactionalAccountType getTinkAccountType() {
        return TransactionalAccountType.from(
                        JyskeConstants.ACCOUNT_TYPE_MAPPER
                                .translate(accountNumber.getRegNo())
                                .get())
                .orElse(TransactionalAccountType.CHECKING);
    }

    private BalanceModule getBalanceModule() {
        return BalanceModule.builder()
                .withBalance(ExactCurrencyAmount.of(balance.getAmount(), balance.getCurrencyCode()))
                .setAvailableBalance(
                        ExactCurrencyAmount.of(
                                disposalAmount.getAmount(), disposalAmount.getCurrencyCode()))
                .build();
    }

    private IdModule getIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(iban)
                .withAccountNumber(accountNumber.getAccountNo())
                .withAccountName(name)
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifierType.DK, accountNumber.getAccountNo(), name))
                .addIdentifier(new IbanIdentifier(iban))
                .build();
    }
}
