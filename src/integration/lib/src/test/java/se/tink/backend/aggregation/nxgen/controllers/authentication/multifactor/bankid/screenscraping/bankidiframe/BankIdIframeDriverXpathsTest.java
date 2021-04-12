package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.bankidiframe;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.screenscraping.WebScrapingConstants.Xpath;
import se.tink.integration.webdriver.ChromeDriverInitializer;

@RunWith(JUnitParamsRunner.class)
public class BankIdIframeDriverXpathsTest {

    private static final String BASE_PATH =
            "src/integration/lib/src/test/java/se/tink/backend/aggregation/nxgen/controllers/authentication/multifactor/bankid/screenscraping/resources";
    private static final String FODELSNUMBER =
            Paths.get(BASE_PATH, "fodelsnumber.html").toUri().toString();
    private static final String BANK_ID_APP_WITHOUT_PASSWORD =
            Paths.get(BASE_PATH, "bankIdAppWithoutPassword.html").toUri().toString();
    private static final String BANK_ID_APP_WITH_PASSWORD =
            Paths.get(BASE_PATH, "bankIdAppWithPassword.html").toUri().toString();
    private static final String DNB_APPLICATION =
            Paths.get(BASE_PATH, "dnbAuthentication.html").toUri().toString();
    private static final String MOBILE_BANKID =
            Paths.get(BASE_PATH, "mobile_bankid.html").toUri().toString();
    private static final String PASSWORD_AFTER_BANKID_IS_ACCEPTED =
            Paths.get(BASE_PATH, "passwordAfterBankIdAppAccepted.html").toUri().toString();
    private static final String REFERENCE_WORDS =
            Paths.get(BASE_PATH, "reference_words.html").toUri().toString();
    private static final String SPAREBANK_CODE_GENERATOR =
            Paths.get(BASE_PATH, "sparebank_code_generator.html").toUri().toString();

    private WebDriver driver;

    @Before
    public void setup() {
        driver =
                ChromeDriverInitializer.constructChromeDriver(
                        DanskeBankConstants.Javascript.USER_AGENT);
    }

    @Test
    @Parameters(method = "elementsThatShouldBeFound")
    public void shouldFindElements(String url, List<By> xpaths) {
        // given
        driver.get(url);

        // when & then
        for (By xpath : xpaths) {
            assertThat(driver.findElements(xpath)).hasSize(1);
        }
    }

    @Test
    @Parameters(method = "elementsThatShouldNotBeFound")
    public void shouldNotFindElements(String url, List<By> xpaths) {
        // given
        driver.get(url);

        // when & then
        for (By xpath : xpaths) {
            assertThat(driver.findElements(xpath)).isEmpty();
        }
    }

    private Object[] elementsThatShouldBeFound() {
        return new Object[] {
            new Object[] {FODELSNUMBER, Arrays.asList(Xpath.USERNAME_XPATH, Xpath.FORM_XPATH)},
            new Object[] {
                BANK_ID_APP_WITHOUT_PASSWORD,
                Collections.singletonList(Xpath.BANK_ID_APP_TITLE_XPATH)
            },
            new Object[] {
                BANK_ID_APP_WITH_PASSWORD,
                Arrays.asList(Xpath.BANK_ID_PASSWORD_INPUT_XPATH, Xpath.BANK_ID_APP_TITLE_XPATH)
            },
            new Object[] {
                DNB_APPLICATION, Collections.singletonList(Xpath.AUTHENTICATION_LIST_BUTTON_XPATH)
            },
            new Object[] {
                MOBILE_BANKID, Arrays.asList(Xpath.MOBILE_BANK_ID_INPUT_XPATH, Xpath.FORM_XPATH)
            },
            new Object[] {
                PASSWORD_AFTER_BANKID_IS_ACCEPTED,
                Collections.singletonList(Xpath.BANK_ID_PASSWORD_INPUT_XPATH)
            },
            new Object[] {REFERENCE_WORDS, Collections.singletonList(Xpath.REFERENCE_WORDS_XPATH)},
            new Object[] {
                SPAREBANK_CODE_GENERATOR,
                Collections.singletonList(Xpath.AUTHENTICATION_LIST_BUTTON_XPATH)
            }
        };
    }

    private Object[] elementsThatShouldNotBeFound() {
        return new Object[] {
            new Object[] {
                BANK_ID_APP_WITHOUT_PASSWORD,
                Arrays.asList(Xpath.MOBILE_BANK_ID_INPUT_XPATH, Xpath.BANK_ID_PASSWORD_INPUT_XPATH)
            },
            new Object[] {
                BANK_ID_APP_WITH_PASSWORD,
                Collections.singletonList(Xpath.MOBILE_BANK_ID_INPUT_XPATH)
            },
            new Object[] {
                DNB_APPLICATION,
                Arrays.asList(
                        Xpath.MOBILE_BANK_ID_INPUT_XPATH,
                        Xpath.BANK_ID_PASSWORD_INPUT_XPATH,
                        Xpath.BANK_ID_APP_TITLE_XPATH)
            },
            new Object[] {
                MOBILE_BANKID,
                Arrays.asList(Xpath.BANK_ID_PASSWORD_INPUT_XPATH, Xpath.BANK_ID_APP_TITLE_XPATH)
            },
            new Object[] {
                PASSWORD_AFTER_BANKID_IS_ACCEPTED,
                Arrays.asList(
                        Xpath.MOBILE_BANK_ID_INPUT_XPATH,
                        Xpath.BANK_ID_APP_TITLE_XPATH,
                        Xpath.AUTHENTICATION_LIST_BUTTON_XPATH,
                        Xpath.REFERENCE_WORDS_XPATH)
            },
            new Object[] {
                SPAREBANK_CODE_GENERATOR,
                Arrays.asList(
                        Xpath.MOBILE_BANK_ID_INPUT_XPATH,
                        Xpath.BANK_ID_PASSWORD_INPUT_XPATH,
                        Xpath.BANK_ID_APP_TITLE_XPATH)
            }
        };
    }
}
