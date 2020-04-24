package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.aktia.apiclient.dto.response;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class FabInfoDto {

    private List<FabOperationInfoDto> fabOperations;
}
