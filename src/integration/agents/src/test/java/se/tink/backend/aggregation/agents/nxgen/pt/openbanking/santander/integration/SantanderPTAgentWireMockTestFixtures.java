package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.santander.integration;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.utils.fixtures.WireMockTestFixtures;
import se.tink.libraries.payment.rpc.Payment;

public class SantanderPTAgentWireMockTestFixtures extends WireMockTestFixtures {

    public SantanderPTAgentWireMockTestFixtures(Properties properties) {
        super(properties);
    }

    @Override
    public Payment createDomesticPayment(LocalDate localDate) {
        Properties properties = getProperties();

        return new Payment.Builder()
                .withCreditor(createCreditor())
                .withExactCurrencyAmount(createExactCurrencyAmount())
                .withExecutionDate(localDate)
                .withCurrency(properties.getCurrency())
                .withRemittanceInformation(createUnstructuredRemittanceInformation())
                .build();
    }
}
