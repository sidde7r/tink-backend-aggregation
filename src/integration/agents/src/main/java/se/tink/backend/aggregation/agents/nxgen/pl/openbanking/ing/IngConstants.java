package se.tink.backend.aggregation.agents.nxgen.pl.openbanking.ing;

import static se.tink.libraries.account.enums.AccountFlag.PSD2_PAYMENT_ACCOUNT;

import lombok.experimental.UtilityClass;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.nxgen.core.account.AccountTypeMapper;

@UtilityClass
class IngConstants {

    @UtilityClass
    static class Urls {
        static final String BASE_URL = "https://api.ing.pl/gateway/api";
        static final String VERSION = "v2_1.1";
    }

    // more info here:
    // https://docs.google.com/spreadsheets/d/19UeuPQ7Qnjk5JoZ4bVMy5budLxThxOgk-owMnMyBcv0/edit?disco=AAAANBX9U8o
    static final AccountTypeMapper ACCOUNT_TYPE_MAPPER =
            AccountTypeMapper.builder()
                    .put(
                            AccountTypes.CHECKING,
                            PSD2_PAYMENT_ACCOUNT,
                            "PL_CRN_AC_INDV",
                            "PL_CRN_AC_SELF_EMP",
                            "PL_CRN_AC_SME_MC",
                            "PL_CRN_OKO_AC_SME_MC")
                    .put(
                            AccountTypes.SAVINGS,
                            PSD2_PAYMENT_ACCOUNT,
                            "PL_SVGS_AC_INDV",
                            "PL_SVGS_AC_SELF_EMP",
                            "PL_ESC_AC_SME_MC",
                            "PL_TRS_AC_SME_MC")
                    .put(
                            AccountTypes.CREDIT_CARD,
                            PSD2_PAYMENT_ACCOUNT,
                            "PL_CR_CARD_INDV",
                            "PL_CR_CARD_SELF_EMP",
                            "PL_PP_CARD_SME_MC",
                            "PL_PP_CARD_INDV",
                            "PL_PP_CARD_SELF_EMP")
                    .put(AccountTypes.CHECKING, PSD2_PAYMENT_ACCOUNT)
                    .build();
}
