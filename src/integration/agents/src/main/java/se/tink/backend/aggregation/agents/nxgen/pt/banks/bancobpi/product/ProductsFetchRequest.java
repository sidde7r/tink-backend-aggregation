package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiProductsData;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class ProductsFetchRequest extends DefaultRequest<BancoBpiProductsData> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/CSM_Reconciliacao/UI_POSI/POSI/ActionGetPOSI";
    private static final String BODY_TEMPLATE =
            "{\"versionInfo\": {\"moduleVersion\": \"%s\",\"apiVersion\": \"T6hJ9sitITzP1ONZcEj7xQ\"},\"viewName\": \"Reconciliacao.PosicaoIntegrada\",\"inputParameters\": {\"NUC\": \"%s\"}}";
    private String nucNumber;

    public ProductsFetchRequest(BancoBpiEntityManager entityManager) {
        super(entityManager.getAuthContext(), URL);
        nucNumber =
                entityManager.getAccountsContext().getAccountInfo().get(0).getInternalAccountId();
    }

    @Override
    protected RequestBuilder withSpecificHeaders(
            TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public RequestBuilder withBody(TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder.body(String.format(BODY_TEMPLATE, getModuleVersion(), nucNumber));
    }

    @Override
    public BancoBpiProductsData execute(RequestBuilder requestBuilder, TinkHttpClient httpClient)
            throws RequestException {
        return new ProductsFetchResponse(requestBuilder.post(String.class)).getProductsData();
    }
}
