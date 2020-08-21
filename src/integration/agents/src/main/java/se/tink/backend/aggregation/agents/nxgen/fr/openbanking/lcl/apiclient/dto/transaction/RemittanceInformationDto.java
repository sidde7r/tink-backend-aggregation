package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.transaction;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class RemittanceInformationDto {

    private List<String> unstructured;
}
