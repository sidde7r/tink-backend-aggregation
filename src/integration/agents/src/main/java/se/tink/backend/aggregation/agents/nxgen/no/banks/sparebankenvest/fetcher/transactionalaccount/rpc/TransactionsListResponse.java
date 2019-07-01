package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.Payload;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionsListResponse {
    private boolean hasErrors;
    private Payload payload;
    private List<Object> errors;

    public void setHasErrors(boolean hasErrors) {
        this.hasErrors = hasErrors;
    }

    public boolean isHasErrors() {
        return hasErrors;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setErrors(List<Object> errors) {
        this.errors = errors;
    }

    public List<Object> getErrors() {
        return errors;
    }
}
