package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class ParamsEntity {

    private String appVersion = "2.32";
    private String clientLanguage = "nl";
    private String transactionType;
    private String sessionId;
    private String aid;
    private String policy;
    private String axaCardNumber;
    private String panSequenceNumber;
    private String transmitTicketId;

    public ParamsEntity withTransactionType(String transactionType) {
        this.transactionType = transactionType;
        return this;
    }

    public ParamsEntity withSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public ParamsEntity withAid(String aid) {
        this.aid = aid;
        return this;
    }

    public ParamsEntity withPolicy(String policy) {
        this.policy = policy;
        return this;
    }

    public ParamsEntity withAxaCardNumber(String axaCardNumber) {
        this.axaCardNumber = axaCardNumber;
        return this;
    }

    public ParamsEntity withPanSequenceNumber(String panSequenceNumber) {
        this.panSequenceNumber = panSequenceNumber;
        return this;
    }

    public ParamsEntity withTransmitTicketId(String transmitTicketId) {
        this.transmitTicketId = transmitTicketId;
        return this;
    }
}
