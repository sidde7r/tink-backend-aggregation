package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.wiremock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.time.Clock;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.agentplatform.authentication.result.error.AgentPlatformAuthenticationProcessException;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationCancelledByUserException;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.RefreshableAccessToken;
import se.tink.backend.aggregation.agentsplatform.agentsframework.common.authentication.Token;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.libraries.account.identifiers.SortCodeIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

@RunWith(JUnitParamsRunner.class)
public class StarlingAgentWireMockTest {

    private static final String PROVIDER_NAME = "uk-starling-oauth2";

    private static final String RESOURCES_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/uk/openbanking/starling/wiremock/resources/";

    private static final String INVALID_GRANT_TRAFFIC = RESOURCES_PATH + "invalid-grant.aap";
    private static final String FPS_PAYMENT_SUCCESSFUL =
            RESOURCES_PATH + "fast-payment-success.aap";

    private static final String FPS_PAYMENT_FAILED = RESOURCES_PATH + "fast-payment-failed.aap";
    private static final String CONFIGURATION_PATH = RESOURCES_PATH + "config.yml";

    private static final String DUMMY_ACCESS_TOKEN = "DUMMY_ACCESS_TOKEN";
    private static final String DUMMY_EXPIRED_ACCESS_TOKEN = "DUMMY_EXPIRED_ACCESS_TOKEN";
    private static final String DUMMY_REFRESH_TOKEN = "DUMMY_REFRESH_TOKEN";
    private static final String TOKEN_TYPE_BEARER = "bearer";

    private RefreshableAccessToken refreshableAccessToken;
    private RefreshableAccessToken refreshableAccessTokenExpired;

    @Before
    public void setUp() throws Exception {
        this.refreshableAccessToken = createRefreshableAccessToken();
        this.refreshableAccessTokenExpired = createRefreshableAccessTokenExpired();
    }

    @Test
    @Parameters({
        "auto-refresh-with-account-creation-date.aap, auto-refresh.json",
        "auto-refresh-without-account-creation-date.aap, auto-refresh.json",
        "business-accounts.aap, business-accounts.json"
    })
    public void shouldAutoRefreshSuccessfullyWithPersonalAccounts(
            String trafficFileName, String contractFileName) throws Exception {

        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(RESOURCES_PATH + trafficFileName)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .testAutoAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addRefreshableAccessToken(refreshableAccessToken)
                        .build();

        // when
        test.executeRefresh();

        // then
        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(RESOURCES_PATH + contractFileName);
        test.assertExpectedData(expected);
    }

    @Test
    public void shouldExitAuthAfterInvalidGrantResponse() throws Exception {
        // given
        final AgentWireMockRefreshTest test =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(MarketCode.UK)
                        .withProviderName(PROVIDER_NAME)
                        .withWireMockFilePath(INVALID_GRANT_TRAFFIC)
                        .withConfigFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                        .testAutoAuthentication()
                        .testOnlyAuthentication()
                        .addRefreshableAccessToken(refreshableAccessTokenExpired)
                        .build();

        // expected
        Assertions.assertThatExceptionOfType(AgentPlatformAuthenticationProcessException.class)
                .isThrownBy(test::executeRefresh)
                .withMessage("SessionExpiredError: Session expired. [APAG-9]");
    }

    @Test
    public void testPaymentCancelledCase() throws Exception {
        // given
        final Payment payment = createDomesticPayment();
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestWithErrorCallbackData(payment);

        // when
        final Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(PaymentAuthorizationCancelledByUserException.class)
                .hasNoCause()
                .hasMessage("Authorisation of payment was cancelled. Please try again.");
    }

    @Test
    public void testPaymentSuccessCase() throws Exception {
        final Payment payment = createDomesticPayment();
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestUserFinishedAuthentication(
                        payment, FPS_PAYMENT_SUCCESSFUL);

        agentWireMockPaymentTest.executePayment();
    }

    @Test
    public void testPaymentFailedCase() throws Exception {
        final Payment payment = createDomesticPayment();
        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                createAgentWireMockPaymentTestUserFinishedAuthentication(
                        payment, FPS_PAYMENT_FAILED);

        final Throwable thrown = catchThrowable(agentWireMockPaymentTest::executePayment);

        // then
        assertThat(thrown)
                .isExactlyInstanceOf(CreditorValidationException.class)
                .hasNoCause()
                .hasMessage("Could not validate the creditor account.");
    }

    private RefreshableAccessToken createRefreshableAccessToken() {
        Token expiredAccessToken = createAccessToken();
        Token refreshToken = createRefreshToken();

        return createRefreshableAccessToken(expiredAccessToken, refreshToken);
    }

    private RefreshableAccessToken createRefreshableAccessTokenExpired() {
        Token expiredAccessToken = createExpiredAccessToken();

        Token refreshToken = createRefreshToken();

        return createRefreshableAccessToken(expiredAccessToken, refreshToken);
    }

    private Token createAccessToken() {
        return Token.builder()
                .body(DUMMY_ACCESS_TOKEN)
                .tokenType(TOKEN_TYPE_BEARER)
                .expiresIn(Clock.systemUTC().millis() / 1000, 100L)
                .build();
    }

    private Token createExpiredAccessToken() {
        return Token.builder()
                .body(DUMMY_EXPIRED_ACCESS_TOKEN)
                .tokenType(TOKEN_TYPE_BEARER)
                .expiresIn(1000L, 0L)
                .build();
    }

    private Token createRefreshToken() {
        return Token.builder().body(DUMMY_REFRESH_TOKEN).build();
    }

    private RefreshableAccessToken createRefreshableAccessToken(
            Token expiredAccessToken, Token refreshToken) {
        return RefreshableAccessToken.builder()
                .accessToken(expiredAccessToken)
                .refreshToken(refreshToken)
                .build();
    }

    private AgentWireMockPaymentTest createAgentWireMockPaymentTestWithErrorCallbackData(
            Payment payment) throws Exception {

        return AgentWireMockPaymentTest.builder(MarketCode.UK, PROVIDER_NAME, INVALID_GRANT_TRAFFIC)
                .withConfigurationFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                .addCallbackData("state", "state")
                .addCallbackData("error", "access_denied")
                .withPayment(payment)
                .buildWithoutLogin(PaymentCommand.class);
    }

    private AgentWireMockPaymentTest createAgentWireMockPaymentTestUserFinishedAuthentication(
            Payment payment, String path) throws Exception {

        return AgentWireMockPaymentTest.builder(MarketCode.UK, PROVIDER_NAME, path)
                .withConfigurationFile(AgentsServiceConfigurationReader.read(CONFIGURATION_PATH))
                .addCallbackData("state", "state")
                .addCallbackData("code", "code")
                .withPayment(payment)
                .buildWithoutLogin(PaymentCommand.class);
    }

    private static Payment createDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setType(RemittanceInformationType.REFERENCE);
        remittanceInformation.setValue("Ref1234");
        return new Payment.Builder()
                .withCreditor(
                        new Creditor(new SortCodeIdentifier("12345612345678"), "Unknown Person"))
                .withDebtor(new Debtor(new SortCodeIdentifier("65432112345678")))
                .withExactCurrencyAmount(ExactCurrencyAmount.of(0.01, "GBP"))
                .withCurrency("GBP")
                .withRemittanceInformation(remittanceInformation)
                .build();
    }
}
