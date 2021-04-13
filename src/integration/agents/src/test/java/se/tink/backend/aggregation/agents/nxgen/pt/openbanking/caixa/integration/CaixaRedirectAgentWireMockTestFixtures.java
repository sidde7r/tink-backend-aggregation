package se.tink.backend.aggregation.agents.nxgen.pt.openbanking.caixa.integration;

import java.time.LocalDate;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.utils.fixtures.WireMockTestFixtures;
import se.tink.libraries.payment.rpc.Payment;

@Ignore
public class CaixaRedirectAgentWireMockTestFixtures extends WireMockTestFixtures {

    public CaixaRedirectAgentWireMockTestFixtures(Properties properties) {
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
