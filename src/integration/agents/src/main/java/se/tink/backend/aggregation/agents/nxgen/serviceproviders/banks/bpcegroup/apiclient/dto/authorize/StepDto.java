package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize;

import java.util.List;
import java.util.Map;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class StepDto {

    private PhaseDto phase;

    public List<Map<String, List<ValidationUnitResponseItemDto>>> validationUnits;
}
