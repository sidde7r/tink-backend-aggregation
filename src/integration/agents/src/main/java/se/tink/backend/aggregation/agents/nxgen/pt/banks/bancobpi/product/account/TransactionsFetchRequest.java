package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.TransactionalAccountBaseInfo;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class TransactionsFetchRequest extends DefaultRequest<TransactionsFetchResponse> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/CSM_Contas/UI_Movimentos/ListaMovimentos/DataActionGetMovimentos";
    private static final String BODY_TEMPLATE =
            "{\"versionInfo\": {\"moduleVersion\": \"%s\",\"apiVersion\": \"QCyg6FMl67VE2IHaVaAWew\"},\"viewName\": \"BPI.ContasECartoes\",\"screenData\": {\"variables\": {\"Paginacao\": {\"uuid\": \"%s\",\"lastPage\": false,\"pageNumber\": %s,\"pageSize\": 10,\"currentPage\": \"\",\"recordCount\": \"\"},\"IsFetchedForLoading\": false,\"IsFirstScroll\": true,\"MovimentosContaUI\": {\"List\": [],\"EmptyListItem\": {\"dataMovimento\": \"1900-01-01\",\"descricao\": \"\",\"valorMoedaConta\": \"0\",\"moedaOperacao\": \"\",\"valorOperacao\": \"0\",\"saldoMoedaConta\": \"0\"}},\"MoedaContaUI\": \"\",\"HasError\": false,\"LastRequestFromOnBack\": false,\"Conta\": {\"nuc\": \"%s\",\"tipo\": \"%s\",\"ordem\": \"%s\"},\"_contaInDataFetchStatus\": 1,\"GetMovimentos\": {}}}}";
    private int pageNo;
    private String bankFetchingUUID;
    private TransactionalAccountBaseInfo transactionalAccountBaseInfo;

    public TransactionsFetchRequest(
            BancoBpiEntityManager entityManager,
            String bankFetchingUUID,
            int pageNo,
            TransactionalAccount account)
            throws RequestException {
        super(entityManager.getAuthContext(), URL);
        this.pageNo = pageNo;
        this.bankFetchingUUID = bankFetchingUUID;
        transactionalAccountBaseInfo =
                entityManager
                        .getAccountsContext()
                        .findAccountInfoByNumber(account.getAccountNumber())
                        .orElseThrow(
                                () ->
                                        new RequestException(
                                                "Cant' find account with number "
                                                        + account.getAccountNumber()));
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder.body(
                String.format(
                        BODY_TEMPLATE,
                        getModuleVersion(),
                        bankFetchingUUID,
                        pageNo,
                        transactionalAccountBaseInfo.getInternalAccountId(),
                        transactionalAccountBaseInfo.getType(),
                        transactionalAccountBaseInfo.getOrder()));
    }

    @Override
    public TransactionsFetchResponse execute(RequestBuilder requestBuilder)
            throws RequestException {
        return new TransactionsFetchResponse(requestBuilder.post(String.class));
    }
}
