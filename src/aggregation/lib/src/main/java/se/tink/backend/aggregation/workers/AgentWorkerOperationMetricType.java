package se.tink.backend.aggregation.workers;

public enum AgentWorkerOperationMetricType {
    EXECUTE_COMMAND("execute"),
    POST_PROCESS_COMMAND("postProcess");

    private String stringRepresentation;

    private AgentWorkerOperationMetricType(String stringRepresentation) {
        this.stringRepresentation = stringRepresentation;
    }

    public String getMetricName() {
        return stringRepresentation;
    }
}
