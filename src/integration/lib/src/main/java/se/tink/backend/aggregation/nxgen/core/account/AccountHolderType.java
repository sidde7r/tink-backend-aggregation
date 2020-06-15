package se.tink.backend.aggregation.nxgen.core.account;

public enum AccountHolderType {
    PERSONAL(se.tink.backend.agents.rpc.AccountHolderType.PERSONAL),
    BUSINESS(se.tink.backend.agents.rpc.AccountHolderType.BUSINESS);

    private final se.tink.backend.agents.rpc.AccountHolderType systemType;

    AccountHolderType(se.tink.backend.agents.rpc.AccountHolderType systemType) {
        this.systemType = systemType;
    }

    public se.tink.backend.agents.rpc.AccountHolderType toSystemType() {
        return systemType;
    }
}
