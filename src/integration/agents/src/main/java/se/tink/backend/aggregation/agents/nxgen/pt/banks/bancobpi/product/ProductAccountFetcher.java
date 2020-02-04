package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.BancoBpiClientApi;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiProductData;

public class ProductAccountFetcher {

    private BancoBpiClientApi clientApi;

    public ProductAccountFetcher(BancoBpiClientApi clientApi) {
        this.clientApi = clientApi;
    }

    public List<BancoBpiProductData> fetchProductsByType(BancoBpiProductType... types)
            throws RequestException {
        return fetchDetails(findByType(types));
    }

    private List<BancoBpiProductData> findByType(BancoBpiProductType... types)
            throws RequestException {
        List<String> codes =
                Arrays.stream(types).map(t -> t.getCode()).collect(Collectors.toList());
        return clientApi.fetchProductsData().getAllProducts().stream()
                .filter(p -> codes.contains(p.getCodeAlfa()))
                .collect(Collectors.toList());
    }

    private List<BancoBpiProductData> fetchDetails(List<BancoBpiProductData> products)
            throws RequestException {
        for (BancoBpiProductData product : products) {
            ProductDetailsResponse details = clientApi.fetchProductDetails(product);
            product.setInitialBalance(details.getInitialBalance());
            product.setInitialDate(details.getInitialDate());
            product.setFinalDate(details.getFinalDate());
            product.setOwner(details.getOwner());
        }
        return products;
    }
}
