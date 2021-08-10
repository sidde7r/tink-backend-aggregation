package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.alior;

import static se.tink.libraries.account.enums.AccountFlag.PSD2_PAYMENT_ACCOUNT;

import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;

@UtilityClass
class AliorConstants {

    @UtilityClass
    static class Urls {
        static final String BASE_URL = "https://gateway.api.aliorbank.pl/openapipl/api";
        static final String VERSION = "v3_0.1";
    }

    // more info here:
    // https://docs.google.com/spreadsheets/d/19UeuPQ7Qnjk5JoZ4bVMy5budLxThxOgk-owMnMyBcv0/edit?disco=AAAANBX9U8o
    static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, PSD2_PAYMENT_ACCOUNT, "4000", "4200", "45", "4600")
                    .put(AccountTypes.SAVINGS, PSD2_PAYMENT_ACCOUNT, "4100", "4300", "4610")
                    .put(AccountTypes.CREDIT_CARD)
                    .build();
}
