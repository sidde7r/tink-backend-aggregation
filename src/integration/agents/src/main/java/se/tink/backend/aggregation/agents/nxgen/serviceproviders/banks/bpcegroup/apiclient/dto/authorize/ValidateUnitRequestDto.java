package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.apiclient.dto.authorize;

import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class ValidateUnitRequestDto {

    public ValidateUnitRequestDto(
            String validationUnitId, ValidationUnitRequestItemBaseDto validationUnitRequestItem) {
        this.validate =
                ImmutableMap.of(
                        validationUnitId, Collections.singletonList(validationUnitRequestItem));
    }

    private Map<String, List<ValidationUnitRequestItemBaseDto>> validate;
}
