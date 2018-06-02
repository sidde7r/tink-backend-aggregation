package se.tink.backend.aggregation.rpc;

import se.tink.backend.core.FraudDetailsContent;
import java.util.List;

public class SeedPersonDataResponse {

    private List<FraudDetailsContent> fraudDetailsContent;

    public List<FraudDetailsContent> getFraudDetailsContent() {
        return fraudDetailsContent;
    }

    public void setFraudDetailsContent(List<FraudDetailsContent> fraudDetailsContent) {
        this.fraudDetailsContent = fraudDetailsContent;
    }
}
