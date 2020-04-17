package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount.Builder;
import se.tink.libraries.account.enums.AccountFlag;

@JsonObject
public class SdcAccount {
    @JsonIgnore
    private static final AggregationLogger LOGGER = new AggregationLogger(SdcAccount.class);

    private SdcAccountKey entityKey;
    private SdcAmount amount;
    private SdcAmount availableAmount;
    private SdcAccountProperties accountProperties;
    private String localizedAccountId;
    private String id;
    private String type;
    private String currency;
    private String name;
    private int sortNumber;
    private String productElementType;

    @JsonIgnore
    public TransactionalAccount toTinkAccount() {

        AccountTypes accountTypes = convertAccountType();

        Builder<?, ?> builder =
                TransactionalAccount.builder(accountTypes, id, amount.toTinkAmount())
                        .setAccountNumber(id)
                        .setName(name)
                        .setBankIdentifier(normalizedBankId());

        if (accountTypes.equals(AccountTypes.CHECKING)
                || accountTypes.equals(AccountTypes.SAVINGS)) {
            builder.addAccountFlag(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        }

        return builder.build();
    }

    @JsonIgnore
    public CreditCardAccount toTinkCreditCardAccount(SdcConfiguration agentConfiguration) {
        return CreditCardAccount.builder(
                        id, amount.toExactCurrencyAmount(), availableAmount.toExactCurrencyAmount())
                .setAccountNumber(localizedAccountId)
                .setName(name)
                .setBankIdentifier(normalizedBankId())
                .build();
    }

    @JsonIgnore
    private AccountTypes convertAccountType() {
        if (isLoanAccount()) {
            return AccountTypes.LOAN;
        }
        SdcConstants.AccountType accountType =
                SdcConstants.AccountType.fromProductType(productElementType);
        if (accountType != SdcConstants.AccountType.UNKNOWN) {
            return accountType.getTinkAccountType();
        }
        LOGGER.info("Found unknown productElementType: " + productElementType);
        return AccountTypes.OTHER;
    }

    @JsonIgnore
    private String normalizedBankId() {
        return id.replace(".", "");
    }

    @JsonIgnore
    public boolean isLoanAccount() {
        if (accountProperties != null) {
            return accountProperties.isLoan();
        }

        return isAccountType(SdcConstants.AccountType.LOAN);
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        AccountTypes tinkAccountType = convertAccountType();
        return TransactionalAccount.ALLOWED_ACCOUNT_TYPES.contains(tinkAccountType);
    }

    @JsonIgnore
    public boolean isCreditCardAccount() {
        return isAccountType(SdcConstants.AccountType.CREDIT_CARD);
    }

    @JsonIgnore
    private boolean isAccountType(SdcConstants.AccountType type) {
        if (productElementType != null) {
            SdcConstants.AccountType accountType =
                    SdcConstants.AccountType.fromProductType(productElementType);
            return type == accountType;
        }

        return false;
    }

    public SdcAccountKey getEntityKey() {
        return entityKey != null ? entityKey : new SdcAccountKey();
    }

    public SdcAmount getAmount() {
        return amount;
    }

    public SdcAmount getAvailableAmount() {
        return availableAmount;
    }

    public SdcAccountProperties getAccountProperties() {
        return accountProperties;
    }

    public String getLocalizedAccountId() {
        return localizedAccountId;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getCurrency() {
        return currency;
    }

    public String getName() {
        return name;
    }

    public int getSortNumber() {
        return sortNumber;
    }

    public String getProductElementType() {
        return productElementType;
    }

    boolean isAccount(SdcCreditCardAccountEntity creditCardAccount) {
        return getEntityKey().hasSameId(creditCardAccount.getEntityKey());
    }
}
