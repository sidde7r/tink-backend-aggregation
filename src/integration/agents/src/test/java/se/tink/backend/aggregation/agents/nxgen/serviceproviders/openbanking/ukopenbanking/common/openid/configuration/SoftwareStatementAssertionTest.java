package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.configuration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SoftwareStatementAssertionTest {

    @Test
    public void shouldParseSimpleSSA() {
        // given
        final String jsonSSA =
                "eyJhbGciOiJub25lIn0.eyJvcmdfbmFtZSI6IlRpbmtBQiIsInNvZnR3YXJlX2NsaWVudF9uYW1lIjoiVGlua0FCIiwib3JnX2p3a3NfZW5kcG9pbnQiOiJodHRwczovL2Nkbi50aW5rLnNlL2VpZGFzL2p3a3MtcHNzLmpzb24iLCJzb2Z0d2FyZV9yZWRpcmVjdF91cmlzIjpbImh0dHBzOi8vYXBpLnRpbmsuY29tL2FwaS92MS9jcmVkZW50aWFscy90aGlyZC1wYXJ0eS9jYWxsYmFjayJdfQ.Cg";

        // when
        SoftwareStatementAssertion actualSSA = SoftwareStatementAssertion.fromJWTJson(jsonSSA);

        // then
        Assertions.assertThat(actualSSA.getJwksEndpoint())
                .isEqualTo(URL.of("https://cdn.tink.se/eidas/jwks-pss.json"));
        Assertions.assertThat(actualSSA.getOrgId()).isNull();
        Assertions.assertThat(actualSSA.getSoftwareId()).isNull();
    }

    @Test
    public void shouldThrowExceptionWhenJWTPayloadMissing() {
        // given
        final String jsonSSA = "eyJhbGciOiJub25lIn0.Cg";

        // when
        final Throwable thrown =
                catchThrowable(() -> SoftwareStatementAssertion.fromJWTJson(jsonSSA));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasNoCause()
                .hasMessage("JWT is not having 3 parts");
    }

    @Test
    public void shouldThrowExceptionWhenJWTHeaderMissing() {
        // given
        final String jsonSSA =
                "eyJvcmdfbmFtZSI6IlRpbmtBQiIsInNvZnR3YXJlX2NsaWVudF9uYW1lIjoiVGlua0FCIiwib3JnX2p3a3NfZW5kcG9pbnQiOiJodHRwczovL2Nkbi50aW5rLnNlL2VpZGFzL2p3a3MtcHNzLmpzb24iLCJzb2Z0d2FyZV9yZWRpcmVjdF91cmlzIjpbImh0dHBzOi8vYXBpLnRpbmsuY29tL2FwaS92MS9jcmVkZW50aWFscy90aGlyZC1wYXJ0eS9jYWxsYmFjayJdfQ.Cg";

        // when
        final Throwable thrown =
                catchThrowable(() -> SoftwareStatementAssertion.fromJWTJson(jsonSSA));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasNoCause()
                .hasMessage("JWT is not having 3 parts");
    }

    @Test
    public void shouldThrowExceptionWhenJWTSignatureMissing() {
        // given
        final String jsonSSA =
                "eyJhbGciOiJub25lIn0.eyJvcmdfbmFtZSI6IlRpbmtBQiIsInNvZnR3YXJlX2NsaWVudF9uYW1lIjoiVGlua0FCIiwib3JnX2p3a3NfZW5kcG9pbnQiOiJodHRwczovL2Nkbi50aW5rLnNlL2VpZGFzL2p3a3MtcHNzLmpzb24iLCJzb2Z0d2FyZV9yZWRpcmVjdF91cmlzIjpbImh0dHBzOi8vYXBpLnRpbmsuY29tL2FwaS92MS9jcmVkZW50aWFscy90aGlyZC1wYXJ0eS9jYWxsYmFjayJdfQ";

        // when
        final Throwable thrown =
                catchThrowable(() -> SoftwareStatementAssertion.fromJWTJson(jsonSSA));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasNoCause()
                .hasMessage("JWT is not having 3 parts");
    }

    @Test
    public void shouldThrowExceptionWhenJWTPayloadEmpty() {
        // given
        final String jsonSSA = "eyJhbGciOiJub25lIn0..Cg";

        // when
        final Throwable thrown =
                catchThrowable(() -> SoftwareStatementAssertion.fromJWTJson(jsonSSA));

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasNoCause()
                .hasMessage("JWT Payload is empty");
    }
}
