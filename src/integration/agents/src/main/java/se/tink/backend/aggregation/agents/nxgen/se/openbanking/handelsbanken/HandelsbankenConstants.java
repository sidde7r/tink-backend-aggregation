package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public abstract class HandelsbankenConstants {

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
            TypeMapper.<AccountTypes>builder()
                    .put(
                            AccountTypes.CHECKING,
                            "Allkonto Ung",
                            "Allkonto",
                            "Checkkonto",
                            "Privatkonto")
                    .build();
}
