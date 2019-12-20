package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity;

import java.util.LinkedList;
import java.util.List;

public class BancoBpiProductsData {

    private List<BancoBpiProductData> assetsProductsData = new LinkedList<>();
    private List<BancoBpiProductData> liabilitiesProductsData = new LinkedList<>();

    public List<BancoBpiProductData> getAssetsProductsData() {
        return assetsProductsData;
    }

    public List<BancoBpiProductData> getLiabilitiesProductsData() {
        return liabilitiesProductsData;
    }

    public List<BancoBpiProductData> getAllProducts() {
        List<BancoBpiProductData> allProducts = new LinkedList<>();
        allProducts.addAll(assetsProductsData);
        allProducts.addAll(liabilitiesProductsData);
        return allProducts;
    }
}
