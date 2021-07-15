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

    // not yet implemented - just copy paste
    static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(AccountTypes.CHECKING, PSD2_PAYMENT_ACCOUNT, "Current account")
                    .put(AccountTypes.CREDIT_CARD, PSD2_PAYMENT_ACCOUNT, "Credit card account")
                    .put(AccountTypes.CHECKING)
                    .build();
}
