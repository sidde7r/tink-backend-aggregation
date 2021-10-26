package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank;

import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;

public class DanskeBankSEConstants {

    public static class TransferType {
        public static final String INTERNAL = "TransferToOwnAccountSEv2";
        public static final String EXTERNAL = "TransferToOtherAccountSE";
        public static final String GIRO = "PayBillSE";
    }

    public static class TransferAccountType {
        public static final String INTERNAL = "internal";
        public static final String EXTERNAL = "external";
        public static final String GIRO = "payBill";
    }

    public static class TransferPayType {
        public static final String ACCOUNT = "";
        public static final String BANK_GIRO = "B";
        public static final String PLUS_GIRO = "P";
    }

    public static class TransferConfig {
        public static final int SOURCE_MESSAGE_MAX_LENGTH = 19;
        public static final int DESTINATION_MESSAGE_MAX_LENGTH = 12;
        public static final String WHITE_LISTED_CHARACTER_STRING = ",._-?!/:()&`~";
    }

    public static class ResponseMessage {
        public static final String MESSAGE_MISSING = "Meddelande till mottagaren skall fyllas i.";
        public static final String EXCESS_AMOUNT = "Från-kontot saknar täckning.";
        public static final String EXECUTION_DAY_INVALID = "Betalningen kan inte utföras i dag.";
    }

    public static final TypeMapper<AccountCapabilities> ACCOUNT_CAPABILITIES_MAPPER =
            TypeMapper.<AccountCapabilities>builder()
                    .put(
                            new AccountCapabilities(
                                    AccountCapabilities.Answer.YES,
                                    AccountCapabilities.Answer.YES,
                                    AccountCapabilities.Answer.YES,
                                    AccountCapabilities.Answer.YES),
                            "2A3",
                            "2B4",
                            "2B5",
                            "2B7",
                            "2B8",
                            "2CY",
                            "2EX",
                            "3BG")
                    .put(
                            new AccountCapabilities(
                                    AccountCapabilities.Answer.NO,
                                    AccountCapabilities.Answer.YES,
                                    AccountCapabilities.Answer.YES,
                                    AccountCapabilities.Answer.YES),
                            "2AB",
                            "2BA",
                            "2BP",
                            "2C2",
                            "2DC",
                            "2EH",
                            "2SF",
                            "3CA")
                    .put(
                            new AccountCapabilities(
                                    AccountCapabilities.Answer.NO,
                                    AccountCapabilities.Answer.YES,
                                    AccountCapabilities.Answer.NO,
                                    AccountCapabilities.Answer.YES),
                            "2DI")
                    .put(
                            new AccountCapabilities(
                                    AccountCapabilities.Answer.NO,
                                    AccountCapabilities.Answer.YES,
                                    AccountCapabilities.Answer.NO,
                                    AccountCapabilities.Answer.NO),
                            "2CF",
                            "2DH")
                    .put(
                            new AccountCapabilities(
                                    AccountCapabilities.Answer.NO,
                                    AccountCapabilities.Answer.NO,
                                    AccountCapabilities.Answer.NO,
                                    AccountCapabilities.Answer.NO),
                            "3AC",
                            "3BJ",
                            "3BK")
                    .build();
}
