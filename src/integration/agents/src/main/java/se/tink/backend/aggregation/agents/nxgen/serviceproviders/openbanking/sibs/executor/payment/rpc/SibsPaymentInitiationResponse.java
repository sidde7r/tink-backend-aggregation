package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsPaymentLinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsScaMethodsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsTppMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsTransactionStatus;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.payment.rpc.Payment;

@JsonObject
public class SibsPaymentInitiationResponse {

    private SibsTransactionStatus transactionStatus;
    private String paymentId;
    private SibsAmountEntity transactionFees;
    private Boolean transactionFeeIndicator;
    private SibsScaMethodsEntity scaMethods;
    private SibsScaMethodsEntity chosenScaMethod;
    private SibsChallengeDataEntity challengeData;

    @JsonProperty("_links")
    private SibsPaymentLinkEntity links;

    private String psuMessage;
    private List<SibsTppMessage> tppMessages;

    public SibsTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(SibsTransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public SibsAmountEntity getTransactionFees() {
        return transactionFees;
    }

    public void setTransactionFees(SibsAmountEntity transactionFees) {
        this.transactionFees = transactionFees;
    }

    public Boolean getTransactionFeeIndicator() {
        return transactionFeeIndicator;
    }

    public void setTransactionFeeIndicator(Boolean transactionFeeIndicator) {
        this.transactionFeeIndicator = transactionFeeIndicator;
    }

    public SibsScaMethodsEntity getScaMethods() {
        return scaMethods;
    }

    public void setScaMethods(SibsScaMethodsEntity scaMethods) {
        this.scaMethods = scaMethods;
    }

    public SibsScaMethodsEntity getChosenScaMethod() {
        return chosenScaMethod;
    }

    public void setChosenScaMethod(SibsScaMethodsEntity chosenScaMethod) {
        this.chosenScaMethod = chosenScaMethod;
    }

    public SibsChallengeDataEntity getChallengeData() {
        return challengeData;
    }

    public void setChallengeData(SibsChallengeDataEntity challengeData) {
        this.challengeData = challengeData;
    }

    public SibsPaymentLinkEntity getLinks() {
        return links;
    }

    public void setLinks(SibsPaymentLinkEntity links) {
        this.links = links;
    }

    public String getPsuMessage() {
        return psuMessage;
    }

    public void setPsuMessage(String psuMessage) {
        this.psuMessage = psuMessage;
    }

    public List<SibsTppMessage> getTppMessages() {
        return tppMessages;
    }

    public void setTppMessages(List<SibsTppMessage> tppMessages) {
        this.tppMessages = tppMessages;
    }

    public PaymentResponse toTinkPaymentResponse(PaymentRequest paymentRequest, String state) {

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(paymentRequest.getPayment().getCreditor())
                        .withDebtor(paymentRequest.getPayment().getDebtor())
                        .withAmount(paymentRequest.getPayment().getAmount())
                        .withExecutionDate(paymentRequest.getPayment().getExecutionDate())
                        .withCurrency(paymentRequest.getPayment().getCurrency())
                        .withUniqueId(getPaymentId())
                        .withStatus(getTransactionStatus().getTinkStatus())
                        .withType(paymentRequest.getPayment().getType());
        Storage storage = paymentRequest.getStorage();
        storage.put(SibsConstants.Storage.PAYMENT_REDIRECT_URI, getLinks().getRedirect());
        storage.put(
                SibsConstants.Storage.PAYMENT_UPDATE_PSU_URI,
                getLinks().getUpdatePsuIdentification());
        storage.put(SibsConstants.Storage.STATE, state);
        PaymentResponse response = new PaymentResponse(buildingPaymentResponse.build(), storage);
        return response;
    }
}
