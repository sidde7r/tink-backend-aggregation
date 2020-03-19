package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.entity;

import static se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Accounts.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class MainAndCoAccountsEntity {
    private String accountNumber;
    private BigDecimal balance;
    private String accountName;
    private String clearingNumber;
    private SavingsGoalEntity savingsGoal;
    private boolean isSelectedStartPageEngagement;
    private boolean localAccount;
    private String bankName;
    private String type;
    private boolean savedRecipient;
    private String ledger;
    private boolean savingsGoalAllowed;
    private boolean fixedRateAccount;
    private String accountInfoText;
    private String relationToAccount;
    private boolean transferFrom;
    private boolean transferTo;
    private boolean youthAccount;

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getClearingNumber() {
        return clearingNumber;
    }

    public boolean isSelectedStartPageEngagement() {
        return isSelectedStartPageEngagement;
    }

    public boolean isLocalAccount() {
        return localAccount;
    }

    public String getType() {
        return type;
    }

    public boolean isSavedRecipient() {
        return savedRecipient;
    }

    public String getLedger() {
        return ledger;
    }

    public boolean isSavingsGoalAllowed() {
        return savingsGoalAllowed;
    }

    public boolean isFixedRateAccount() {
        return fixedRateAccount;
    }

    public String getRelationToAccount() {
        return relationToAccount;
    }

    public boolean isTransferFrom() {
        return transferFrom;
    }

    public boolean isTransferTo() {
        return transferTo;
    }

    public boolean isYouthAccount() {
        return youthAccount;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountInfoText() {
        return accountInfoText;
    }

    public SavingsGoalEntity getSavingsGoal() {
        return savingsGoal;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {

        // TODO: check flags (add PSD2 if applicable
        Optional<TransactionalAccount> transactionalAccount =
                TransactionalAccount.nxBuilder()
                        .withTypeAndFlagsFrom(Accounts.ACCOUNT_TYPE_MAPPER, type)
                        .withBalance(
                                BalanceModule.of(
                                        ExactCurrencyAmount.of(balance, Accounts.CURRENCY)))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(accountNumber)
                                        .withAccountNumber(accountNumber)
                                        .withAccountName(accountName)
                                        .addIdentifier(new SwedishIdentifier(accountNumber))
                                        .build())
                        .setApiIdentifier(accountNumber)
                        .build();
        return transactionalAccount;
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return ACCOUNT_TYPE_MAPPER.isOf(type, AccountTypes.CHECKING)
                || ACCOUNT_TYPE_MAPPER.isOf(type, AccountTypes.SAVINGS);
    }
}
