package se.tink.backend.common.utils.giro.lookup;

import com.google.common.base.Charsets;
import java.util.Optional;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import se.tink.libraries.account.AccountIdentifier;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

public class PlusGiroCrawlerTest {

    @Test
    public void testFindPlusGiro() {
        testFindWithPrediction("9020900", true);
        testFindWithPrediction("902090-0", true);
        testFindWithPrediction("90-2090-0", true);
        testFindWithPrediction("4-2", true);
        testFindWithPrediction("42", true);
        testFindWithPrediction("123456789", false);
        testFindWithPrediction("1", false);
        testFindWithPrediction("", false);
        testFindWithPrediction(null, false);
    }

    private void testFindWithPrediction(String accountNumber, boolean prediction) {
        Client client = createMockedClient();

        PlusGiroCrawler crawler = new PlusGiroCrawler(client);
        Optional<AccountIdentifier> identifier = crawler.find(accountNumber);

        Assertions.assertThat(identifier.isPresent()).isEqualTo(prediction);
    }

    private Client createMockedClient() {
        WebResource.Builder builder = mock(WebResource.Builder.class);

        WebResource webResource = mock(WebResource.class);
        when(webResource.type(any(MediaType.class))).thenReturn(builder);
        when(builder.accept(any(String.class))).thenReturn(builder);
        when(builder.post(eq(String.class), any(MultivaluedMap.class))).thenReturn(HTML_RESPONSE);

        InputStream stubInputStream =
                IOUtils.toInputStream(HTML_RESPONSE, Charsets.ISO_8859_1);
        ClientResponse response = mock(ClientResponse.class);
        when(response.getEntityInputStream()).thenReturn(stubInputStream);
        when(builder.post(eq(ClientResponse.class), any(MultivaluedMap.class))).thenReturn(response);

        Client client = mock(Client.class);
        when(client.resource(any(String.class)))
                .thenReturn(webResource);

        return client;
    }

    private static final String HTML_RESPONSE = "<html>\n" +
            " <body> \n" +
            "  <br /> \n" +
            "  <!-- Formul�r - START --> \n" +
            "  <form name=\"pgsearch\" method=\"POST\"> \n" +
            "   <input type=\"hidden\" name=\"rowcounter\" value=\"\" /> \n" +
            "   <table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"> \n" +
            "    <tbody>\n" +
            "     <tr> \n" +
            "      <td align=\"left\" valign=\"middle\"><span class=\"bt\">PlusGironummer:</span></td> \n" +
            "      <td> </td> \n" +
            "      <td align=\"left\" colspan=\"1\"> <input type=\"TEXT\" name=\"SO_KTO\" size=\"09\" maxlength=\"09\" value=\"90 20 90-0\" /> </td> \n" +
            "     </tr> \n" +
            "     <tr> \n" +
            "      <td colspan=\"3\"><img src=\"/ku/bin/clear.gif\" width=\"1\" height=\"5\" border=\"0\" /></td> \n" +
            "     </tr> \n" +
            "     <tr> \n" +
            "      <td align=\"left\" valign=\"middle\" nowrap=\"\"><span class=\"bt\">Kontohavare:</span></td> \n" +
            "      <td> </td> \n" +
            "      <td colspan=\"1\"> <input type=\"TEXT\" name=\"ENAMN\" size=\"25\" maxlength=\"35\" value=\"\" id=\"input_text\" /> </td> \n" +
            "     </tr> \n" +
            "     <tr> \n" +
            "      <td colspan=\"3\"><img src=\"/ku/bin/clear.gif\" width=\"1\" height=\"5\" border=\"0\" /></td> \n" +
            "     </tr> \n" +
            "     <tr> \n" +
            "      <td align=\"left\" valign=\"middle\" nowrap=\"\"><span class=\"bt\">Postadress:</span> </td> \n" +
            "      <td> </td> \n" +
            "      <td colspan=\"1\"> <input type=\"TEXT\" name=\"UTDADR\" size=\"25\" maxlength=\"35\" value=\"\" id=\"input_text\" /> </td> \n" +
            "     </tr> \n" +
            "     <tr> \n" +
            "      <td colspan=\"3\"><img src=\"/ku/bin/clear.gif\" width=\"1\" height=\"5\" border=\"0\" /></td> \n" +
            "     </tr> \n" +
            "     <tr> \n" +
            "      <td align=\"left\" valign=\"middle\" nowrap=\"\"><span class=\"bt\">Postort/Land:</span></td> \n" +
            "      <td> </td> \n" +
            "      <td colspan=\"1\"> <input type=\"TEXT\" name=\"ORTLAND\" size=\"25\" maxlength=\"35\" value=\"\" id=\"input_text\" /> </td> \n" +
            "     </tr> \n" +
            "     <tr> \n" +
            "      <td colspan=\"3\"><img src=\"/ku/bin/clear.gif\" width=\"1\" height=\"5\" border=\"0\" /></td> \n" +
            "     </tr> \n" +
            "     <tr>\n" +
            "      <td align=\"left\" valign=\"middle\" nowrap=\"\"><span class=\"bt\">Organisationsnr:</span></td> \n" +
            "      <td> </td> \n" +
            "      <td colspan=\"3\"> <input type=\"TEXT\" name=\"ORGNR\" size=\"25\" maxlength=\"13\" value=\"\" id=\"input_text\" /> </td> \n" +
            "     </tr>\n" +
            "     <tr> \n" +
            "      <td colspan=\"3\"><img src=\"/ku/bin/clear.gif\" width=\"1\" height=\"5\" border=\"0\" /></td> \n" +
            "     </tr> \n" +
            "     <tr> \n" +
            "      <td colspan=\"3\" align=\"right\" valign=\"middle\" id=\"input_text\"> <a href=\"#\" onclick=\"return checkForm(this.form)\"><input type=\"image\" src=\"/ku/bin/visaresu.gif\" alt=\"Visa resultat\" value=\"Skicka\" border=\"0\" name=\"image\" /></a> \n" +
            "       <!--<input type=\"image\" SRC=\"\" BORDER=0 value=\"Send\">--> </td> \n" +
            "     </tr> \n" +
            "    </tbody>\n" +
            "   </table> \n" +
            "   <!-- result table - START --> \n" +
            "   <a name=\"result\"></a> \n" +
            "   <table border=\"0\" cellspacing=\"0\" cellpadding=\"2\"> \n" +
            "    <!-- kontroll om f�rsta g�ng --> \n" +
            "    <!-- inte f�rsta g�ng --> \n" +
            "    <tbody>\n" +
            "     <tr> \n" +
            "      <td colspan=\"1\"><span class=\"in\">Du s&ouml;kte p&aring;:</span> <br /> <span class=\"bt\">PlusGironummer:&nbsp;90 20 90-0</span> </td> \n" +
            "     </tr> \n" +
            "     <tr> \n" +
            "      <td colspan=\"1\"><span class=\"bt\"><b>Resultat </b>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; <a href=\"javascript:nySok();\" title=\"G&ouml;r en ny s&ouml;kning\"> Ny s&ouml;kning</a></span> </td> \n" +
            "     </tr> \n" +
            "     <tr> \n" +
            "      <td align=\"left\" valign=\"middle\" colspan=\"1\" color=\"red\"><span class=\"bt\"> PlusGironummer:&nbsp; 90 20 90-0&nbsp; SEK &nbsp; <b></b> </span> <br /> <span class=\"bt\"> BARNCANCERFONDEN/BARN- BOX 5408 </span> <br /> <span class=\"bt\"> 11484, STOCKHOLM </span> </td> \n" +
            "     </tr> \n" +
            "     <!-- kontroll om f�rsta g�ng - slut--> \n" +
            "    </tbody>\n" +
            "   </table> \n" +
            "   <!-- result table - END --> \n" +
            "   <input type=\"HIDDEN\" name=\"KATKOD\" value=\" \" /> \n" +
            "   <input type=\"HIDDEN\" name=\"SOEK\" value=\" \" /> \n" +
            "   <input type=\"HIDDEN\" name=\"FNAMN\" value=\" \" /> \n" +
            "  </form>   \n" +
            " </body>\n" +
            "</html>";
}
