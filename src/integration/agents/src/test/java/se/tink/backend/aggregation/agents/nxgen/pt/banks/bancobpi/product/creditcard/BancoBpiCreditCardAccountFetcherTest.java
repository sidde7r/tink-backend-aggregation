package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.creditcard;

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
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

public class BancoBpiCreditCardAccountFetcherTest {

    private static int productCounter = 1;
    private BancoBpiClientApi clientApi;
    private BancoBpiCreditCardAccountFetcher objectUnderTest;

    @Before
    public void init() {
        clientApi = Mockito.mock(BancoBpiClientApi.class);
        objectUnderTest = new BancoBpiCreditCardAccountFetcher(clientApi);
    }

    @Test
    public void shouldReturnCreditCardAccount() throws RequestException {
        // given
        BancoBpiProductData p1 = createCreditCardProduct();
        List<BancoBpiProductData> products = Lists.newArrayList(p1);
        Mockito.when(clientApi.getProductsByType(BancoBpiProductType.CREDIT_CARD))
                .thenReturn(products);
        // when
        Collection<CreditCardAccount> accounts = objectUnderTest.fetchAccounts();
        // then
        Assert.assertFalse(accounts.isEmpty());
        Assert.assertEquals(1, accounts.size());
        CreditCardAccount a = accounts.iterator().next();
        Assert.assertEquals(p1.getNumber(), a.getAccountNumber());
        Assert.assertEquals(p1.getName(), a.getName());
        Assert.assertEquals(p1.getNumber(), a.getApiIdentifier());
        Assert.assertEquals(p1.getOwner(), a.getHolderName().toString());
        Assert.assertEquals(p1.getInitialBalance(), a.getExactAvailableCredit().getExactValue());
        Assert.assertEquals(p1.getCurrencyCode(), a.getExactAvailableCredit().getCurrencyCode());
        Assert.assertEquals(p1.getBalance(), a.getExactBalance().getExactValue());
        Assert.assertEquals(p1.getCurrencyCode(), a.getExactBalance().getCurrencyCode());
    }

    @Test
    public void shouldMultipleCreditCardAccounts() throws RequestException {
        // given
        BancoBpiProductData p1 = createCreditCardProduct();
        BancoBpiProductData p2 = createCreditCardProduct();
        List<BancoBpiProductData> products = Lists.newArrayList(p1, p2);
        Mockito.when(clientApi.getProductsByType(BancoBpiProductType.CREDIT_CARD))
                .thenReturn(products);
        // when
        Collection<CreditCardAccount> accounts = objectUnderTest.fetchAccounts();
        // then
        Assert.assertFalse(accounts.isEmpty());
        Assert.assertEquals(2, accounts.size());
    }

    @Test(expected = BankServiceException.class)
    public void shouldThrowBankServiceException() throws RequestException {
        // given
        Mockito.when(clientApi.getProductsByType(BancoBpiProductType.CREDIT_CARD))
                .thenThrow(new RequestException(""));
        // when
        objectUnderTest.fetchAccounts();
    }

    private BancoBpiProductData createCreditCardProduct() {
        BancoBpiProductData p = Mockito.mock(BancoBpiProductData.class);
        Mockito.when(p.getNumber()).thenReturn(appendCounter("1234"));
        Mockito.when(p.getName()).thenReturn(appendCounter("name"));
        Mockito.when(p.getOwner()).thenReturn(appendCounter("Owner"));
        Mockito.when(p.getCurrencyCode()).thenReturn("EUR");
        Mockito.when(p.getBalance()).thenReturn(new BigDecimal(productCounter));
        Mockito.when(p.getInitialBalance()).thenReturn(new BigDecimal(productCounter * 2));
        Mockito.when(p.getInitialDate()).thenReturn(LocalDate.now().minusMonths(productCounter));
        Mockito.when(p.getFinalDate()).thenReturn(LocalDate.now().plusMonths(productCounter));
        return p;
    }

    private String appendCounter(String value) {
        return value + productCounter;
    }
}
