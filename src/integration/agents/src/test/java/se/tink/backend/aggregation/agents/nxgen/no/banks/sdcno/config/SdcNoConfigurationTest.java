package se.tink.backend.aggregation.agents.nxgen.no.banks.sdcno.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;

public class SdcNoConfigurationTest {

    private static final String NETT_BANK_CODE = "1254";
    private static final String NETT_BANK_INDIVIDUAL_URL = "https://cultura.no/";
    private static final String PORTAL_BANK_CODE = "3730";
    private static final String PORTAL_BANK_INDIVIDUAL_URL = "https://sognbank.no/";
    private static final String EIKA_BANK_CODE = "1821";
    private static final String EIKA_INDIVIDUAL_URL = "https://eika.no/";
    private static final Provider NETTBANK_PROVIDER = new Provider();
    private static final Provider PORTALBANK_PROVIDER = new Provider();
    private static final Provider EIKA_PROVIDER = new Provider();

    static {
        NETTBANK_PROVIDER.setPayload(NETT_BANK_CODE);
        PORTALBANK_PROVIDER.setPayload(PORTAL_BANK_CODE);
        EIKA_PROVIDER.setPayload(EIKA_BANK_CODE);
    }

    @Test
    public void testNettBankConfiguration() {
        // given
        SdcNoConfiguration config = new SdcNoConfiguration(NETTBANK_PROVIDER);

        // when
        String baseUrl = config.getBasePageUrl();
        String baseApiUrl = config.getBaseApiUrl();
        String loginUrl = config.getLoginUrl();
        AuthenticationType authType = config.getAuthenticationType();
        String individualUrl = config.getIndividualBaseURL();

        // then
        assertThat(baseUrl).isEqualTo("https://www.nettbankportal.no/" + NETT_BANK_CODE + "/");
        assertThat(baseApiUrl).isEqualTo("https://www.nettbankportal.no/");
        assertThat(loginUrl)
                .isEqualTo(
                        "https://www.nettbankportal.no/"
                                + NETT_BANK_CODE
                                + "/nettbank2/logon/bankidjs/?portletname=bankidloginjs&portletaction=openmobilelogin");
        assertThat(authType).isEqualTo(AuthenticationType.NETTBANK);
        assertThat(individualUrl).isEqualTo(NETT_BANK_INDIVIDUAL_URL);
    }

    @Test
    public void testPortalBankConfiguration() {
        // given
        SdcNoConfiguration config = new SdcNoConfiguration(PORTALBANK_PROVIDER);

        // when
        String baseUrl = config.getBasePageUrl();
        String baseApiUrl = config.getBaseApiUrl();
        String loginUrl = config.getLoginUrl();
        AuthenticationType authType = config.getAuthenticationType();
        String individualUrl = config.getIndividualBaseURL();

        // then
        assertThat(baseUrl).isEqualTo("https://www.portalbank.no/" + PORTAL_BANK_CODE + "/");
        assertThat(baseApiUrl).isEqualTo("https://www.portalbank.no/");
        assertThat(loginUrl)
                .isEqualTo("https://id.portalbank.no/wsl/slogin/Run?n_bank=" + PORTAL_BANK_CODE);
        assertThat(authType).isEqualTo(AuthenticationType.PORTAL);
        assertThat(individualUrl).isEqualTo(PORTAL_BANK_INDIVIDUAL_URL);
    }

    @Test
    public void testEikaBankConfiguration() {
        // given
        SdcNoConfiguration config = new SdcNoConfiguration(EIKA_PROVIDER);

        // when
        String baseUrl = config.getBasePageUrl();
        String baseApiUrl = config.getBaseApiUrl();
        String loginUrl = config.getLoginUrl();
        AuthenticationType authType = config.getAuthenticationType();
        String individualUrl = config.getIndividualBaseURL();

        // then
        assertThat(baseUrl).isEqualTo("https://www.portalbank.no/" + EIKA_BANK_CODE + "/");
        assertThat(baseApiUrl).isEqualTo("https://www.portalbank.no/");
        assertThat(loginUrl)
                .isEqualTo(
                        "https://id.portalbank.no/web-kundeid/webresources/identifiser/eika/0770?returnUrl=https%3a%2f%2feika.no%2flogin%3freturnUrl%3d%2foversikt");
        assertThat(authType).isEqualTo(AuthenticationType.EIKA);
        assertThat(individualUrl).isEqualTo(EIKA_INDIVIDUAL_URL);
    }
}
