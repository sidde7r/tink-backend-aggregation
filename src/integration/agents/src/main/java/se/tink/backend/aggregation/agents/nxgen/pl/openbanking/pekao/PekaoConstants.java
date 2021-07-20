package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.pekao;

import static se.tink.libraries.account.enums.AccountFlag.PSD2_PAYMENT_ACCOUNT;

import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;

@UtilityClass
class PekaoConstants {

    @UtilityClass
    static class Urls {
        static final String BASE_URL = "https://api.pekao.com.pl";
        static final String VERSION = "v2_1_1.1";
    }

    static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, PSD2_PAYMENT_ACCOUNT, "ROR")
                    .put(AccountTypes.SAVINGS, PSD2_PAYMENT_ACCOUNT, "ROS")
                    .put(AccountTypes.CREDIT_CARD, PSD2_PAYMENT_ACCOUNT, "RKK")
                    .build();
}
