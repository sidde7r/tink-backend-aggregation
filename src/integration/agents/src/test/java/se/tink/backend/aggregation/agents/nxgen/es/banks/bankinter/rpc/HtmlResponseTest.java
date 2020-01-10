package se.tink.backend.aggregation.agents.nxgen.es.banks.bankinter.rpc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HtmlResponseTest {
    private static String HTML_WITH_DASHES =
            "<html>test <!--\ncomment with -- dashes and \\1 --- \\2 ---- - --\\0 $1\n-->\ndashes -- outside comment\n<!--\ncomment $3 without dashes\n-->\nsomething -- else\n</html>";
    private static String HTML_WITH_DASHES_REMOVED =
            "<html>test <!--\ncomment with __ dashes and \\1 __- \\2 ____ - __\\0 $1\n-->\ndashes -- outside comment\n<!--\ncomment $3 without dashes\n-->\nsomething -- else\n</html>";

    @Test
    public void testDoubleDashesRemovedFromComments() {
        assertEquals(
                HTML_WITH_DASHES_REMOVED,
                HtmlResponse.removeDoubleDashesFromComments(HTML_WITH_DASHES));
    }

    @Test
    public void testParseAmount() {
        final HtmlResponse response = new HtmlResponse("<html/>");
        assertEquals(1337.42, response.parseAmount("1.337,42").getDoubleValue(), 0.001);
        assertEquals(1337.42, response.parseAmount("1.337,42 €").getDoubleValue(), 0.001);
        assertEquals(1337.42, response.parseAmount("1.337,42 EUROS").getDoubleValue(), 0.001);
        assertEquals(1337.42, response.parseAmount("1.337,42 ").getDoubleValue(), 0.001);
    }
}
