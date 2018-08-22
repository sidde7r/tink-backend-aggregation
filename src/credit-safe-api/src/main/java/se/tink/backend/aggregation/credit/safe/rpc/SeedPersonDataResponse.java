package se.tink.backend.aggregation.credit.safe.rpc;

import java.util.List;
import se.tink.backend.core.FraudDetailsContent;

public class SeedPersonDataResponse {

    private List<FraudDetailsContent> fraudDetailsContent;

    public List<FraudDetailsContent> getFraudDetailsContent() {
        return fraudDetailsContent;
    }

    public void setFraudDetailsContent(List<FraudDetailsContent> fraudDetailsContent) {
        this.fraudDetailsContent = fraudDetailsContent;
    }
}
