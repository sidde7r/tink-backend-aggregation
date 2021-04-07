package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.iframe.steps;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants.HtmlLocators.LOC_SSN_INPUT;

import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.BankIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.BankIdWebDriver;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements.BankIdElementsSearchQuery;

@Slf4j
@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class BankIdEnterSSNStep {

    private final BankIdWebDriver driver;

    @SuppressWarnings("unused")
    public void enterSSN(Credentials credentials) {
        waitForSSNInput();

        // the rest will be implemented in next PRs
    }

    private void waitForSSNInput() {
        log.info("{} Waiting for SSN input", BankIdConstants.BANK_ID_LOG_PREFIX);
        boolean ssnInputFound =
                driver.searchForFirstMatchingLocator(
                                BankIdElementsSearchQuery.builder()
                                        .searchFor(LOC_SSN_INPUT)
                                        .searchForSeconds(10)
                                        .build())
                        .isNotEmpty();
        if (!ssnInputFound) {
            throw new IllegalStateException("Cannot find SSN input");
        }
    }
}
