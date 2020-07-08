package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebPaymentAccountCapabilities;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AccountEntity {
    private static final String ACCOUNT_NUMBER_PATTERN = "^[0-9]{11}|[0-9a-f]{32}$";

    @JsonProperty("ROW_ID")
    private String rowId;

    @JsonProperty("KUND_NR_PERSORG")
    private String KUND_NR_PERSORG;

    @JsonProperty("KONTO_NR")
    private String accountNumber;

    // type, and name if accountName is empty
    @JsonProperty("KTOSLAG_TXT")
    private String accountTypeName;

    // Shown in app as balance
    @JsonProperty("BOKF_SALDO")
    private BigDecimal balance;

    // Shown in app as available amount, includes reserved transactions
    @JsonProperty("DISP_BEL")
    private BigDecimal availableAmount;

    // Account name might be empty
    @JsonProperty("KTOBEN_TXT")
    private String accountName;

    @JsonProperty("KREDBEL")
    private BigDecimal creditAmount;

    @JsonProperty("KHAV")
    private String holderName;

    @JsonProperty("BETFL")
    private String canPayInvoices;

    @JsonProperty("INSFL")
    private String canTransferTo;

    @JsonProperty("UTTFL")
    private String canTransferFrom;

    @JsonProperty("KTOSLAG_KOD")
    private String accountType;

    @JsonProperty("KTOUTDR_UTSKR")
    private String KTOUTDR_UTSKR;

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return SebConstants.ACCOUNT_TYPE_MAPPER.isOf(accountType, AccountTypes.CHECKING)
                || SebConstants.ACCOUNT_TYPE_MAPPER.isOf(accountType, AccountTypes.SAVINGS);
    }

    @JsonIgnore
    private String getAccountName() {
        if (Strings.isNullOrEmpty(accountName)) {
            return StringUtils.firstLetterUppercaseFormatting(accountTypeName);
        } else {
            return StringUtils.firstLetterUppercaseFormatting(accountName);
        }
    }

    @JsonIgnore
    private String getHolderName() {
        // For business accounts, KHAV is empty while KTOBEN_TXT is the company name
        return !Strings.isNullOrEmpty(holderName)
                ? StringUtils.firstLetterUppercaseFormatting(holderName)
                : accountName;
    }

    @JsonIgnore
    private Optional<AccountTypes> getAccountType() {
        return SebConstants.ACCOUNT_TYPE_MAPPER.translate(accountType);
    }

    @JsonIgnore
    private String getCurrency() {
        return SebConstants.DEFAULT_CURRENCY;
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        if (Objects.isNull(creditAmount) || BigDecimal.ZERO.equals(creditAmount)) {
            return ExactCurrencyAmount.of(balance, getCurrency());
        } else {
            // if there is a credit on this account
            return ExactCurrencyAmount.of(balance.subtract(creditAmount), getCurrency());
        }
    }

    @JsonIgnore
    private ExactCurrencyAmount getAvailableCredit() {
        return ExactCurrencyAmount.of(availableAmount, getCurrency());
    }

    @JsonIgnore
    private BalanceModule getBalanceModule() {
        return BalanceModule.builder()
                .withBalance(getBalance())
                .setAvailableCredit(getAvailableCredit())
                .build();
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(String customerId) {
        final Optional<AccountTypes> tinkAccountType = getAccountType();
        Preconditions.checkState(tinkAccountType.isPresent());
        Preconditions.checkNotNull(accountNumber);
        Preconditions.checkState(
                accountNumber.matches(ACCOUNT_NUMBER_PATTERN),
                "Unexpected account format '%s'.",
                accountNumber);

        final IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(accountNumber)
                        .withAccountNumber(accountNumber)
                        .withAccountName(getAccountName())
                        .addIdentifier(new SwedishIdentifier(accountNumber))
                        .build();

        return TransactionalAccount.nxBuilder()
                .withType(
                        TransactionalAccountType.from(tinkAccountType.get())
                                .orElse(TransactionalAccountType.OTHER))
                .withPaymentAccountFlag()
                .withBalance(getBalanceModule())
                .withId(idModule)
                .setApiIdentifier(accountNumber)
                .addHolderName(getHolderName())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_CUSTOMER_ID, customerId)
                .canWithdrawCash(SebPaymentAccountCapabilities.canWithdrawCash(accountType))
                .canPlaceFunds(SebPaymentAccountCapabilities.canPlaceFunds(accountType))
                .canExecuteExternalTransfer(
                        SebPaymentAccountCapabilities.canExecuteExternalTransfer(accountType))
                .canReceiveExternalTransfer(
                        SebPaymentAccountCapabilities.canReceiveExternalTransfer(accountType))
                .build();
    }
}
