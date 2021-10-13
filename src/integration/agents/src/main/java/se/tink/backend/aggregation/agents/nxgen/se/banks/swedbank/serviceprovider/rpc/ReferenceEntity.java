package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.google.common.base.Strings;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@JsonObject
@Getter
public class ReferenceEntity {
    private String type; // Seen: OCR and MESSAGE
    private String value;

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

    public RemittanceInformation toRemittanceInformation() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(value);
        remittanceInformation.setType(getRemittanceInformationType());
        return remittanceInformation;
    }

    private RemittanceInformationType getRemittanceInformationType() {
        switch (this.type) {
            case "OCR":
                return RemittanceInformationType.OCR;
            case "MESSAGE":
                return RemittanceInformationType.UNSTRUCTURED;
            default:
                throw new IllegalStateException("Unknown remittance information type");
        }
    }
}
