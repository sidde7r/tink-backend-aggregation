package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.ScaMethod;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitScaEntity {
    private List<String> availableApprovalMethods;
    private String salutationName;
    private String salutationTitle;

    public boolean isPhotoTanScanningAvailable() {
        if (availableApprovalMethods == null) {
            return false;
        }

        return availableApprovalMethods.contains(ScaMethod.PHOTO_TAN);
    }
}
