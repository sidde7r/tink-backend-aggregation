package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken;

import java.util.HashMap;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public abstract class HandelsbankenConstants {

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
        TypeMapper.<AccountTypes>builder()
            .put(AccountTypes.CHECKING, "Betaalrekening")
            .build();

    public static class Account {

        public static HashMap<AccountTypes, String> TYPES = new HashMap() {{
            put(AccountTypes.CHECKING, "Betaalrekening");
        }};
    }

    public static class Urls {

    }

    public static class StorageKeys {

    }

    public static class QueryKeys {

    }

    public static class QueryValues {

    }

    public static class HeaderKeys {

    }

    public static class FormKeys {

    }

    public static class FormValues {

    }

    public static class LogTags {

    }
}
