package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.IngConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class Product {

    private String productNumber;
    private double balance;
    private double availableBalance;
    private int type;
    private String subtype;
    private String name;
    private int interventionCod;
    private String interventionDesc;
    private Status status;
    private boolean moreInterveners;
    private boolean enableAlias;
    private String openingDate;
    private String cid;
    private String currency;
    private String branchCode;
    private List<Holder> holders = null;
    private String bic;
    private String bank;
    private String iban;
    private double assignedOverdraftLimit;
    private String uuid;
    private double nominal;
    private String tae;
    private double sinceOpen;
    private double sinceYear;
    private double sinceMonth;
    private double yearRetention;
    private String renta4Id;
    private String brokerageRate;
    private Account associatedAccount;
    private Holder holder;
    private Status statusIndra;
    private Status statusOnOff;
    private Status statusContactless;
    private String cardType;
    private double pendingAuthorizationAmount;
    private double creditLimit;
    private double monthPurchasesAmount;
    private String paymentMethod;
    private double monthlyFee;
    private double lastMonthDeferredAmount;
    private double spentAmount;
    private String nextPaymentDate;
    private String anualInterest;
    private String expedientNumber;
    private boolean isRealTimeData;
    private String codConTar;
    private double availableCreditAmount;

    @JsonIgnore
    public String getUniqueIdentifier() {
        return iban.replaceAll(" ", "").toLowerCase();
    }

    public String getProductNumber() {
        return productNumber;
    }

    public void setProductNumber(String productNumber) {
        this.productNumber = productNumber;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(double availableBalance) {
        this.availableBalance = availableBalance;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getInterventionCod() {
        return interventionCod;
    }

    public void setInterventionCod(int interventionCod) {
        this.interventionCod = interventionCod;
    }

    public String getInterventionDesc() {
        return interventionDesc;
    }

    public void setInterventionDesc(String interventionDesc) {
        this.interventionDesc = interventionDesc;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isMoreInterveners() {
        return moreInterveners;
    }

    public void setMoreInterveners(boolean moreInterveners) {
        this.moreInterveners = moreInterveners;
    }

    public boolean isEnableAlias() {
        return enableAlias;
    }

    public void setEnableAlias(boolean enableAlias) {
        this.enableAlias = enableAlias;
    }

    public String getOpeningDate() {
        return openingDate;
    }

    public void setOpeningDate(String openingDate) {
        this.openingDate = openingDate;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getCurrency() {
        return currency != null ? currency : IngConstants.CURRENCY;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getBranchCode() {
        return branchCode;
    }

    public void setBranchCode(String branchCode) {
        this.branchCode = branchCode;
    }

    /** @see #getHolder() */
    public List<Holder> getHolders() {
        if (holders != null && holders.size() > 0) {
            return holders;
        } else if (holder != null) {
            return Collections.singletonList(holder);
        } else {
            return Collections.emptyList();
        }
    }

    public void setHolders(List<Holder> holders) {
        this.holders = holders;
    }

    public String getBic() {
        return bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public double getAssignedOverdraftLimit() {
        return assignedOverdraftLimit;
    }

    public void setAssignedOverdraftLimit(double assignedOverdraftLimit) {
        this.assignedOverdraftLimit = assignedOverdraftLimit;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public double getNominal() {
        return nominal;
    }

    public void setNominal(double nominal) {
        this.nominal = nominal;
    }

    public String getTae() {
        return tae;
    }

    public void setTae(String tae) {
        this.tae = tae;
    }

    public double getSinceOpen() {
        return sinceOpen;
    }

    public void setSinceOpen(double sinceOpen) {
        this.sinceOpen = sinceOpen;
    }

    public double getSinceYear() {
        return sinceYear;
    }

    public void setSinceYear(double sinceYear) {
        this.sinceYear = sinceYear;
    }

    public double getSinceMonth() {
        return sinceMonth;
    }

    public void setSinceMonth(double sinceMonth) {
        this.sinceMonth = sinceMonth;
    }

    public double getYearRetention() {
        return yearRetention;
    }

    public void setYearRetention(double yearRetention) {
        this.yearRetention = yearRetention;
    }

    public String getRenta4Id() {
        return renta4Id;
    }

    public void setRenta4Id(String renta4Id) {
        this.renta4Id = renta4Id;
    }

    public String getBrokerageRate() {
        return brokerageRate;
    }

    public void setBrokerageRate(String brokerageRate) {
        this.brokerageRate = brokerageRate;
    }

    public Account getAssociatedAccount() {
        return associatedAccount;
    }

    public void setAssociatedAccount(Account associatedAccount) {
        this.associatedAccount = associatedAccount;
    }

    /** Different products supply holder information different ways - list or single attribute. This is an attempt to unify them. */
    public Holder getHolder() {
        if (holder != null) {
            return holder;
        } else if (holders != null && holders.size() > 0) {
            // Check if instead possible to get from the list
            return holders.get(0);
        } else {
            // No option left
            return null;
        }
    }

    public void setHolder(Holder holder) {
        this.holder = holder;
    }

    public Status getStatusIndra() {
        return statusIndra;
    }

    public void setStatusIndra(Status statusIndra) {
        this.statusIndra = statusIndra;
    }

    public Status getStatusOnOff() {
        return statusOnOff;
    }

    public void setStatusOnOff(Status statusOnOff) {
        this.statusOnOff = statusOnOff;
    }

    public Status getStatusContactless() {
        return statusContactless;
    }

    public void setStatusContactless(Status statusContactless) {
        this.statusContactless = statusContactless;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public double getPendingAuthorizationAmount() {
        return pendingAuthorizationAmount;
    }

    public void setPendingAuthorizationAmount(double pendingAuthorizationAmount) {
        this.pendingAuthorizationAmount = pendingAuthorizationAmount;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public double getMonthPurchasesAmount() {
        return monthPurchasesAmount;
    }

    public void setMonthPurchasesAmount(double monthPurchasesAmount) {
        this.monthPurchasesAmount = monthPurchasesAmount;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getMonthlyFee() {
        return monthlyFee;
    }

    public void setMonthlyFee(double monthlyFee) {
        this.monthlyFee = monthlyFee;
    }

    public double getLastMonthDeferredAmount() {
        return lastMonthDeferredAmount;
    }

    public void setLastMonthDeferredAmount(double lastMonthDeferredAmount) {
        this.lastMonthDeferredAmount = lastMonthDeferredAmount;
    }

    public double getSpentAmount() {
        return spentAmount;
    }

    public void setSpentAmount(double spentAmount) {
        this.spentAmount = spentAmount;
    }

    public String getNextPaymentDate() {
        return nextPaymentDate;
    }

    public void setNextPaymentDate(String nextPaymentDate) {
        this.nextPaymentDate = nextPaymentDate;
    }

    public String getAnualInterest() {
        return anualInterest;
    }

    public void setAnualInterest(String anualInterest) {
        this.anualInterest = anualInterest;
    }

    public String getExpedientNumber() {
        return expedientNumber;
    }

    public void setExpedientNumber(String expedientNumber) {
        this.expedientNumber = expedientNumber;
    }

    public boolean isIsRealTimeData() {
        return isRealTimeData;
    }

    public void setIsRealTimeData(boolean isRealTimeData) {
        this.isRealTimeData = isRealTimeData;
    }

    public String getCodConTar() {
        return codConTar;
    }

    public void setCodConTar(String codConTar) {
        this.codConTar = codConTar;
    }

    public double getAvailableCreditAmount() {
        return availableCreditAmount;
    }

    public void setAvailableCreditAmount(double availableCreditAmount) {
        this.availableCreditAmount = availableCreditAmount;
    }
}
