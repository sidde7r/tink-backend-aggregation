package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.getsigningprotocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.Text;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RPScenarioEntity {
    @JsonProperty("lb_scen_data_formatted")
    private Text lbScenDataFormatted;

    @JsonProperty("lb_scen_type")
    private Text lbScenType;

    @JsonProperty("lb_scen_label")
    private Text lbScenLabel;

    @JsonProperty("lb_scen_data")
    private Text lbScenData;

    public String getSignType() {
        return (lbScenData == null || lbScenData.equals("")) ? "" : lbScenData.getText();
    }
}
