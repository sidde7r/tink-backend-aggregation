package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants.TRANSACTIONAL_ACCOUNTS_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
        return TRANSACTIONAL_ACCOUNTS_TYPE_MAPPER
                .translate(aktiaAccountType)
                .map(
                        type ->
                                TransactionalAccount.nxBuilder()
                                        .withType(getTransactionalAccountType())
                                        .withPaymentAccountFlag()
                                        .withBalance(
                                                BalanceModule.of(
                                                        ExactCurrencyAmount.of(balance, "EUR")))
                                        .withId(
                                                IdModule.builder()
                                                        .withUniqueIdentifier(id)
                                                        .withAccountNumber(iban)
                                                        .withAccountName(name)
                                                        .addIdentifier(new IbanIdentifier(iban))
                                                        .build())
                                        .addHolderName(primaryOwnerName)
                                        .setApiIdentifier(id)
                                        .build()
                                        .get());
    }

    private TransactionalAccountType getTransactionalAccountType() {
        return TRANSACTIONAL_ACCOUNTS_TYPE_MAPPER.translate(accountType.toString()).orElse(null);
    }
}
