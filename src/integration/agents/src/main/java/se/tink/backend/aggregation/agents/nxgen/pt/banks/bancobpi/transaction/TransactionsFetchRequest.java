package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.transaction;

import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.TransactionalAccountBaseInfo;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class TransactionsFetchRequest extends DefaultRequest<TransactionsFetchResponse> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/CSM_Contas/UI_Movimentos/ListaMovimentos/DataActionGetMovimentos";
    private static final String BODY_TEMPLATE =
            "{\"versionInfo\": {\"moduleVersion\": \"%s\",\"apiVersion\": \"QCyg6FMl67VE2IHaVaAWew\"},\"viewName\": \"BPI.ContasECartoes\",\"screenData\": {\"variables\": {\"Paginacao\": {\"uuid\": \"%s\",\"lastPage\": false,\"pageNumber\": %s,\"pageSize\": 10,\"currentPage\": \"\",\"recordCount\": \"\"},\"IsFetchedForLoading\": false,\"IsFirstScroll\": true,\"MovimentosContaUI\": {\"List\": [],\"EmptyListItem\": {\"dataMovimento\": \"1900-01-01\",\"descricao\": \"\",\"valorMoedaConta\": \"0\",\"moedaOperacao\": \"\",\"valorOperacao\": \"0\",\"saldoMoedaConta\": \"0\"}},\"MoedaContaUI\": \"\",\"HasError\": false,\"LastRequestFromOnBack\": false,\"Conta\": {\"nuc\": \"%s\",\"tipo\": \"%s\",\"ordem\": \"%s\"},\"_contaInDataFetchStatus\": 1,\"GetMovimentos\": {}}}}";
    private int pageNo;
    private String bankFetchingUUID;
    private TransactionalAccountBaseInfo transactionalAccountBaseInfo;

    protected TransactionsFetchRequest(
            BancoBpiEntityManager entityManager,
            String bankFetchingUUID,
            int pageNo,
            TransactionalAccount account)
            throws RequestException {
        super(entityManager.getAuthContext(), URL);
        this.pageNo = pageNo;
        this.bankFetchingUUID = bankFetchingUUID;
        transactionalAccountBaseInfo = findTransactionalAccountBaseInfo(entityManager, account);
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
                        bankFetchingUUID,
                        pageNo,
                        transactionalAccountBaseInfo.getInternalAccountId(),
                        transactionalAccountBaseInfo.getType(),
                        transactionalAccountBaseInfo.getOrder()));
    }

    @Override
    public TransactionsFetchResponse execute(
            RequestBuilder requestBuilder, TinkHttpClient httpClient) throws RequestException {
        return new TransactionsFetchResponse(requestBuilder.post(String.class));
    }

    private TransactionalAccountBaseInfo findTransactionalAccountBaseInfo(
            BancoBpiEntityManager entityManager, TransactionalAccount account)
            throws RequestException {
        return entityManager.getAccountsContext().getAccountInfo().stream()
                .filter(o -> o.getInternalAccountId().equals(account.getAccountNumber()))
                .findAny()
                .orElseThrow(
                        () ->
                                new RequestException(
                                        "Cant' find account with number "
                                                + account.getAccountNumber()));
    }
}
