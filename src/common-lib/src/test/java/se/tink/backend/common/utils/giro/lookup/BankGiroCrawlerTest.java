package se.tink.backend.common.utils.giro.lookup;

import java.util.Optional;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BankGiroCrawlerTest {

    @Test
    public void testFindBankGiro() {
        testFindWithPrediction("9020900", true);
        testFindWithPrediction("902-0900", true);
        testFindWithPrediction("90-2090-0", true);
        testFindWithPrediction("4-2", false);
        testFindWithPrediction("42", false);
        testFindWithPrediction("123456789", false);
        testFindWithPrediction("1", false);
        testFindWithPrediction("", false);
        testFindWithPrediction(null, false);

    }

    private void testFindWithPrediction(String accountNumber, boolean prediction) {
        Client client = createMockedClient();

        BankGiroCrawler crawler = new BankGiroCrawler(client);
        Optional<AccountIdentifier> identifier = crawler.find(accountNumber);

        Assertions.assertThat(identifier.isPresent()).isEqualTo(prediction);
    }

    private Client createMockedClient() {
        WebResource.Builder builder = mock(WebResource.Builder.class);

        WebResource webResource = mock(WebResource.class);
        when(webResource.accept(any(String.class))).thenReturn(builder);
        when(builder.get(String.class)).thenReturn(HTML_RESPONSE);

        Client client = mock(Client.class);
        when(client.resource(any(String.class)))
                .thenReturn(webResource);

        return client;
    }

    private static final String HTML_RESPONSE = "<html>\n" +
            "<body> \n" +
            "  <div class=\"backdrop\"> \n" +
            "  </div> \n" +
            "  <div class=\"container\"> \n" +
            "   <!--eri-no-index--> \n" +
            "   <header class=\"header\"> \n" +
            "    <div class=\"row\"> \n" +
            "     <div class=\"large-6 columns\"> \n" +
            "      <a class=\"logo\" href=\"/\"> <img src=\"/Content/images/bankgirot-logo.png\" alt=\"\" /></a> \n" +
            "     </div> \n" +
            "     <!-- Module:Global menu --> \n" +
            "     <ul class=\"global-menu inline-list hide-for-print\"> \n" +
            "      <li> <a href=\"/kundservice/#contact\"> Kontakta oss </a> </li> \n" +
            "      <li> <a href=\"/om-bankgirot/\"> Om Bankgirot </a> </li> \n" +
            "      <li class=\"dropdown\"> <a href=\"#\" data-dropdown=\"drop1\">Inloggning E-tj&auml;nster <i class=\"icon-angle-down\"></i> </a> \n" +
            "       <ul id=\"drop1\" class=\"f-dropdown2\" data-dropdown-content=\"\"> \n" +
            "        <li> <a href=\"https://www.bgonline.se/Menu/\">Autogiro Online<i class=\"icon-lock\"></i></a> </li> \n" +
            "        <li> <a href=\"https://www.bgonline.se/Menu/\">Ins&auml;ttningsuppgift via Internet<i class=\"icon-lock\"></i></a> </li> \n" +
            "        <li> <a href=\"/link/5fa68534817e4727a41915fd4e436b47.aspx?id=6710&amp;epslanguage=sv\">BGC Inovoice validering<i class=\"icon-lock\"></i></a> </li> \n" +
            "        <li> <a href=\"https://extranet.bankgirot.se/\">Bankgirots Extran&auml;t<i class=\"icon-lock\"></i></a> </li> \n" +
            "       </ul> </li> \n" +
            "      <li> <a href=\"/en/sok-bg-nr/\">In English</a> </li> \n" +
            "     </ul> \n" +
            "     <!-- /Module:Global menu --> \n" +
            "    </div> \n" +
            "    <!-- Module:Menu & mega menu --> \n" +
            "    <div class=\"row\"> \n" +
            "     <div class=\"large-12 columns\"> \n" +
            "      <nav class=\"top-bar hide-for-print\"> \n" +
            "       <section class=\"top-bar-section clearfix\"> \n" +
            "        <!-- Navigation --> \n" +
            "        <ul class=\"left\" role=\"navigation\"> \n" +
            "         <li class=\"home\"> <a href=\"/\"> <i class=\"icon-home\"></i> <span>Start</span> </a> </li> \n" +
            "         <li> <a href=\"/tjanster/\" data-mega-menu=\"7\" class=\"has-mega-menu\">Tj&auml;nster<i class=\"icon-angle-down\"></i></a> </li> \n" +
            "         <li> <a href=\"/kundservice/\">Kundservice</a> </li> \n" +
            "         <li class=\"active\"> <a href=\"/sok-bg-nr/\">S&ouml;k bankgironummer</a> </li> \n" +
            "        </ul> \n" +
            "        <!-- Search + search button/expand button --> \n" +
            "        <form action=\"/sok/#bgsearchform\"> \n" +
            "         <div class=\"search\"> \n" +
            "          <div class=\"search-field-container\"> \n" +
            "           <label for=\"quicksearch\" class=\"screen-reader-text\">S&ouml;kord</label> \n" +
            "           <input data-default-text=\"S&ouml;kord\" id=\"quicksearch\" type=\"search\" name=\"q\" placeholder=\"S&ouml;kord\" autocomplete=\"off\" class=\"switch-form\" data-switch-regex=\"^[\\d]{3,4}-[\\d]{4}$\" data-switch-target=\"#qsbgnr\" /> \n" +
            "          </div> \n" +
            "         </div> \n" +
            "         <ul class=\"right top-bar-right-nav\"> \n" +
            "          <li> <button id=\"qsbtn\" type=\"submit\" class=\"button search-button\"> <i class=\"icon-search\"></i> S&ouml;k </button> </li> \n" +
            "         </ul> \n" +
            "        </form> \n" +
            "       </section> \n" +
            "       <!-- Search bgnr --> \n" +
            "       <div class=\"top-bar-bgnr\"> \n" +
            "        <form class=\"search main-searchform meta hide-for-print\" action=\"/sok-bg-nr/#bgsearchform\" id=\"topBarSearchAdvanced\"> \n" +
            "         <div class=\"advanced-search-form bg-nr-form\"> \n" +
            "          <div class=\"search-field-container\"> \n" +
            "           <div class=\"row\"> \n" +
            "            <div class=\"col-first columns\"> \n" +
            "             <input id=\"qsbgnr\" name=\"bgnr\" type=\"text\" placeholder=\"Bankgironummer\" /> \n" +
            "            </div> \n" +
            "            <div class=\"col-first columns\"> \n" +
            "             <input name=\"orgnr\" type=\"text\" placeholder=\"Organisationsnummer\" /> \n" +
            "            </div> \n" +
            "            <div class=\"col-first left columns\"> \n" +
            "             <input name=\"company\" type=\"text\" placeholder=\"F&ouml;retag/organisation\" /> \n" +
            "            </div> \n" +
            "            <div class=\"col-last left columns\"> \n" +
            "             <div class=\"search\"> \n" +
            "              <div class=\"city-field-container\"> \n" +
            "               <input name=\"city\" type=\"search\" class=\"city\" placeholder=\"Ort\" /> \n" +
            "              </div> \n" +
            "             </div> \n" +
            "             <button class=\"button search-button right\"> <i class=\"icon-search\"></i> S&ouml;k </button> \n" +
            "            </div> \n" +
            "           </div> \n" +
            "          </div> \n" +
            "         </div> \n" +
            "        </form> \n" +
            "       </div> \n" +
            "      </nav> \n" +
            "      <!-- One for each topitem with children --> \n" +
            "      <nav class=\"mega-menu\" id=\"7\" role=\"navigation\"> \n" +
            "       <div class=\"mega-menu-inner clearfix\"> \n" +
            "        <ul class=\"large-3 columns submenu\"> \n" +
            "         <li> <a href=\"/tjanster/\">&Ouml;versikt</a> </li> \n" +
            "         <li> <a href=\"/tjanster/utbetalningar/\">Utbetalningar</a> \n" +
            "          <ul> \n" +
            "           <li> <a href=\"/tjanster/utbetalningar/manuella-betalningar/\">Manuella betalningar</a> </li> \n" +
            "           <li> <a href=\"/tjanster/utbetalningar/leverantorsbetalningar/\">Leverant&ouml;rsbetalningar</a> </li> \n" +
            "           <li> <a href=\"/tjanster/utbetalningar/loner/\">L&ouml;ner</a> </li> \n" +
            "          </ul> </li> \n" +
            "         <li> <a href=\"/tjanster/ovriga/\">&Ouml;vriga</a> \n" +
            "          <ul> \n" +
            "           <li> <a href=\"/tjanster/ovriga/swiftnet/\">Bg Kommunikationstj&auml;nst f&ouml;r Swiftnet</a> </li> \n" +
            "           <li> <a href=\"/tjanster/ovriga/skatteoverforing/\">&Ouml;verskjutande skatt</a> </li> \n" +
            "           <li> <a href=\"/tjanster/ovriga/skattefullmakt/\">Skattefullmakt</a> </li> \n" +
            "          </ul> </li> \n" +
            "        </ul> \n" +
            "        <ul class=\"large-3 columns submenu\"> \n" +
            "         <li> <a href=\"/tjanster/autogiro/Autogiro/\">Autogiro</a> \n" +
            "          <ul> \n" +
            "           <li> <a href=\"/tjanster/autogiro/Autogiro/\">Bg Autogiro</a> </li> \n" +
            "          </ul> </li> \n" +
            "         <li> <a href=\"/tjanster/fakturatjanster/\">Fakturatj&auml;nster</a> \n" +
            "          <ul> \n" +
            "           <li> <a href=\"/tjanster/fakturatjanster/e-faktura-till-internetbank/\">E-faktura till Internetbank</a> </li> \n" +
            "           <li> <a href=\"/tjanster/fakturatjanster/e-faktura-foretag/\">E-faktura F&ouml;retag</a> </li> \n" +
            "           <li> <a href=\"/tjanster/fakturatjanster/scanning-solution/\">Scanning Solution</a> </li> \n" +
            "          </ul> </li> \n" +
            "        </ul> \n" +
            "        <ul class=\"large-3 columns submenu\"> \n" +
            "         <li> <a href=\"/tjanster/clearing-och-avveckling/clearing-och-avveckling/\">Clearing och Avveckling</a> \n" +
            "          <ul> \n" +
            "           <li> <a href=\"/tjanster/clearing-och-avveckling/clearing-och-avveckling/\">Clearing och Avveckling</a> </li> \n" +
            "          </ul> </li> \n" +
            "         <li> <a href=\"/tjanster/e-identifiering/\">E-identifiering</a> \n" +
            "          <ul> \n" +
            "           <li> <a href=\"/tjanster/e-identifiering/pki-services/\">Bg PKI Services</a> </li> \n" +
            "          </ul> </li> \n" +
            "        </ul> \n" +
            "        <ul class=\"large-3 columns submenu\"> \n" +
            "         <li> <a href=\"/tjanster/inbetalningar/\">Inbetalningar</a> \n" +
            "          <ul> \n" +
            "           <li> <a href=\"/tjanster/inbetalningar/bankgironummer/\">Bankgironummer</a> </li> \n" +
            "           <li> <a href=\"/tjanster/inbetalningar/insattningsuppgift-via-internet/\">Ins&auml;ttningsuppgift via Internet</a> </li> \n" +
            "           <li> <a href=\"/tjanster/inbetalningar/bankgiro-inbetalningar/\">Bankgiro Inbetalningar</a> </li> \n" +
            "          </ul> </li> \n" +
            "         <li> <a href=\"/tjanster/kommunikation/\">Kommunikationsl&ouml;sningar</a> \n" +
            "          <ul> \n" +
            "           <li> <a href=\"/tjanster/kommunikation/bankgiro-link/\">Bankgiro Link</a> </li> \n" +
            "           <li> <a href=\"/tjanster/kommunikation/ftp-kommunikation/\">FTP kommunikation</a> </li> \n" +
            "           <li> <a href=\"/tjanster/kommunikation/forandringsskydd/\">F&ouml;r&auml;ndringsskydd</a> </li> \n" +
            "          </ul> </li> \n" +
            "        </ul> \n" +
            "       </div> \n" +
            "       <a href=\"#\" class=\"close-mega-menu\"><i class=\"icon-cancel\"></i></a> \n" +
            "      </nav> \n" +
            "      <nav class=\"mega-menu\" id=\"2252\" role=\"navigation\"> \n" +
            "       <div class=\"mega-menu-inner clearfix\"> \n" +
            "        <ul class=\"large-3 columns submenu\"> \n" +
            "         <li> <a href=\"/sok-bg-nr/\">&Ouml;versikt</a> </li> \n" +
            "        </ul> \n" +
            "       </div> \n" +
            "       <a href=\"#\" class=\"close-mega-menu\"><i class=\"icon-cancel\"></i></a> \n" +
            "      </nav> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "    <!-- /Module:Menu & mega menu --> \n" +
            "    <div class=\"row\"> \n" +
            "     <nav class=\"large-12 columns\"> \n" +
            "      <ul class=\"breadcrumbs\"> \n" +
            "       <li> <a href=\"/\">Start</a> </li> \n" +
            "       <li class=\"current\"> <a>S&ouml;k bankgironummer</a> </li> \n" +
            "      </ul> \n" +
            "     </nav> \n" +
            "    </div> \n" +
            "   </header> \n" +
            "   <!--/eri-no-index--> \n" +
            "   <section class=\"row maincontent\"> \n" +
            "    <div class=\"large-12 columns\"> \n" +
            "     <header class=\"page-header\"> \n" +
            "      <h1>S&ouml;k bankgironummer</h1> \n" +
            "     </header> \n" +
            "     <ul class=\"tabs hide-for-print stand-alone\" id=\"bgsearchform\"> \n" +
            "      <li><a href=\"/sok/\">Webbplats</a></li> \n" +
            "      <li class=\"active\"><a href=\"/sok-bg-nr/\">Bankgironummer</a></li> \n" +
            "     </ul> \n" +
            "     <div class=\"clearfix\"> \n" +
            "      <div class=\"row\"> \n" +
            "       <div class=\"large-12 columns\"> \n" +
            "        <!-- Main search form --> \n" +
            "        <form action=\"/sok-bg-nr/#bgsearchform\" method=\"get\" class=\"search main-searchform meta hide-for-print\"> \n" +
            "         <div class=\"advanced-search-form bg-nr-form\"> \n" +
            "          <div class=\"search-field-container\"> \n" +
            "           <div class=\"row\"> \n" +
            "            <div class=\"large-3 medium-6 columns\"> \n" +
            "             <input id=\"bgnr\" name=\"bgnr\" placeholder=\"Bankgironummer\" type=\"text\" value=\"9020900\" /> \n" +
            "            </div> \n" +
            "            <div class=\"large-3 medium-6 columns\"> \n" +
            "             <input id=\"orgnr\" name=\"orgnr\" placeholder=\"Organisationsnummer\" type=\"text\" value=\"\" /> \n" +
            "            </div> \n" +
            "            <div class=\"large-2 medium-6 left columns\"> \n" +
            "             <input id=\"company\" name=\"company\" placeholder=\"F&ouml;retag/organisation\" type=\"text\" value=\"\" /> \n" +
            "            </div> \n" +
            "            <div class=\"large-2 medium-6 left columns\"> \n" +
            "             <input id=\"city\" name=\"city\" placeholder=\"Ort\" type=\"text\" value=\"\" /> \n" +
            "            </div> \n" +
            "            <div class=\"large-2 medium-12 right columns\"> \n" +
            "             <button type=\"submit\" class=\"button search-button right\"> <i class=\"icon-search\"></i> S&ouml;k </button> \n" +
            "            </div> \n" +
            "           </div> \n" +
            "          </div> \n" +
            "          <div class=\"search-field-container search-field-footer\"> \n" +
            "           <div class=\"row\"> \n" +
            "            <div class=\"large-12 columns\"> \n" +
            "             <a href=\"javascript:void(0)\" class=\"right clear-filters-bg\"><i class=\"icon-cancel\"></i>Rensa</a> \n" +
            "            </div> \n" +
            "           </div> \n" +
            "          </div> \n" +
            "         </div> \n" +
            "        </form> \n" +
            "        <!-- /Main search form --> \n" +
            "        <!-- Results bar bankgirot (total hits) --> \n" +
            "        <div class=\"results-bar bar meta\">\n" +
            "          Bankgironummer \n" +
            "         <span class=\"num-of-hits\"> (1) </span> \n" +
            "         <span class=\"show-current-count right\"> Visar <span id=\"currentCount\">1</span> av 1 resultat </span> \n" +
            "        </div> \n" +
            "        <!-- /Main search form --> \n" +
            "        <!-- Results bar bankgirot (total hits) --> \n" +
            "        <ol class=\"search-result clearfix bgnr-results\"> \n" +
            "         <li class=\"large-12 columns\"> \n" +
            "          <div class=\"row result-container\"> \n" +
            "           <div class=\"large-12\"> \n" +
            "            <h3 class=\"title meta\">BARNCANCERFONDEN/ </h3> \n" +
            "           </div> \n" +
            "           <div class=\"large-3\"> \n" +
            "            <ul class=\"meta\"> \n" +
            "             <li class=\"subtitle\">Adress</li> \n" +
            "             <li>BARNCANCERF&Ouml;RENINGARNAS RIKSF&Ouml;RBUND</li> \n" +
            "             <li>BOX 5408 </li> \n" +
            "             <li>11484 STOCKHOLM </li> \n" +
            "            </ul> \n" +
            "           </div> \n" +
            "           <div class=\"large-3\"> \n" +
            "            <ul class=\"meta\"> \n" +
            "             <li class=\"subtitle\">Organisationsnummer</li> \n" +
            "             <li>8020106566</li> \n" +
            "            </ul> \n" +
            "           </div> \n" +
            "           <div class=\"large-3\"> \n" +
            "            <ul class=\"meta\"> \n" +
            "             <li class=\"subtitle\">Bankgironummer</li> \n" +
            "             <li>902-0900</li> \n" +
            "            </ul> \n" +
            "           </div> \n" +
            "          </div> </li> \n" +
            "        </ol> \n" +
            "        <!-- Did you not find what you are looking for - foldout --> \n" +
            "        <div class=\"advanced-search-fold-out-container clearfix\"> \n" +
            "         <div class=\"large-12 columns \"> \n" +
            "          <ul class=\"right\"> \n" +
            "           <li><a class=\"advanced-search fold-out\" href=\"#bg-info\">Hittar du inte det du s&ouml;ker?<i class=\"icon-angle-down\"></i></a></li> \n" +
            "          </ul> \n" +
            "         </div> \n" +
            "         <div id=\"bg-info\" class=\"fold-down hidden large-12 columns\"> \n" +
            "          <div class=\"search-field-container hidden\"> \n" +
            "           <div class=\"row\"> \n" +
            "            <div class=\"large-7 columns\"> \n" +
            "             <h3>Hittar du inte det du s&ouml;ker?</h3> \n" +
            "             <p>Det kan bero p&aring; flera saker.&nbsp;</p> \n" +
            "             <h3>B&ouml;rja med att kontrollera:&nbsp;</h3> \n" +
            "             <ul> \n" +
            "              <li>Du fyllt i r&auml;tt information i r&auml;tt f&auml;lt.</li> \n" +
            "              <li>Din stavning &auml;r korrekt.</li> \n" +
            "              <li>Det finns inga on&ouml;diga mellanslag.</li> \n" +
            "              <li>Bankgironumret m&aring;ste alltid skrivas med bindestreck. Inga mellanslag varken i eller efter Bankgironumret.&nbsp;</li> \n" +
            "             </ul> \n" +
            "             <h3>Det kan ocks&aring; bero p&aring; f&ouml;ljande:&nbsp;</h3> \n" +
            "             <ul> \n" +
            "              <li>Det organisations- eller bankgironummer du angivit finns inte eller &auml;r felaktigt inskrivet.</li> \n" +
            "              <li>Informationen har skrivits in i fel f&auml;lt.</li> \n" +
            "              <li>Angivet nummer &auml;r inte ett inbetalningsnummer.</li> \n" +
            "              <li>Bankgironummer knutna till personnummer, enskild firma eller sekretessbelagda uppgifter visas inte i s&ouml;ksvar.<br /><br /></li> \n" +
            "             </ul> \n" +
            "             <p>&nbsp;</p> \n" +
            "             <p>&nbsp;</p> \n" +
            "            </div> \n" +
            "           </div> \n" +
            "          </div> \n" +
            "         </div> \n" +
            "        </div> \n" +
            "       </div> \n" +
            "      </div> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "   </section> \n" +
            "  </div> \n" +
            "  <!--eri-no-index--> \n" +
            "  <!-- Module: Footer --> \n" +
            "  <footer class=\"footer\"> \n" +
            "   <!-- Module: Footer nav --> \n" +
            "   <div class=\"row air hide-for-print\"> \n" +
            "    <div class=\"large-3 columns\"> \n" +
            "     <h3>Popul&auml;ra sidor</h3> \n" +
            "     <ul class=\"side-nav\"> \n" +
            "      <li><a href=\"/om-bankgirot/press-och-aktuellt/nyheter/\">Aktuellt</a></li> \n" +
            "      <li><a href=\"/kundservice/programtorget/\">Programtorget</a></li> \n" +
            "      <li><a href=\"/om-bankgirot/press-och-aktuellt/bankgirots-webb/\">Nya bankgirot.se</a></li> \n" +
            "      <li><a href=\"/kundservice/kontakta-din-bank-i-dessa-arenden/\" target=\"_blank\">Adress&auml;ndra i Bankgironummer</a></li> \n" +
            "     </ul> \n" +
            "    </div> \n" +
            "    <div class=\"large-3 columns\"> \n" +
            "     <h3>Tj&auml;nster</h3> \n" +
            "     <ul class=\"side-nav\"> \n" +
            "      <li><a href=\"/tjanster/inbetalningar/\">Inbetalningar</a></li> \n" +
            "      <li><a href=\"/tjanster/utbetalningar/\">Utbetalningar</a></li> \n" +
            "      <li><a href=\"/tjanster/fakturatjanster/\">Fakturatj&auml;nster</a></li> \n" +
            "      <li><a href=\"/tjanster/e-identifiering/\">E-identifiering</a></li> \n" +
            "     </ul> \n" +
            "    </div> \n" +
            "    <div class=\"large-3 columns\"> \n" +
            "     <h3>Kundservice</h3> \n" +
            "     <ul class=\"side-nav\"> \n" +
            "      <li><a href=\"/kundservice/\">Kontakta kundservice och support</a></li> \n" +
            "      <li><a href=\"/kundservice/e-formular/\">E-formul&auml;r</a></li> \n" +
            "      <li><a href=\"/kundservice/vanliga-fragor/\">Vanliga fr&aring;gor</a></li> \n" +
            "      <li><a href=\"/kundservice/programtorget/\">Programtorget</a></li> \n" +
            "     </ul> \n" +
            "    </div> \n" +
            "    <div class=\"large-3 columns\"> \n" +
            "     <h3>Om Bankgirot</h3> \n" +
            "     <ul class=\"side-nav\"> \n" +
            "      <li><a href=\"/om-bankgirot/kontakta-bankgirot/\">Kontakta Bankgirot</a></li> \n" +
            "      <li><a href=\"/om-bankgirot/jobba-pa-bankgirot/\">Jobba p&aring; Bankgirot</a></li> \n" +
            "      <li><a href=\"/om-bankgirot/press-och-aktuellt/grafisk-profil-och-logotyp/\">Grafisk profil och logotyp</a></li> \n" +
            "      <li><a href=\"/om-bankgirot/for-programvaruleverantorer-och-utvecklare/\">Programvaruleverant&ouml;r och utvecklare</a></li> \n" +
            "     </ul> \n" +
            "    </div> \n" +
            "   </div> \n" +
            "   <!-- /Module: Footer nav --> \n" +
            "   <!-- Module: Integrety menu --> \n" +
            "   <div class=\"lighter air\"> \n" +
            "    <div class=\"row \"> \n" +
            "     <div class=\"nine large-9 columns\"> \n" +
            "      <ul class=\"inline-list\"> \n" +
            "       <li> Bankgirot, 105 19 Stockholm, 08 – 725 60 00 </li> \n" +
            "       <li class=\"hide-for-print\">|</li> \n" +
            "       <li class=\"hide-for-print\"><a href=\"/om-bankgirot/kontakta-bankgirot/\">Kontakta Bankgirot</a></li> \n" +
            "       <li class=\"hide-for-print\">|</li> \n" +
            "       <li class=\"hide-for-print\"><a href=\"/om-cookies-pa-bankgirot.se/\">Om cookies p&aring; bankgirot.se</a></li> \n" +
            "      </ul> \n" +
            "     </div> \n" +
            "     <div class=\"large-3 columns hide-for-print\"> \n" +
            "      <img class=\"footer-logo\" src=\"/Content/images/bankgirot-logo-white.png\" alt=\"\" /> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "   </div> \n" +
            "   <!-- /Module: Integrety menu --> \n" +
            "  </footer> \n" +
            "  <!-- /Module: Footer --> \n" +
            "  <!--/eri-no-index--> \n" +
            "  <!--eri-no-index--> \n" +
            "  <div id=\"cookiealert\" class=\"cookie hide-for-print\"> \n" +
            "   <div class=\"row\"> \n" +
            "    <p class=\"left\">Vi anv&auml;nder cookies f&ouml;r att v&aring;ra webbaserade tj&auml;nster skall fungera. Ingen personlig information sparas.</p> \n" +
            "    <form action=\"/cookie/accept?returnUrl=%2Fsok-bg-nr%2F\" data-ajax=\"true\" id=\"form0\" method=\"post\">\n" +
            "     <input name=\"__RequestVerificationToken\" type=\"hidden\" value=\"VKUGJrTUTYuC0y29BOZjD8dgOkB2ot44mDwRyYyC92Ea_1qyxZZBgZ43fIY7c_M1SHJzwW7zxrAFn3bs5ahqX0N8JRel9hz216YHsimnlKKDS4BHpur7A-X3lT1zYE80WxlGdw2\" /> \n" +
            "     <button type=\"submit\" class=\"button right radius\">Jag f&ouml;rst&aring;r, st&auml;ng detta meddelande</button> \n" +
            "    </form> \n" +
            "   </div> \n" +
            "  </div> \n" +
            " </body>\n" +
            "</html>";
}
