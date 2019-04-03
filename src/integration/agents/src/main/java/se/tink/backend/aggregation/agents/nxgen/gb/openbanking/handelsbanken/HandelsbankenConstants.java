package se.tink.backend.aggregation.agents.nxgen.gb.openbanking.handelsbanken;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public abstract class HandelsbankenConstants {

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
        TypeMapper.<AccountTypes>builder()
            .put(AccountTypes.CHECKING, "Current Account")
            .put(AccountTypes.SAVINGS, "Instant Access Deposit Account")
            .build();
}
