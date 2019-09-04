package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ForfallResponse {
    private boolean isSuccess;
    private List<Object> forfall;

    public void setIsSuccess(boolean isSuccess) {
        this.isSuccess = isSuccess;
    }

    public boolean isIsSuccess() {
        return isSuccess;
    }

    public void setForfall(List<Object> forfall) {
        this.forfall = forfall;
    }

    public List<Object> getForfall() {
        return forfall;
    }
}
