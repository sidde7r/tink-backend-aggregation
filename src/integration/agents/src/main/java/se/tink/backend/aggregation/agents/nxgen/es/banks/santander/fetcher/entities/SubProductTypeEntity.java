package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class SubProductTypeEntity {
    @JsonProperty("TIPO_DE_PRODUCTO")
    private ProductTypeEntity productType;

    @JsonProperty("SUBTIPO_DE_PRODUCTO")
    private int subProductNumber;

    public ProductTypeEntity getProductType() {
        return productType;
    }

    public int getSubProductNumber() {
        return subProductNumber;
    }
}
