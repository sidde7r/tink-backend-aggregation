package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.santander;

import static se.tink.libraries.account.enums.AccountFlag.PSD2_PAYMENT_ACCOUNT;

import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;

@UtilityClass
class SantanderConstants {

    @UtilityClass
    static class Urls {
        static final String BASE_URL = "https://api.santanderbankpolska.pl/api";
        static final String AS_VERSION = "v2_1_1.4";
        static final String AIS_VERSION = "v2_1_1.1";
    }

    // more info:
    // https://docs.google.com/spreadsheets/d/1_Wy1wPQ0-QXbIbRd_se6hW-fk2q4PJRT/edit#gid=658244358
    static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, PSD2_PAYMENT_ACCOUNT, "current")
                    .put(AccountTypes.SAVINGS, PSD2_PAYMENT_ACCOUNT, "saving")
                    .put(AccountTypes.CREDIT_CARD, PSD2_PAYMENT_ACCOUNT, "creditCard")
                    .put(AccountTypes.CHECKING, PSD2_PAYMENT_ACCOUNT)
                    .build();
}
