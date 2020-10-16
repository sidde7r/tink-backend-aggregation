package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.investment.rpc;

import com.amazonaws.util.CollectionUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.investment.entity.InvestmentAccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class InvestmentResponse {
    private Body body;

    @JsonObject
    @Getter
    public static class Body {

        @JsonProperty("listaPosizioneBuoni")
        private List<Object> investmentPositions;

        @JsonProperty("posizioneConti")
        private List<InvestmentAccountPosition> investmentAccounts;

        @JsonObject
        @Getter
        public static class InvestmentAccountPosition {

            @JsonProperty("listaBuoni")
            private List<InvestmentAccountEntity> investments;
        }
    }

    public boolean isInvestmentAvailable() {
        return getBody() != null
                && (!CollectionUtils.isNullOrEmpty(getBody().getInvestmentPositions())
                        || !CollectionUtils.isNullOrEmpty(getBody().getInvestmentAccounts()));
    }
}
