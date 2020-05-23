package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.fetcher.account;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.Test;
import se.tink.libraries.pair.Pair;

public class AccountIdPairsTest {

    private AccountIdPairs parser;

    @Test
    public void extractAccountIdPairsWhenWebPageHasNoAccountIdAnchors() {
        // given
        String webpage = "<html><body><a href=\"#\">no account ids</a></body></html>";
        // and
        parser = new AccountIdPairs(webpage);

        // when
        Set<Pair<String, String>> result = parser.extractAll();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void extractAccountIdPairsWhenWebPageHasMultipleAccountIdAnchors() {
        // given
        String webpage =
                "<html><body>"
                        + "<a class=\"list__anchor\" data-id=\"1-account-id\" data-idkey=\"1-account-idkey\">1st account id</a>"
                        + "<a class=\"list__anchor\" data-id=\"2-account-id\" data-idkey=\"2-account-idkey\">2nd account id</a>"
                        + "</body></html>";
        // and
        parser = new AccountIdPairs(webpage);

        // when
        Set<Pair<String, String>> result = parser.extractAll();

        // then
        assertThat(result)
                .containsOnly(
                        new Pair<>("1-account-id", "1-account-idkey"),
                        new Pair<>("2-account-id", "2-account-idkey"));
    }

    @Test
    public void extractAccountIdPairsWhenWebPageHasNoAnchorWithListAnchorClass() {
        // given
        String webpage =
                "<html><body>"
                        + "<a data-id=\"1-account-id\" data-idkey=\"1-account-idkey\">1st account id</a>"
                        + "<a data-id=\"2-account-id\" data-idkey=\"2-account-idkey\">2nd account id</a>"
                        + "</body></html>";
        // and
        parser = new AccountIdPairs(webpage);

        // when
        Set<Pair<String, String>> result = parser.extractAll();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void extractAccountIdPairsWhenWebPageHasNoAnchorWithDataIdAttr() {
        // given
        String webpage =
                "<html><body>"
                        + "<a class=\"list__anchor\" data-idkey=\"1-account-idkey\">1st account id</a>"
                        + "<a class=\"list__anchor\" data-idkey=\"2-account-idkey\">2nd account id</a>"
                        + "</body></html>";
        // and
        parser = new AccountIdPairs(webpage);

        // when
        Set<Pair<String, String>> result = parser.extractAll();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    public void extractAccountIdPairsWhenWebPageHasNoAnchorWithDataIdKeyAttr() {
        // given
        String webpage =
                "<html><body>"
                        + "<a class=\"list__anchor\" data-id=\"1-account-id\">1st account id</a>"
                        + "<a class=\"list__anchor\" data-id=\"2-account-id\">2nd account id</a>"
                        + "</body></html>";
        // and
        parser = new AccountIdPairs(webpage);

        // when
        Set<Pair<String, String>> result = parser.extractAll();

        // then
        assertThat(result).isEmpty();
    }
}
