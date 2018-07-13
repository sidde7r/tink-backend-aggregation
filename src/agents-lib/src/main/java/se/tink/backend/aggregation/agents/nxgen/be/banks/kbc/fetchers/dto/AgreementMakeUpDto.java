package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AgreementMakeUpDto {
    private TypeValuePair name;
    private TypeValuePair partitionNumber;
    private TypeValuePair pictureNumber;
    private TypeValuePair picture;

    public TypeValuePair getName() {
        return name;
    }

    public TypeValuePair getPartitionNumber() {
        return partitionNumber;
    }

    public TypeValuePair getPictureNumber() {
        return pictureNumber;
    }

    public TypeValuePair getPicture() {
        return picture;
    }
}
