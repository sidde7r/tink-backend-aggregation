package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.investment;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.BancoBpiClientApi;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiProductData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.BancoBpiProductType;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;

public class BancoBpiInvestmentAccountFetcherTest {

    private static int productCounter = 1;
    private BancoBpiClientApi clientApi;
    private BancoBpiInvestmentAccountFetcher objectUnderTest;

    @Before
    public void init() {
        clientApi = Mockito.mock(BancoBpiClientApi.class);
        objectUnderTest = new BancoBpiInvestmentAccountFetcher(clientApi);
    }

    @Test
    public void shouldReturnInvestmentAccounts() throws RequestException {
        // given
        BancoBpiProductData product = createInvestmentProduct();
        Mockito.when(clientApi.getProductsByType(BancoBpiProductType.DEPOSIT))
                .thenReturn(Lists.newArrayList(product));
        // when
        Collection<InvestmentAccount> result = objectUnderTest.fetchAccounts();
        // then
        Assert.assertEquals(1, result.size());
        InvestmentAccount ia = result.iterator().next();
        Assert.assertEquals(product.getNumber(), ia.getAccountNumber());
        Assert.assertEquals(product.getName(), ia.getName());
        Assert.assertEquals(product.getOwner(), ia.getHolderName().toString());
        Assert.assertTrue(
                product.getBalance().doubleValue() == ia.getExactBalance().getDoubleValue());
        Assert.assertEquals(product.getCurrencyCode(), ia.getExactBalance().getCurrencyCode());
        Assert.assertEquals(
                PortfolioModule.PortfolioType.DEPOT.toSystemType(),
                ia.getSystemPortfolios().get(0).getType());
    }

    @Test(expected = BankServiceException.class)
    public void shouldThrowBankServiceException() throws RequestException {
        // given
        Mockito.when(clientApi.getProductsByType(BancoBpiProductType.DEPOSIT))
                .thenThrow(new RequestException(""));
        // when
        objectUnderTest.fetchAccounts();
    }

    private BancoBpiProductData createInvestmentProduct() {
        BancoBpiProductData p = Mockito.mock(BancoBpiProductData.class);
        Mockito.when(p.getNumber()).thenReturn(appendCounter("1234"));
        Mockito.when(p.getName()).thenReturn(appendCounter("name"));
        Mockito.when(p.getOwner()).thenReturn(appendCounter("Owner"));
        Mockito.when(p.getCurrencyCode()).thenReturn("EUR");
        Mockito.when(p.getBalance()).thenReturn(new BigDecimal(productCounter));
        Mockito.when(p.getInitialBalance()).thenReturn(new BigDecimal(0));
        Mockito.when(p.getInitialDate()).thenReturn(LocalDate.now().minusMonths(productCounter));
        Mockito.when(p.getFinalDate()).thenReturn(LocalDate.now().plusMonths(productCounter));
        return p;
    }

    private String appendCounter(String value) {
        return value + productCounter;
    }
}
