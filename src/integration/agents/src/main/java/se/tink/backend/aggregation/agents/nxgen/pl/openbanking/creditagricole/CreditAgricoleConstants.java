package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.creditagricole;

import static se.tink.libraries.account.enums.AccountFlag.PSD2_PAYMENT_ACCOUNT;

import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;

@UtilityClass
class CreditAgricoleConstants {

    @UtilityClass
    static class Urls {
        static final String BASE_URL =
                "https://xs2a.credit-agricole.pl/CaPolishAPI/prod/individual";
        static final String VERSION = "v3_0.1";
    }

    // not yet implemented - just copy paste
    static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.SAVINGS, PSD2_PAYMENT_ACCOUNT, "loka")
                    .put(AccountTypes.CREDIT_CARD, PSD2_PAYMENT_ACCOUNT, "karta")
                    .put(AccountTypes.CHECKING, PSD2_PAYMENT_ACCOUNT)
                    .build();
}
