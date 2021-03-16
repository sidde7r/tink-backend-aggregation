package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.nextbankid.driver.searchelements;

import org.openqa.selenium.WebElement;

public interface BankIdElementFilter {

    boolean matches(WebElement element);
}
