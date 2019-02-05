package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.fi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.List;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaHashMapSerializer;

public class ConfirmPaymentsIn {

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String confirmationCode;

    @JsonSerialize(using = NordeaHashMapSerializer.class)
    private String challenge;

    @JsonProperty("payments")
    private List<ConfirmPayment> payment;

    public String getConfirmationCode() {
        return confirmationCode;
    }

    public void setConfirmationCode(String confirmationCode) {
        this.confirmationCode = confirmationCode;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public List<ConfirmPayment> getPayment() {
        return payment;
    }

    public void setPayment(List<ConfirmPayment> payment) {
        this.payment = payment;
    }
}
