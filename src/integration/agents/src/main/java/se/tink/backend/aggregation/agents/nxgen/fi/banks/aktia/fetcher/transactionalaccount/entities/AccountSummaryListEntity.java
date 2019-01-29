package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@JsonObject
public class AccountSummaryListEntity {
    private String id;
    private String name;
    private String primaryOwnerName;
    private String iban;
    private String bic;
    private AccountTypeEntity accountType;
    private double balance;
    private double balanceTotal;
    private double duePaymentsTotal;
    private boolean hideFromSummary;
    private int sortingOrder;
    private List<PartiesEntity> parties;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrimaryOwnerName() {
        return primaryOwnerName;
    }

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }

    public AccountTypeEntity getAccountType() {
        return accountType;
    }

    public double getBalance() {
        return balance;
    }

    public double getBalanceTotal() {
        return balanceTotal;
    }

    public double getDuePaymentsTotal() {
        return duePaymentsTotal;
    }

    public boolean isHideFromSummary() {
        return hideFromSummary;
    }

    public int getSortingOrder() {
        return sortingOrder;
    }

    public List<PartiesEntity> getParties() {
        return parties;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {
        if (Objects.isNull(accountType)) {
            return Optional.empty();
        }

        String aktiaAccountType = accountType.getCategoryCode();

        // Note: Aktia does not specify currency. All amounts are in EUR.
        return AktiaConstants.TRANSACTIONAL_ACCOUNTS_TYPE_MAPPER.translate(aktiaAccountType)
                .map(type -> TransactionalAccount.builder(type, iban.toLowerCase())
                        .setBankIdentifier(id)
                        .addIdentifier(new IbanIdentifier(iban))
                        .setAccountNumber(iban)
                        .setBalance(Amount.inEUR(balance))
                        .setName(name)
                        .setHolderName(new HolderName(primaryOwnerName))
                        .build()
                );
    }
}
