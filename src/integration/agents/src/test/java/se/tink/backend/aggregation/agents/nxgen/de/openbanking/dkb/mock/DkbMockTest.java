package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.mock;

import static org.assertj.core.api.Assertions.assertThatCode;
import static se.tink.libraries.enums.MarketCode.DE;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.assertions.AgentContractEntitiesJsonFileParser;
import se.tink.backend.aggregation.agents.framework.assertions.entities.AgentContractEntity;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.PaymentCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockrefresh.AgentWireMockRefreshTest;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DkbMockTest {

    private static final String BASE_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/de/openbanking/dkb/mock/resources/";

    private static final String CONFIGURATION_FILE = BASE_PATH + "configuration.yml";

    @Test
    public void testFullFlow() throws Exception {
        // given
        final String mockFilePath = BASE_PATH + "dkb_manual_aggregation.aap";
        final String contractFilePath = BASE_PATH + "dkb_manual_aggregation.json";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);
        final AgentWireMockRefreshTest agentWireMockRefreshTest =
                AgentWireMockRefreshTest.nxBuilder()
                        .withMarketCode(DE)
                        .withProviderName("de-dkb-ob")
                        .withWireMockFilePath(mockFilePath)
                        .withConfigFile(configuration)
                        .testFullAuthentication()
                        .addRefreshableItems(RefreshableItem.allRefreshableItemsAsArray())
                        .addCallbackData("pushTan", "123456")
                        .addCredentialField("username", "username")
                        .addCredentialField("password", "password")
                        .enableHttpDebugTrace()
                        .build();

        final AgentContractEntity expected =
                new AgentContractEntitiesJsonFileParser()
                        .parseContractOnBasisOfFile(contractFilePath);

        // when
        assertThatCode(agentWireMockRefreshTest::executeRefresh).doesNotThrowAnyException();

        // then
        agentWireMockRefreshTest.assertExpectedData(expected);
    }

    @Test
    public void testSepaPaymentInitiation() throws Exception {
        // given
        final String mockFilePath = BASE_PATH + "dkb_payment.aap";

        final AgentsServiceConfiguration configuration =
                AgentsServiceConfigurationReader.read(CONFIGURATION_FILE);

        Payment payment = createRealDomesticPayment().build();

        final AgentWireMockPaymentTest agentWireMockPaymentTest =
                AgentWireMockPaymentTest.builder(MarketCode.DE, "de-dkb-ob", mockFilePath)
                        .withConfigurationFile(configuration)
                        .withPayment(payment)
                        .addCallbackData("pushTan", "123456")
                        .addCredentialField("username", "username")
                        .addCredentialField("password", "password")
                        .buildWithoutLogin(PaymentCommand.class);

        // when / then
        assertThatCode(agentWireMockPaymentTest::executePayment).doesNotThrowAnyException();
    }

    private Payment.Builder createRealDomesticPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        LocalDateTime localDateTime = new ConstantLocalDateTimeSource().now();

        remittanceInformation.setValue(
                "SepaReferenceToCreditor "
                        + localDateTime.format(
                                DateTimeFormatter.ofPattern("yyyy/MM/dd 'at' HH:mm:ss")));
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("DE65500105179548288115");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier("DE43500105178515291667");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
        String currency = "EUR";

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation)
                .withExecutionDate(localDateTime.toLocalDate());
    }
}
