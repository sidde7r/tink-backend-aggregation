package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.bnpparibas;

import static se.tink.libraries.account.enums.AccountFlag.PSD2_PAYMENT_ACCOUNT;

import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;

@UtilityClass
class BnpParibasConstants {

    @UtilityClass
    static class Urls {
        static final String BASE_URL = "https://goapi.bnpparibas.pl";
        static final String VERSION = "v2_1.4";
    }

    // List here:
    // https://docs.google.com/spreadsheets/d/1FM6yKfwT8PVnwokfbKHLgyEmWxcFOR1YHvjce9lIVIk/edit#gid=0
    static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(
                            AccountTypes.SAVINGS,
                            PSD2_PAYMENT_ACCOUNT,
                            "1k",
                            "9a",
                            "9b",
                            "9c",
                            "9e",
                            "9f",
                            "9g",
                            "9h",
                            "9l",
                            "9t")
                    .put(AccountTypes.SAVINGS, "cu") // IKP
                    .put(AccountTypes.CREDIT_CARD, PSD2_PAYMENT_ACCOUNT, "9d", "c6", "um")
                    .put(
                            AccountTypes.CHECKING,
                            PSD2_PAYMENT_ACCOUNT,
                            "1p",
                            "c4",
                            "ca",
                            "cb",
                            "cc",
                            "cd",
                            "ce",
                            "ck")
                    .build();
}
