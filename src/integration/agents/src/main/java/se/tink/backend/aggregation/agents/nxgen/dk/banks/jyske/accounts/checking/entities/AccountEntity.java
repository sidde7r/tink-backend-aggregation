package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.accounts.checking.entities;

import se.tink.backend.aggregation.agents.utils.typeguesser.TypeGuesser;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.account.AccountIdentifier;

@JsonObject
public class AccountEntity extends AccountBriefEntity {

    // account type might have something to do with shadow accounts according to the app, although the json data is
    // not used there.
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
        return TypeGuesser.DANISH.guessAccountType(name);
    }

    private String getAccountNumber() {
        return regNo + accountNo;
    }

    private Amount getTinkBalance() {
        return new Amount(currencyCode, balance);
    }

    public TransactionalAccount toTransactionalAccount() {
        return TransactionalAccount.builder(getType(), regNo + accountNo, getTinkBalance())
                .setAccountNumber(getAccountNumber())
                .setName(name)
                .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .build();
    }

    public AccountBriefEntity toAccountBriefEntity() {
        AccountBriefEntity accountBriefEntity = new AccountBriefEntity();
        accountBriefEntity.setAccountNo(accountNo);
        accountBriefEntity.setShadowAccountId("");
        accountBriefEntity.setRegNo(regNo);
        return accountBriefEntity;
    }

}
