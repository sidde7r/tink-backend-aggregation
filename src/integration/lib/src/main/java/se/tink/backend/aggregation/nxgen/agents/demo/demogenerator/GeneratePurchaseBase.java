package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import java.util.List;

public class GeneratePurchaseBase {
    private String company;
    private List<Double> itemPrices;

    public String getCompany() {
        return company;
    }

    public List<Double> getItemPrices() {
        return itemPrices;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setItemPrices(List<Double> itemPrices) {
        this.itemPrices = itemPrices;
    }

    public GeneratePurchaseBase() {}
}
