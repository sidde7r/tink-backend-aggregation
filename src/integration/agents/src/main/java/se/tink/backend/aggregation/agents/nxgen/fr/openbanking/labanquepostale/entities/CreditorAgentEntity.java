package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@JsonObject
@EqualsAndHashCode
public class CreditorAgentEntity {
    private String bicFi;
    private String name;
}
