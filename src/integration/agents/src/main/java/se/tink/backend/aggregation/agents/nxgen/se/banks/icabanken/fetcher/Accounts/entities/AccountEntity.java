package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.accounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.annotations.JsonDouble;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.builder.BalanceBuilderStep;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.AccountIdentifier;
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
        return TransactionalAccount.nxBuilder()
                .withPatternTypeAndFlagsFrom(
                        IcaBankenConstants.ACCOUNT_TYPE_MAPPER,
                        type,
                        TransactionalAccountType.CHECKING)
                .withBalance(getBalancesModule())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(name)
                                .addIdentifier(new SwedishIdentifier(accountNumber))
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .addHolderName(holder)
                .setApiIdentifier(accountId)
                .setBankIdentifier(accountId)
                .sourceInfo(createAccountSourceInfo())
                .canWithdrawCash(getAccountCapabilities().getCanWithdrawCash())
                .canPlaceFunds(getAccountCapabilities().getCanPlaceFunds())
                .canExecuteExternalTransfer(
                        getAccountCapabilities().getCanExecuteExternalTransfer())
                .canReceiveExternalTransfer(
                        getAccountCapabilities().getCanReceiveExternalTransfer())
                .build()
                .get();
    }

    @JsonIgnore
    private AccountCapabilities getAccountCapabilities() {
        return IcaBankenConstants.ACCOUNT_CAPABILITIES_MAPPER
                .translate(type)
                .orElse(
                        new AccountCapabilities(
                                Answer.UNKNOWN, Answer.UNKNOWN, Answer.UNKNOWN, Answer.UNKNOWN));
    }

    @JsonIgnore
    private BalanceModule getBalancesModule() {
        BalanceBuilderStep balanceBuilder = BalanceModule.builder().withBalance(getBalance());
        Optional.ofNullable(availableAmount)
                .map(b -> ExactCurrencyAmount.of(b, IcaBankenConstants.CURRENCY))
                .ifPresent(balanceBuilder::setAvailableBalance);
        Optional.ofNullable(creditLimit)
                .map(b -> ExactCurrencyAmount.of(b, IcaBankenConstants.CURRENCY))
                .ifPresent(balanceBuilder::setCreditLimit);
        return balanceBuilder.build();
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
