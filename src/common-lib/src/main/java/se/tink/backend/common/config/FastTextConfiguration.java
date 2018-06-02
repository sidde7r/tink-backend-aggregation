package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import se.tink.backend.core.Market;

public class FastTextConfiguration {
    @JsonProperty
    private String model;
    @JsonProperty
    private Market.Code market;
    @JsonProperty
    private List<StringConverterFactory> preformatters = Collections
            .singletonList(new IdentityStringConverterFactory());
    @JsonProperty
    private String name;

    public FastTextConfiguration() {
    }

    public FastTextConfiguration(String name, String model, Market.Code market, List<StringConverterFactory> preformatters) {
        this.name = name;
        this.model = model;
        this.market = market;
        this.preformatters = preformatters;
    }

    public String getModel() {
        return model;
    }

    public List<StringConverterFactory> getPreformatters() {
        return preformatters;
    }

    public String getName() {
        return name;
    }

    public Market.Code getMarket() {
        return market;
    }
}
