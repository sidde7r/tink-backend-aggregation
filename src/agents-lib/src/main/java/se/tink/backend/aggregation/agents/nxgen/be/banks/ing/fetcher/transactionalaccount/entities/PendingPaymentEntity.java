package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.DateUtils;

@JsonObject
public class PendingPaymentEntity {
    private String pendingPaymentType;
    private String trxId;
    private String executionDate;
    private String amountType;
    private String amount;
    private String currency;
    private String beneficiaryAccount;
    private String beneficiaryName;
    private String beneficiaryAddress;
    private String beneficiaryCity;
    private String beneficiaryCountry;
    private String communicationLine1;
    private String communicationLine2;
    private String communicationLine3;
    private String communicationLine4;
    private String communicationType;
    private String end2EndReference;
    private String transactionStatus;
    private String transferType;
    private String isZoomitPayment;
    private String frequencyStandingOrder;
    private String monthStandingOrder;
    private String dayStandingOrder;
    private String maturityType;

    public String getPendingPaymentType() {
        return pendingPaymentType;
    }

    public String getTrxId() {
        return trxId;
    }

    public String getExecutionDate() {
        return executionDate;
    }

    public String getAmountType() {
        return amountType;
    }

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public String getBeneficiaryAccount() {
        return beneficiaryAccount;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public String getBeneficiaryAddress() {
        return beneficiaryAddress;
    }

    public String getBeneficiaryCity() {
        return beneficiaryCity;
    }

    public String getBeneficiaryCountry() {
        return beneficiaryCountry;
    }

    public String getCommunicationLine1() {
        return communicationLine1;
    }

    public String getCommunicationLine2() {
        return communicationLine2;
    }

    public String getCommunicationLine3() {
        return communicationLine3;
    }

    public String getCommunicationLine4() {
        return communicationLine4;
    }

    public String getCommunicationType() {
        return communicationType;
    }

    public String getEnd2EndReference() {
        return end2EndReference;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public String getTransferType() {
        return transferType;
    }

    public String getIsZoomitPayment() {
        return isZoomitPayment;
    }

    public String getFrequencyStandingOrder() {
        return frequencyStandingOrder;
    }

    public String getMonthStandingOrder() {
        return monthStandingOrder;
    }

    public String getDayStandingOrder() {
        return dayStandingOrder;
    }

    public String getMaturityType() {
        return maturityType;
    }

    public UpcomingTransaction toTinkUpcomingTransaction() {
        return UpcomingTransaction.builder()
                .setAmount(Amount.inEUR(IngHelper.parseAmountStringToDouble(amount)))
                .setDate(DateUtils.parseDate(executionDate))
                .setDescription(beneficiaryName)
                .setRawDetails(getRawDetails())
                .build();
    }

    @JsonIgnore
    private RawDetails getRawDetails() {
        return new RawDetails(this);
    }

    @JsonObject
    public class RawDetails {
        private String beneficiaryAccount;
        private String beneficiaryName;
        private String beneficiaryAddress;
        private String beneficiaryCity;
        private String beneficiaryCountry;
        private String communicationLine1;
        private String communicationLine2;
        private String communicationLine3;
        private String communicationLine4;

        public RawDetails(PendingPaymentEntity pendingPaymentEntity) {
            this.beneficiaryAccount = pendingPaymentEntity.beneficiaryAccount;
            this.beneficiaryName = pendingPaymentEntity.beneficiaryName;
            this.beneficiaryAddress = pendingPaymentEntity.beneficiaryAddress;
            this.beneficiaryCity = pendingPaymentEntity.beneficiaryCity;
            this.beneficiaryCountry = pendingPaymentEntity.beneficiaryCountry;
            this.communicationLine1 = pendingPaymentEntity.communicationLine1;
            this.communicationLine2 = pendingPaymentEntity.communicationLine2;
            this.communicationLine3 = pendingPaymentEntity.communicationLine3;
            this.communicationLine4 = pendingPaymentEntity.communicationLine4;
        }
    }
}
