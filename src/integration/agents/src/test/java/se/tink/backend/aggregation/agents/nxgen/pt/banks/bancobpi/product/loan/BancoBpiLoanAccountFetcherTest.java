package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.loan;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.BancoBpiClientApi;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiProductData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.BancoBpiProductType;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails.Type;

public class BancoBpiLoanAccountFetcherTest {

    private static int productCounter = 1;
    private BancoBpiClientApi clientApi;
    private BancoBpiLoanAccountFetcher objectUnderTest;

    @Before
    public void init() {
        clientApi = Mockito.mock(BancoBpiClientApi.class);
        objectUnderTest = new BancoBpiLoanAccountFetcher(clientApi);
    }

    @Test
    public void shouldReturnLoanAccount() throws RequestException {
        // given
        BancoBpiProductData p1 = createLoanProduct(BancoBpiProductType.LOAN);
        List<BancoBpiProductData> products = Lists.newArrayList(p1);
        Mockito.when(clientApi.getProductsByType(BancoBpiProductType.getLoanProductTypes()))
                .thenReturn(products);
        // when
        Collection<LoanAccount> accounts = objectUnderTest.fetchAccounts();
        // then
        Assert.assertFalse(accounts.isEmpty());
        Assert.assertEquals(1, accounts.size());
        LoanAccount a = accounts.iterator().next();
        Assert.assertEquals(p1.getNumber(), a.getAccountNumber());
        Assert.assertEquals(p1.getName(), a.getName());
        Assert.assertTrue(
                p1.getInitialBalance().doubleValue()
                        == a.getDetails().getInitialBalance().doubleValue());
        Assert.assertEquals(p1.getCurrencyCode(), a.getDetails().getInitialBalance().getCurrency());
        Assert.assertEquals(p1.getBalance(), a.getExactBalance().getExactValue());
        Assert.assertEquals(p1.getCurrencyCode(), a.getExactBalance().getCurrencyCode());
        Assert.assertEquals(Type.CREDIT, a.getDetails().getType());
    }

    @Test
    public void shouldReturnVehicleAccount() throws RequestException {
        // given
        BancoBpiProductData p1 = createLoanProduct(BancoBpiProductType.LOAN_VEHICLE);
        List<BancoBpiProductData> products = Lists.newArrayList(p1);
        Mockito.when(clientApi.getProductsByType(BancoBpiProductType.getLoanProductTypes()))
                .thenReturn(products);
        // when
        Collection<LoanAccount> accounts = objectUnderTest.fetchAccounts();
        // then
        Assert.assertFalse(accounts.isEmpty());
        Assert.assertEquals(1, accounts.size());
        LoanAccount a = accounts.iterator().next();
        Assert.assertEquals(p1.getNumber(), a.getAccountNumber());
        Assert.assertEquals(p1.getName(), a.getName());
        Assert.assertTrue(
                p1.getInitialBalance().doubleValue()
                        == a.getDetails().getInitialBalance().doubleValue());
        Assert.assertEquals(p1.getCurrencyCode(), a.getDetails().getInitialBalance().getCurrency());
        Assert.assertEquals(p1.getBalance(), a.getExactBalance().getExactValue());
        Assert.assertEquals(p1.getCurrencyCode(), a.getExactBalance().getCurrencyCode());
        Assert.assertEquals(Type.VEHICLE, a.getDetails().getType());
    }

    @Test
    public void shouldReturnMortgageAccount() throws RequestException {
        // given
        BancoBpiProductData p1 = createLoanProduct(BancoBpiProductType.MORTGAGE);
        List<BancoBpiProductData> products = Lists.newArrayList(p1);
        Mockito.when(clientApi.getProductsByType(BancoBpiProductType.getLoanProductTypes()))
                .thenReturn(products);
        // when
        Collection<LoanAccount> accounts = objectUnderTest.fetchAccounts();
        // then
        Assert.assertFalse(accounts.isEmpty());
        Assert.assertEquals(1, accounts.size());
        LoanAccount a = accounts.iterator().next();
        Assert.assertEquals(p1.getNumber(), a.getAccountNumber());
        Assert.assertEquals(p1.getName(), a.getName());
        Assert.assertTrue(
                p1.getInitialBalance().doubleValue()
                        == a.getDetails().getInitialBalance().doubleValue());
        Assert.assertEquals(p1.getCurrencyCode(), a.getDetails().getInitialBalance().getCurrency());
        Assert.assertEquals(p1.getBalance(), a.getExactBalance().getExactValue());
        Assert.assertEquals(p1.getCurrencyCode(), a.getExactBalance().getCurrencyCode());
        Assert.assertEquals(Type.MORTGAGE, a.getDetails().getType());
    }

    @Test
    public void shouldReturnMultipleAccounts() throws RequestException {
        // given
        BancoBpiProductData p1 = createLoanProduct(BancoBpiProductType.MORTGAGE);
        BancoBpiProductData p2 = createLoanProduct(BancoBpiProductType.LOAN);
        List<BancoBpiProductData> products = Lists.newArrayList(p1, p2);
        Mockito.when(clientApi.getProductsByType(BancoBpiProductType.getLoanProductTypes()))
                .thenReturn(products);
        // when
        Collection<LoanAccount> accounts = objectUnderTest.fetchAccounts();
        // then
        Assert.assertFalse(accounts.isEmpty());
        Assert.assertEquals(2, accounts.size());
    }

    @Test(expected = BankServiceException.class)
    public void shouldThrowBankServiceException() throws RequestException {
        // given
        Mockito.when(clientApi.getProductsByType(Mockito.any()))
                .thenThrow(new RequestException(""));
        // when
        objectUnderTest.fetchAccounts();
    }

    private BancoBpiProductData createLoanProduct(BancoBpiProductType type) {
        BancoBpiProductData p = Mockito.mock(BancoBpiProductData.class);
        Mockito.when(p.getNumber()).thenReturn(appendCounter("1234"));
        Mockito.when(p.getName()).thenReturn(appendCounter("name"));
        Mockito.when(p.getOwner()).thenReturn(appendCounter("owner"));
        Mockito.when(p.getCurrencyCode()).thenReturn("EUR");
        Mockito.when(p.getBalance()).thenReturn(new BigDecimal(productCounter));
        Mockito.when(p.getInitialBalance()).thenReturn(new BigDecimal(productCounter * 2));
        Mockito.when(p.getInitialDate()).thenReturn(LocalDate.now().minusMonths(productCounter));
        Mockito.when(p.getFinalDate()).thenReturn(LocalDate.now().plusMonths(productCounter));
        Mockito.when(p.getCodeAlfa()).thenReturn(type.getCode());
        return p;
    }

    private String appendCounter(String value) {
        return value + productCounter;
    }
}
