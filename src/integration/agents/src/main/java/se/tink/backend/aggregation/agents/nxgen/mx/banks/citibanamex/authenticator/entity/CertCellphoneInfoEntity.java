package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.authenticator.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CertCellphoneInfoEntity {
    private String campaignStatus;
    private List<CellPhoneInfoEntity> cellPhoneInfo;
    private String cellPhoneInfoStatus;
}
