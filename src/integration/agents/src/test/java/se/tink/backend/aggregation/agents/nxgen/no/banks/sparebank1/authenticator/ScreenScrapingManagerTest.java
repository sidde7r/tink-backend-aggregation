package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.io.File;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.InitBankIdParams;

public class ScreenScrapingManagerTest {
    private static final String INIT_BANK_ID_HTML =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebank1/resources/login_bankid.html";
    private static final String WRONG_PHONE_HTML =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebank1/resources/wrong_phone_login.html";

    @SneakyThrows
    @Test
    public void getBankIdInitParamsShouldReturnBankIdInitParamsIfPossible() {
        // given
        String bankIdInitHtml =
                FileUtils.readFileToString(new File(INIT_BANK_ID_HTML), StandardCharsets.UTF_8);
        // when
        InitBankIdParams params = ScreenScrapingManager.getBankIdInitParams(bankIdInitHtml);
        // then
        assertThat(params.getFormId()).isEqualTo("j_idt197");
        assertThat(params.getViewState()).isEqualTo("-4818047786543362728:-1860433859868414757");
    }

    @Test
    public void getBankIdInitShouldThrowExceptionIfInitBankIdParamsNotAvailable() {
        // given
        String bankIdInitHtml = "dummyHtml";
        // when
        Throwable throwable =
                catchThrowable(() -> ScreenScrapingManager.getBankIdInitParams(bankIdInitHtml));
        // then
        assertThat(throwable)
                .isInstanceOf(BankIdException.class)
                .hasMessageContaining("Missing bank id init params:");
    }

    @SneakyThrows
    @Test
    public void
            getPollingElementShouldThrowUnknownExceptionIfPollingElementNotAvailableAndReasonNotIdentified() {
        // given
        String dummyHtml = "dummyHtml";
        // when
        Throwable throwable =
                catchThrowable(() -> ScreenScrapingManager.getPollingElement(dummyHtml));
        // then
        assertThat(throwable)
                .isInstanceOf(BankIdException.class)
                .hasMessageContaining("Unknown reason of missing polling element:");
    }

    @SneakyThrows
    @Test
    public void
            getPollingElementShouldThrowKnownExceptionIfPollingElementNotAvailableAndReasonIdentified() {
        // given
        String wrongPhoneHtml =
                FileUtils.readFileToString(new File(WRONG_PHONE_HTML), StandardCharsets.UTF_8);
        // when
        Throwable throwable =
                catchThrowable(() -> ScreenScrapingManager.getPollingElement(wrongPhoneHtml));
        // then
        String key = ((LoginException) throwable).getUserMessage().get();
        assertThat(key)
                .isEqualTo(LoginError.WRONG_PHONENUMBER_OR_INACTIVATED_SERVICE.userMessage().get());
    }
}
