package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.ScaMethod;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class InitScaEntity {
    private List<String> availableApprovalMethods;

    public boolean isPushPhotoTanAvailable() {
        if (availableApprovalMethods == null) {
            return false;
        }

        return availableApprovalMethods.contains(ScaMethod.PUSH_PHOTO_TAN);
    }
}
