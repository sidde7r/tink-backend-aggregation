package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank;

import static org.mockito.Mockito.when;

import java.io.File;
import lombok.RequiredArgsConstructor;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.investment.rpc.InvestmentResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
@RequiredArgsConstructor
public class JyskeBankApiClientMockWrapper {
    private final JyskeBankApiClient mockApiClient;

    public void mockFetchInvestmentsUsingFile(String filePath) {
        when(mockApiClient.fetchInvestments())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                new File(filePath), InvestmentResponse.class));
    }
}
