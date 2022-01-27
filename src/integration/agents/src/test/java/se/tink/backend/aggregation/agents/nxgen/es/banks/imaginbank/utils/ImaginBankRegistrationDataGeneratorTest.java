package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.utils;

import static org.assertj.core.api.Assertions.assertThat;

import junitparams.JUnitParamsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class ImaginBankRegistrationDataGeneratorTest {

    private static final String APP_NAME = "es.lacaixa.mobile.imaginBank_iPhone";
    private static final String APP_UUID = "E9606BD1-D916-4CB0-A6D6-A7AA1176A177";
    private static final String IDENTIFIER_ID = "mXLzxVM9zY2W5vsh4r8x7DJ9JIMp";
    private static final String USERNAME = "01234678";
    private static final String USER_AGENT =
            "IMAGINBANK_eIAPPLh10,4RFTRs-LgSBHFoVN30ZFEYP46uF4H_IPHONE_4.13.2_Apple_iPhone10,4_14.4.2_ADAM";
    private static final String APP_INSTALLATION_ID = "eIAPPLh10,4RFTRs-LgSBHFoVN30ZFEYP46uF4H";

    @Test
    public void generateEncodedIdentifier() {
        // when
        String result =
                ImaginBankRegistrationDataGenerator.generateEncodedIdentifier(APP_NAME + APP_UUID);

        // then
        assertThat(result).isEqualTo(IDENTIFIER_ID);
    }

    @Test
    public void generateUserAgent() {
        // when
        String result = ImaginBankRegistrationDataGenerator.generateUserAgent(USERNAME, false);
        // then
        assertThat(result).isEqualTo(USER_AGENT);
    }

    @Test
    public void generateAppInstallationId() {
        // when
        String result =
                ImaginBankRegistrationDataGenerator.generateAppInstallationId(USERNAME, false);
        // then
        assertThat(result).isEqualTo(APP_INSTALLATION_ID);
    }
}
