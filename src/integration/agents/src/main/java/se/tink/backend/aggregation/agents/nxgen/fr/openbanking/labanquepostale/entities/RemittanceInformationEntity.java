package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RemittanceInformationEntity {

    private List<String> structured;
    private List<String> unstructured;
}
