package se.tink.backend.aggregation.agents.banks.uk.barclays.entities.account;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {

    private final static String GROUP_TYPE_BUSINESS = "B";
    private final static String GROUP_TYPE_PERSONAL = "P";


    private final static ImmutableMap<String, AccountTypes> accountTypesMap = new ImmutableMap.Builder<String, AccountTypes>()
            .put("SV", AccountTypes.SAVINGS)
            .put("CU", AccountTypes.CHECKING)
            .put("LN", AccountTypes.LOAN)
            .put("BC", AccountTypes.OTHER)
            .put("MO", AccountTypes.OTHER)
            .put("WM", AccountTypes.OTHER)
            .put("ML", AccountTypes.OTHER)
            .build();

    private String groupType;
    private String overdraftLimit;
    private String interestRate;
    private String reserveLimit;
    private String remainingTerm;
    private String balanceTypeDescription;
    private String productIdentifier;
    @JsonProperty("accountName")
    private String accountOwnerName;
    private String maskedAccountNumber;
    private String productName;
    private String groupId;
    private String lastNightBalance;
    private boolean ukba;
    private boolean isHidden;
    @JsonProperty("productClass")
    private String accountType;
    private String balance;
    private boolean barclayloan;
    private String formattedSortCode;

    public String getGroupType() {
        return groupType;
    }

    public String getOverdraftLimit() {
        return overdraftLimit;
    }

    public String getInterestRate() {
        return interestRate;
    }

    public String getReserveLimit() {
        return reserveLimit;
    }

    public String getRemainingTerm() {
        return remainingTerm;
    }

    public String getBalanceTypeDescription() {
        return balanceTypeDescription;
    }

    public String getProductIdentifier() {
        return productIdentifier;
    }

    public String getAccountOwnerName() {
        return accountOwnerName;
    }

    public String getMaskedAccountNumber() {
        return maskedAccountNumber;
    }

    public String getProductName() {
        return productName;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getLastNightBalance() {
        return lastNightBalance;
    }

    public boolean isUkba() {
        return ukba;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getBalance() {
        return balance;
    }

    public boolean isBarclayloan() {
        return barclayloan;
    }

    public String getFormattedSortCode() {
        return formattedSortCode;
    }

    public String getSortCode() {
        return formattedSortCode.replaceAll("[^0-9]", "");
    }

    public boolean isAccountPersonal() {
        return GROUP_TYPE_PERSONAL.equals(groupType);
    }

    public AccountTypes getTinkAccountType() {
        if (accountTypesMap.containsKey(accountType)) {
            return accountTypesMap.get(accountType);
        }
        return AccountTypes.OTHER;
    }

    public Account toTinkAccount() {
        Account tinkAccount = new Account();
        tinkAccount.setName(getProductName());
        tinkAccount.setBankId(getSortCode() + getMaskedAccountNumber());
        tinkAccount.setAccountNumber(
                String.format(
                        "%s %s",
                        getFormattedSortCode(),
                        getMaskedAccountNumber()));
        tinkAccount.setBalance(StringUtils.parseAmount(getBalance()));
        tinkAccount.setType(getTinkAccountType());
        return tinkAccount;
    }
}
