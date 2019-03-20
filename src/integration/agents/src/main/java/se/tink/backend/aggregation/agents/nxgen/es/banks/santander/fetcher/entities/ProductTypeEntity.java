package se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@XmlRootElement
public class ProductTypeEntity {
    @JsonProperty("EMPRESA")
    private int company;

    @JsonProperty("TIPO_DE_PRODUCTO")
    private int productTypeNumber;

    public int getCompany() {
        return company;
    }

    public int getProductTypeNumber() {
        return productTypeNumber;
    }
}
