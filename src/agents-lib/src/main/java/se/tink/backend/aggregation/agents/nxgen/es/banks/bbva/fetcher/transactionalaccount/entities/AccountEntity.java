package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.AccountIdentifier;
import static se.tink.libraries.account.AccountIdentifier.Type.IBAN;

@JsonObject
public class AccountEntity {
    private String id;
    private String name;
    private String productDescription;
    private String iban;
    private String productFamilyCode;
    private String subfamilyCode;
    private String subfamilyTypeCode;
    private String typeCode;
    private String typeDescription;
    private String familyCode;
    private String currency;
    private double availableBalance;
    private List<BalancesEntity> availableBalances;
    private String branch;
    private String accountProductId;
    private String accountProductDescription;
    private double actualBalanceInOriginalCurrency;
    private double actualBalance;

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {
        String normalizedIban = iban.replaceAll(" ","").toLowerCase();

        return TransactionalAccount.builder(getTinkAccountType(), normalizedIban, new Amount(currency, availableBalance))
                .setAccountNumber(iban)
                .setName(name)
                .putInTemporaryStorage(BbvaConstants.Storage.ACCOUNT_ID, id)
                .addIdentifier(AccountIdentifier.create(IBAN, normalizedIban))
                .build();
    }

    @JsonIgnore
    // This is a temporary method until we have figured out how to get the balance on accounts that don't have it set
    // in this model.
    public boolean hasBalance() {
        return Objects.nonNull(availableBalance);
    }

    @JsonIgnore
    // until we have seen more than on account we do this
    public boolean isKnownAccountType() {
        return isCheckingAccount();
    }

    @JsonIgnore
    // until we have seen more than on account we do this
    private boolean isCheckingAccount() {
        String bbvaType = String.format("%s:%s", subfamilyCode, subfamilyTypeCode);

        return BbvaConstants.AccountTypes.CHECKING_TYPES.contains(bbvaType);
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {
        if (isCheckingAccount()) {
            return AccountTypes.CHECKING;
        }

        return AccountTypes.OTHER;
    }

    @JsonIgnore
    // until we have seen more than on account we do this
    public boolean isCreditCard() {
        return BbvaConstants.AccountTypes.CREDIT_CARD.equalsIgnoreCase(subfamilyTypeCode);
    }

    public List<BalancesEntity> getAvailableBalances() {
        return availableBalances;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIban() {
        return iban;
    }

    public String getCurrency() {
        return currency;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public String getBranch() {
        return branch;
    }
}
