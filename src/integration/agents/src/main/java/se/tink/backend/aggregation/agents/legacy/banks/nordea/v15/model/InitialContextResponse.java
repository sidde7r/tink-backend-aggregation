package se.tink.backend.aggregation.agents.banks.nordea.v15.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaAgentUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
public class InitialContextResponse {
    private BankingServiceResponse bankingServiceResponse;
    @JsonProperty("getInitialContextOut")
    private InitialContextData data;

    public InitialContextData getData() {
        return data;
    }

    public BankingServiceResponse getBankingServiceResponse() {
        return bankingServiceResponse;
    }

    public void setBankingServiceResponse(BankingServiceResponse bankingServiceResponse) {
        this.bankingServiceResponse = bankingServiceResponse;
    }

    public void setData(InitialContextData data) {
        this.data = data;
    }

    @JsonIgnore
    public List<ProductEntity> getProductsOfTypes(String... types) {
        List<ProductEntity> products = Lists.newArrayList();
        if (getData() == null || getData().getProducts() == null) {
            return products;
        }

        return getData().getProducts().stream()
                .filter(NordeaAgentUtils.getProductsOfType(types)::apply)
                .collect(Collectors.toList());
    }
}
