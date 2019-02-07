package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.VolvoFinansConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.amount.Amount;

@JsonObject
public class SavingsAccountEntity {
    @JsonProperty("kontoId")
    private String accountId;
    @JsonProperty("kontonummer")
    private String accountNumber;
    @JsonProperty("produkt")
    private String product;
    @JsonProperty("namn")
    private String name;
    @JsonProperty("saldo")
    private double balance;
    @JsonProperty("rantesats")
    private double interestRate;
    @JsonProperty("kontoRoll")
    private String accountRole;
    @JsonProperty("intressenter")
    private List<AccountHolderEntity> accountHolders;

    private double upplupenRanta;
    private boolean kanOverfora;
    private String bankgiroInbetalning;
    private String ocrnummerInbetalning;

    public SavingsAccount toTinkAccount() {
        return SavingsAccount.builder(accountNumber, Amount.inSEK(balance))
                .setBankIdentifier(accountId)
                .setAccountNumber(accountNumber)
                .setInterestRate(interestRate)
                .setName(name)
                .setHolderName(getHolderName())
                .build();
    }

    @JsonIgnore
    public boolean isAccountHolder() {
        return VolvoFinansConstants.Fetcher.ACCOUNT_ROLE_MAIN_APPLICANT.equalsIgnoreCase(accountRole);
    }

    @JsonIgnore
    private HolderName getHolderName() {
        return Optional.ofNullable(accountHolders).orElse(Collections.emptyList()).stream()
                .filter(holder ->
                        VolvoFinansConstants.Fetcher.ACCOUNT_ROLE_MAIN_APPLICANT.equalsIgnoreCase(holder.getRole()))
                .findFirst()
                .map(AccountHolderEntity::getName)
                .map(HolderName::new).orElse(null);
    }

    public String getAccountId() {
        return accountId;
    }
}
