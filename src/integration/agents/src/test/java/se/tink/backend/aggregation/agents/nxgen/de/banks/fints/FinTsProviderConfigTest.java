package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.Bank;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.PayloadParser;
import se.tink.backend.aggregation.configuration.ProviderConfig;

public class FinTsProviderConfigTest {

    @Test
    @Ignore
    public void allPayloadsShouldHaveSetRequiredParameters() throws IOException {
        List<Provider> finTsProviders = readFinTsProviders();
        for (Provider provider : finTsProviders) {
            PayloadParser.Payload payload = PayloadParser.parse(provider.getPayload());
            assertThat(payload.getBlz()).isNotEmpty();
            assertThat(payload.getEndpoint()).isNotEmpty();
            assertThat(payload.getBankName()).isNotEmpty();
            assertBankName(payload.getBankName());
        }
    }

    private void assertBankName(String bankName) {
        Throwable thrown = catchThrowable(() -> Bank.of(bankName));
        assertThat(thrown).as("Bank enum does not have entry for %s: .", bankName).isNull();
    }

    private static List<Provider> readFinTsProviders() throws IOException {
        String providersFilePath = "data/seeding/providers-de.json";
        File providersFile = new File(providersFilePath);
        ObjectMapper mapper = new ObjectMapper();
        ProviderConfig providerConfig = mapper.readValue(providersFile, ProviderConfig.class);
        return providerConfig.getProviders().stream()
                .filter(
                        provider ->
                                "nxgen.de.banks.fints.FinTsAgent".equals(provider.getClassName()))
                .collect(Collectors.toList());
    }
}
