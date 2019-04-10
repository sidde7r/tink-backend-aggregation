package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.rpc.initialContext;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.entities.BankingServiceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.entities.InitialContextData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class InitialContextResponse extends NordeaResponse {
    private BankingServiceEntity bankingServiceResponse;

    @JsonProperty("getInitialContextOut")
    private InitialContextData data;

    public InitialContextData getData() {
        return data;
    }

    public BankingServiceEntity getBankingServiceResponse() {
        return bankingServiceResponse;
    }

    @JsonIgnore
    public List<ProductEntity> getProductsOfTypes(String... types) {
        List<ProductEntity> products = Lists.newArrayList();
        if (getData() == null || getData().getProducts() == null) {
            return products;
        }

        // Need a more specific filter for fetching only credit cards since Nordea also return
        // debit cards in the initial context response (which are handled when fetching
        // transactional
        // accounts).
        if (types.length == 1 && Objects.equal(types[0], NordeaV21Constants.ProductType.CARD)) {
            return getData().getProducts().stream()
                    .filter(isCreditCardProduct())
                    .collect(Collectors.toList());
        }

        Set<String> setOfTypes = Sets.newHashSet();
        for (String type : types) {
            setOfTypes.add(type.toLowerCase());
        }

        return getData().getProducts().stream()
                .filter(pe -> setOfTypes.contains(pe.getNordeaProductType().toLowerCase()))
                .collect(Collectors.toList());
    }

    private Predicate<ProductEntity> isCreditCardProduct() {
        return pe ->
                Objects.equal(pe.getNordeaProductType().toLowerCase(), "card")
                        && (Objects.equal(pe.getNordeaCardGroup().toLowerCase(), "credit")
                                || Objects.equal(pe.getNordeaCardGroup().toLowerCase(), "combine"));
    }
}
