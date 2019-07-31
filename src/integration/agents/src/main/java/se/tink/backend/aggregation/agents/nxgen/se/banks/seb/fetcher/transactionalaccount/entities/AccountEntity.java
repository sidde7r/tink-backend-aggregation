package se.tink.backend.aggregation.agents.nxgen.se.banks.seb.fetcher.transactionalaccount.entities;

import static se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.ACCOUNT_TYPE_MAPPER;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.seb.SEBConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {
    private static final String ACCOUNT_NUMBER_PATTERN = "^[0-9]{11}|[0-9a-f]{32}$";

    private Optional<? extends AccountIdentifier> parsedIdentifier;

    @JsonProperty("ROW_ID")
    private String rowId;

    @JsonProperty("KUND_NR_PERSORG")
    private String KUND_NR_PERSORG;

    @JsonProperty("KONTO_NR")
    private String accountNumber;

    @JsonProperty("KTOSLAG_TXT")
    private String accountTypeName; // type, and name if accountName is empty

    @JsonProperty("BOKF_SALDO")
    private BigDecimal balance;

    @JsonProperty("DISP_BEL")
    private BigDecimal availableAmount; // including reserved transactions

    @JsonProperty("KTOBEN_TXT")
    private String accountName; // might be empty

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
    private Integer accountType;

    @JsonProperty("KTOUTDR_UTSKR")
    private String KTOUTDR_UTSKR;

    @JsonIgnore
    private boolean canPayInvoices() {
        return "1".equals(canPayInvoices);
    }

    @JsonIgnore
    private boolean canTransferFrom() {
        return "1".equals(canTransferFrom);
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        final AccountTypes type = getAccountType().orElse(AccountTypes.OTHER);
        switch (type) {
            case CHECKING:
            case SAVINGS:
                return true;
            default:
                return false;
        }
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
        return StringUtils.firstLetterUppercaseFormatting(holderName);
    }

    @JsonIgnore
    private Optional<AccountTypes> getAccountType() {
        return ACCOUNT_TYPE_MAPPER.translate(accountType);
    }

    @JsonIgnore
    private String getCurrency() {
        return "SEK";
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        if (creditAmount == null || BigDecimal.ZERO.equals(creditAmount)) {
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
    public TransactionalAccount toTinkAccount(String customerId) {
        final Optional<AccountTypes> accountType = getAccountType();
        Preconditions.checkState(accountType.isPresent());
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
                .withType(TransactionalAccountType.from(accountType.get()))
                .withBalance(getBalanceModule())
                .withId(idModule)
                .setApiIdentifier(accountNumber)
                .addHolderName(getHolderName())
                .putInTemporaryStorage(StorageKeys.ACCOUNT_CUSTOMER_ID, customerId)
                .build();
    }
}
