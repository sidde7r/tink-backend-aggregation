package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.millenium;

import static se.tink.libraries.account.enums.AccountFlag.PSD2_PAYMENT_ACCOUNT;

import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;

@UtilityClass
class MilleniumConstants {

    @UtilityClass
    static class Urls {
        static final String BASE_URL = "https://tpp.api.bankmillennium.pl";
        static final String VERSION = "v2_1_1.1";
    }

    // more info here:
    // https://docs.google.com/spreadsheets/d/1hU5E7U09du1a7K1rK2YNexD-ve1uCLP4tA9wc654rX0/edit#gid=1410165880
    static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, PSD2_PAYMENT_ACCOUNT, "Current account")
                    .put(AccountTypes.CREDIT_CARD, PSD2_PAYMENT_ACCOUNT, "Credit card account")
                    .put(AccountTypes.CHECKING)
                    .build();
}
