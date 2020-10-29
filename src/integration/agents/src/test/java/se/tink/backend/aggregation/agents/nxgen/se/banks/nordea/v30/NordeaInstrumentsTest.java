package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.investment.entities.InvestmentAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.investment.rpc.FetchInvestmentResponse;

public class NordeaInstrumentsTest {

    private List<InvestmentAccountEntity> investmentAccountEntities = new ArrayList<>();

    @Before
    public void getInvestmentAccountEntities() throws IOException {
        investmentAccountEntities = readInvestmentResponseJson().getAccounts();
    }

    @Test
    public void hasInstruments() {
        investmentAccountEntities.stream()
                .forEach(entity -> Assert.assertTrue(!entity.getHoldings().isEmpty()));
    }

    @Test
    @Ignore // TODO previously unmaintained -- should be fixed
    public void isInstruments() {
        investmentAccountEntities.stream()
                .forEach(
                        entity ->
                                entity.getHoldings().stream()
                                        .forEach(
                                                holdingEntity ->
                                                        Assert.assertTrue(
                                                                holdingEntity.isInstrument())));
    }

    private static FetchInvestmentResponse readInvestmentResponseJson() throws IOException {
        String jsonFilePath = "data/aggregation/nordea/investmentEntity.json";
        File investmentFile = new File(jsonFilePath);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(investmentFile, FetchInvestmentResponse.class);
    }
}
