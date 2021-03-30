package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.loan.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import org.apache.commons.collections4.ListUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.identifiers.DanishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class LoanEntity {

    private static final int MONEY_DECIMAL_SCALE = 2;

    @Getter private String accountId;
    private String currency;
    @Getter private Boolean deleted;
    private double interestRate;
    private BigDecimal loanAmount;
    private String loanProduct;
    private BigDecimal monthlyDownpaymentAmount;

    @JsonProperty("remainingAmount")
    private BigDecimal remainingAmountWithInterestAmount;

    private BigDecimal totalLoanWithInterestAmount;
    private List<TransactionEntity> transactions;

    public List<TransactionEntity> getTransactions() {
        return ListUtils.emptyIfNull(transactions);
    }

    @JsonIgnore
    public LoanAccount toTinkLoan(String accountHolder) {
        return LoanAccount.nxBuilder()
                .withLoanDetails(buildLoanDetails(accountHolder))
                .withId(buildIdModule())
                .addParties(getParties(accountHolder))
                .build();
    }

    private IdModule buildIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(accountId)
                .withAccountNumber(accountId)
                .withAccountName(loanProduct)
                .addIdentifier(new DanishIdentifier(accountId))
                .setProductName(loanProduct)
                .build();
    }

    private LoanModule buildLoanDetails(String accountHolder) {
        return LoanModule.builder()
                .withType(LoanDetails.Type.OTHER)
                .withBalance(getBalance())
                .withInterestRate(interestRate)
                .setApplicants(
                        accountHolder != null ? Collections.singletonList(accountHolder) : null)
                .setInitialBalance(getInitialBalance())
                .setAmortized(getAmortized())
                .setLoanNumber(accountId)
                .setInitialDate(getInitialDate())
                .setMonthlyAmortization(ExactCurrencyAmount.of(monthlyDownpaymentAmount, currency))
                .build();
    }

    private ExactCurrencyAmount getBalance() {
        return ExactCurrencyAmount.of(
                remainingAmountWithInterestAmount
                        .setScale(MONEY_DECIMAL_SCALE, RoundingMode.HALF_EVEN)
                        .negate(),
                currency);
    }

    private ExactCurrencyAmount getInitialBalance() {
        return ExactCurrencyAmount.of(
                loanAmount.setScale(MONEY_DECIMAL_SCALE, RoundingMode.HALF_EVEN).negate(),
                currency);
    }

    private ExactCurrencyAmount getAmortized() {
        return ExactCurrencyAmount.of(
                totalLoanWithInterestAmount
                        .subtract(remainingAmountWithInterestAmount)
                        .setScale(MONEY_DECIMAL_SCALE, RoundingMode.HALF_EVEN),
                currency);
    }

    private LocalDate getInitialDate() {
        // First transaction is an initial transfer of loan money
        return getTransactions().isEmpty()
                ? null
                : Instant.parse(transactions.get(0).getDisplayDate())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
    }

    private List<Party> getParties(String accountHolder) {
        return accountHolder != null
                ? Collections.singletonList(new Party(accountHolder, Party.Role.HOLDER))
                : Collections.emptyList();
    }
}
