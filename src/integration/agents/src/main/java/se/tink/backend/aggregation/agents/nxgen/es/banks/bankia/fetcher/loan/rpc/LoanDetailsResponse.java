package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.AmortizationLiquidationEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.CommissionsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.DebtorEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.InterestTypesEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.LoanAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.LoanAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.LoanInstallmentsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.PaymentDatesEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.ProductInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.loan.entities.TermsAndPeriodicitiesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;
import se.tink.libraries.amount.Amount;

@JsonObject
public class LoanDetailsResponse {
    @JsonProperty("amortizacionLiquidacionPrestamo")
    private AmortizationLiquidationEntity amortizationLiquidation;
    @JsonProperty("comisiones")
    private CommissionsEntity commissions;
    @JsonProperty("cuantiaPrestamo")
    private LoanAmountEntity loanAmountEntity;
    @JsonProperty("plazosYPeriodicidadesPrestamo")
    private TermsAndPeriodicitiesEntity termsAndPeriodicities;
    @JsonProperty("fechasPago")
    private PaymentDatesEntity paymentDates;
    @JsonProperty("tiposInteres")
    private InterestTypesEntity interestTypes;
    @JsonProperty("cuotasPrestamo")
    private LoanInstallmentsEntity loanInstallments;
    @JsonProperty("informacionProducto")
    private ProductInformationEntity productInformation;
    @JsonProperty("intervinientes")
    private List<DebtorEntity> debtors;
    @JsonProperty("clavePaginacionSalida")
    private String exitPageKey;
    @JsonProperty("numeroIntervinientes")
    private int numberDebtors;
    @JsonProperty("indicadorMasIntervinientes")
    private boolean moreDebtors;

    @JsonIgnore
    public LoanDetails toLoanDetails(LoanAccountEntity loan) {
        List<String> applicants = getApplicants();
        AmountEntity grantedAmount = loan.getGrantedAmount();
        AmountEntity amortizedAmount = loanAmountEntity.getAmortizedAmount();
        amortizedAmount.setCurrencyName(grantedAmount.toTinkAmount().getCurrency());

        return LoanDetails.builder(getType(loan))
                .setAmortized(amortizedAmount.toTinkAmount())
                .setInitialBalance(grantedAmount.toTinkAmount())
                .setInitialDate(productInformation.getInitialDate().toJavaLangDate())
                .setLoanNumber(loan.getLoanIdentifier())
                .setMonthlyAmortization(getMonthlyAmortization(grantedAmount.toTinkAmount().getCurrency()))
                .setApplicants(applicants)
                .setCoApplicant(applicants.size() > 1)
                .build();
    }

    @JsonIgnore
    private Amount getMonthlyAmortization(String currency) {
        AmountEntity amortizationAmount = null;

        if (loanInstallments != null) {
            amortizationAmount = loanInstallments.getNextAmortizationAmount();
            if (amortizationAmount == null) {
                amortizationAmount = loanInstallments.getCurrentAmortizationAmount();
            }
        }

        if (amortizationAmount != null) {
            amortizationAmount.setCurrencyName(currency);
            return amortizationAmount.toTinkAmount();
        }

        return null;
    }

    @JsonIgnore
    private LoanDetails.Type getType(LoanAccountEntity loan) {
        return BankiaConstants.LOAN_TYPE_MAPPER.translate(loan.getContract().getCustomizedProductCode())
            .orElse(Type.OTHER);
    }

    @JsonIgnore
    private List<String> getApplicants() {
        return Optional.ofNullable(debtors).orElse(Collections.emptyList()).stream()
                .filter(Predicates.not(debtor -> Strings.isNullOrEmpty(debtor.getDebtorName())))
                .filter(debtor -> BankiaConstants.Loans.DEBTOR_CODE.equalsIgnoreCase(debtor.getDebtorRoleCode()))
                .map(DebtorEntity::getDebtorName)
                .collect(Collectors.toList());
    }

    @JsonIgnore
    public String getLoanName() {
        return productInformation.getProductName();
    }

    @JsonIgnore
    public HolderName getHolderName() {
        return getApplicants().stream()
                .findFirst()
                .map(HolderName::new)
                .orElse(null);
    }

    @JsonIgnore
    public double getInterestRate() {

        return interestTypes.getPercentageContract().percentageValue()
                .divide(BigDecimal.valueOf(100, 0))
                .doubleValue();
    }
}
