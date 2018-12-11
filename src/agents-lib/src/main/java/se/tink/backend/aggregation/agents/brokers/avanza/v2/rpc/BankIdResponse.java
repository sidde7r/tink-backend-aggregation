package se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.LoginEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankIdResponse {
    private String state;
    private String name;
    private String transactionId;
    private List<LoginEntity> logins;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<LoginEntity> getLogins() {
        return logins;
    }

    public void setLogins(List<LoginEntity> logins) {
        this.logins = logins;
    }
}
