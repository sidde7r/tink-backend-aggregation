package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;

import java.util.List;

@JsonObject
public class AccountSummaryEntity extends AbstractAccountEntity {
    private static final AggregationLogger log = new AggregationLogger(AccountSummaryEntity.class);
    private String primaryOwnerName;
    private String bic;
    private AccountTypeEntity accountType;
    private double balanceTotal;
    private double duePaymentsTotal;
    private boolean hideFromSummary;
    private int sortingOrder;
    private FabInfoEntity fabInfo;
    private List<PartyEntity> parties;

    public String getPrimaryOwnerName() {
        return primaryOwnerName;
    }

    public String getBic() {
        return bic;
    }

    public AccountTypeEntity getAccountType() {
        return accountType;
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

    public FabInfoEntity getFabInfo() {
        return fabInfo;
    }

    public List<PartyEntity> getParties() {
        return parties;
    }

    public TransactionalAccount toTinkAccount() {
        return TransactionalAccount.builder(getTinkAccountType(), iban, Amount.inEUR(balance))
                .setAccountNumber(iban)
                .setName(name)
                .setBankIdentifier(id)
                .build();
    }

    private AccountTypes getTinkAccountType() {
        if (accountType == null || accountType.getCategoryCode() == null) {
            return AccountTypes.OTHER;
        }

        switch (accountType.getCategoryCode().toLowerCase()) {
            case AktiaConstants.Accounts.CHECKING_ACCOUNT:
                return AccountTypes.CHECKING;
            case AktiaConstants.Accounts.SAVINGS_ACCOUNT:
                return AccountTypes.SAVINGS;
            default:
                log.warn(String.format(
                        "Could not map account type [%s] to a Tink account type", accountType.getCategoryCode()));
                return AccountTypes.OTHER;
        }
    }
}
