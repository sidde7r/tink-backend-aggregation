package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsPaymentLinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsScaMethodsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsTppMessageEntity;
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
    private List<SibsTppMessageEntity> tppMessages;

    public SibsTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public SibsAmountEntity getTransactionFees() {
        return transactionFees;
    }

    public Boolean getTransactionFeeIndicator() {
        return transactionFeeIndicator;
    }

    public SibsScaMethodsEntity getScaMethods() {
        return scaMethods;
    }

    public SibsScaMethodsEntity getChosenScaMethod() {
        return chosenScaMethod;
    }

    public SibsChallengeDataEntity getChallengeData() {
        return challengeData;
    }

    public SibsPaymentLinkEntity getLinks() {
        return links;
    }

    public String getPsuMessage() {
        return psuMessage;
    }

    public List<SibsTppMessageEntity> getTppMessages() {
        return tppMessages;
    }

    public PaymentResponse toTinkPaymentResponse(PaymentRequest paymentRequest, String state) {

        Payment.Builder buildingPaymentResponse =
                new Payment.Builder()
                        .withCreditor(paymentRequest.getPayment().getCreditor())
                        .withDebtor(paymentRequest.getPayment().getDebtor())
                        .withExactCurrencyAmount(
                                paymentRequest.getPayment().getExactCurrencyAmountFromField())
                        .withExecutionDate(paymentRequest.getPayment().getExecutionDate())
                        .withCurrency(paymentRequest.getPayment().getCurrency())
                        .withUniqueId(getPaymentId())
                        .withStatus(getTransactionStatus().getTinkStatus())
                        .withPaymentScheme(paymentRequest.getPayment().getPaymentScheme())
                        .withType(paymentRequest.getPayment().getType());
        Storage storage = paymentRequest.getStorage();
        storage.put(SibsConstants.Storage.PAYMENT_REDIRECT_URI, getLinks().getRedirect());
        storage.put(
                SibsConstants.Storage.PAYMENT_UPDATE_PSU_URI,
                getLinks().getUpdatePsuIdentification());
        storage.put(SibsConstants.Storage.STATE, state);
        return new PaymentResponse(buildingPaymentResponse.build(), storage);
    }
}
