package se.tink.libraries.transfer.rpc;

public enum ExecutionRule {
    PRECEDING("preceding"),
    FOLLOWING("following");

    private ExecutionRule(String value) {
        this.value = value;
    }

    private final String value;

    @Override
    public String toString() {
        return this.value;
    }
}
