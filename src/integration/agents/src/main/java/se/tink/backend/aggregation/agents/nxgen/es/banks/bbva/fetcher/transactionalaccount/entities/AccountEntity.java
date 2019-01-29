package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Objects;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.Amount;
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
    private Double availableBalance;
    private List<BalancesEntity> availableBalances;
    private String branch;
    private String accountProductId;
    private String accountProductDescription;
    private double actualBalanceInOriginalCurrency;
    private double actualBalance;

    @JsonIgnore
    public TransactionalAccount toTinkAccount(HolderName holderName) {
        String normalizedIban = iban.replaceAll(" ","").toLowerCase();

        return TransactionalAccount.builder(getTinkAccountType(), normalizedIban, new Amount(currency, availableBalance))
                .setAccountNumber(iban)
                .setName(name)
                .setHolderName(holderName)
                .putInTemporaryStorage(BbvaConstants.Storage.ACCOUNT_ID, id)
                .addIdentifier(AccountIdentifier.create(IBAN, normalizedIban))
                .build();
    }

    @JsonIgnore
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
