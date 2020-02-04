package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product;

import java.math.BigDecimal;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiProductData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiProductsData;

public class ProductsFetchResponse {

    private BancoBpiProductsData productsData = new BancoBpiProductsData();

    ProductsFetchResponse(String rawResponse) throws RequestException {
        parseResponse(rawResponse);
    }

    private void parseResponse(String rawJsonResponse) throws RequestException {
        try {
            JSONObject data = new JSONObject(rawJsonResponse).getJSONObject("data");
            parseAssets(
                    data.getJSONObject("POSIFamila")
                            .getJSONObject("SubFamiliaList")
                            .getJSONArray("List"));
            parseLiabilities(
                    data.getJSONObject("POSIFamilaPassiva")
                            .getJSONObject("SubFamiliaList")
                            .getJSONArray("List"));
        } catch (JSONException e) {
            throw new RequestException("Response has unexpected structure");
        }
    }

    private void parseAssets(JSONArray assets) throws JSONException {
        parseProductGroups(assets, productsData.getAssetsProductsData());
    }

    private void parseLiabilities(JSONArray liabilities) throws JSONException {
        parseProductGroups(liabilities, productsData.getLiabilitiesProductsData());
    }

    private void parseProductGroups(JSONArray productGroups, List<BancoBpiProductData> storage)
            throws JSONException {
        for (int i = 0; i < productGroups.length(); i++) {
            JSONObject productGroup =
                    productGroups.getJSONObject(i).getJSONObject("IS_POSISubFamilia");
            String codeAlfa = productGroup.getString("CodigoAlfa");
            JSONArray productList = productGroup.getJSONObject("ProdutoList").getJSONArray("List");
            for (int j = 0; j < productList.length(); j++) {
                BancoBpiProductData productData =
                        parseProduct(productList.getJSONObject(j).getJSONObject("IS_POSIProduto"));
                productData.setCodeAlfa(codeAlfa);
                storage.add(productData);
            }
        }
    }

    private BancoBpiProductData parseProduct(JSONObject jsonProduct) throws JSONException {
        BancoBpiProductData productData = new BancoBpiProductData();
        productData.setCodeFamily(jsonProduct.getString("CodigoFamilia"));
        productData.setCodeSubFamily(jsonProduct.getString("CodigoSubFamilia"));
        productData.setCode(jsonProduct.getString("Codigo"));
        productData.setBalance(new BigDecimal(jsonProduct.getString("Valor")));
        productData.setCurrencyCode(jsonProduct.getString("CodigoMoeda"));
        productData.setName(jsonProduct.getString("DescricaoTitulo"));
        productData.setNumber(jsonProduct.getString("NumeroOperacao"));
        return productData;
    }

    public BancoBpiProductsData getProductsData() {
        return productsData;
    }
}
