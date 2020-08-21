package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.rpc;

public class ErrorResponse {
    private String exitCode;
    private String message;

    public String getExitCode() {
        return exitCode;
    }

    public String getMessage() {
        return message;
    }
}
