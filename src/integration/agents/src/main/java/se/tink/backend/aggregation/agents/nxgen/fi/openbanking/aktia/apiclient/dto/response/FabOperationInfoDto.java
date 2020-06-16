package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.data.FabOperationCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
public class FabOperationInfoDto {

    private FabOperationCode fabOperationCode;

    private String fabOperation;
}
