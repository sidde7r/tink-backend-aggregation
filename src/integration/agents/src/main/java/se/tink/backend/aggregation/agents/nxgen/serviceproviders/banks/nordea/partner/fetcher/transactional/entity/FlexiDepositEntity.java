package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FlexiDepositEntity {
    @JsonProperty("original_amount")
    private BigDecimal originalAmount;

    @JsonProperty("earnings_before_tax")
    private BigDecimal earningsBeforeTax;

    @JsonProperty("tax_amount")
    private BigDecimal taxAmount;

    @JsonProperty("auto_renewal_enabled")
    private boolean autoRenewalEnabled;

    @JsonProperty("settlement_account_number")
    private String settlementAccountNumber;

    @JsonProperty("period_length")
    private String periodLength;

    @JsonProperty("lowest_allowed_balance")
    private BigDecimal lowestAllowedBalance;

    @JsonProperty("highest_allowed_balance")
    private BigDecimal highestAllowedBalance;

    @JsonProperty("lowest_allowed_balance_in_next_period")
    private BigDecimal lowestAllowedBalanceInNextPeriod;

    @JsonProperty("highest_allowed_balance_in_next_period")
    private BigDecimal highestAllowedBalanceInNextPeriod;

    @JsonProperty("lowest_balance")
    private BigDecimal lowestBalance;

    @JsonProperty("current_interest_rate")
    private BigDecimal currentInterestRate;

    @JsonProperty("interest_payment_date")
    private String interestPaymentDate;

    @JsonProperty("start_date")
    private String startDate;
}
