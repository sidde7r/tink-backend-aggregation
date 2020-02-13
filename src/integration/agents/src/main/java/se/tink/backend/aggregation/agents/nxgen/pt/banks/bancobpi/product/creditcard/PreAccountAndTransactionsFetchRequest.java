package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.creditcard;

import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class PreAccountAndTransactionsFetchRequest extends DefaultRequest<Void> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/CSM_Cartoes/ActionMobileExecuteListarContasCartaoCredito";
    private static final String BODY_TEMPLATE =
            "{\"versionInfo\": {\"moduleVersion\": \"%s\",\"apiVersion\": \"2ViYgCe5UnJeUj6DEWOn6w\"},\"viewName\": \"BPI.HomeConta\",\"inputParameters\": {\"NIP\": \"%s\",\"TipoConsultaId\": \"NaoPrivateLabel\",\"Situacao\": \"ActivosOuCanceladosComDivida\",\"ForcarConsulta\": true,\"Paginacao\": {\"pageNumber\": \"1\",\"pageSize\": \"10\",\"uid\": \"\",\"currentPage\": \"\",\"recordCount\": \"\",\"lastPage\": false},\"ChallengeResponse\": {\"Id\": \"\",\"Response\": \"\"},\"IncluirTimeStampImagem\": true}}";

    private String nip;

    public PreAccountAndTransactionsFetchRequest(BancoBpiEntityManager entityManager) {
        super(entityManager.getAuthContext(), URL);
        this.nip = entityManager.getAccountsContext().getNip();
    }

    @Override
    public RequestBuilder withBody(RequestBuilder requestBuilder) {
        return requestBuilder.body(String.format(BODY_TEMPLATE, getModuleVersion(), nip));
    }

    @Override
    public Void execute(RequestBuilder requestBuilder) throws RequestException {
        requestBuilder.post(String.class);
        return null;
    }
}
