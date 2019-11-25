package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.creditcard;

import static se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseLabels.CREDIT_CARDS;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntityWrapper;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.creditcard.GetSummaryBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.generic.DetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.generic.SectionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetSummaryResponse extends HeaderEntityWrapper {
    @JsonProperty("Body")
    private GetSummaryBodyEntity body;

    public GetSummaryBodyEntity getBody() {
        return body;
    }

    public Collection<String> getCreditCardsContractsIds() {
        return Optional.of(body)
                .map(GetSummaryBodyEntity::getResponsibilities)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .filter(r -> CREDIT_CARDS.equals(r.getDesignation()))
                .findFirst()
                .map(SectionEntity::getDetails)
                .map(Collection::stream)
                .orElse(Stream.empty())
                .map(DetailsEntity::getContract)
                .collect(Collectors.toList());
    }
}
