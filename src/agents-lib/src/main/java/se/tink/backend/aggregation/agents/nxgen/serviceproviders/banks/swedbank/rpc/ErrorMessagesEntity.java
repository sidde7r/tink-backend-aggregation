package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorMessagesEntity {
    private List<GeneralEntity> general;

    public List<GeneralEntity> getGeneral() {
        return general;
    }
}
