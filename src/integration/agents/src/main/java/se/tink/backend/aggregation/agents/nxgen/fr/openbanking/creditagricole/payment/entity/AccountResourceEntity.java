package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountResourceEntity {
    @JsonProperty("resourceId")
    private String resourceId = null;

    @JsonProperty("accountId")
    private PsuAccountIdentificationEntity accountId = null;

    @JsonProperty("name")
    private String name = null;

    @JsonProperty("linkedAccount")
    private String linkedAccount = null;

    @JsonProperty("cashAccountType")
    private CashAccountTypeEnumEntity cashAccountType = null;

    @JsonProperty("balances")
    private List<BalanceResourceEntity> balances = new ArrayList<BalanceResourceEntity>();

    @JsonProperty("psuStatus")
    private String psuStatus = null;

    @JsonProperty("_links")
    private AccountLinksEntity links = null;

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        return CheckingAccount.builder()
                .setUniqueIdentifier(accountId.getIban())
                .setAccountNumber(accountId.getIban())
                .setBalance(getBalance())
                .setAlias(name)
                .addAccountIdentifier(new IbanIdentifier(accountId.getIban()))
                .setApiIdentifier(resourceId)
                .build();
    }

    private Amount getBalance() {
        return balances.stream()
                .filter(item -> item.getBalanceType().equals(BalanceStatusEntity.XPCD))
                .findFirst()
                .map(
                        item ->
                                new Amount(
                                        item.getBalanceAmount().getCurrency(),
                                        Double.parseDouble(item.getBalanceAmount().getAmount())))
                .orElseThrow(() -> new IllegalStateException("Balance not found"));
    }
}
