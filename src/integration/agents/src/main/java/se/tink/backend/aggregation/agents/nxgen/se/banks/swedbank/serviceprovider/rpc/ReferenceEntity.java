package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
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
        referenceEntity.value = Strings.nullToEmpty(message);

        return referenceEntity;
    }

    public static ReferenceEntity create(String message, SwedbankBaseConstants.ReferenceType type) {
        return create(message, type.name());
    }
}
