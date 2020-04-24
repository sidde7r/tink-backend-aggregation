package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities;

import com.google.common.base.Strings;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.JyskeConstants;
import se.tink.backend.aggregation.agents.utils.typeguesser.TypeGuesser;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity extends AccountBriefEntity {

    // account type might have something to do with shadow accounts according to the app, although
    // the json data is
    // not used there.
    private boolean external;
    private Integer accountType;
    private Double balance;
    private String bicSwift;
    private String currencyCode;
    private Double drawingRight;
    private String iban;
    private String name;
    private Boolean overdraft;
    private Boolean transfersToAllowed;
    private Boolean transfersFromAllowed;
    private Boolean editNameAllowed;
    private Double yearToDayPayout;
    private Double yearToDayDeposit;
    private String accountOwner;
    private Boolean allShadowAccountsInactive;
    private Boolean ownAccount;
    private Double amountAtDisposal;
    private Boolean amountAtDisposalToBeShown;
    private Double totalReservation;
    private Double totalShadowAccountBalance;
    private Integer priority;
    private boolean mastercard;
    private Integer regNoAsInt;
    private Long accountNoAsLong;
    private Long accountOwnerRefNo;

    public Integer getAccountType() {
        return accountType;
    }

    public boolean isMastercard() {
        return mastercard;
    }

    private AccountTypes getType() {
        AccountTypes type = TypeGuesser.DANISH.guessAccountType(name);

        if (type == AccountTypes.OTHER) {
            type = JyskeConstants.ACCOUNT_TYPE_MAPPER.translate(name).orElse(AccountTypes.OTHER);
        }

        return type;
    }

    private String getAccountNumber() {
        return regNo + accountNo;
    }

    private ExactCurrencyAmount getTinkBalance() {
        return ExactCurrencyAmount.of(balance, currencyCode);
    }

    public TransactionalAccount toTransactionalAccount() {
        TransactionalAccount.Builder<?, ?> builder =
                TransactionalAccount.builder(getType(), regNo + accountNo, getTinkBalance())
                        .setAccountNumber(getAccountNumber())
                        .setName(name);

        if (!Strings.isNullOrEmpty(iban)) {
            builder.addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban));
        }

        if (!Strings.isNullOrEmpty(accountOwner)) {
            builder.setHolderName(new HolderName(accountOwner));
        }

        return builder.build();
    }

    public AccountBriefEntity toAccountBriefEntity() {
        AccountBriefEntity accountBriefEntity = new AccountBriefEntity();
        accountBriefEntity.setAccountNo(accountNo);
        accountBriefEntity.setRegNo(regNo);
        return accountBriefEntity;
    }

    /**
     * Account belongs to this bank and not one that this bank is aggregating
     *
     * @return true if account belongs to this bank
     */
    public boolean isInternalAccount() {
        return !external;
    }
}
