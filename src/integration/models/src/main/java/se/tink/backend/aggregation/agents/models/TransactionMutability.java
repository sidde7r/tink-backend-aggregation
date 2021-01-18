package se.tink.backend.aggregation.agents.models;

public enum TransactionMutability {
    IMMUTABLE,
    MUTABLE;

    public static TransactionMutability valueOf(Boolean mutable) {
        if (mutable == null) {
            return null;
        }
        if (mutable) {
            return MUTABLE;
        } else {
            return IMMUTABLE;
        }
    }
}
