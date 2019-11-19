package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MortgageDetailsResponse {
    private String accountOA;
    private BigDecimal actualInterestRate;
    private BigDecimal amountCharged;
    private BigDecimal amountCommissionPayed;
    private BigDecimal amountContracted;
    private BigDecimal amountDeferred;
    private BigDecimal amountExpensesPayed;
    private BigDecimal amountExtraAmortizations;
    private BigDecimal amountInterestPayed;
    private BigDecimal amountNextInstallment;
    private BigDecimal amountOverdue;
    private BigDecimal amountPayed;
    private BigDecimal amountUsed;
    private BigDecimal anualRate;
    private BigDecimal availableAmount;
    private String bonusClass;
    private Integer bonusCode;
    private String bonusCodeDesc;
    private BigDecimal bonusRate;
    private String branchName;
    private String currency;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dateCreated;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dateNextInstallment;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date dateNextInterestInstallment;

    private Integer deferrementTermType;
    private String deferrementTermTypeDesc;
    private String financialSituation;
    private Integer installmentPeriodicity;
    private String installmentPeriodicityDesc;
    private Integer installmentsPeriodicity;
    private String intallmentType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date loanEndDate;

    private Integer loanFinalityCode;
    private String loanFinalityCodeDesc;
    private String loanICPTStatus;
    private String loanStatusOPCR;
    private String loanType;
    private String loanTypeOPRC;
    private BigDecimal nominalTermInterestRate;
    private Integer numberInstallmentsCollection;
    private Integer numberInstallmentsDebt;
    private Integer numberPayedInstallments;
    private Integer overdueInstallmentsNumber;
    private Integer paymentTerm;
    private String paymentTermType;
    private String paymentTermTypeDesc;
    private BigDecimal percentageAmountDeferred;
    private BigDecimal periodicityInstallmentValue;
    private String product;
    private BigDecimal rateStampAmountPayed;
    private String rateType;
    private String referenceInterestRate;
    private String refundTerm;
    private Integer refundTermType;
    private String refundTermTypeDesc;
    private BigDecimal spread;
    private String spreadSignal;
    private Integer termDefferement;
    private Integer totalNumberInstallments;
    private Integer useTerm;
    private Integer utilizationTermType;
    private String utilizationTermTypeDesc;
    private String warrantyCode;

    public String getAccountOA() {
        return accountOA;
    }

    public BigDecimal getActualInterestRate() {
        return actualInterestRate;
    }

    public BigDecimal getAmountCharged() {
        return amountCharged;
    }

    public BigDecimal getAmountCommissionPayed() {
        return amountCommissionPayed;
    }

    public BigDecimal getAmountContracted() {
        return amountContracted;
    }

    public BigDecimal getAmountDeferred() {
        return amountDeferred;
    }

    public BigDecimal getAmountExpensesPayed() {
        return amountExpensesPayed;
    }

    public BigDecimal getAmountExtraAmortizations() {
        return amountExtraAmortizations;
    }

    public BigDecimal getAmountInterestPayed() {
        return amountInterestPayed;
    }

    public BigDecimal getAmountNextInstallment() {
        return amountNextInstallment;
    }

    public BigDecimal getAmountOverdue() {
        return amountOverdue;
    }

    public BigDecimal getAmountPayed() {
        return amountPayed;
    }

    public BigDecimal getAmountUsed() {
        return amountUsed;
    }

    public BigDecimal getAnualRate() {
        return anualRate;
    }

    public BigDecimal getAvailableAmount() {
        return availableAmount;
    }

    public String getBonusClass() {
        return bonusClass;
    }

    public Integer getBonusCode() {
        return bonusCode;
    }

    public String getBonusCodeDesc() {
        return bonusCodeDesc;
    }

    public BigDecimal getBonusRate() {
        return bonusRate;
    }

    public String getBranchName() {
        return branchName;
    }

    public Integer getDeferrementTermType() {
        return deferrementTermType;
    }

    public String getDeferrementTermTypeDesc() {
        return deferrementTermTypeDesc;
    }

    public String getFinancialSituation() {
        return financialSituation;
    }

    public Integer getInstallmentPeriodicity() {
        return installmentPeriodicity;
    }

    public String getInstallmentPeriodicityDesc() {
        return installmentPeriodicityDesc;
    }

    public Integer getInstallmentsPeriodicity() {
        return installmentsPeriodicity;
    }

    public String getIntallmentType() {
        return intallmentType;
    }

    public Integer getLoanFinalityCode() {
        return loanFinalityCode;
    }

    public String getLoanFinalityCodeDesc() {
        return loanFinalityCodeDesc;
    }

    public String getLoanICPTStatus() {
        return loanICPTStatus;
    }

    public String getLoanStatusOPCR() {
        return loanStatusOPCR;
    }

    public String getLoanType() {
        return loanType;
    }

    public String getLoanTypeOPRC() {
        return loanTypeOPRC;
    }

    public BigDecimal getNominalTermInterestRate() {
        return nominalTermInterestRate;
    }

    public Integer getNumberInstallmentsCollection() {
        return numberInstallmentsCollection;
    }

    public Integer getNumberInstallmentsDebt() {
        return numberInstallmentsDebt;
    }

    public Integer getNumberPayedInstallments() {
        return numberPayedInstallments;
    }

    public Integer getOverdueInstallmentsNumber() {
        return overdueInstallmentsNumber;
    }

    public Integer getPaymentTerm() {
        return paymentTerm;
    }

    public String getPaymentTermType() {
        return paymentTermType;
    }

    public String getPaymentTermTypeDesc() {
        return paymentTermTypeDesc;
    }

    public BigDecimal getPercentageAmountDeferred() {
        return percentageAmountDeferred;
    }

    public BigDecimal getPeriodicityInstallmentValue() {
        return periodicityInstallmentValue;
    }

    public String getProduct() {
        return product;
    }

    public BigDecimal getRateStampAmountPayed() {
        return rateStampAmountPayed;
    }

    public String getRateType() {
        return rateType;
    }

    public String getReferenceInterestRate() {
        return referenceInterestRate;
    }

    public String getRefundTerm() {
        return refundTerm;
    }

    public Integer getRefundTermType() {
        return refundTermType;
    }

    public String getRefundTermTypeDesc() {
        return refundTermTypeDesc;
    }

    public BigDecimal getSpread() {
        return spread;
    }

    public String getSpreadSignal() {
        return spreadSignal;
    }

    public Integer getTermDefferement() {
        return termDefferement;
    }

    public Integer getTotalNumberInstallments() {
        return totalNumberInstallments;
    }

    public Integer getUseTerm() {
        return useTerm;
    }

    public Integer getUtilizationTermType() {
        return utilizationTermType;
    }

    public String getUtilizationTermTypeDesc() {
        return utilizationTermTypeDesc;
    }

    public String getWarrantyCode() {
        return warrantyCode;
    }

    public String getCurrency() {
        return currency;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getDateNextInstallment() {
        return dateNextInstallment;
    }

    public Date getDateNextInterestInstallment() {
        return dateNextInterestInstallment;
    }

    public Date getLoanEndDate() {
        return loanEndDate;
    }
}
