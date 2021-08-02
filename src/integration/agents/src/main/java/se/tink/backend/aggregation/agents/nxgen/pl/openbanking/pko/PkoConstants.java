package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.pko;

import static se.tink.libraries.account.enums.AccountFlag.PSD2_PAYMENT_ACCOUNT;

import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;

@UtilityClass
class PkoConstants {

    @UtilityClass
    static class Urls {
        static final String BASE_URL = "https://api.pkobp.pl";
        static final String VERSION = "v2_1_1.1";
    }

    // not yet implemented - needs to be done when on prod (no info from bank)
    static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, PSD2_PAYMENT_ACCOUNT, "loka", "oszcz")
                    .put(AccountTypes.CREDIT_CARD, PSD2_PAYMENT_ACCOUNT, "karta")
                    .put(AccountTypes.CHECKING)
                    .build();
}
