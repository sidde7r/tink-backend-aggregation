package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount.Builder;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity implements GeneralAccountEntity {
    @JsonIgnore private static final Logger log = LoggerFactory.getLogger(AccountEntity.class);

    @JsonProperty("Type")
    private String type;

    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("AccountNumber")
    private String accountNumber;

    @JsonProperty("Name")
    private String name;

    @JsonDouble
    @JsonProperty("AvailableAmount")
    private BigDecimal availableAmount;

    @JsonDouble
    @JsonProperty("CurrentAmount")
    private BigDecimal currentAmount;

    @JsonDouble
    @JsonProperty("OutstandingAmount")
    private BigDecimal outstandingAmount;

    @JsonDouble
    @JsonProperty("CreditLimit")
    private BigDecimal creditLimit;

    @JsonProperty("ValidFor")
    private List<String> validFor;

    @JsonProperty("IBAN")
    private String iban;

    @JsonProperty("BIC")
    private String bic;

    @JsonProperty("Address")
    private String address;

    @JsonProperty("Holder")
    private String holder;

    @JsonProperty("Services")
    private List<String> services;

    @JsonProperty("AccountOwner")
    private AccountOwnerEntity accountOwner;

    @JsonIgnore
    public TransactionalAccount toTinkTransactionalAccount() {
        AccountTypes accountType = getTinkAccountType();
        Builder builder =
                TransactionalAccount.builder(accountType, accountNumber, getBalance())
                        .setAccountNumber(accountNumber)
                        .setName(name)
                        .setHolderName(new HolderName(holder))
                        .setBankIdentifier(accountId)
                        .addIdentifier(new SwedishIdentifier(accountNumber))
                        .addIdentifier(new IbanIdentifier(iban))
                        .sourceInfo(createAccountSourceInfo());
        if (accountType == AccountTypes.CHECKING) {
            builder.addAccountFlag(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        }
        if (!creditLimit.equals(BigDecimal.ZERO)) {
            builder.setExactAvailableCredit(getAvailableCredit());
        }
        return builder.build();
    }

    @JsonIgnore
    public CreditCardAccount toCreditCardAccount() {
        return CreditCardAccount.builder(accountNumber, getBalance(), getAvailableCredit())
                .setAccountNumber(accountNumber)
                .setName(name)
                .setHolderName(new HolderName(holder))
                .setBankIdentifier(accountId)
                .sourceInfo(createAccountSourceInfo())
                .build();
    }

    @JsonIgnore
    private AccountSourceInfo createAccountSourceInfo() {
        return AccountSourceInfo.builder().bankAccountType(type).bankProductName(name).build();
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        return ExactCurrencyAmount.of(
                currentAmount.subtract(outstandingAmount), IcaBankenConstants.CURRENCY);
    }

    @JsonIgnore
    private ExactCurrencyAmount getAvailableCredit() {
        return ExactCurrencyAmount.of(availableAmount, IcaBankenConstants.CURRENCY);
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {
        if (isCheckingAccount()) {
            return AccountTypes.CHECKING;
        }

        if (isSavingsAccount()) {
            return AccountTypes.SAVINGS;
        }

        if (isCreditCardAccount()) {
            return AccountTypes.CREDIT_CARD;
        }

        log.warn("Unknown account type. Logging account of type: {}", type);
        return AccountTypes.OTHER;
    }

    @JsonIgnore
    private boolean isCheckingAccount() {
        return IcaBankenConstants.AccountTypes.ICA_ACCOUNT.equalsIgnoreCase(type);
    }

    @JsonIgnore
    private boolean isSavingsAccount() {
        return IcaBankenConstants.AccountTypes.SAVINGS_ACCOUNT.equalsIgnoreCase(type);
    }

    @JsonIgnore
    public boolean isCreditCardAccount() {
        return IcaBankenConstants.AccountTypes.CREDIT_CARD_ACCOUNT.equalsIgnoreCase(type);
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return isCheckingAccount() || isSavingsAccount();
    }

    @JsonIgnore
    public String getUnformattedAccountNumber() {
        return accountNumber.replaceAll("[ -]", "");
    }

    @JsonIgnore
    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(accountNumber);
    }

    @JsonIgnore
    @Override
    public String generalGetBank() {
        AccountIdentifier accountIdentifier = generalGetAccountIdentifier();

        return accountIdentifier.isValid()
                ? accountIdentifier.to(SwedishIdentifier.class).getBankName()
                : null;
    }

    @JsonIgnore
    @Override
    public String generalGetName() {
        return getName();
    }

    public String getType() {
        return type;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getName() {
        return name;
    }

    public List<String> getValidFor() {
        return validFor;
    }

    public String getIban() {
        return iban;
    }

    public String getBic() {
        return bic;
    }

    public String getAddress() {
        return address;
    }

    public String getHolder() {
        return holder;
    }

    public List<String> getServices() {
        return services;
    }

    public AccountOwnerEntity getAccountOwner() {
        return accountOwner;
    }
}
