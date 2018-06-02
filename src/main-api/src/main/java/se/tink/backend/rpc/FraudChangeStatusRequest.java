package se.tink.backend.rpc;

import java.util.List;

import io.protostuff.Tag;
import se.tink.backend.core.FraudStatus;

public class FraudChangeStatusRequest {

    @Tag(1)
    private List<String> fraudDetailIds;
    @Tag(2)
    private FraudStatus status;
    
    public List<String> getFraudDetailIds() {
        return fraudDetailIds;
    }
    public void setFraudDetailIds(List<String> fraudDetailIds) {
        this.fraudDetailIds = fraudDetailIds;
    }
    public FraudStatus getStatus() {
        return status;
    }
    public void setStatus(FraudStatus status) {
        this.status = status;
    }
}
