package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiProductData;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class ProductDetailsRequest extends DefaultRequest<ProductDetailsResponse> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/CSM_Reconciliacao/UI_POSI/POSI_Detail/ActionMobileExecuteConsultaDetalhePOSI";
    private static final String BODY_TEMPLATE =
            "{\"versionInfo\": {\"moduleVersion\": \"%s\",\"apiVersion\": \"U8v93Y1rNPmxJWNBOI_abw\"},\"viewName\": \"Reconciliacao.DetalheDeProduto\",\"inputParameters\": {\"ChallengeResponse\": {\"MobileChallengeResponse\": {\"Id\": \"\",\"Response\": \"\"}},\"ProdutoDados\": {\"CodigoFamilia\": %s,\"NUC\": \"%s\",\"NIP\": \"%s\",\"CodigoSubFamilia\": %s,\"CodigoProduto\": %s,\"NumeroOperacao\": \"%s\",\"DataHistorico\": \"1900-01-01\",\"NumeroContaOrdenante\": {\"nuc\": \"\",\"tipo\": \"\",\"ordem\": \"\"}}}}";
    private BancoBpiProductData productData;
    private String nuc;
    private String nip;

    public ProductDetailsRequest(
            BancoBpiEntityManager entityManager, BancoBpiProductData productData) {
        super(entityManager.getAuthContext(), URL);
        this.productData = productData;
        nuc = entityManager.getAccountsContext().getNuc();
        nip = entityManager.getAccountsContext().getNip();
    }

    @Override
    protected RequestBuilder withSpecificHeaders(
            TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public RequestBuilder withBody(TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder.body(
                String.format(
                        BODY_TEMPLATE,
                        getModuleVersion(),
                        productData.getCodeFamily(),
                        nuc,
                        nip,
                        productData.getCodeSubFamily(),
                        productData.getCode(),
                        productData.getNumber()));
    }

    @Override
    public ProductDetailsResponse execute(RequestBuilder requestBuilder, TinkHttpClient httpClient)
            throws RequestException {
        return new ProductDetailsResponse(requestBuilder.post(String.class));
    }
}
