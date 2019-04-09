package se.tink.libraries.account.identifiers;

import se.tink.libraries.account.AccountIdentifier;

public class TestAccount {
    public static final String ALANDSBANKEN_DL = "23100379401";
    public static final String DANSKEBANK_FH = "12780236434";
    public static final String DANSKEBANK_ANOTHER_FH = "12780236442";
    public static final String HANDELSBANKEN_FH = "6769392752158";
    public static final String HANDELSBANKEN_ANOTHER_FH = "6769933539738";
    public static final String HANDELSBANKEN_JE = "6152609866052";
    public static final String ICABANKEN_FH = "92717696652";
    public static final String ICABANKEN_ANOTHER_FH = "92716555613";
    public static final String LANSFORSAKRINGAR_FH = "90230694847";
    public static final String LANSFORSAKRINGAR_ANOTHER_FH = "90230760092";
    public static final String NORDEA_EP = "16034332648";
    public static final String NORDEASSN_EP = "33008401141935";
    public static final String NORDEASSN_JK = "33009009186132";
    public static final String SAVINGSBANK_AL = "8422831270465";
    public static final String SEB_DL = "53680239572";
    public static final String SEB_ANOTHER_DL = "56943546619";
    public static final String SEB_JR = "53570077470";
    public static final String SEB_ANOTHER_JR = "56243031772";
    public static final String SKANDIABANKEN_FH = "91594749703";
    public static final String SWEDBANK_FH = "821499246657853";
    public static final String SWEDBANK_ANOTHER_FH = "821499246657929";
    public static final String SBAB_NO = "92521968323";
    public static final String SBAB_ANOTHER_NO = "92523761793";
    public static final String AVANZA_NO = "95502957650";
    public static final String HANDELSBANKEN_NO = "6140634216392";
    public static final String HANDELSBANKEN_ANOTHER_NO = "6140635486202";

    public static class Identifiers {
        public static final AccountIdentifier ALANDSBANKEN_DL =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.ALANDSBANKEN_DL);
        public static final AccountIdentifier DANSKEBANK_FH =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.DANSKEBANK_FH);
        public static final AccountIdentifier DANSKEBANK_ANOTHER_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.DANSKEBANK_ANOTHER_FH);
        public static final AccountIdentifier HANDELSBANKEN_FH =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.HANDELSBANKEN_FH);
        public static final AccountIdentifier HANDELSBANKEN_ANOTHER_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.HANDELSBANKEN_ANOTHER_FH);
        public static final AccountIdentifier HANDELSBANKEN_JE =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.HANDELSBANKEN_JE);
        public static final AccountIdentifier ICABANKEN_FH =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.ICABANKEN_FH);
        public static final AccountIdentifier ICABANKEN_ANOTHER_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.ICABANKEN_ANOTHER_FH);
        public static final AccountIdentifier LANSFORSAKRINGAR_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.LANSFORSAKRINGAR_FH);
        public static final AccountIdentifier LANSFORSAKRINGAR_ANOTHER_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.LANSFORSAKRINGAR_ANOTHER_FH);
        public static final AccountIdentifier NORDEA_EP =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.NORDEA_EP);
        public static final AccountIdentifier NORDEASSN_EP =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.NORDEASSN_EP);
        public static final AccountIdentifier NORDEASSN_JK =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.NORDEASSN_JK);
        public static final AccountIdentifier SAVINGSBANK_AL =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.SAVINGSBANK_AL);
        public static final AccountIdentifier SEB_DL =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.SEB_DL);
        public static final AccountIdentifier SEB_ANOTHER_DL =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.SEB_ANOTHER_DL);
        public static final AccountIdentifier SEB_JR =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.SEB_JR);
        public static final AccountIdentifier SEB_ANOTHER_JR =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.SEB_ANOTHER_JR);
        public static final AccountIdentifier SKANDIABANKEN_FH =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.SKANDIABANKEN_FH);
        public static final AccountIdentifier SWEDBANK_FH =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.SWEDBANK_FH);
        public static final AccountIdentifier SWEDBANK_ANOTHER_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.SWEDBANK_ANOTHER_FH);
        public static final AccountIdentifier SBAB_NO =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.SBAB_NO);
        public static final AccountIdentifier SBAB_ANOTHER_NO =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.SBAB_ANOTHER_NO);
        public static final AccountIdentifier AVANZA_NO =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.AVANZA_NO);
        public static final AccountIdentifier HANDELSBANKEN_NO =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.HANDELSBANKEN_NO);
        public static final AccountIdentifier HANDELSBANKEN_ANOTHER_NO =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.HANDELSBANKEN_ANOTHER_NO);
    }

    public static class IdentifiersWithName {
        public static final AccountIdentifier ALANDSBANKEN_DL =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.ALANDSBANKEN_DL, "Aland DL");
        public static final AccountIdentifier DANSKEBANK_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.DANSKEBANK_FH, "Danske FH 1");
        public static final AccountIdentifier DANSKEBANK_ANOTHER_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE,
                        TestAccount.DANSKEBANK_ANOTHER_FH,
                        "Danske FH 2");
        public static final AccountIdentifier HANDELSBANKEN_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.HANDELSBANKEN_FH, "SHB FH");
        public static final AccountIdentifier HANDELSBANKEN_ANOTHER_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.HANDELSBANKEN_ANOTHER_FH, "SHB FH");
        public static final AccountIdentifier HANDELSBANKEN_JE =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.HANDELSBANKEN_JE, "SHB JE");
        public static final AccountIdentifier ICABANKEN_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.ICABANKEN_FH, "ICA FH");
        public static final AccountIdentifier ICABANKEN_ANOTHER_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.ICABANKEN_ANOTHER_FH, "ICA FH 2");
        public static final AccountIdentifier LANSFORSAKRINGAR_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.LANSFORSAKRINGAR_FH, "LF FH");
        public static final AccountIdentifier LANSFORSAKRINGAR_ANOTHER_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE,
                        TestAccount.LANSFORSAKRINGAR_ANOTHER_FH,
                        "LF FH 2");
        public static final AccountIdentifier NORDEA_EP =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.NORDEA_EP, "Nordea EP");
        public static final AccountIdentifier NORDEASSN_EP =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.NORDEASSN_EP, "Nordea SSN EP");
        public static final AccountIdentifier NORDEASSN_JK =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.NORDEASSN_JK, "Nordea SSN JK");
        public static final AccountIdentifier SAVINGSBANK_AL =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.SAVINGSBANK_AL, "Savingsbank AL");
        public static final AccountIdentifier SEB_DL =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.SEB_DL, "SEB DL");
        public static final AccountIdentifier SEB_ANOTHER_DL =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.SEB_ANOTHER_DL, "SEB DL 2");
        public static final AccountIdentifier SEB_JR =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.SEB_JR, "SEB JR");
        public static final AccountIdentifier SEB_ANOTHER_JR =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.SEB_ANOTHER_JR, "SEB JR 2");
        public static final AccountIdentifier SKANDIABANKEN_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.SKANDIABANKEN_FH, "Skandia FH");
        public static final AccountIdentifier SWEDBANK_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.SWEDBANK_FH, "Swed FH");
        public static final AccountIdentifier SWEDBANK_ANOTHER_FH =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.SWEDBANK_ANOTHER_FH, "Swed FH 2");
        public static final AccountIdentifier SBAB_NO =
                AccountIdentifier.create(AccountIdentifier.Type.SE, TestAccount.SBAB_NO, "SBAB NO");
        public static final AccountIdentifier SBAB_ANOTHER_NO =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.SBAB_ANOTHER_NO, "SBAB NO 2");
        public static final AccountIdentifier AVANZA_NO =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.AVANZA_NO, "Avanza NO");
        public static final AccountIdentifier HANDELSBANKEN_NO =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE, TestAccount.HANDELSBANKEN_NO, "SHB NO");
        public static final AccountIdentifier HANDELSBANKEN_ANOTHER_NO =
                AccountIdentifier.create(
                        AccountIdentifier.Type.SE,
                        TestAccount.HANDELSBANKEN_ANOTHER_NO,
                        "SHB NO 2");
    }

    public static class SwedishBGIdentifiers {
        public static final AccountIdentifier NO_OCR_RADIOHJALPEN =
                AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "9019506");
        public static final AccountIdentifier SOFT_OCR_TYPE1_AMEX =
                AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "52355294");
        // TODO: Missing a BG for hard validation without any additional checks
        /*public final static AccountIdentifier HARD_OCR_TYPE2_XXXXX =
        AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "XXXXXXXX");*/
        public static final AccountIdentifier HARD_OCR_TYPE3_KLARNA =
                AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "2211555");
        public static final AccountIdentifier HARD_OCR_TYPE4_AMEX =
                AccountIdentifier.create(AccountIdentifier.Type.SE_BG, "7308596");
    }

    public static class SwedishPGIdentifiers {
        public static final AccountIdentifier NO_OCR_RADIOHJALPEN =
                AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "9019506");
        public static final AccountIdentifier HARD_OCR_TYPE3_KLARNA =
                AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "41585027");
        public static final AccountIdentifier HARD_OCR_TYPE4_VOLVOFINANS =
                AccountIdentifier.create(AccountIdentifier.Type.SE_PG, "47501044");
    }

    /**
     * DO NOT USE FOR COMPLETING PAYMENTS (ABORT IN BANKID), THIS IS JUST AN OLD REFERENCE I FOUND
     */
    public static class ValidSwedishGiroMessages {
        /**
         * DO NOT USE FOR COMPLETING PAYMENTS (ABORT IN BANKID), THIS IS JUST A MESSAGE I CAME UP
         * WITH
         */
        public static final String NO_OCR_RADIOHJALPEN = "Stöd för Radiohjälpen";
        /**
         * DO NOT USE FOR COMPLETING PAYMENTS (ABORT IN BANKID), THIS IS JUST AN OCR I CAME UP WITH
         */
        public static final String SOFT_MESSAGE_AMEX = "Payment to AMEX";
        /**
         * DO NOT USE FOR COMPLETING PAYMENTS (ABORT IN BANKID), THIS IS JUST AN OCR I CAME UP WITH
         */
        public static final String SOFT_OCR_TYPE1_AMEX = "1212121212";
        /**
         * DO NOT USE FOR COMPLETING PAYMENTS (ABORT IN BANKID), THIS IS JUST AN OCR I CAME UP WITH
         */
        public static final String HARD_OCR_TYPE3_KLARNA = "12121212121212179";
        /**
         * DO NOT USE FOR COMPLETING PAYMENTS (ABORT IN BANKID), THIS IS JUST AN OCR I CAME UP WITH
         */
        public static final String HARD_OCR_TYPE4_AMEX = "12121212121212120";
        /**
         * DO NOT USE FOR COMPLETING PAYMENTS (ABORT IN BANKID), THIS IS JUST AN OCR I CAME UP WITH
         */
        public static final String HARD_OCR_TYPE4_VOLVOFINANS = "1212121212";
    }

    /**
     * DO NOT USE FOR COMPLETING PAYMENTS (ABORT IN BANKID), THIS IS JUST AN OLD REFERENCE I FOUND
     */
    public static class InvalidSwedishGiroMessages {
        /**
         * DO NOT USE FOR COMPLETING PAYMENTS (ABORT IN BANKID), THIS IS JUST A MESSAGE I CAME UP
         * WITH
         */
        public static final String NO_OCR_RADIOHJALPEN = "";
        /**
         * DO NOT USE FOR COMPLETING PAYMENTS (ABORT IN BANKID), THIS IS JUST AN OCR I CAME UP WITH
         */
        public static final String SOFT_MESSAGE_AMEX = "";
        /**
         * DO NOT USE FOR COMPLETING PAYMENTS (ABORT IN BANKID), THIS IS JUST AN OCR I CAME UP WITH
         */
        public static final String SOFT_OCR_TYPE1_AMEX = "1212121210";
        /**
         * DO NOT USE FOR COMPLETING PAYMENTS (ABORT IN BANKID), THIS IS JUST AN OCR I CAME UP WITH
         */
        public static final String HARD_OCR_TYPE3_KLARNA = "12121212121212109";
        /**
         * DO NOT USE FOR COMPLETING PAYMENTS (ABORT IN BANKID), THIS IS JUST AN OCR I CAME UP WITH
         */
        public static final String HARD_OCR_TYPE4_AMEX = "12121212121212121";
    }
}
