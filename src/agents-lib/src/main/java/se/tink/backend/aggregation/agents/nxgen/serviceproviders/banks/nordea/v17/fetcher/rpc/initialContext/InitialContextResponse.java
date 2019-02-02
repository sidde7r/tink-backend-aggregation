package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.rpc.initialContext;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.entities.BankingServiceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.entities.InitialContextData;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.rpc.NordeaResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
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

        Set<String> setOfTypes = Sets.newHashSet();
        for (String type : types) {
            setOfTypes.add(type.toLowerCase());
        }

        return getData().getProducts().stream()
                .filter(pe -> setOfTypes.contains(pe.getNordeaProductType().toLowerCase()))
                .collect(Collectors.toList());
    }
}
