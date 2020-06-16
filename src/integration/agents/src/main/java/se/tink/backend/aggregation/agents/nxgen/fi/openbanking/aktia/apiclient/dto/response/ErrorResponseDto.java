package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.data.ErrorCode;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponseDto {

    private ErrorCode errorCode;

    private String message;
}
