package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class ArkeaRemittanceInformationEntity {

    @JsonProperty("unstructured")
    private List<String> remittanceInformationUnstructuredList;
}
