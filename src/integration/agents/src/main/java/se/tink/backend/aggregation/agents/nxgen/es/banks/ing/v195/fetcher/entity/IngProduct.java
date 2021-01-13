package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.AccountCategories;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

@JsonObject
public final class IngProduct {

    private String productNumber;
    private BigDecimal balance;
    private Double availableBalance;
    private Integer type;
    private String subtype;
    private String name;
    private Integer interventionCod;
    private String interventionDesc;
    private IngStatus status;
    private Boolean moreInterveners;
    private Boolean enableAlias;
    private String openingDate;
    private String cid;
    private String currency;
    private String branchCode;

    // Cards have a single holder (Credit card, debit card, etc)
    // Accounts/Loans have an array of holders
    private IngHolder holder;
    private List<IngHolder> holders;

    private String bic;
    private String bank;
    private String iban;
    private Double assignedOverdraftLimit;
    private String uuid;
    private String alias;
    private Double nominal;
    private Object tae; // this is sometimes a double and sometimes a string
    private Double sinceOpen;
    private Double sinceYear;
    private Double sinceMonth;
    private Double yearRetention;
    private Double initialAmount;
    private Double pendingAmount;
    private String signClientDate;
    private String expirationDate;
    private String nextPaymentDate;
    private Double nextPaymentAmount;
    private Double nominalType;
    private IngAssociatedAccount associatedAccount;
    private Boolean stableType;
    private Boolean fixedType;
    private Double euribor;
    private String differential;
    private String nextRevisionDate;
    private String nextRevisionEuribor;
    private String nextRevisionExpirationDate;
    private Boolean mortgageRD;
    private Boolean hasPayroll;
    private Boolean hasLifeInsurance;
    private Boolean hasHomeInsurance;
    private String deedDifferential;
    private IngStatus insuranceStatus;
    private Double capitalInsured;
    private Double capitalOptional;
    private Double capital;
    private Boolean conditionsNotAccepted;
    private IngStatus statusIndra;
    private IngStatus statusOnOff;
    private IngStatus statusContactless;
    private String cardType;
    private Double pendingAuthorizationAmount;
    private Double creditLimit;
    private Double monthPurchasesAmount;
    private String paymentMethod;
    private Double monthlyFee;
    private Double lastMonthDeferredAmount;
    private Double spentAmount;
    private String anualInterest;
    private String expedientNumber;
    private Boolean isRealTimeData;
    private String codConTar;
    private Double availableCreditAmount;
    private Double paidAmount;
    private Integer paidPayments;
    private Integer pendingPayments;
    private Double nextPayOffAmount;
    private String nextPayOffDate;
    private String initDate;
    private Double investment;
    private Double performance;
    private Double assessment;
    private String assessmentDate;
    private Double cumulativeReturn;
    private Double currentYearReturn;
    private Double lastTwelveMonthsReturn;
    private Double lastMonthReturn;
    private Double numberOfShares;
    private Double lastNetAssetValue;
    private String lastNetAssetValueDate;

    public String getProductNumber() {
        return productNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Double getAvailableBalance() {
        return availableBalance;
    }

    public Integer getType() {
        return type;
    }

    public String getSubtype() {
        return subtype;
    }

    public String getName() {
        return name;
    }

    public Integer getInterventionCod() {
        return interventionCod;
    }

    public String getInterventionDesc() {
        return interventionDesc;
    }

    public IngStatus getStatus() {
        return status;
    }

    public Boolean getMoreInterveners() {
        return moreInterveners;
    }

    public Boolean getEnableAlias() {
        return enableAlias;
    }

    public String getOpeningDate() {
        return openingDate;
    }

    public String getCid() {
        return cid;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public List<IngHolder> getHolders() {
        return holders;
    }

    public String getBic() {
        return bic;
    }

    public String getBank() {
        return bank;
    }

    public String getIban() {
        return iban;
    }

    public Double getAssignedOverdraftLimit() {
        return assignedOverdraftLimit;
    }

    public String getUuid() {
        return uuid;
    }

    public String getAlias() {
        return alias;
    }

    public Double getNominal() {
        return nominal;
    }

    public Double getSinceOpen() {
        return sinceOpen;
    }

    public Double getSinceYear() {
        return sinceYear;
    }

    public Double getSinceMonth() {
        return sinceMonth;
    }

    public Double getYearRetention() {
        return yearRetention;
    }

    public Double getInitialAmount() {
        return initialAmount;
    }

    public Double getPendingAmount() {
        return pendingAmount;
    }

    public String getSignClientDate() {
        return signClientDate;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public String getNextPaymentDate() {
        return nextPaymentDate;
    }

    public Double getNextPaymentAmount() {
        return nextPaymentAmount;
    }

    public Double getNominalType() {
        return nominalType;
    }

    public IngAssociatedAccount getAssociatedAccount() {
        return associatedAccount;
    }

    public Boolean getStableType() {
        return stableType;
    }

    public Boolean getFixedType() {
        return fixedType;
    }

    public Double getEuribor() {
        return euribor;
    }

    public String getDifferential() {
        return differential;
    }

    public String getNextRevisionDate() {
        return nextRevisionDate;
    }

    public String getNextRevisionEuribor() {
        return nextRevisionEuribor;
    }

    public String getNextRevisionExpirationDate() {
        return nextRevisionExpirationDate;
    }

    public Boolean getMortgageRD() {
        return mortgageRD;
    }

    public Boolean getHasPayroll() {
        return hasPayroll;
    }

    public Boolean getHasLifeInsurance() {
        return hasLifeInsurance;
    }

    public Boolean getHasHomeInsurance() {
        return hasHomeInsurance;
    }

    public String getDeedDifferential() {
        return deedDifferential;
    }

    public IngStatus getInsuranceStatus() {
        return insuranceStatus;
    }

    public Double getCapitalInsured() {
        return capitalInsured;
    }

    public Double getCapitalOptional() {
        return capitalOptional;
    }

    public Double getCapital() {
        return capital;
    }

    public Boolean getConditionsNotAccepted() {
        return conditionsNotAccepted;
    }

    public IngHolder getHolder() {
        return holder;
    }

    public IngStatus getStatusIndra() {
        return statusIndra;
    }

    public IngStatus getStatusOnOff() {
        return statusOnOff;
    }

    public IngStatus getStatusContactless() {
        return statusContactless;
    }

    public String getCardType() {
        return cardType;
    }

    public Double getPendingAuthorizationAmount() {
        return pendingAuthorizationAmount;
    }

    public Double getCreditLimit() {
        return creditLimit;
    }

    public Double getMonthPurchasesAmount() {
        return monthPurchasesAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public Double getMonthlyFee() {
        return monthlyFee;
    }

    public Double getLastMonthDeferredAmount() {
        return lastMonthDeferredAmount;
    }

    public Double getSpentAmount() {
        return spentAmount;
    }

    public String getAnualInterest() {
        return anualInterest;
    }

    public String getExpedientNumber() {
        return expedientNumber;
    }

    public Boolean getRealTimeData() {
        return isRealTimeData;
    }

    public String getCodConTar() {
        return codConTar;
    }

    public Double getAvailableCreditAmount() {
        return availableCreditAmount;
    }

    public Double getPaidAmount() {
        return paidAmount;
    }

    public Integer getPaidPayments() {
        return paidPayments;
    }

    public Integer getPendingPayments() {
        return pendingPayments;
    }

    public Double getNextPayOffAmount() {
        return nextPayOffAmount;
    }

    public String getNextPayOffDate() {
        return nextPayOffDate;
    }

    public String getInitDate() {
        return initDate;
    }

    public Double getInvestment() {
        return investment;
    }

    public Double getPerformance() {
        return performance;
    }

    public Double getAssessment() {
        return assessment;
    }

    public String getAssessmentDate() {
        return assessmentDate;
    }

    public Double getCumulativeReturn() {
        return cumulativeReturn;
    }

    public Double getCurrentYearReturn() {
        return currentYearReturn;
    }

    public Double getLastTwelveMonthsReturn() {
        return lastTwelveMonthsReturn;
    }

    public Double getLastMonthReturn() {
        return lastMonthReturn;
    }

    public Double getNumberOfShares() {
        return numberOfShares;
    }

    public Double getLastNetAssetValue() {
        return lastNetAssetValue;
    }

    public String getLastNetAssetValueDate() {
        return lastNetAssetValueDate;
    }

    @JsonIgnore
    public String getAliasOrProductName() {
        return !Strings.isNullOrEmpty(alias) ? alias : name;
    }

    @JsonIgnore
    public String getIbanCanonical() {
        if (iban != null) {
            return iban.replaceAll("[^A-Z0-9]", "");
        }

        return null;
    }

    @JsonIgnore
    public Double getTaeAsDouble() {
        if (tae != null) {
            if (tae instanceof Number) {
                return ((Number) tae).doubleValue();
            } else if (tae instanceof String) {
                try {
                    return Double.parseDouble((String) tae);
                } catch (NumberFormatException ex) {
                    // Ignored
                }
            }
        }

        return null;
    }

    @JsonIgnore
    public boolean isActiveTransactionalAccount() {
        boolean isTransactionalAccount =
                IngConstants.AccountCategories.TRANSACTION_ACCOUNTS.contains(type);
        boolean isSavingsAccount = IngConstants.AccountCategories.SAVINGS_ACCOUNTS.contains(type);

        boolean isOperative = IngConstants.AccountStatus.OPERATIVE.equals(status.getCod());
        boolean isSingleContract =
                IngConstants.AccountStatus.SINGLE_CONTRACT.equals(status.getCod());

        return (isTransactionalAccount || isSavingsAccount) && (isOperative || isSingleContract);
    }

    @JsonIgnore
    public boolean isActiveCreditCardAccount() {
        boolean isCreditCard = IngConstants.AccountTypes.CREDIT_CARD.equals(type);
        boolean isActive = IngConstants.AccountStatus.ACTIVE.equals(status.getCod());
        return isCreditCard && isActive;
    }

    @JsonIgnore
    public boolean isActiveInvestmentAccount() {
        boolean isInvestmentAccount = IngConstants.AccountCategories.INVESTMENT.contains(type);
        boolean isActive = IngConstants.AccountStatus.OPERATIVE.equals(status.getCod());
        return isInvestmentAccount && isActive;
    }

    @JsonIgnore
    public boolean isActiveLoanAccount() {
        boolean isLoanAccount = IngConstants.AccountCategories.LOANS.contains(type);
        boolean isActive = IngConstants.AccountStatus.OPERATIVE.equals(status.getCod());
        return isLoanAccount && isActive;
    }

    @JsonIgnore
    public TransactionalAccountType getTransactionalAccountType() {
        if (AccountCategories.TRANSACTION_ACCOUNTS.contains(type)) {
            return TransactionalAccountType.CHECKING;
        } else if (AccountCategories.SAVINGS_ACCOUNTS.contains(type)) {
            return TransactionalAccountType.SAVINGS;
        } else {
            throw new IllegalStateException("Not a transactional account");
        }
    }
}
