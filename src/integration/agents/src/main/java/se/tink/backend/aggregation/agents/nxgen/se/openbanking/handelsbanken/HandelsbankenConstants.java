package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public abstract class HandelsbankenConstants {

    public static final TypeMapper<AccountTypes> ACCOUNT_TYPE_MAPPER =
        TypeMapper.<AccountTypes>builder()
            .put(AccountTypes.CHECKING, "Allkonto Ung", "Allkonto", "Checkkonto", "Privatkonto")
            .build();


    public static class Account {

        public static HashMap<AccountTypes, List<String>> TYPES = new HashMap<AccountTypes, List<String>>() {{
            put(AccountTypes.CHECKING,
                Arrays.asList("Allkonto Ung", "Allkonto", "Checkkonto", "Privatkonto"));
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

    public static class AccountBalance {

    }

    public static class Transactions {

    }

    public static class ExceptionMessages {

    }

    public static class Configuration {

    }
}
