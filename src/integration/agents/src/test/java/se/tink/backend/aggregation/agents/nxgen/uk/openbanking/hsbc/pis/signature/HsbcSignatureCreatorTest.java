package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.pis.signature;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingFlowFacade;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.jwt.signer.iface.JwtSigner;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.signature.JwtHeaders;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;

public class HsbcSignatureCreatorTest {

    private static final String TRUST_ANCHOR_DOMAIN = "dummy.domain";
    private static final String QSEALC_EXAMPLE =
            "MIIExDCCA6ygAwIBAgIJAK0JmDc/YXWsMA0GCSqGSIb3DQEBBQUAMIGcMQswCQYD"
                    + "VQQGEwJJTjELMAkGA1UECBMCQVAxDDAKBgNVBAcTA0hZRDEZMBcGA1UEChMQUm9j"
                    + "a3dlbGwgY29sbGluczEcMBoGA1UECxMTSW5kaWEgRGVzaWduIENlbnRlcjEOMAwG"
                    + "A1UEAxMFSU1BQ1MxKTAnBgkqhkiG9w0BCQEWGmJyYWphbkBSb2Nrd2VsbGNvbGxp"
                    + "bnMuY29tMB4XDTExMDYxNjE0MTQyM1oXDTEyMDYxNTE0MTQyM1owgZwxCzAJBgNV"
                    + "BAYTAklOMQswCQYDVQQIEwJBUDEMMAoGA1UEBxMDSFlEMRkwFwYDVQQKExBSb2Nr"
                    + "d2VsbCBjb2xsaW5zMRwwGgYDVQQLExNJbmRpYSBEZXNpZ24gQ2VudGVyMQ4wDAYD"
                    + "VQQDEwVJTUFDUzEpMCcGCSqGSIb3DQEJARYaYnJhamFuQFJvY2t3ZWxsY29sbGlu"
                    + "cy5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDfjHgUAsbXQFkF"
                    + "hqv8OTHSzuj+8SKGh49wth3UcH9Nk/YOug7ZvI+tnOcrCZdeG2Ot8Y19Wusf59Y7"
                    + "q61jSbDWt+7u7P0ylWWcQfCE9IHSiJIaKAklMu2qGB8bFSPqDyVJuWSwcSXEb9C2"
                    + "xJsabfgJr6mpfWjCOKd58wFprf0RF58pWHyBqBOiZ2U20PKhq8gPJo/pEpcnXTY0"
                    + "x8bw8LZ3SrrIQZ5WntFKdB7McFKG9yFfEhUamTKOffQ2Y+SDEGVDj3eshF6+Fxgj"
                    + "8plyg3tZPRLSHh5DR42HTc/35LA52BvjRMWYzrs4nf67gf652pgHh0tFMNMTMgZD"
                    + "rpTkyts9AgMBAAGjggEFMIIBATAdBgNVHQ4EFgQUG0cLBjouoJPM8dQzKUQCZYNY"
                    + "y8AwgdEGA1UdIwSByTCBxoAUG0cLBjouoJPM8dQzKUQCZYNYy8ChgaKkgZ8wgZwx"
                    + "CzAJBgNVBAYTAklOMQswCQYDVQQIEwJBUDEMMAoGA1UEBxMDSFlEMRkwFwYDVQQK"
                    + "ExBSb2Nrd2VsbCBjb2xsaW5zMRwwGgYDVQQLExNJbmRpYSBEZXNpZ24gQ2VudGVy"
                    + "MQ4wDAYDVQQDEwVJTUFDUzEpMCcGCSqGSIb3DQEJARYaYnJhamFuQFJvY2t3ZWxs"
                    + "Y29sbGlucy5jb22CCQCtCZg3P2F1rDAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEB"
                    + "BQUAA4IBAQCyYZxEzn7203no9TdhtKDWOFRwzYvY2kZppQ/EpzF+pzh8LdBOebr+"
                    + "DLRXNh2NIFaEVV0brpQTI4eh6b5j7QyF2UmA6+44zmku9LzS9DQVKGLhIleB436K"
                    + "ARoWRqxlEK7TF3TauQfaalGH88ZWoDjqqEP/5oWeQ6pr/RChkCHkBSgq6FfGGSLd"
                    + "ktgFcF0S9U7Ybii/MD+tWMImK8EE3GGgs876yqX/DDhyfW8DfnNZyl35VF/80j/s"
                    + "0Lj3F7Po1zsaRbQlhOK5rzRVQA2qnsa4IcQBuYqBWiB6XojPgu9PpRSL7ure7sj6"
                    + "gRQT0OIU5vXzsmhjqKoZ+dBlh1FpSOX2";
    private HsbcSignatureCreator hsbcSignatureCreator;

    @Before
    public void setUp() {
        final UkOpenBankingFlowFacade ukOpenBankingFlowFacade = mock(UkOpenBankingFlowFacade.class);
        final AgentConfiguration agentConfiguration = mock(AgentConfiguration.class);
        when(ukOpenBankingFlowFacade.getAgentConfiguration()).thenReturn(agentConfiguration);
        when(agentConfiguration.getQsealc()).thenReturn(QSEALC_EXAMPLE);
        final JwtSigner jwtSignerMock = mock(JwtSigner.class);
        hsbcSignatureCreator = new HsbcSignatureCreator(ukOpenBankingFlowFacade);
        Mockito.when(ukOpenBankingFlowFacade.getJwtSinger()).thenReturn(jwtSignerMock);
        hsbcSignatureCreator.setTrustAnchorDomain(TRUST_ANCHOR_DOMAIN);
    }

    @Test
    public void shouldCreateJwtHeaders() {
        // when
        final Map<String, Object> returned = hsbcSignatureCreator.createJwtHeaders();

        // then
        final Set<String> expectedHeaderKeys =
                ImmutableSet.of(
                        JwtHeaders.IAT_KEY_HEADER,
                        JwtHeaders.ISS_KEY_HEADER,
                        JwtHeaders.TAN_KEY_HEADER,
                        JwtHeaders.CRIT_KEY_HEADER);
        assertThat(returned.keySet()).containsExactlyElementsOf(expectedHeaderKeys);
        assertThat(returned).containsKey(JwtHeaders.ISS_KEY_HEADER).isNotNull();
    }
}
