package se.tink.backend.aggregation.agents.banks.norwegian;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.banks.norwegian.model.AccountEntity;
import se.tink.backend.aggregation.agents.banks.norwegian.utils.CreditCardParsingUtils;
import se.tink.backend.aggregation.agents.banks.norwegian.utils.CreditCardParsingUtils.AccountNotFoundException;
import se.tink.backend.aggregation.agents.banks.norwegian.utils.SavingsAccountParsingUtils;
import se.tink.libraries.social.security.TestSSN;

public class NorwegianTest extends AbstractAgentTest<NorwegianAgent> {
    private static final String savingsAccountPage =
            "<div class=\"grid\">"
                    + "<div class=\"card\">"
                    + "<div class=\"card__body\" data-target=\"/MinSida/SavingsAccount/Details?AccountNumber=10317169001\">"
                    + "<div class=\"grid-u-1-2 card__data\" style=\"text-align: left\">"
                    + "Sparkonto                    <div class=\"card__data--label\">11111111111</div>"
                    + "</div>"
                    + "<div class=\"grid-u-1-2 card__data\">"
                    + "100,11"
                    + "<div class=\"card__data--label\">tillgängligt</div>"
                    + "</div>"
                    + "</div>"
                    + "<a class=\"card__expander collapse\" href=\"#\">"
                    + "<img src=\"/Images/chevron_down.svg\" alt=\"Visa genvägar\" />"
                    + "</a>"
                    + "<div class=\"card__collapsable__content collapse in\">"
                    + "<div class=\"card__actions\">"
                    + "<a href=\"/MinSida/SavingsAccount/Transactions?selectedAccount=10317169001\" class=\"uit-cardaction uit-mypage2savingsaccounttransactions\">"
                    + "<span class=\"icon icon--transactions\"></span>"
                    + "Transaktioner"
                    + "</a>"
                    + "<a href=\"/MinSida/SavingsAccount/Payment?accountNumber=10317169001\" class=\"uit-cardaction uit-mypage2savingsaccountpayment\">"
                    + "<span class=\"icon icon--payment\"></span>"
                    + "Överföra"
                    + "</a>"
                    + "</div>"
                    + "</div>"
                    + "</div>"
                    + "</div>";

    public NorwegianTest() {
        super(NorwegianAgent.class);
    }

    @Test
    public void testMobilebankId() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setUsername(TestSSN.EP);
        credentials.setType(CredentialsTypes.MOBILE_BANKID);

        testAgent(credentials);
    }

    @Test
    public void testBankIdValidFormats() {
        assertValidBankId("522767******6982");
        assertValidBankId("111111******1111");
    }

    @Test
    public void testBankIdInvalidFormats() {
        assertInvalidBankId("522767******698");
        assertInvalidBankId("1111111111111111");
    }

    private void assertValidBankId(String accountNumber) {
        AccountEntity account = new AccountEntity();
        account.setAccountNumber(accountNumber);
        Assert.assertTrue(account.hasValidBankId());
    }

    private void assertInvalidBankId(String accountNumber) {
        AccountEntity account = new AccountEntity();
        account.setAccountNumber(accountNumber);
        Assert.assertFalse(account.hasValidBankId());
    }

    @Test
    public void testParseBalanceFromHtmlContent() {
        String htmlContent =
                "<div class=\"grid\">"
                        + "<div class=\"grid-u-1-2\">Kreditgräns (SEK)</div>"
                        + "<div class=\"grid-u-1-2 text-right\">20&#160;000</div>"
                        + "<div class=\"grid-u-1-2\">Utnyttjad kredit (SEK)</div>"
                        + "<div class=\"grid-u-1-2 text-right\">150</div>"
                        + "<div class=\"grid-u-1-2\">Kvar att utnyttja (SEK)</div>"
                        + "<div class=\"grid-u-1-2 text-right\">19&#160;850</div>"
                        + "</div>"
                        + "<div class=\"grid\"><hr/>"
                        + "<h2>Senaste faktura</h2>"
                        + "<div class=\"grid-u-1-2\">Förfallodatum</div>"
                        + "<div class=\"grid-u-1-2 text-right\">2018-06-28</div>"
                        + "<div class=\"grid-u-1-2\">Utnyttjad kredit</div>"
                        + "<div class=\"grid-u-1-2 text-right\">111,11</div>"
                        + "<div class=\"grid-u-1-2\">Belopp att betala</div>"
                        + "<div class=\"grid-u-1-2 text-right\">100,00</div>"
                        + "<div class=\"grid-u-1-2\">Plusgiro</div>"
                        + "<div class=\"grid-u-1-2 text-right\">418 75 00-6</div>"
                        + "<div class=\"grid-u-1-2\">OCR</div>"
                        + "<div class=\"grid-u-1-2 text-right\">XXXXXX</div>"
                        + "<div class=\"grid-u-1-2\">Status</div>"
                        + "<div class=\"grid-u-1-2 text-right\">Betald</div>"
                        + "<div class=\"grid-u-1\">"
                        + " <a href=\"/MinSida/Invoice/InvoiceView/1234567\" target=\"_blank\">Visa faktura</a>"
                        + "</div>"
                        + "</div>";

        Double balance = CreditCardParsingUtils.parseBalance(htmlContent);

        Assert.assertEquals(-150, balance, 0.0001);
    }

    @Test
    public void testParseCardNumberFromHtmlContent() throws AccountNotFoundException {
        String htmlContent =
                "<div class=\"creditcard\">"
                        + "     <div class=\"creditcard__body-container\" id=\"creditcard__body-container\">"
                        + "         <div class=\"creditcard__body\" id=\"creditcard__body\">"
                        + "             <div class=\"creditcard__number\">6543 21** **** 4321</div>"
                        + "             <div class=\"creditcard__expire text-right\">01/21</div>"
                        + "             <div class=\"creditcard__name\">TEST TESTSSON</div>"
                        + "         </div>"
                        + "     </div>"
                        + "</div>";

        String accountNumber = CreditCardParsingUtils.parseAccountNumber(htmlContent);

        Assert.assertEquals("654321******4321", accountNumber);
    }

    @Test
    public void testParsingOfAccountNumberForTransactions() {
        String htmlContent =
                "    <script type=\"text/javascript\">\n"
                        + "        var viewModel;\n"
                        + "        $(function () {\n"
                        + "\n"
                        + "\n"
                        + "            var data = {\"region\":2,\"accountNo\":\"11111111111\",\"accountList\":"
                        + "[{\"disabled\":false,\"group\":null,\"selected\":false,\"text\":\"Kreditkort (11111111111)\","
                        + "\"value\":\"11111111111\"}],\"yearList\":[{\"disabled\":false,\"group\":null,\"selected\":false,"
                        + "\"text\":\"Ofakturerade transaktioner\",\"value\":\"0\"},{\"disabled\":false,\"group\":null,"
                        + "\"selected\":false,\"text\":\"2018\",\"value\":\"2018\"},{\"disabled\":false,\"group\":null,"
                        + "\"selected\":false,\"text\":\"2017\",\"value\":\"2017\"},{\"disabled\":false,\"group\":null,"
                        + "\"selected\":false,\"text\":\"2016\",\"value\":\"2016\"},{\"disabled\":false,\"group\":null,"
                        + "\"selected\":false,\"text\":\"2015\",\"value\":\"2015\"},{\"disabled\":false,\"group\":null,"
                        + "\"selected\":false,\"text\":\"2014\",\"value\":\"2014\"},{\"disabled\":false,\"group\":null,"
                        + "\"selected\":false,\"text\":\"2013\",\"value\":\"2013\"},{\"disabled\":false,\"group\":null,"
                        + "\"selected\":false,\"text\":\"2012\",\"value\":\"2012\"},{\"disabled\":false,\"group\":null,"
                        + "\"selected\":false,\"text\":\"2011\",\"value\":\"2011\"},{\"disabled\":false,\"group\":null,"
                        + "\"selected\":false,\"text\":\"2010\",\"value\":\"2010\"},{\"disabled\":false,\"group\":null,"
                        + "\"selected\":false,\"text\":\"2009\",\"value\":\"2009\"},{\"disabled\":false,\"group\":null,"
                        + "\"selected\":false,\"text\":\"2008\",\"value\":\"2008\"},{\"disabled\":false,\"group\":null,"
                        + "\"selected\":false,\"text\":\"2007\",\"value\":\"2007\"}],\"monthList\":[{\"disabled\":false,"
                        + "\"group\":null,\"selected\":false,\"text\":\"\",\"value\":\"\"},{\"disabled\":false,"
                        + "\"group\":null,\"selected\":false,\"text\":\"Januari\",\"value\":\"1\"},{\"disabled\":false,"
                        + "\"group\":null,\"selected\":false,\"text\":\"Februari\",\"value\":\"2\"},{\"disabled\":false,"
                        + "\"group\":null,\"selected\":false,\"text\":\"Mars\",\"value\":\"3\"},{\"disabled\":false,"
                        + "\"group\":null,\"selected\":false,\"text\":\"April\",\"value\":\"4\"},{\"disabled\":false,"
                        + "\"group\":null,\"selected\":false,\"text\":\"Maj\",\"value\":\"5\"},{\"disabled\":false,"
                        + "\"group\":null,\"selected\":false,\"text\":\"Juni\",\"value\":\"6\"},{\"disabled\":false,"
                        + "\"group\":null,\"selected\":false,\"text\":\"Juli\",\"value\":\"7\"},{\"disabled\":false,"
                        + "\"group\":null,\"selected\":false,\"text\":\"Augusti\",\"value\":\"8\"},{\"disabled\":false,"
                        + "\"group\":null,\"selected\":false,\"text\":\"September\",\"value\":\"9\"},{\"disabled\":false,"
                        + "\"group\":null,\"selected\":false,\"text\":\"Oktober\",\"value\":\"10\"},{\"disabled\":false,"
                        + "\"group\":null,\"selected\":false,\"text\":\"November\",\"value\":\"11\"},{\"disabled\":false,"
                        + "\"group\":null,\"selected\":false,\"text\":\"December\",\"value\":\"12\"}],"
                        + "\"showAccountList\":false,\"showPdfStatement\":false,\"showCurrencyAmount\":true,"
                        + "\"showMerchantInfo\":true,\"showForeignAccountInfo\":false,"
                        + "\"amountToPay\":{\"11111111111\":-1000.0000},"
                        + "\"tempUnavailableText\":\"Ofakturerade transaktioner - Tillfälligt otillgänglig\"};;\n"
                        + "\n"
                        + "            viewModel = new TransactionViewModel(data,\n"
                        + "                \"/MyPage2/Transaction/GetTransactions\",\n"
                        + "                \"/MinSida/Transactions/TransactionReceipt\",\n"
                        + "                \"/MinSida/Transactions/Statement\",\n"
                        + "                false\n"
                        + "            );\n"
                        + "            ko.applyBindings(viewModel);\n"
                        + "            viewModel.getTransactions();\n"
                        + "        });\n"
                        + "\n"
                        + "    </script>";

        Optional<String> stringOptional =
                CreditCardParsingUtils.parseTransactionalAccountNumber(htmlContent);
        Assert.assertEquals("11111111111", stringOptional.get());
    }

    @Test(expected = AccountNotFoundException.class)
    public void testMissingAccount() throws AccountNotFoundException {
        String htmlContent =
                "<div class=\"main-content\">"
                        + "                    <div class=\"topnav visible-xs\">"
                        + "                            <a href=\"/MinSida\" class=\"glyphicon glyphicons-home-image\"></a>"
                        + "        <a href=\"/MinSida\" class=\"glyphicon glyphicons-circle-arrow-left-image\"></a>"
                        + "                        <h1>Kreditkort</h1>"
                        + "                    </div>"
                        + "                        <h1 class=\"hidden-xs content-heading\">Kreditkort</h1>"
                        + ""
                        + "                    <div style=\"background-color:#C2C2C2; padding:30px\">"
                        + "       <h1>Vi har inte registrerat att du har något kreditkort hos oss</h1>"
                        + "    </div>"
                        + "    <div style=\"color:#0b4d9c; height:300px\">"
                        + "        <div style=\"float:left; width:500px; padding:30px\">"
                        + "            <h1>Spara till semestern hela året!</h1>"
                        + "            <p>"
                        + "                <b>Några av våra fördelar:</b>"
                        + "           <ul>"
                        + "               <li>Tjäna 1 % CashPoints på alla varuköp - spara till resan varje dag!</li>"
                        + "               <li>Tjäna upp till 20 % CashPoints hos Norwegian</li>"
                        + "               <li>Reseförsäkring och avbeställningsskydd</li>"
                        + "               <li>Lost connection-försäkring</li>"
                        + "           </ul>"
                        + "            </p>"
                        + "            <p>"
                        + "                Norwegian-kortet kostar ingenting att skaffa sig, och har ingen årsavgift!"
                        + "            </p>"
                        + "        </div>"
                        + "    </div>"
                        + "                </div>";
        CreditCardParsingUtils.parseAccountNumber(htmlContent);
    }

    @Test
    public void testParsingOfSavingsAccountNumber() {
        Optional<String> accountNumber =
                SavingsAccountParsingUtils.parseSavingsAccountNumber(savingsAccountPage);
        Assert.assertTrue(accountNumber.isPresent());
        Assert.assertEquals("11111111111", accountNumber.get());
    }

    @Test
    public void testParsingOfSavingsBalance() {
        Double balance = SavingsAccountParsingUtils.parseSavingsAccountBalance(savingsAccountPage);
        Assert.assertEquals(100.11, balance, 0.5);
    }
}
