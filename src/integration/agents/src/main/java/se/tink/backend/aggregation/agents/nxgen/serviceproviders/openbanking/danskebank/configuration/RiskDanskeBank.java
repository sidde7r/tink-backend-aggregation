package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.danskebank.configuration;

import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.Risk;
import se.tink.backend.aggregation.annotations.JsonObject;

@Setter
@Getter
@JsonObject
public class RiskDanskeBank extends Risk {

    private String paymentContextCode;
}
