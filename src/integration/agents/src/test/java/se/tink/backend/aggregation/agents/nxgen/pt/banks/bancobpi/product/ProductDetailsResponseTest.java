package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.common.RequestException;

public class ProductDetailsResponseTest {

    static final String RESPONSE_EXPECTED =
            "{\"versionInfo\": {\"hasModuleVersionChanged\": false,\"hasApiVersionChanged\": false},\"data\": {\"TransactionStatus\": {\"TransactionStatus\": {\"OperationStatusId\": 1,\"TransactionErrors\": {\"List\": [],\"EmptyListItem\": {\"TransactionError\": {\"Source\": \"\",\"Code\": \"\",\"Level\": 0,\"Description\": \"\"}}},\"AuthStatusReason\": {\"List\": [],\"EmptyListItem\": {\"Status\": \"\",\"Code\": \"\",\"Description\": \"\"}}}},\"MobileChallenge\": {\"MobileChallenge\": {\"Id\": 0,\"CreationDate\": \"1900-01-01T00:00:00\",\"UUID\": \"\",\"MobileChallengeRequestedToken\": {\"List\": [],\"EmptyListItem\": {\"UUID\": \"\",\"TokenDefinition\": \"\",\"ChallengeTokenType\": \"\"}}}},\"DetalheProduto\": {\"NUC\": \"1962511\",\"CodigoFamilia\": 10,\"CodigoSubFamilia\": 39,\"Descricao\": \"C.HABITACAO - GERAL\",\"ValorActual\": \"-68361.92\",\"ValorEUR\": \"-68361.92\",\"Moeda\": \"EUR\",\"CodigoAplicacao\": \"CRED-ODS\",\"Operacao\": {\"IS_POSIOperacao\": {\"NumeroOperacao\": \"1962511165001\",\"DataInicio\": \"2007-04-03\",\"DataFim\": \"04-04-2047\"}},\"Unidades\": \"0.000000\",\"SaldoMedio\": \"0.00\",\"Taxa\": {\"IS_Taxa\": {\"ValorActual\": \"0.1060000\",\"DataInicio\": \"2019-10-04\",\"DataFim\": \"2020-04-04\",\"ValorDevedora\": \"0.1060000\",\"ValorCredora\": \"0.0\"}},\"Credito\": {\"IS_Credito\": {\"ValorLimite\": \"88500.00\",\"DataFimLimite\": \"2047-04-04\"}},\"DataUltimaPrestacao\": \"2019-12-04\",\"ValorUltimaPrestacao\": \"211.46\",\"Nome\": \"NAME SURNAME\",\"TipoDetalhe\": \"GN\"}}}";

    @Test
    public void shouldParseLoanResponse() throws RequestException {
        // given
        final String response = RESPONSE_EXPECTED;
        // when
        ProductDetailsResponse objectUnderTest = new ProductDetailsResponse(response);
        // then
        Assert.assertEquals(LocalDate.parse("2047-04-04"), objectUnderTest.getFinalDate());
        Assert.assertEquals(LocalDate.parse("2007-04-03"), objectUnderTest.getInitialDate());
        Assert.assertEquals(new BigDecimal("88500.00"), objectUnderTest.getInitialBalance());
        Assert.assertEquals("NAME SURNAME", objectUnderTest.getOwner());
    }

    @Test
    public void shouldParseAssetResponse() throws RequestException {
        // given
        final String response =
                "{\"versionInfo\": {\"hasModuleVersionChanged\": false,\"hasApiVersionChanged\": false},\"data\": {\"TransactionStatus\": {\"TransactionStatus\": {\"OperationStatusId\": 1,\"TransactionErrors\": {\"List\": [],\"EmptyListItem\": {\"TransactionError\": {\"Source\": \"\",\"Code\": \"\",\"Level\": 0,\"Description\": \"\"}}},\"AuthStatusReason\": {\"List\": [],\"EmptyListItem\": {\"Status\": \"\",\"Code\": \"\",\"Description\": \"\"}}}},\"MobileChallenge\": {\"MobileChallenge\": {\"Id\": 0,\"CreationDate\": \"1900-01-01T00:00:00\",\"UUID\": \"\",\"MobileChallengeRequestedToken\": {\"List\": [],\"EmptyListItem\": {\"UUID\": \"\",\"TokenDefinition\": \"\",\"ChallengeTokenType\": \"\"}}}},\"DetalheProduto\": {\"NUC\": \"1962511\",\"CodigoFamilia\": 1,\"CodigoSubFamilia\": 4,\"Descricao\": \"BPI POUPANCA Férias de Verão\",\"ValorActual\": \"200.00\",\"ValorEUR\": \"200.00\",\"Moeda\": \"EUR\",\"CodigoAplicacao\": \"CONTPOUP\",\"Operacao\": {\"IS_POSIOperacao\": {\"NumeroOperacao\": \"1962511431008\",\"DataInicio\": \"2019-12-02\",\"DataFim\": \"02-03-2020\"}},\"Unidades\": \"0.000000\",\"SaldoMedio\": \"0.00\",\"Taxa\": {\"IS_Taxa\": {\"ValorActual\": \"0.0\",\"DataInicio\": \"2019-12-02\",\"DataFim\": \"2020-03-02\",\"ValorDevedora\": \"0.0000000\",\"ValorCredora\": \"0.0\"}},\"Credito\": {\"IS_Credito\": {\"ValorLimite\": \"0.00\",\"DataFimLimite\": \"1900-01-01\"}},\"DataUltimaPrestacao\": \"1900-01-01\",\"ValorUltimaPrestacao\": \"0.0\",\"Nome\": \"NAME SURNAME\",\"TipoDetalhe\": \"GN\"}}}";
        // when
        ProductDetailsResponse objectUnderTest = new ProductDetailsResponse(response);
        // then
        Assert.assertEquals(LocalDate.parse("2020-03-02"), objectUnderTest.getFinalDate());
        Assert.assertEquals(LocalDate.parse("2019-12-02"), objectUnderTest.getInitialDate());
        Assert.assertEquals(new BigDecimal("0.00"), objectUnderTest.getInitialBalance());
        Assert.assertEquals("NAME SURNAME", objectUnderTest.getOwner());
    }
}
