package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.entities.preparetransfer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.PropertiesEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.ScreenUpdateResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.Widget;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepareRoot extends BelfiusResponse {

    public List<BeneficiariesContacts> getBeneficiaries() {
        return ScreenUpdateResponse.streamWidgetsWithId(this, BelfiusConstants.Response.BENEFICIARY_WIDGET)
                .flatMap(widget -> getBeneficiaries(widget).stream())
                .collect(Collectors.toList());
    }

    private List<BeneficiariesContacts> getBeneficiaries(Widget widget) {
        PropertiesEntity propertiesEntity = widget.getProperties(PropertiesEntity.class);
        return propertiesEntity != null ? propertiesEntity.getBeneficiaries() : Collections.emptyList();
    }
}
