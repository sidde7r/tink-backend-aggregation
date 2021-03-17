package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createSoftwareStatementAssertionWithJwksEndpoint;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration.SoftwareStatementAssertion;

public class TrustAnchorDomainParserTest {

    @Test
    public void shouldGetTrustAnchorOutOf2ndLevelDomain() {
        // given
        final String domain = "dummy.org";
        final SoftwareStatementAssertion softwareStatement =
                createSoftwareStatementAssertionWithJwksEndpoint(domain);

        // when
        final String result = TrustAnchorDomainParser.getTrustAnchorDomain(softwareStatement);

        // then
        assertThat(result).isEqualTo(domain);
    }

    @Test
    public void shouldGetTrustAnchorOutOf3rdLevelDomain() {
        // given
        final String domain = "foo.dummy.org";
        final SoftwareStatementAssertion softwareStatement =
                createSoftwareStatementAssertionWithJwksEndpoint(domain);

        // when
        final String result = TrustAnchorDomainParser.getTrustAnchorDomain(softwareStatement);

        // then
        assertThat(result).isEqualTo(domain);
    }

    @Test
    public void shouldGetTrustAnchorOutOf4thLevelDomain() {
        // given
        final String domain = "foo.bar.dummy.org";
        final SoftwareStatementAssertion softwareStatement =
                createSoftwareStatementAssertionWithJwksEndpoint(domain);

        // when
        final String result = TrustAnchorDomainParser.getTrustAnchorDomain(softwareStatement);

        // then
        final String expected = "bar.dummy.org";
        assertThat(result).isEqualTo(expected);
    }
}
