package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional;

import org.junit.Ignore;

@Ignore
public class TransactionTestData {

    public static String transaction =
            "{"
                    + "\"lb_Date\": {\"text\": \"22/10/2020\"},"
                    + "\"lb_AccountOppositeSide\": {\"text\": \"sample account opposite side\"},"
                    + "\"mlb_SplitAllowed\": {\"text\": \"Y\"},"
                    + "\"mlb_TransferAllowed\": {\"text\": \"Y\"},"
                    + "\"lb_Pending\": {\"text\": \"Y\"},"
                    + "\"lb_Amount\": {\"text\": \"-10,00 EUR\"},"
                    + "\"lb_NameOppositeSide\": {\"text\": \"sample name opposite side\"},"
                    + "\"lb_Communication\": {\"text\": \"sample communication\"},"
                    + "\"lb_Description\": {\"text\": \"sample description\"}"
                    + "}";

    public static String notPendingTransaction =
            "{"
                    + "\"lb_Date\": {\"text\": \"22/10/2020\"},"
                    + "\"lb_AccountOppositeSide\": {\"text\": \"sample account opposite side\"},"
                    + "\"mlb_SplitAllowed\": {\"text\": \"Y\"},"
                    + "\"mlb_TransferAllowed\": {\"text\": \"Y\"},"
                    + "\"lb_Pending\": {\"text\": \"N\"},"
                    + "\"lb_Amount\": {\"text\": \"-10,00 EUR\"},"
                    + "\"lb_NameOppositeSide\": {\"text\": \"sample name opposite side\"},"
                    + "\"lb_Communication\": {\"text\": \"sample communication\"},"
                    + "\"lb_Description\": {\"text\": \"sample description\"}"
                    + "}";

    public static String nullAmountTransaction =
            "{"
                    + "\"lb_Date\": {\"text\": \"22/10/2020\"},"
                    + "\"lb_AccountOppositeSide\": {\"text\": \"sample account opposite side\"},"
                    + "\"mlb_SplitAllowed\": {\"text\": \"Y\"},"
                    + "\"mlb_TransferAllowed\": {\"text\": \"Y\"},"
                    + "\"lb_Pending\": {\"text\": \"N\"},"
                    + "\"lb_NameOppositeSide\": {\"text\": \"sample name opposite side\"},"
                    + "\"lb_Communication\": {\"text\": \"sample communication\"},"
                    + "\"lb_Description\": {\"text\": \"sample description\"}"
                    + "}";

    public static String nullNameOppositeSide =
            "{"
                    + "\"lb_Date\": {\"text\": \"22/10/2020\"},"
                    + "\"lb_AccountOppositeSide\": {\"text\": \"sample account opposite side\"},"
                    + "\"mlb_SplitAllowed\": {\"text\": \"Y\"},"
                    + "\"mlb_TransferAllowed\": {\"text\": \"Y\"},"
                    + "\"lb_Pending\": {\"text\": \"Y\"},"
                    + "\"lb_Amount\": {\"text\": \"-10,00 EUR\"},"
                    + "\"lb_Communication\": {\"text\": \"sample communication\"},"
                    + "\"lb_Description\": {\"text\": \"sample description\"}"
                    + "}";

    public static String nullNameOppositeAndCommunicationSide =
            "{"
                    + "\"lb_Date\": {\"text\": \"22/10/2020\"},"
                    + "\"lb_AccountOppositeSide\": {\"text\": \"sample account opposite side\"},"
                    + "\"mlb_SplitAllowed\": {\"text\": \"Y\"},"
                    + "\"mlb_TransferAllowed\": {\"text\": \"Y\"},"
                    + "\"lb_Pending\": {\"text\": \"Y\"},"
                    + "\"lb_Amount\": {\"text\": \"-10,00 EUR\"},"
                    + "\"lb_Description\": {\"text\": \"sample description\"}"
                    + "}";

    public static String nullNameOppositeAndCommunicationAndDescriptionSide =
            "{"
                    + "\"lb_Date\": {\"text\": \"22/10/2020\"},"
                    + "\"lb_AccountOppositeSide\": {\"text\": \"sample account opposite side\"},"
                    + "\"mlb_SplitAllowed\": {\"text\": \"Y\"},"
                    + "\"mlb_TransferAllowed\": {\"text\": \"Y\"},"
                    + "\"lb_Pending\": {\"text\": \"Y\"},"
                    + "\"lb_Amount\": {\"text\": \"-10,00 EUR\"}"
                    + "}";

    public static String maestroTransactionString2 =
            "{"
                    + "\"lb_Date\": {\"text\": \"20/02/2018\"},"
                    + "\"lb_AccountOppositeSide\": {\"text\": \"\"},"
                    + "\"mlb_SplitAllowed\": {\"text\": \"Y\"},"
                    + "\"mlb_TransferAllowed\": {\"text\": \"N\"},"
                    + "\"lb_Pending\": {\"text\": \"N\"},"
                    + "\"lb_Amount\": {\"text\": \"-15,00 EUR\"},"
                    + "\"lb_NameOppositeSide\": {\"text\": \"\"},"
                    + "\"lb_Communication\": {\"text\": \"\"},"
                    + "\"lb_Description\": {\"text\": \"MAESTRO-BETALING 19/02-MERCHANT NAME BE 15,00   \\nEUR KAART NR 1234 1234 1234 1234 - LASTNAME FIRSTNAME   \\nREF. : 123456789 VAL. 20-02                       \\n\"}"
                    + "}";

    public static String transactionString =
            "{"
                    + "\"lb_Date\": {\"text\": \"12/03/2014\"},"
                    + "\"lb_AccountOppositeSide\": {\"text\": \"BE12 1234 1234 1234\"},"
                    + "\"mlb_SplitAllowed\": {\"text\": \"N\"},"
                    + "\"mlb_TransferAllowed\": {\"text\": \"N\"},"
                    + "\"lb_Pending\": {\"text\": \"N\"},"
                    + "\"lb_Amount\": {\"text\": \"-10,00 EUR\"},"
                    + "\"lb_NameOppositeSide\": {\"text\": \"MERCHANT NAME\"},"
                    + "\"lb_Communication\": {\"text\": \"\"},"
                    + "\"lb_Description\": {\"text\": \"TEXT TEXT TEXT TEXT 123456789 TEXT TEXT   \\nTEXT TEXT: MORE TEXT    \\ntext text 1234567 03/14 TEXT:\\n1234567890                        \\nREF. : 123456789 VAL. 12-34                       \\n\"}"
                    + "}";
}
