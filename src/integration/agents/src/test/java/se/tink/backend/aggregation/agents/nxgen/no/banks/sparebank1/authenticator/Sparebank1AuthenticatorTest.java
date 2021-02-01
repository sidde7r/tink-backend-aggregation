package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.AgreementsResponse;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.rpc.BankBranchResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class Sparebank1AuthenticatorTest {
    private Sparebank1ApiClient apiClient;
    private PersistentStorage storage;
    private Sparebank1Authenticator authenticator;

    private static final String INIT_BANK_ID_HTML =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebank1/resources/login_bankid.html";
    private static final String SELECT_MARKET_HTML =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebank1/resources/select_market.html";
    private static final String BANK_BRANCH_RESPONSE =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebank1/resources/bank_branch_response.json";

    @Before
    public void initSetup() {
        Credentials credentials = mock(Credentials.class);
        apiClient = mock(Sparebank1ApiClient.class);
        storage = mock(PersistentStorage.class);
        authenticator = new Sparebank1Authenticator(apiClient, credentials, storage, "fid-bank1");
    }

    @SneakyThrows
    @Test
    public void initShouldReturnPollingElement() {
        // given
        String bankIdInitHtml =
                FileUtils.readFileToString(new File(INIT_BANK_ID_HTML), StandardCharsets.UTF_8);
        when(apiClient.initLogin()).thenReturn(bankIdInitHtml);

        String selectMarketHtml =
                FileUtils.readFileToString(new File(SELECT_MARKET_HTML), StandardCharsets.UTF_8);
        when(apiClient.selectMarketAndAuthentication(any())).thenReturn(selectMarketHtml);
        // when
        String pollingElement = authenticator.init("dummyString", "dummyString", "dummyString");
        // then
        assertThat(pollingElement).isNotNull();
    }

    @Test
    public void initShouldThrowBankIdExceptionIfBankIdInitElementsNotFound() {
        // given
        when(apiClient.initLogin()).thenReturn("dummyHtmlContent");

        // when
        Throwable throwable =
                catchThrowable(
                        () -> authenticator.init("dummyString", "dummyString", "dummyString"));

        // then
        assertThat(throwable)
                .isInstanceOf(BankIdException.class)
                .hasMessage("Missing bank id init params: dummyHtmlContent");
    }

    @SneakyThrows
    @Test
    public void initShouldThrowBankIdErrorIfPollingElementNotFound() {
        // given
        String bankIdInitHtml =
                FileUtils.readFileToString(new File(INIT_BANK_ID_HTML), StandardCharsets.UTF_8);
        when(apiClient.initLogin()).thenReturn(bankIdInitHtml);

        when(apiClient.selectMarketAndAuthentication(any())).thenReturn("dummyHtmlContent");
        // when
        Throwable throwable =
                catchThrowable(
                        () -> authenticator.init("dummyString", "dummyString", "dummyString"));
        // then
        assertThat(throwable)
                .isInstanceOf(BankIdException.class)
                .hasMessageContaining("Unknown reason of missing polling element");
    }

    @Test
    public void finishActivationShouldThrowExceptionIfNoBankBranchesAvailable() {
        // given
        when(apiClient.getUserBranches())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"banks\":[]}", BankBranchResponse.class));
        // when
        Throwable throwable = catchThrowable(() -> authenticator.finishActivation());
        // then
        assertThat(throwable).isInstanceOf(LoginException.class);
    }

    @Test
    public void finishActivationShouldThrowExceptionIfEmptyAgreements() {
        // given
        when(apiClient.getUserBranches())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(BANK_BRANCH_RESPONSE), BankBranchResponse.class));
        when(apiClient.getAgreements())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"agreements\":[]}", AgreementsResponse.class));

        // when
        Throwable throwable = catchThrowable(() -> authenticator.finishActivation());

        // then
        assertThat(throwable).isInstanceOf(LoginException.class);
    }
}
