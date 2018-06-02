package se.tink.backend.aggregation.agents.banks;

import java.util.Optional;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.AbstractAgentTest;
import se.tink.backend.aggregation.agents.banks.norwegian.NorwegianAgent;
import se.tink.backend.aggregation.agents.banks.norwegian.model.AccountEntity;
import se.tink.backend.aggregation.agents.banks.norwegian.utils.CreditCardParsingUtils;
import se.tink.backend.aggregation.agents.banks.norwegian.utils.CreditCardParsingUtils.AccountNotFoundException;
import se.tink.backend.aggregation.agents.banks.norwegian.utils.SavingsAccountParsingUtils;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.CredentialsTypes;
import se.tink.backend.common.utils.TestSSN;

public class NorwegianTest extends AbstractAgentTest<NorwegianAgent> {
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
        String htmlContent = " <div class=\"page\">"
                + "         <form action=\"/MinSida/Creditcard/Overview\" method=\"post\">"
                + "             <table class=\"data-table\">"
                + "                 <tr>"
                + "                     <td colspan=\"2\"><h2>Bank Norwegian Kreditkort</h2></td>"
                + "                 </tr>"
                + "                 <tr>"
                + "                     <td class=\"data-table-label\"><label>Kreditgräns (SEK):</label></td>"
                + "                     <td class=\"data-table-value&#32;text-right\">50 000,00</td>"
                + "                 </tr>"
                + "                 <tr>"
                + "                     <td class=\"data-table-label\"><label>Utnyttjad kredit (SEK):</label></td>"
                + "                     <td class=\"data-table-value&#32;text-right\">1 819,61</td>"
                + "                 </tr>"
                + "                 <tr>"
                + "                     <td class=\"data-table-label\"><label>Kvar att utnyttja (SEK):</label></td>"
                + "                     <td class=\"data-table-value&#32;text-right\">48 180,39</td>"
                + "                 </tr>"
                + "                 <tr>"
                + "                     <td colspan=\"2\"><hr /></td>"
                + "                 </tr>"
                + "                 <tr>"
                + "                     <td colspan=\"2\"><h2>Sista faktura</h2></td>"
                + "                     </tr>"
                + "                 <tr>"
                + "                     <td class=\"data-table-label\"><label>Förfallodatum:</label></td>"
                + "                     <td class=\"data-table-value&#32;text-right\">2017-03-28</td>"
                + "                 </tr>"
                + "                 <tr>"
                + "                     <td class=\"data-table-label\"><label>Utnyttjad kredit:</label></td>"
                + "                     <td class=\"data-table-value&#32;text-right\">511,39</td>"
                + "                 </tr>"
                + "                 <tr>"
                + "                     <td class=\"data-table-label\"><label>Belopp att betala:</label></td>"
                + "                     <td class=\"data-table-value&#32;text-right\">0,00</td>"
                + "                 </tr>"
                + "                 <tr>"
                + "                     <td class=\"data-table-label\"><label>Plusgiro:</label></td>"
                + "                     <td class=\"data-table-value&#32;text-right\">418 75 00-6</td>"
                + "                 </tr>"
                + "                 <tr>"
                + "                     <td class=\"data-table-label\">"
                + "                         <label>OCR:</label>"
                + "                     </td>"
                + "                     <td class=\"data-table-value&#32;text-right\">1111111111111</td>"
                + "                 </tr>"
                + "                 <tr>"
                + "                     <td class=\"data-table-label\">"
                + "                         <label>Status:</label>"
                + "                     </td>"
                + "                     <td class=\"data-table-value&#32;text-right\">Betald</td>"
                + "                 </tr>"
                + "                 <tr>"
                + "                     <td colspan=\"2\">"
                + "                         <div><a href=\"/MinSida/Invoice/InvoiceView/67756264\" target=\"_blank\">Visa faktura</a>"
                + "                         </div>"
                + "                     </td>"
                + "                 </tr>"
                + "         </table>"
                + "     </form>"
                + " </div>";

        Double balance = CreditCardParsingUtils.parseBalance(htmlContent);

        Assert.assertEquals(-1819.61, balance, 0.0001);
    }

    @Test
    public void testParseCardNumberFromHtmlContent() throws AccountNotFoundException {
        String htmlContent = "<div class=\"page\">"
                +                 "<h2>Aktiva kreditkort</h2>"
                + "<p>Listan visar aktiva kort. Om kortet inte fungerar på grund av trasigt chip eller magnetremsa kan du kostnadsfritt beställa ett nytt kort. Ditt nuvarande kort är då aktivt i 30 dagar.</p><p>Om ditt kort närmar sig utgångsdatum kommer det automatiskt skickas ut ett nytt kort. Utgångsdatum 0316 betyder att kortet är giltigt till och med 31/3 2016. Du får det nya kortet senast 3 veckor innan sista giltighetsdag.</p>"
                +     "<table class=\"table table--hover table--data\">"
                +         "<thead>"
                +             "<tr>"
                +                 "<th>Kortnummer</th>"
                +                 "<th>Utställt</th>"
                +                 "<th>Utgår</th>"
                +             "</tr>"
                +         "</thead>"
                +             "<tr>"
                +                 "<td>654321******4321</td>"
                +                 "<td>2016-09-21</td>"
                +                 "<td>2019-11-30</td>"
                +             "</tr>"
                +     "</table>"
                +     "<div>"
                +         "<p>OBS. Det tar upp till 24 timmar från att kortet spärrats tills det försvinner från listan ovan.</p>"
                +     "</div>"
                +     "<a href=\"#inactiveCards\" data-toggle=\"collapse\">Visa inaktiva kreditkort</a>"
                +     "<div role=\"menu\" class=\"collapse\" id=\"inactiveCards\">"
                +         "<h2>Inaktiva kreditkort</h2>"
                +         "<table class=\"table table--hover table--data\">"
                +             "<thead>"
                +                 "<tr>"
                +                     "<th>Kortnummer</th>"
                +                     "<th>Utställt</th>"
                +                     "<th>Utgår</th>"
                +                 "</tr>"
                +             "</thead>"
                +                 "<tr>"
                +                     "<td>123456******1234</td>"
                +                     "<td>2015-06-25</td>"
                +                     "<td>2018-06-30</td>"
                +                 "</tr>"
                +         "</table>"
                +     "</div>"
                + "</div>";

        String accountNumber = CreditCardParsingUtils.parseAccountNumber(htmlContent);

        Assert.assertEquals("654321******4321", accountNumber);
    }

    @Test
    public void testParsingOfAccountNumberForTransactions() {
        String htmlContent =  " <script type=\"text/javascript\">"
                +         " var viewModel;"
                +         " $(function () {"
                +             " viewModel = new TransactionViewModel(\"11111111111\","
                +                 " \"/MyPage2/Transaction/GetTransactions\","
                +                 " \"/MinSida/Transactions/TransactionReceipt\","
                +                 " \"/MinSida/Transactions/Statement\","
                +                 " false"
                +             " );"
                +             " ko.applyBindings(viewModel);"
                +             " viewModel.getTransactions();"
                +         " });"
                + " </script>";

        Optional<String> stringOptional = CreditCardParsingUtils.parseTransactionalAccountNumber(htmlContent);
        Assert.assertEquals("11111111111", stringOptional.get());
    }

    @Test(expected=AccountNotFoundException.class)
    public void testMissingAccount() throws AccountNotFoundException {
        String htmlContent = "<div class=\"main-content\">"
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
        String htmlContent =  " <div class=\"page\">"
                + "     <div class=\"box box--light\">"
                + "         <h3 class=\"hidden-small\">Kreditkort</h3>"
                + "         <table class=\"table table--data table--responsive-data\">"
                + "             <thead>"
                + "                 <tr>"
                + "                     <th class=\"text-left visible-xs\"><h3>Kreditkort</h3></th>"
                + "                     <th class=\"text-left hidden-small\">Namn</th>"
                + "                     <th class=\"text-right hidden-small\">Kreditgräns</th>"
                + "                     <th class=\"text-right hidden-small\">Tillgängligt</th>"
                + "                     <th class=\"text-right\"></th>"
                + "                 </tr>"
                + "             </thead>"
                + "             <tbody>"
                + "                 <tr>"
                + "                     <td class=\"text-left\"><a href=\"/MinSida/Creditcard/Overview\">Norwegian-kortet</a></td>"
                + "                     <td class=\"text-right hidden-small tabular\">50 000,00</td>"
                + "                     <td class=\"text-right hidden-small tabular\">45 730,87</td>"
                + "                     <td class=\"text-right\" style=\"width: 90px\">"
                + "                         <div id=\"optionList_712ec16424d14ad9a62b981b9e659dc9\" class=\"option__toggle--parent\"><button type=\"button\" class=\"option__toggle\">Välj</button><div class=\"options__body\"><div class=\"options__content&#32;options__content--left\"><a href=\"/MinSida/Creditcard/Overview\" class=\"options__content--item\">Översikt</a><a href=\"/MinSida/Creditcard/Transactions\" class=\"options__content--item\">Transaktioner</a><a href=\"/MinSida/Creditcard/Invoice\" class=\"options__content--item\">Faktura</a><a href=\"/MinSida/Creditcard/Payment\" class=\"options__content--item\">Betalning</a><a href=\"/MinSida/Invoice/AnnualStatements\" class=\"options__content--item\">Årsbesked</a></div></div><div class=\"options__backdrop\"></div></div>"
                + "                     </td>"
                + "                 </tr>"
                + "                 <tr class=\"hidden-from-medium\">"
                + "                     <td colspan=\"2\" class=\"tabular\"><label>Tillgängligt</label>45 730,87</td>"
                + "                 </tr>"
                + "             </tbody>"
                + "         </table>"
                + "     </div>"
                + "     <div class=\"box box--light\">"
                + "         <h3 class=\"hidden-small\">Sparkonto</h3>"
                + "         <table id=\"savingAccountsTable\" class=\"table table--data table--responsive-data\">"
                + "             <thead>"
                + "                 <tr>"
                + "                     <th class=\"text-left visible-xs\"><h3>Sparkonto</h3></th>"
                + "                     <th class=\"text-left hidden-small\">Namn</th>"
                + "                     <th class=\"text-right hidden-small\" data-sort-method=\"dotsep\">Kontonummer</th>"
                + "                     <th class=\"text-right hidden-small\" data-sort-method=\"number\">Saldo</th>"
                + "                     <th class=\"text-right hidden-small\" data-sort-method=\"number\">Tillgängligt</th>"
                + "                     <th class=\"text-right\" data-sort-method=\"none\"></th>"
                + "                 </tr>"
                + "             </thead>"
                + "             <tbody>"
                + "                 <tr>"
                + "                     <td class=\"text-left\">"
                + "                         <a class=\"uit-savingsaccount\" href=\"/MinSida/SavingsAccount/Overview?accountNumber=11111111111\">Sparkonto</a>"
                + "                     </td>"
                + "                     <td class=\"text-right hidden-small tabular\">10155051002</td>"
                + "                     <td class=\"text-right hidden-small tabular\" data-sort=\"1009.27\">1 009,27</td>"
                + "                     <td class=\"text-right hidden-small tabular\" data-sort=\"1009.27\">1 009,27</td>"
                + "                     <td class=\"text-right\" style=\"width: 90px\">"
                + "                         <div id=\"optionList_32692dde610e414e88614ecc1a422ed5\" class=\"option__toggle--parent\"><button type=\"button\" class=\"option__toggle\">Välj</button><div class=\"options__body\"><div class=\"options__content&#32;options__content--left\"><a href=\"/MinSida/SavingsAccount/Overview?accountNumber=10155051002\" class=\"options__content--item\">Översikt</a><a href=\"/MinSida/SavingsAccount/Transactions?selectedAccount=10155051002\" class=\"options__content--item\">Transaktioner</a><a href=\"/MinSida/SavingsAccount/Payment?accountNumber=10155051002\" class=\"options__content--item\">Överföra</a><a href=\"/MinSida/Invoice/AnnualStatements\" class=\"options__content--item\">Årsbesked</a></div></div><div class=\"options__backdrop\"></div></div>"
                + "                     </td>"
                + "                 </tr>"
                + "                 <tr class=\"hidden-from-medium\" data-sort-method=\"none\">"
                + "                     <td colspan=\"2\" class=\"tabular\"><label>Tillgängligt</label>1 009,27</td>"
                + "                 </tr>"
                + "             </tbody>"
                + "         </table>"
                + "     </div>"
                + "     <h3>Lån</h3>"
                + "     <ul class=\"list--image-list list--image-list-no-border\">"
                + "         <li>"
                + "             <img src=\"/Content/images/icons/medium/arrow-right.svg\">"
                + "             <h4><a href=\"/Lana-Pengar\">Sök lån</a></h4>"
                + "             <span>Våra lån kräver ingen säkerhet och vi frågar inte vad du ska använda pengarna till.</span>"
                + "         </li>"
                + "     </ul>"
                + "     <div class=\"hidden-from-medium space-before\">"
                + "         <ul class=\"pagenav\">"
                + "             <li class=\"pagenav__item\"><a href=\"/MinSida/Loan\" class=\"pagenav__item-link\"><span>Lån</span><i class=\"chevron pagenav__item-chevron\"></i></a></li>"
                + "             <li class=\"pagenav__item\"><a href=\"/MinSida/Creditcard\" class=\"pagenav__item-link\"><span>Kreditkort</span><i class=\"chevron pagenav__item-chevron\"></i></a></li>"
                + "             <li class=\"pagenav__item\"><a href=\"/MinSida/SavingsAccount\" class=\"pagenav__item-link\"><span>Spara</span><i class=\"chevron pagenav__item-chevron\"></i></a></li>"
                + "             <li class=\"pagenav__item\"><a href=\"/MinSida/CashPoints\" class=\"pagenav__item-link\"><span>CashPoints</span><i class=\"chevron pagenav__item-chevron\"></i></a></li>"
                + "             <li class=\"pagenav__item\"><a href=\"/MinSida/Meddelanden\" class=\"pagenav__item-link\"><span>Meddelande</span><i class=\"chevron pagenav__item-chevron\"></i></a></li>"
                + "             <li class=\"pagenav__item\"><a href=\"/MinSida/Settings\" class=\"pagenav__item-link\"><span>Annat</span><i class=\"chevron pagenav__item-chevron\"></i></a></li>"
                + "         </ul>"
                + "     </div>"
                + " </div>";

        Optional<String> accountNumber = SavingsAccountParsingUtils.parseSavingsAccountNumber(htmlContent);
        Assert.assertTrue(accountNumber.isPresent());
        Assert.assertEquals("11111111111", accountNumber.get());
    }

    @Test
    public void testParsingOfSavingsBalance() {
        String htmlContent =  " <div class=\"grid mypage grid--gutters\">"
                + "     <div class=\"grid-u-m-7-24 hidden-small page-container hidden-print\">"
                + "         <nav class=\"sidebar__nav\">"
                + "             <div class=\"leftnav\">"
                + "                 <a href=\"/MinSida\" class=\"sidebar__backlink\"><span class=\"sidebar__backlink-icon icon-small icon-small--arrow-left\"></span></a>"
                + "                 <h2 class=\"sidebar__title\">Spara</h2>"
                + "             </div>"
                + "             <ul class=\"sidebar__menu\">"
                + "                 <li>"
                + "                     <a href=\"/MinSida/SavingsAccount/Overview\" target=\"_self\" class='sidebar__menu-item-link sidebar__menu-item-link--active'><span class=\"sidebar__menu-item-text\">Översikt</span></a>"
                + "                 </li>"
                + "                 <li>"
                + "                     <a href=\"/MinSida/SavingsAccount/Transactions\" target=\"_self\" class='sidebar__menu-item-link '><span class=\"sidebar__menu-item-text\">Transaktioner</span></a>"
                + "                 </li>"
                + "                 <li>"
                + "                     <a href=\"/MinSida/SavingsAccount/Payment\" target=\"_self\" class='sidebar__menu-item-link '><span class=\"sidebar__menu-item-text\">Överföra</span></a>"
                + "                 </li>"
                + "                 <li>"
                + "                     <a href=\"/MinSida/SavingsAccount/AccountStatements\" target=\"_self\" class='sidebar__menu-item-link '><span class=\"sidebar__menu-item-text\">Kontoutdrag </span></a>"
                + "                 </li>"
                + "             </ul>"
                + "         </nav>"
                + "     </div>"
                + "     <div class=\"grid-u-1 grid-u-m-17-24 print-full-width mypage-content-column\">"
                + "         <main role=\"main\" class=\"page-container page-container--mypage\">"
                + "         <div class=\"page-cover page-cover--mypage\">"
                + "             <div class=\"page-content-header\">"
                + "                 <nav class=\"breadcrumbs breadcrumbs--mypage hidden-print\">"
                + "                     <a href=\"/MinSida/SavingsAccount\" class=\"breadcrumbs__link uit-backlink\"><span class=\"icon-small icon-small--arrow-left\"></span> Spara</a>"
                + "                 </nav>"
                + "                 <h1 class=\"page-cover__title\">Översikt</h1>"
                + "             </div>"
                + "             <hr class=\"page-cover__separator\" />"
                + "         </div>"
                + "         <div class=\"page\">"
                + "             <form action=\"/MinSida/SavingsAccount/Overview\" enctype=\"multipart/form-data\" method=\"post\" role=\"form\"><input name=\"__RequestVerificationToken\" type=\"hidden\" value=\"3N4bEugROvZddtHUekdywywsCAi4PoIPi2xSBauWDYaxf5i2aZF9NUCiSSZalRkYnq3zAtcsKTCHoaVFx33cmsOw--VVQUCi1OroOPWmWQamT4lbW8M3Po7o16nuDR9zARz2nw2\" />    <table class=\"data-table\">"
                + "             <tr><td colspan=\"2\"><h2>Bank Norwegian Sparkonto</h2></td></tr>"
                + "             <tr><td colspan=\"2\"><div></div></td></tr>        <tr><td class=\"data-table-label\"><label>Saldo (SEK):</label></td><td class=\"data-table-value&#32;text-right\">1 009,27</td></tr>"
                + "             <tr class=\"uit-amountavailable\"><td class=\"data-table-label\"><label>Tillgängligt belopp (SEK):</label></td><td class=\"data-table-value&#32;text-right\">1 009,27</td></tr>"
                + "             <tr><td class=\"data-table-label\"><label>Upplupen ränta (SEK):</label></td><td class=\"data-table-value&#32;text-right\">1,74</td></tr>"
                + "             <tr><td colspan=\"2\"><hr /></td></tr>"
                + "             <tr><td colspan=\"2\"><h2>Inbetalning</h2></td></tr><tr><td class=\"data-table-label\"><label>Plusgiro för inbetalning:</label></td><td class=\"data-table-value&#32;text-right\">418 76 01-2</td></tr><tr><td class=\"data-table-label\"><label>OCR:</label></td><td class=\"data-table-value&#32;text-right\">1015505100237</td></tr>            </table>"
                + "             <div class=\"row\">"
                + "             </div>"
                + "         </form>"
                + "     </div>"
                + "     </main>"
                + " </div>";

        Double balance = SavingsAccountParsingUtils.parseSavingsAccountBalance(htmlContent);
        Assert.assertEquals(1009.27, balance, 0.5);
    }
}
