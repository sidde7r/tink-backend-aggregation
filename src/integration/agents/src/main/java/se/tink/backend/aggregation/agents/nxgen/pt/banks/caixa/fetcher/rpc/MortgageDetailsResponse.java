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

    public BigDecimal getAmountContracted() {
        return amountContracted;
    }

    public BigDecimal getAmountNextInstallment() {
        return amountNextInstallment;
    }

    public BigDecimal getAmountOverdue() {
        return amountOverdue;
    }

    public BigDecimal getAnualRate() {
        return anualRate;
    }

    public String getLoanType() {
        return loanType;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getLoanEndDate() {
        return loanEndDate;
    }
}
