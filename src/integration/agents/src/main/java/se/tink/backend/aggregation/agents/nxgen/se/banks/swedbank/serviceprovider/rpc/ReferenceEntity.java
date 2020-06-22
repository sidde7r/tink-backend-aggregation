package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

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

    public static ReferenceEntity create(RemittanceInformation remittanceInformation) {
        ReferenceEntity referenceEntity = new ReferenceEntity();
        referenceEntity.value = Strings.nullToEmpty(remittanceInformation.getValue());

        switch (remittanceInformation.getType()) {
            case OCR:
                referenceEntity.type = "OCR";
                return referenceEntity;
            case UNSTRUCTURED:
                referenceEntity.type = "MESSAGE";
                return referenceEntity;
            default:
                throw new IllegalStateException("Unknown remittance information type");
        }
    }
}
