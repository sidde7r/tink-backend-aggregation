package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.rpc;

import lombok.Builder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsCommonConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Builder
public class ConsentRequest {

    private AccessEntity access;
    private boolean recurringIndicator;
    private String validUntil;

    @Builder.Default private int frequencyPerDay = QueryValues.FREQUENCY_PER_DAY;
}
