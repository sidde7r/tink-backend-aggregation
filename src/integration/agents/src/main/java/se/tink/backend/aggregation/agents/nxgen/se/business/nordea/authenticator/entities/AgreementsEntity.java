package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.entities;

import com.google.common.base.Strings;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgreementsEntity {
    private List<AgreementEntity> agreement;

    public String getId() {
        return agreement.stream()
                .filter(a -> !Strings.isNullOrEmpty(a.getId()))
                .map(AgreementEntity::getId)
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
