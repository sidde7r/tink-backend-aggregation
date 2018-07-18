package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ReferenceEntity {
    private String type; // Seen: OCR and MESSAGE
    private String value;

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public static ReferenceEntity create(String message, String type) {
        ReferenceEntity referenceEntity = new ReferenceEntity();

        referenceEntity.type = type;
        referenceEntity.value = message;

        return referenceEntity;
    }

    public static ReferenceEntity create(String message, SwedbankBaseConstants.ReferenceType type) {
        return create(message, type.name());
    }
}
