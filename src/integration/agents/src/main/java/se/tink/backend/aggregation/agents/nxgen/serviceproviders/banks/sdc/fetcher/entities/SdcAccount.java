package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.converter.AccountNumberToIbanConverter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@Slf4j
@JsonObject
public class SdcAccount {

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
    public TransactionalAccount toTinkAccount(final AccountNumberToIbanConverter converter) {
        AccountTypes accountTypes = convertAccountType();

        if (accountTypes.equals(AccountTypes.CHECKING)
                || accountTypes.equals(AccountTypes.SAVINGS)) {
            return TransactionalAccount.nxBuilder()
                    .withType(
                            accountTypes.equals(AccountTypes.CHECKING)
                                    ? TransactionalAccountType.CHECKING
                                    : TransactionalAccountType.SAVINGS)
                    .withFlags(AccountFlag.PSD2_PAYMENT_ACCOUNT)
                    .withBalance(BalanceModule.of(amount.toExactCurrencyAmount()))
                    .withId(
                            IdModule.builder()
                                    .withUniqueIdentifier(converter.convertToIban(id))
                                    .withAccountNumber(converter.convertToIban(id))
                                    .withAccountName(name)
                                    .addIdentifier(new BbanIdentifier(normalizedBankId()))
                                    .addIdentifier(new IbanIdentifier(converter.convertToIban(id)))
                                    .build())
                    .setApiIdentifier(id)
                    .canPlaceFunds(canPlaceFunds())
                    .canWithdrawCash(canWithdrawCash())
                    .canExecuteExternalTransfer(canExecuteExternalTransfer())
                    .canReceiveExternalTransfer(canReceiveExternalTransfer())
                    .build()
                    .get();
        }

        return TransactionalAccount.builder(
                        accountTypes, converter.convertToIban(id), amount.toExactCurrencyAmount())
                .setAccountNumber(id)
                .setName(name)
                .setBankIdentifier(normalizedBankId())
                .canPlaceFunds(canPlaceFunds())
                .canWithdrawCash(canWithdrawCash())
                .canExecuteExternalTransfer(canExecuteExternalTransfer())
                .canReceiveExternalTransfer(canReceiveExternalTransfer())
                .build();
    }

    @JsonIgnore
    public CreditCardAccount toTinkCreditCardAccount() {
        return CreditCardAccount.builder(
                        id, amount.toExactCurrencyAmount(), availableAmount.toExactCurrencyAmount())
                .setAccountNumber(localizedAccountId)
                .setName(name)
                .setBankIdentifier(normalizedBankId())
                .canPlaceFunds(canPlaceFunds())
                .canWithdrawCash(canWithdrawCash())
                .canExecuteExternalTransfer(canExecuteExternalTransfer())
                .canReceiveExternalTransfer(canReceiveExternalTransfer())
                .sourceInfo(createAccountSourceInfo())
                .build();
    }

    @JsonIgnore
    private AccountSourceInfo createAccountSourceInfo() {
        return AccountSourceInfo.builder()
                .bankProductCode(productElementType)
                .bankAccountType(type)
                .bankProductName(name)
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
        log.info("Found unknown productElementType: " + productElementType);
        return AccountTypes.OTHER;
    }

    @JsonIgnore
    public AccountCapabilities.Answer canPlaceFunds() {
        if (Objects.isNull(accountProperties)) {
            return AccountCapabilities.Answer.UNKNOWN;
        }

        // We can place funds if we can debit the account.
        return AccountCapabilities.Answer.From(accountProperties.isDebitable());
    }

    @JsonIgnore
    public AccountCapabilities.Answer canWithdrawCash() {
        if (Objects.isNull(accountProperties)) {
            return AccountCapabilities.Answer.UNKNOWN;
        }

        // We can withdraw cash if we can credit the account.
        return AccountCapabilities.Answer.From(accountProperties.isCreditable());
    }

    @JsonIgnore
    public AccountCapabilities.Answer canExecuteExternalTransfer() {
        if (Objects.isNull(accountProperties)) {
            return AccountCapabilities.Answer.UNKNOWN;
        }

        // We can make a transfer if we can credit the account.
        return AccountCapabilities.Answer.From(accountProperties.isCreditable());
    }

    @JsonIgnore
    public AccountCapabilities.Answer canReceiveExternalTransfer() {
        if (Objects.isNull(accountProperties)) {
            return AccountCapabilities.Answer.UNKNOWN;
        }

        // We can receive a transfer if we can debit the account.
        return AccountCapabilities.Answer.From(accountProperties.isDebitable());
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
