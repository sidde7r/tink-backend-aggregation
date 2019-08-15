package se.tink.backend.aggregation.agents.banks.norwegian.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SavingsAccountEntity {
    private String accountNumber;
    private String ocrNumber;
    private double amountAvailable;
    private double balance;
    private double interestAccumulated;
    private String payToAccountNumber;
    private String nickName;
    private int region;
    private String danishOcrGiroNummer;
    private boolean showNickName;
    private boolean isPreferredPaymentAccountNumber;

    @JsonIgnore
    public Account toTinkAccount() {

        Account account = new Account();
        account.setType(AccountTypes.SAVINGS);
        account.setBankId("NORWEGIAN_SAVINGS_ACCOUNT");
        account.setAccountNumber(accountNumber);
        account.setBalance(balance);
        account.setName("Norwegian Sparkonto");

        return account;
    }
}
