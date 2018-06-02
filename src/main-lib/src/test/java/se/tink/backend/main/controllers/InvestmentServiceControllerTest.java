package se.tink.backend.main.controllers;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.common.repository.cassandra.InstrumentRepository;
import se.tink.backend.common.repository.cassandra.PortfolioRepository;
import se.tink.backend.core.Instrument;
import se.tink.backend.core.Portfolio;
import se.tink.libraries.uuid.UUIDUtils;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InvestmentServiceControllerTest {

    @Test
    public void twoPortfolios_noInvestments_generatesCorrectPortfolioList_withEmptyInvestmentLists() {
        PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);
        InstrumentRepository instrumentRepository = mock(InstrumentRepository.class);

        InvestmentServiceController controller = new InvestmentServiceController(portfolioRepository,
                instrumentRepository);

        UUID userId = UUID.randomUUID();

        when(instrumentRepository.findAllByUserId(userId)).thenReturn(Lists.newArrayList());
        when(portfolioRepository.findAllByUserId(userId))
                .thenReturn(Lists.newArrayList(new Portfolio(), new Portfolio()));

        List<se.tink.backend.rpc.Portfolio> portfolioResponse = controller.getPortfolios(UUIDUtils.toTinkUUID(userId));

        assertEquals(2, portfolioResponse.size());

        for (se.tink.backend.rpc.Portfolio portfolio : portfolioResponse) {
            assertEquals(0, portfolio.getInstruments().size());
        }
    }

    @Test
    public void twoPortfolios_threeInstruments_generatesCorrectPortfolioList_withInstrumentsOnCorrectPortfolio() {
        PortfolioRepository portfolioRepository = mock(PortfolioRepository.class);
        InstrumentRepository instrumentRepository = mock(InstrumentRepository.class);

        InvestmentServiceController controller = new InvestmentServiceController(portfolioRepository,
                instrumentRepository);

        UUID userId = UUID.randomUUID();
        UUID portfolioId1 = UUID.randomUUID();
        UUID portfolioId2 = UUID.randomUUID();

        List<Instrument> instruments = Lists.newArrayList(new Instrument(), new Instrument(), new Instrument());
        instruments.get(0).setPortfolioId(portfolioId1);
        instruments.get(1).setPortfolioId(portfolioId2);
        instruments.get(2).setPortfolioId(portfolioId1);

        List<Portfolio> portfolios = Lists.newArrayList(new Portfolio(), new Portfolio());
        portfolios.get(0).setId(portfolioId1);
        portfolios.get(1).setId(portfolioId2);

        when(instrumentRepository.findAllByUserId(userId)).thenReturn(instruments);
        when(portfolioRepository.findAllByUserId(userId)).thenReturn(portfolios);

        List<se.tink.backend.rpc.Portfolio> portfolioResponse = controller.getPortfolios(UUIDUtils.toTinkUUID(userId));

        assertEquals(2, portfolioResponse.size());

        Map<UUID, se.tink.backend.rpc.Portfolio> portfolioResponseById = portfolioResponse.stream()
                .collect(Collectors.toMap(se.tink.backend.rpc.Portfolio::getId, p -> p));

        assertTrue(portfolioResponseById.containsKey(portfolioId1));
        assertTrue(portfolioResponseById.containsKey(portfolioId2));

        se.tink.backend.rpc.Portfolio portfolio1 = portfolioResponseById.get(portfolioId1);
        se.tink.backend.rpc.Portfolio portfolio2 = portfolioResponseById.get(portfolioId2);

        assertEquals(2, portfolio1.getInstruments().size());
        assertEquals(1, portfolio2.getInstruments().size());

        assertEquals(portfolioId1, portfolio1.getInstruments().get(0).getPortfolioId());
        assertEquals(portfolioId1, portfolio1.getInstruments().get(1).getPortfolioId());
        assertEquals(portfolioId2, portfolio2.getInstruments().get(0).getPortfolioId());
    }
}
