package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.investment.entities.FundCategoryEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.investment.entities.FundEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AllFundsResponse extends SpankkiResponse {
    private List<FundCategoryEntity> categories;

    @JsonIgnore
    public Map<String, String> getFundIdIsinMapper() {
        return categories.stream()
                .map(FundCategoryEntity::getItems)
                .flatMap(Collection::stream)
                .collect(Collectors.toMap(FundEntity::getId, FundEntity::getIsin));
    }
}
