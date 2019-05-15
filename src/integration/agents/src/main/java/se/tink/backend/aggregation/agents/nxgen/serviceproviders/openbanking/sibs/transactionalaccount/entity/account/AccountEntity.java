package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.entity.account;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private String id;
    private String iban;
    private String bban;
    private String pan;
    private String maskedPan;
    private String msisdn;
    private String currency;
    private String name;
    private String accountType;
    private String cashAccountType;
    private String bic;
    private List<BalanceEntity> balances;
    private AccountLinksEntity links;

    public String getId() {
        return id;
    }

    @JsonIgnore
    public CheckingAccount toTinkAccount(Amount balance) {
        return CheckingAccount.builder()
                .setUniqueIdentifier(iban)
                .setAccountNumber(iban)
                .setBalance(balance)
                .setAlias(name)
                .addAccountIdentifier(new IbanIdentifier(iban))
                .setApiIdentifier(id)
                .build();
    }
}
