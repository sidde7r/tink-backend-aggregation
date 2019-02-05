package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities;

import se.tink.backend.agents.rpc.AccountTypes;

public enum AccountTypeEnum {
    // TODO: Find out type number - is it dynamic?
    DEPOT("01", AccountTypes.CHECKING),
    SAVING("02", AccountTypes.SAVINGS),
    LOANS("12", AccountTypes.LOAN),
    CREDIT_CARD("22", AccountTypes.CREDIT_CARD),
    UNKNOWN("", null);

    private final String type;
    private final AccountTypes tinkType;

    AccountTypeEnum(String type, AccountTypes tinkType) {
        this.type = type;
        this.tinkType = tinkType;
    }

    public String getType() {
        return type;
    }

    public AccountTypes getTinkType() {
        return tinkType;
    }
}
