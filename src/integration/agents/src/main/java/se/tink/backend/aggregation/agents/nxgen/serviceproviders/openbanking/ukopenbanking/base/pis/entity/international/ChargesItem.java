package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.pis.entity.international;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChargesItem {
    private List<String> type;
    private AmountEntity amount;
    private String chargeBearer;
}
