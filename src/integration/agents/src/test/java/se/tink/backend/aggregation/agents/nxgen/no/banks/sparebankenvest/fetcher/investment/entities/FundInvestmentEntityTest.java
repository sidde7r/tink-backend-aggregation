package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;

public class FundInvestmentEntityTest {

    @Test
    public void areInvestmentsTypesMappedCorrectly() {
        // given
        FundInvestmentEntity pensionEntity = new FundInvestmentEntity();
        pensionEntity.setType(SparebankenVestConstants.Investments.PENSION_PORTFOLIO_TYPE);
        FundInvestmentEntity fundEntity = new FundInvestmentEntity();
        fundEntity.setType(SparebankenVestConstants.Investments.FUND_TYPE);
        FundInvestmentEntity stockEntity = new FundInvestmentEntity();
        stockEntity.setType(SparebankenVestConstants.Investments.STOCK_OPTIONS_TYPE);
        FundInvestmentEntity otherEntity = new FundInvestmentEntity();
        otherEntity.setType("Other");

        // when
        PortfolioModule.PortfolioType pensionEntityPortfolioType =
                pensionEntity.getTinkPortfolioType();
        PortfolioModule.PortfolioType fundEntityPortfolioType = fundEntity.getTinkPortfolioType();
        PortfolioModule.PortfolioType stockEntityPortfolioType = stockEntity.getTinkPortfolioType();
        PortfolioModule.PortfolioType otherEntityPortfolioType = otherEntity.getTinkPortfolioType();

        InstrumentModule.InstrumentType pensionEntityInstrumentType =
                pensionEntity.getTinkInstrumentType();
        InstrumentModule.InstrumentType fundEntityInstrumentType =
                fundEntity.getTinkInstrumentType();
        InstrumentModule.InstrumentType stockEntityInstrumentType =
                stockEntity.getTinkInstrumentType();
        InstrumentModule.InstrumentType otherEntityInstrumentType =
                otherEntity.getTinkInstrumentType();

        // then
        assertThat(pensionEntityPortfolioType).isEqualTo(PortfolioModule.PortfolioType.PENSION);
        assertThat(fundEntityPortfolioType).isEqualTo(PortfolioModule.PortfolioType.DEPOT);
        assertThat(stockEntityPortfolioType).isEqualTo(PortfolioModule.PortfolioType.OTHER);
        assertThat(otherEntityPortfolioType).isEqualTo(PortfolioModule.PortfolioType.OTHER);

        assertThat(pensionEntityInstrumentType).isEqualTo(InstrumentModule.InstrumentType.FUND);
        assertThat(fundEntityInstrumentType).isEqualTo(InstrumentModule.InstrumentType.FUND);
        assertThat(stockEntityInstrumentType).isEqualTo(InstrumentModule.InstrumentType.STOCK);
        assertThat(otherEntityInstrumentType).isEqualTo(InstrumentModule.InstrumentType.OTHER);
    }
}
