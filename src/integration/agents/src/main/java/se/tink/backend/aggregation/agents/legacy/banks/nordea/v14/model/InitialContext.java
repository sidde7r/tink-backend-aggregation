package se.tink.backend.aggregation.agents.banks.nordea.v14.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@XmlRootElement
public class InitialContext {
    protected List<ProductEntity> product;

    public List<ProductEntity> getProduct() {
        return product;
    }

    public void setProduct(List<ProductEntity> product) {
        this.product = product;
    }
}
