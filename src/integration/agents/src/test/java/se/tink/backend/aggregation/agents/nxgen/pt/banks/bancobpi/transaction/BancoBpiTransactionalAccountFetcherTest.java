package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.transaction;

import java.math.BigDecimal;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAccountsContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiAuthContext;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.TransactionalAccountBaseInfo;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public class BancoBpiTransactionalAccountFetcherTest {

    private static final String RESPONSE_EXPECTED =
            "{\"versionInfo\": {\"hasModuleVersionChanged\": false,\"hasApiVersionChanged\": false},\"data\": {\"ContaSaldoInfo\": {\"Saldo\": {\"montanteCativo\": \"0.00\",\"moedaConta\": \"EUR\",\"saldoContabilisticoConta\": \"321.74\",\"saldoDisponivelConta\": \"321.74\",\"valoresPendentes\": \"0.00\",\"codigoBalcao\": \"32\"},\"Erro\": false},\"SaldoCartao\": {\"HasSaldoError\": false,\"SaldoDisponivel\": \"\",\"SaldoExtractoInfo\": {\"nome\": \"\",\"saldoActual\": \"0.0\",\"montanteDividaUltimoExtracto\": \"0.0\",\"montanteMinimoPagamento\": \"0.0\",\"totalAutorizacoesAprovadas\": 0,\"totalAutorizacoesDeclinadas\": 0,\"plafondActual\": 0,\"plafondDisponivel\": 0,\"quantidadeAutorizacoesCurso\": 0,\"montanteAutorizacoesCurso\": \"0.0\",\"plafondCash\": 0,\"montantePlafondExcedido\": \"0.0\",\"estadoContaCartao\": \"\",\"cicloExtracto\": 0,\"montanteCashDisponivel\": 0,\"indicadorDebitoConta\": false,\"opcaoPagamento\": 0}},\"IsFetched\": true}}";

    private RequestBuilder requestBuilder;
    private TinkHttpClient httpClient;
    private BancoBpiAuthContext authContext;
    private BancoBpiEntityManager entityManager;

    @Before
    public void init() {
        httpClient = Mockito.mock(TinkHttpClient.class);
        requestBuilder = Mockito.mock(RequestBuilder.class);
        Mockito.when(httpClient.request(Mockito.anyString())).thenReturn(requestBuilder);
        Mockito.when(requestBuilder.header(Mockito.any(String.class), Mockito.any()))
                .thenReturn(requestBuilder);
        Mockito.when(requestBuilder.body(Mockito.anyString())).thenReturn(requestBuilder);
        authContext = Mockito.mock(BancoBpiAuthContext.class);
        entityManager = Mockito.mock(BancoBpiEntityManager.class);
        Mockito.when(entityManager.getAuthContext()).thenReturn(authContext);
    }

    @Test
    public void fetchAccountsShouldFetchAccounts() {
        // given
        BancoBpiAccountsContext accountsInfo = new BancoBpiAccountsContext();
        TransactionalAccountBaseInfo accountBaseInfo =
                Mockito.mock(TransactionalAccountBaseInfo.class);
        Mockito.when(accountBaseInfo.getInternalAccountId()).thenReturn("internalAccountId");
        Mockito.when(accountBaseInfo.getCurrency()).thenReturn("EUR");
        Mockito.when(accountBaseInfo.getAccountName()).thenReturn("accountName");
        Mockito.when(accountBaseInfo.getIban()).thenReturn("PT50000000000000000000");
        accountsInfo.getAccountInfo().add(accountBaseInfo);
        Mockito.when(entityManager.getAccountsContext()).thenReturn(accountsInfo);
        Mockito.when(requestBuilder.post(String.class)).thenReturn(RESPONSE_EXPECTED);
        // when
        Collection<TransactionalAccount> result =
                new BancoBpiTransactionalAccountFetcher(httpClient, entityManager).fetchAccounts();
        // then
        Assert.assertFalse(result.isEmpty());
        TransactionalAccount transactionalAccount = result.iterator().next();
        Assert.assertEquals(accountBaseInfo.getAccountName(), transactionalAccount.getName());
        Assert.assertEquals(
                accountBaseInfo.getCurrency(),
                transactionalAccount.getExactBalance().getCurrencyCode());
        Assert.assertEquals(
                new BigDecimal("321.74"), transactionalAccount.getExactBalance().getExactValue());
        Assert.assertEquals(
                accountBaseInfo.getInternalAccountId(), transactionalAccount.getAccountNumber());
        Assert.assertEquals(
                accountBaseInfo.getIban(), transactionalAccount.getIdModule().getUniqueId());
    }
}
