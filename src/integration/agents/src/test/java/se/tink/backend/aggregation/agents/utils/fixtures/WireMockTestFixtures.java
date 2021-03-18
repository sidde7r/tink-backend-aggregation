package se.tink.backend.aggregation.agents.utils.fixtures;

import java.time.LocalDate;
import java.util.Optional;
import lombok.Builder;
import lombok.Getter;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.base.CompositeAgentTestCommand;
import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.enums.MarketCode;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public abstract class WireMockTestFixtures {

    private final Properties properties;

    public WireMockTestFixtures(Properties properties) {
        this.properties = properties;
    }

    public static AgentsServiceConfiguration readAgentConfiguration(String configurationPath)
            throws Exception {
        return AgentsServiceConfigurationReader.read(configurationPath);
    }

    public abstract Payment createDomesticPayment(LocalDate localDate);

    public Payment createDomesticPayment() {
        return createDomesticPayment(getTodayDate());
    }

    public Payment createFarFutureDomesticPayment() {
        return createDomesticPayment(getFarFutureDate());
    }

    private LocalDate getTodayDate() {
        return LocalDate.now();
    }

    private LocalDate getFarFutureDate() {
        return LocalDate.of(2120, 11, 2);
    }

    protected ExactCurrencyAmount createExactCurrencyAmount() {
        return ExactCurrencyAmount.of(properties.amount, properties.currency);
    }

    protected Creditor createCreditor() {
        return new Creditor(
                AccountIdentifier.create(
                        properties.accountIdentifierType, properties.destinationIdentifier),
                properties.creditorName);
    }

    protected RemittanceInformation createUnstructuredRemittanceInformation() {
        return RemittanceInformationUtils.generateUnstructuredRemittanceInformation(
                properties.remittanceInfoValue);
    }

    public <T extends CompositeAgentTestCommand>
            AgentWireMockPaymentTest getAgentWireMockPaymentTestWithAuthCodeCallbackData(
                    String wireMockFileName, Payment payment, Class<T> command) throws Exception {

        return getPreconfiguredAgentWireMockPaymentTestBuilder(wireMockFileName, payment)
                .addCallbackData("code", properties.authCode)
                .buildWithoutLogin(command);
    }

    public <T extends CompositeAgentTestCommand>
            AgentWireMockPaymentTest getAgentWireMockPaymentTestWithErrorCallbackData(
                    String wireMockFileName, Payment payment, Class<T> command) throws Exception {

        return getPreconfiguredAgentWireMockPaymentTestBuilder(wireMockFileName, payment)
                .addCallbackData("error", "access_denied")
                .buildWithoutLogin(command);
    }

    public AgentWireMockPaymentTest.Builder getPreconfiguredAgentWireMockPaymentTestBuilder(
            String wireMockFileName, Payment payment) throws Exception {

        return AgentWireMockPaymentTest.builder(
                        properties.marketCode,
                        properties.providerName,
                        getWireMockFilePath(wireMockFileName))
                .withConfigurationFile(readAgentConfiguration(properties.configurationFileName))
                .withHttpDebugTrace()
                .withPayment(payment);
    }

    private String getWireMockFilePath(String filename) {
        return properties.resourcesPath + filename;
    }

    protected Properties getProperties() {
        return properties;
    }

    @Getter
    public static class Properties {

        private final String providerName;
        private final String currency;
        private final String destinationIdentifier;
        private final AccountIdentifierType accountIdentifierType;
        private final String remittanceInfoValue;
        private final MarketCode marketCode;
        private final String configurationFileName;
        private final String resourcesPath;
        private String authCode = "DUMMY_AUTH_CODE";
        private String amount = "1.00";
        private String creditorName = "Dummy creditor name";
        private String state = "00000000-0000-4000-0000-000000000000";

        @Builder
        private Properties(
                String authCode,
                String amount,
                String creditorName,
                String state,
                String providerName,
                String currency,
                String destinationIdentifier,
                AccountIdentifierType accountIdentifierType,
                String remittanceInfoValue,
                MarketCode marketCode,
                String configurationFileName,
                String resourcesPath) {

            this.authCode = Optional.ofNullable(authCode).orElse(this.authCode);
            this.amount = Optional.ofNullable(amount).orElse(this.amount);
            this.creditorName = Optional.ofNullable(creditorName).orElse(this.creditorName);
            this.state = Optional.ofNullable(state).orElse(this.state);
            this.providerName = providerName;
            this.currency = currency;
            this.destinationIdentifier = destinationIdentifier;
            this.accountIdentifierType = accountIdentifierType;
            this.remittanceInfoValue = remittanceInfoValue;
            this.marketCode = marketCode;
            this.resourcesPath = resourcesPath;
            this.configurationFileName = resourcesPath + configurationFileName;
        }
    }
}
