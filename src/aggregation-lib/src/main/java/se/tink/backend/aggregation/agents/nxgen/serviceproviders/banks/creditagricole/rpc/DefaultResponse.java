package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DefaultResponse {
    protected List<ErrorEntity> errors;
    protected List<WarningEntity> warnings;
    protected List<InfoEntity> infos;

    public List<ErrorEntity> getErrors() {
        return errors;
    }

    public List<WarningEntity> getWarnings() {
        return warnings;
    }

    public List<InfoEntity> getInfos() {
        return infos;
    }
}
