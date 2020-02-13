package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.creditcard;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class TransactionsFetchRequest extends DefaultRequest<TransactionsFetchResponse> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/CSM_Cartoes/UI_Cartoes/ConsultaListaMovimentosCartao/DataActionGetMovimentosCartao";
    private static final String BODY_TEMPLATE =
            "{\"versionInfo\": {\"moduleVersion\": \"%s\",\"apiVersion\": \"D7UETcNYRptt19jFz6Torw\"},\"viewName\": \"BPI.ContasECartoes\",\"screenData\": {\"variables\": {\"Paginacao\": {\"uuid\": \"%s\",\"lastPage\": false,\"pageNumber\": %s,\"pageSize\": 10,\"currentPage\": \"\",\"recordCount\": \"\"},\"isDataFetched\": false,\"isFirstLoad\": true,\"ListaMovimentosTotal\": {\"EmptyListItem\": {\"DataTransaccao\": \"1900-01-01\",\"DataMovimento\": \"1900-01-01\"}},\"hasError\": false,\"Cartao\": {\"Cartao\": {\"contaCartao\": \"%s\"}},\"NIP\": %s}}}";
    private CreditCardAccount account;
    private int pageNo;
    private String nip;
    private String fetchingUUID;

    public TransactionsFetchRequest(
            BancoBpiEntityManager entityManager,
            CreditCardAccount account,
            int pageNo,
            String fetchingUUID) {
        super(entityManager.getAuthContext(), URL);
        this.account = account;
        this.pageNo = pageNo;
        this.nip = entityManager.getAccountsContext().getNip();
        this.fetchingUUID = fetchingUUID;
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder.body(
                String.format(
                        BODY_TEMPLATE,
                        getModuleVersion(),
                        fetchingUUID,
                        pageNo,
                        account.getAccountNumber(),
                        nip));
    }

    @Override
    public TransactionsFetchResponse execute(RequestBuilder requestBuilder)
            throws RequestException {
        return new TransactionsFetchResponse(requestBuilder.post(String.class), account);
    }
}
