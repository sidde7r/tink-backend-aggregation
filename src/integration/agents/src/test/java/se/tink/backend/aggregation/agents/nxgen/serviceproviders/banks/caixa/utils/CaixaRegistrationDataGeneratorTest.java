package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.caixa.utils;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class CaixaRegistrationDataGeneratorTest {

    private static final String APP_NAME = "es.lacaixa.mobile.imaginBank_iPhone";
    private static final String APP_UUID = "E9606BD1-D916-4CB0-A6D6-A7AA1176A177";
    private static final String IDENTIFIER_ID = "mXLzxVM9zY2W5vsh4r8x7DJ9JIMp";
    private static final String USERNAME = "01234678";
    private static final String USER_AGENT =
            "IMAGINBANK_eIAPPLh10,4RFTRs-LgSBHFoVN30ZFEYP46uF4H_IPHONE_0.0.0_Apple_iPhone10,4_14.4.2_ADAM";
    private static final String APP_INSTALLATION_ID = "eIAPPLh10,4RFTRs-LgSBHFoVN30ZFEYP46uF4H";
    private static final String DUMMY_PREFIX = "IMAGINBANK_";
    private static final String DUMMY_APP_VERSION = "0.0.0";

    @Test
    public void generateEncodedIdentifier() {
        // when
        String result =
                CaixaRegistrationDataGenerator.generateEncodedIdentifier(APP_NAME + APP_UUID);

        // then
        assertThat(result).isEqualTo(IDENTIFIER_ID);
    }

    @Test
    public void generateUserAgent() {
        // when
        String result =
                CaixaRegistrationDataGenerator.generateUserAgent(
                        USERNAME, false, DUMMY_PREFIX, DUMMY_APP_VERSION);
        // then
        assertThat(result).isEqualTo(USER_AGENT);
    }

    @Test
    public void generateAppInstallationId() {
        // when
        String result = CaixaRegistrationDataGenerator.generateAppInstallationId(USERNAME, false);
        // then
        assertThat(result).isEqualTo(APP_INSTALLATION_ID);
    }
}
