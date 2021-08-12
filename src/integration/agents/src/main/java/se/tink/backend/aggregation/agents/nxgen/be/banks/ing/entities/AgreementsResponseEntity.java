package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.entities;

import java.util.List;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@JsonObject
public class AgreementsResponseEntity {

    private List<AgreementEntity> agreements;

    public String toString() {
        return "AgreementsResponseEntity(agreements="
                + this.getAgreements().stream()
                        .map(AgreementEntity::toString)
                        .reduce("", String::concat)
                + ")";
    }
}
