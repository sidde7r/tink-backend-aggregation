package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class MortgageInstallmentEntity {
    private static final int VALUE_DECIMAL_PLACES = 2;
    private static final String INCOMING = "PROX PRESTACAO";

    private BigDecimal amount;
    private BigDecimal amountBonus;
    private BigDecimal amountCapital;
    private BigDecimal amountComission;
    private BigDecimal amountExpenses;
    private BigDecimal amountInterests;
    private BigDecimal amountTax;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date billingDate;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dueDate;

    private BigDecimal effectiveInterestRate;
    private BigDecimal number;
    private BigDecimal owedAmount;
    private String situation;

    public Transaction toTinkTransaction(String accountCurrency) {
        return Transaction.builder()
                .setAmount(
                        ExactCurrencyAmount.of(
                                amount.movePointLeft(VALUE_DECIMAL_PLACES), accountCurrency))
                .setDate(billingDate)
                .setPending(INCOMING.equals(situation))
                .build();
    }
}
