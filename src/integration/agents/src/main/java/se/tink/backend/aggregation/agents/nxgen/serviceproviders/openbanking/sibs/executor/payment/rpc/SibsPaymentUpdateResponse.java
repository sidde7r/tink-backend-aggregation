package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsChallengeDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsPaymentLinkEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsScaMethodsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsTppMessageEntity;

public class SibsPaymentUpdateResponse {

    private SibsScaMethodsEntity chosenScaMethod;
    private SibsChallengeDataEntity challengeData;
    private SibsScaMethodsEntity scaMethods;

    @JsonProperty("_links")
    private SibsPaymentLinkEntity links;

    private String transactionStatus;
    private String psuMessage;
    private List<SibsTppMessageEntity> tppMessages;

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

    public SibsScaMethodsEntity getScaMethods() {
        return scaMethods;
    }

    public void setScaMethods(SibsScaMethodsEntity scaMethods) {
        this.scaMethods = scaMethods;
    }

    public SibsPaymentLinkEntity getLinks() {
        return links;
    }

    public void setLinks(SibsPaymentLinkEntity links) {
        this.links = links;
    }

    public String getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public String getPsuMessage() {
        return psuMessage;
    }

    public void setPsuMessage(String psuMessage) {
        this.psuMessage = psuMessage;
    }

    public List<SibsTppMessageEntity> getTppMessages() {
        return tppMessages;
    }

    public void setTppMessages(List<SibsTppMessageEntity> tppMessages) {
        this.tppMessages = tppMessages;
    }
}
