package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ReferenceType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@JsonObject
@Getter
@JsonInclude(Include.NON_NULL)
public class RemittanceInformationStructuredEntity {
    private String reference;
    private String referenceType;

    public RemittanceInformationStructuredEntity(RemittanceInformation remittanceInformation) {
        this.reference = remittanceInformation.getValue();
        this.referenceType =
                RemittanceInformationType.OCR.equals(remittanceInformation.getType())
                        ? ReferenceType.OCR
                        : ReferenceType.MSG;
    }
}
