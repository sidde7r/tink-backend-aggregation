package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants.PostParameter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BasicEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class FilterInvestmentsEntity {
    private String id;
    private List<NumberFormatsEntity> numberFormats;
    private List<BasicEntity> stockMarkets;
    private List<InvestmentValuesEntity> investmentValues;

    public FilterInvestmentsEntity(String id, String currency, BigDecimal amount) {
        this.id = id;
        this.numberFormats =
                Collections.singletonList(new NumberFormatsEntity("000000000000", "ISIN"));
        this.stockMarkets = Collections.singletonList(new BasicEntity("0000"));
        this.investmentValues =
                Collections.singletonList(new InvestmentValuesEntity(currency, amount));
    }

    public FilterInvestmentsEntity(String id) {
        this.id = id;
        this.numberFormats =
                Collections.singletonList(
                        new NumberFormatsEntity(
                                PostParameter.ANY_ISIN, PostParameter.ISIN_ID_TYPE));
        this.stockMarkets = Collections.singletonList(new BasicEntity(PostParameter.ANY_MARKET));
    }
}
