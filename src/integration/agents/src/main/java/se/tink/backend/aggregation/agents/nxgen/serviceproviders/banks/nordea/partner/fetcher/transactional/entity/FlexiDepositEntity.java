package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.partner.fetcher.transactional.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FlexiDepositEntity {
    @JsonProperty("original_amount")
    private double originalAmount;

    @JsonProperty("earnings_before_tax")
    private double earningsBeforeTax;

    @JsonProperty("tax_amount")
    private double taxAmount;

    @JsonProperty("auto_renewal_enabled")
    private boolean autoRenewalEnabled;

    @JsonProperty("settlement_account_number")
    private String settlementAccountNumber;

    @JsonProperty("period_length")
    private String periodLength;

    @JsonProperty("lowest_allowed_balance")
    private double lowestAllowedBalance;

    @JsonProperty("highest_allowed_balance")
    private double highestAllowedBalance;

    @JsonProperty("lowest_allowed_balance_in_next_period")
    private double lowestAllowedBalanceInNextPeriod;

    @JsonProperty("highest_allowed_balance_in_next_period")
    private double highestAllowedBalanceInNextPeriod;

    @JsonProperty("lowest_balance")
    private double lowestBalance;

    @JsonProperty("current_interest_rate")
    private double currentInterestRate;

    @JsonProperty("interest_payment_date")
    private String interestPaymentDate;

    @JsonProperty("start_date")
    private String startDate;
}
