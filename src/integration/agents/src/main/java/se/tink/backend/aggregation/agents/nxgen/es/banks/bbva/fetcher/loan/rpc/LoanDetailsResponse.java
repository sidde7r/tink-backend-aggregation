package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.rpc;

import static se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.Defaults.TIMEZONE_CET;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.FormatsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.RelatedContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities.AmortizationScheduleEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities.InstallmentsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities.InterestEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities.ProductEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class LoanDetailsResponse {
    private static final Logger log = LoggerFactory.getLogger(LoanDetailsResponse.class);

    private AmountEntity awardedAmount;
    private ProductEntity product;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date nextPaymentDate;

    private FormatsEntity formats;
    private AmountEntity redeemedAmount;
    private String counterPart;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date dueDate;

    private List<AmortizationScheduleEntity> amortizationSchedule;
    private BankEntity bank;
    private List<RelatedContractEntity> relatedContracts;
    private InstallmentsEntity installments;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date validityDate;

    private int installmentTotalCount;
    private AmountEntity initialAmount;
    private String amortizationDescription;
    private AmountEntity delinquencyAmount;
    private String id;
    private int pendingPayments;
    private AmountEntity pendingAmount;

    @JsonProperty("interests")
    private List<InterestEntity> interestRates;

    @JsonIgnore
    private LocalDate getValidityDateAsLocalDate() {
        return validityDate.toInstant().atZone(ZoneId.of(TIMEZONE_CET)).toLocalDate();
    }

    @JsonIgnore
    private BigDecimal getFirstInterestRate() {
        BigDecimal interestPercentage =
                interestRates.headOption().map(InterestEntity::getPercentage).getOrNull();

        if (interestPercentage == null) {
            return null;
        }

        return AgentParsingUtils.parsePercentageFormInterest(interestPercentage);
    }

    @JsonIgnore
    private LocalDate getFirstInterestReviewDate() {
        return interestRates.headOption().map(InterestEntity::getReviewDateAsLocalDate).getOrNull();
    }

    @JsonIgnore
    public ExactCurrencyAmount getBalance() {
        if (Objects.nonNull(pendingAmount)) {
            return pendingAmount.toTinkAmount().negate();
        }

        // Sometimes pendingAmount isn't there
        final BigDecimal balance =
                initialAmount.getAmount().subtract(redeemedAmount.getAmount()).negate();
        return ExactCurrencyAmount.of(balance, initialAmount.getCurrency());
    }

    @JsonIgnore
    public Optional<LoanModule> getLoanModuleWithTypeAndLoanNumber(
            LoanDetails.Type loanType, String loanNumber) {

        final BigDecimal interestRate = getFirstInterestRate();

        if (interestRate == null) {
            log.warn("Unable to parse interest for loan with id {}. Ignoring loan.", loanNumber);
            return Optional.empty();
        }

        return Optional.of(
                LoanModule.builder()
                        .withType(loanType)
                        .withBalance(getBalance())
                        .withInterestRate(interestRate.doubleValue())
                        .setInitialBalance(initialAmount.toTinkAmount().negate())
                        .setMonthlyAmortization(installments.getInstallmentAmount().toTinkAmount())
                        .setAmortized(redeemedAmount.toTinkAmount())
                        .setLoanNumber(loanNumber)
                        .setInitialDate(getValidityDateAsLocalDate())
                        .setNextDayOfTermsChange(getFirstInterestReviewDate())
                        .build());
    }
}
