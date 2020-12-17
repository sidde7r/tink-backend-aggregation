package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    private int errorCode;
    private String errorMessage;
    private String debugMessage;
    private String status;
}
