package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.preparetransfer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.PropertiesEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ScreenUpdateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.Widget;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepareRoot extends BelfiusResponse{
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    public Boolean getPendingResponseSets() {
        return pendingResponseSets;
    }

    public void setPendingResponseSets(Boolean pendingResponseSets) {
        this.pendingResponseSets = pendingResponseSets;
    }

    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    public ArrayList<BeneficiariesContacts> getBeneficiaries(){
        Widget widget = ScreenUpdateResponse.widgetContains(this,
                BelfiusConstants.Response.BENEFICIARY_WIDGET);
        ArrayList<BeneficiariesContacts> beneficiaries = widget.getProperties(PropertiesEntity.class).getBeneficiaries();

        return beneficiaries;
    }
}




