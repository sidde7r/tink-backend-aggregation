package se.tink.backend.common.providers.booli.entities.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.core.BooliSoldProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BooliEstimateResponse {
    private Double accuracy;
    private Integer price;
    private Integer priceRangeHigh;
    private Integer priceRangeLow;
    private Double sqmPrice;
    private Double sqmPriceRangeHigh;
    private Double sqmPriceRangeLow;
    private MetaData meta;
    private Residence residence;
    private List<Reference> references;

    public Double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getPriceRangeHigh() {
        return priceRangeHigh;
    }

    public void setPriceRangeHigh(Integer priceRangeHigh) {
        this.priceRangeHigh = priceRangeHigh;
    }

    public Integer getPriceRangeLow() {
        return priceRangeLow;
    }

    public void setPriceRangeLow(Integer priceRangeLow) {
        this.priceRangeLow = priceRangeLow;
    }

    public Double getSqmPrice() {
        return sqmPrice;
    }

    public void setSqmPrice(Double sqmPrice) {
        this.sqmPrice = sqmPrice;
    }

    public Double getSqmPriceRangeHigh() {
        return sqmPriceRangeHigh;
    }

    public void setSqmPriceRangeHigh(Double sqmPriceRangeHigh) {
        this.sqmPriceRangeHigh = sqmPriceRangeHigh;
    }

    public Double getSqmPriceRangeLow() {
        return sqmPriceRangeLow;
    }

    public void setSqmPriceRangeLow(Double sqmPriceRangeLow) {
        this.sqmPriceRangeLow = sqmPriceRangeLow;
    }

    public MetaData getMeta() {
        return meta;
    }

    public void setMeta(MetaData meta) {
        this.meta = meta;
    }

    public Residence getResidence() {
        return residence;
    }

    public void setResidence(Residence residence) {
        this.residence = residence;
    }

    public List<Reference> getReferences() {
        return references;
    }

    public void setReferences(List<Reference> references) {
        this.references = references;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("accuracy", accuracy)
                .add("price", price)
                .add("priceRangeHigh", priceRangeHigh)
                .add("priceRangeLow", priceRangeLow)
                .add("sqmPrice", sqmPrice)
                .add("sqmPriceRangeHigh", sqmPriceRangeHigh)
                .add("sqmPriceRangeLow", sqmPriceRangeLow)
                .add("meta", meta)
                .add("residence", residence)
                .add("references", references)
                .toString();
    }

    public List<BooliSoldProperty> getSoldProperties() {
        List<BooliSoldProperty> soldProperties = Lists.newArrayList();

        for (Reference reference : getReferences()) {
            soldProperties.add(BooliModelMapper.referenceToSoldProperty(reference));
        }

        return soldProperties;
    }
}
