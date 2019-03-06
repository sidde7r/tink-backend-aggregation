package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.FormatsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.RelatedContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities.AmortizationScheduleEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities.InstallmentsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities.InterestEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.entities.ProductEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.DateUtils;

@JsonObject
public class LoanDetailsResponse {
    private AmountEntity awardedAmount;
    private ProductEntity product;
    private String nextPaymentDate;
    private FormatsEntity formats;
    private AmountEntity redeemedAmount;
    private String counterPart;
    private String dueDate;
    private List<AmortizationScheduleEntity> amortizationSchedule;
    private BankEntity bank;
    private List<RelatedContractEntity> relatedContracts;
    private InstallmentsEntity installments;
    private String validityDate;
    private int installmentTotalCount;
    private AmountEntity initialAmount;
    private String amortizationDescription;
    private AmountEntity delinquencyAmount;
    private String id;
    private int pendingPayments;
    private AmountEntity pendingAmount;

    @JsonProperty("interests")
    private List<InterestEntity> interestRates;

    public AmountEntity getAwardedAmount() {
        return awardedAmount;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public String getNextPaymentDate() {
        return nextPaymentDate;
    }

    public FormatsEntity getFormats() {
        return formats;
    }

    public AmountEntity getRedeemedAmount() {
        return redeemedAmount;
    }

    public String getCounterPart() {
        return counterPart;
    }

    public String getDueDate() {
        return dueDate;
    }

    public List<AmortizationScheduleEntity> getAmortizationSchedule() {
        return amortizationSchedule;
    }

    public BankEntity getBank() {
        return bank;
    }

    public List<RelatedContractEntity> getRelatedContracts() {
        return relatedContracts;
    }

    public InstallmentsEntity getInstallments() {
        return installments;
    }

    public String getValidityDate() {
        return validityDate;
    }

    public int getInstallmentTotalCount() {
        return installmentTotalCount;
    }

    public AmountEntity getInitialAmount() {
        return initialAmount;
    }

    public String getAmortizationDescription() {
        return amortizationDescription;
    }

    public AmountEntity getDelinquencyAmount() {
        return delinquencyAmount;
    }

    public String getId() {
        return id;
    }

    public List<InterestEntity> getInterestRates() {
        return interestRates;
    }

    @JsonIgnore
    public Optional<InterestEntity> getFirstInterestRate() {
        return Optional.ofNullable(interestRates)
                .filter(list -> list.size() > 0)
                .map(list -> list.get(0));
    }

    public int getPendingPayments() {
        return pendingPayments;
    }

    public AmountEntity getPendingAmount() {
        return pendingAmount;
    }

    public LoanAccount toTinkLoanAccount(LoanEntity loan) {
        final double interestRate =
                getFirstInterestRate().map(InterestEntity::getPercentage).orElse((double) 0);

        final Date reviewDate =
                getFirstInterestRate()
                        .map(InterestEntity::getReviewDate)
                        .map(DateUtils::parseDate)
                        .orElse(null);

        final Date initialDate = DateUtils.parseDate(validityDate);
        final Amount initialBalance = initialAmount.toTinkAmount().negate();

        final LoanDetails loanDetails =
                LoanDetails.builder(loan.getTinkLoanType())
                        .setInitialDate(initialDate)
                        .setInitialBalance(initialBalance)
                        .setLoanNumber(loan.getDigit())
                        .setNextDayOfTermsChange(reviewDate)
                        .setNumMonthsBound(installmentTotalCount)
                        .setAmortized(redeemedAmount.toTinkAmount())
                        // .setApplicants()
                        .build();

        return loan.toTinkLoanAccount(interestRate, loanDetails);
    }
}
