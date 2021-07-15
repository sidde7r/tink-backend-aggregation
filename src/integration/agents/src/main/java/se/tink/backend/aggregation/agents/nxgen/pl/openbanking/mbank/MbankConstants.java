package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.mbank;

import static se.tink.libraries.account.enums.AccountFlag.PSD2_PAYMENT_ACCOUNT;

import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;

@UtilityClass
class MbankConstants {

    @UtilityClass
    static class Urls {
        static final String BASE_URL = "https://open.api.mbank.pl";
        static final String API_TYPE = "pl-retail";
        static final String VERSION = "v2";
    }

    static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CREDIT_CARD, PSD2_PAYMENT_ACCOUNT, "CREDIT CARD")
                    .put(AccountTypes.CHECKING)
                    .build();
}
