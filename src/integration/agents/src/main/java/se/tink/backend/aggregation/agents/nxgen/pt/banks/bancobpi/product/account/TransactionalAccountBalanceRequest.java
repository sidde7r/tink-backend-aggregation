package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.common.DefaultRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.TransactionalAccountBaseInfo;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;

public class TransactionalAccountBalanceRequest extends DefaultRequest<BigDecimal> {

    private static final String URL =
            "https://apps.bancobpi.pt/BPIAPP/screenservices/BPIAPP/BPI/wbCarouselContasCartoes/DataActionGetSaldosContaContexto";
    private static final String BODY_TEMPLATE =
            "{\"versionInfo\": {\"moduleVersion\": \"%s\",\"apiVersion\": \"vVLHktRU3V+t8fJJlgM5_Q\"},\"viewName\": \"BPI.ContasECartoes\",\"screenData\": {\"variables\": {\"CarouselStructure\":%s}}}";
    private TransactionalAccountBaseInfo accountBaseInfo;
    private static final Gson gson = new Gson();

    public TransactionalAccountBalanceRequest(
            BancoBpiAuthContext authContext, TransactionalAccountBaseInfo accountBaseInfo) {
        super(authContext, URL);
        this.accountBaseInfo = accountBaseInfo;
    }

    @Override
    protected RequestBuilder withSpecificHeaders(
            TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        return requestBuilder;
    }

    @Override
    public RequestBuilder withBody(TinkHttpClient httpClient, RequestBuilder requestBuilder) {
        String accountParamsJson = gson.toJson(new CarouselStructure(accountBaseInfo));
        return requestBuilder.body(
                String.format(BODY_TEMPLATE, getModuleVersion(), accountParamsJson));
    }

    @Override
    public BigDecimal execute(RequestBuilder requestBuilder, TinkHttpClient httpClient)
            throws RequestException {
        return new BigDecimal(extractBalance(requestBuilder.post(String.class)));
    }

    private String extractBalance(String rawResponse) throws RequestException {
        try {
            JSONObject response = new JSONObject(rawResponse);
            return response.getJSONObject("data")
                    .getJSONObject("ContaSaldoInfo")
                    .getJSONObject("Saldo")
                    .getString("saldoContabilisticoConta");
        } catch (JSONException e) {
            throw new RequestException("Fetching account unexpected response format");
        }
    }

    private class CarouselStructure {
        @SerializedName("List")
        List<CarouselStructureElement> elements = new LinkedList<>();

        public CarouselStructure() {}

        CarouselStructure(TransactionalAccountBaseInfo accountBaseInfo) {
            elements.add(new CarouselStructureElement(accountBaseInfo));
        }
    }

    private class CarouselStructureElement {
        @SerializedName("IsConta")
        boolean isConta = true;

        @SerializedName("Titulo")
        String titulo;

        @SerializedName("Conta")
        Conta conta;

        public CarouselStructureElement() {}

        CarouselStructureElement(TransactionalAccountBaseInfo accountBaseInfo) {
            titulo = accountBaseInfo.getAccountName();
            conta = new Conta(accountBaseInfo);
        }
    }

    private class Conta {
        @SerializedName("Conta")
        ContaInternal conta;

        public Conta() {}

        Conta(TransactionalAccountBaseInfo accountBaseInfo) {
            conta = new ContaInternal(accountBaseInfo);
        }
    }

    private class ContaInternal {
        String nuc;
        String tipo;
        String ordem;

        public ContaInternal() {}

        ContaInternal(TransactionalAccountBaseInfo transactionalAccountBaseInfo) {
            nuc = accountBaseInfo.getInternalAccountId();
            tipo = accountBaseInfo.getType();
            ordem = accountBaseInfo.getOrder();
        }
    }
}
