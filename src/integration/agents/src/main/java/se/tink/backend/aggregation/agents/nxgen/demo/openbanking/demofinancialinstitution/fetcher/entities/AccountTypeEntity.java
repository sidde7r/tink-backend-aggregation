package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demofinancialinstitution.fetcher.entities;

import io.vavr.control.Option;
import io.vavr.control.Try;
import se.tink.backend.agents.rpc.AccountTypes;

public enum AccountTypeEntity {
    CHECKING,
    SAVINGS,
    INVESTMENT,
    MORTGAGE,
    CREDIT_CARD,
    LOAN,
    DUMMY,
    PENSION,
    OTHER,
    EXTERNAL;

    public Option<AccountTypes> maybeToTinkAccountTypes() {
        return Try.of(() -> AccountTypes.valueOf(this.toString())).toOption();
    }
}
