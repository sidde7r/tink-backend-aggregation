package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc;

import lombok.Data;

@Data
public class ErrorResponse {
    private String exitCode;
    private String message;
}
