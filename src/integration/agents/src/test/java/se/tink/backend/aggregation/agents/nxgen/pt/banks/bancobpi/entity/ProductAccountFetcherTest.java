package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity;

import com.google.common.collect.Lists;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.BancoBpiClientApi;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.BancoBpiProductType;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.ProductAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.ProductDetailsResponse;

public class ProductAccountFetcherTest {

    private ProductAccountFetcher objectUnderTest;
    private BancoBpiProductsData productsData;
    private BancoBpiClientApi clientApi;
    private ProductDetailsResponse productDetailsResponse;

    @Before
    public void init() throws RequestException {
        clientApi = Mockito.mock(BancoBpiClientApi.class);
        objectUnderTest = new ProductAccountFetcher(clientApi);
        productsData = Mockito.mock(BancoBpiProductsData.class);
        Mockito.when(clientApi.fetchProductsData()).thenReturn(productsData);
        productDetailsResponse = Mockito.mock(ProductDetailsResponse.class);
        Mockito.when(clientApi.fetchProductDetails(Mockito.any()))
                .thenReturn(productDetailsResponse);
    }

    @Test
    public void shouldReturnSingleProductForRequestedCode() throws RequestException {
        // given
        BancoBpiProductData p1 = createProduct(BancoBpiProductType.LOAN.getCode());
        BancoBpiProductData p2 = createProduct(BancoBpiProductType.DEPOSIT.getCode());
        Mockito.when(productsData.getAllProducts()).thenReturn(Lists.newArrayList(p1, p2));
        Mockito.when(productDetailsResponse.getInitialDate())
                .thenReturn(LocalDate.parse("2019-06-11"));
        Mockito.when(productDetailsResponse.getFinalDate())
                .thenReturn(LocalDate.parse("2020-06-11"));
        Mockito.when(productDetailsResponse.getInitialBalance()).thenReturn(new BigDecimal(100));
        Mockito.when(productDetailsResponse.getOwner()).thenReturn("Owner");
        // when
        List<BancoBpiProductData> products =
                objectUnderTest.fetchProductsByType(BancoBpiProductType.LOAN);
        // then
        Assert.assertFalse(products.isEmpty());
        Assert.assertEquals(1, products.size());
        BancoBpiProductData p = products.get(0);
        Assert.assertEquals(BancoBpiProductType.LOAN.getCode(), p.getCodeAlfa());
        Assert.assertEquals(productDetailsResponse.getOwner(), p.getOwner());
        Assert.assertEquals(productDetailsResponse.getInitialBalance(), p.getInitialBalance());
        Assert.assertEquals(productDetailsResponse.getInitialDate(), p.getInitialDate());
        Assert.assertEquals(productDetailsResponse.getFinalDate(), p.getFinalDate());
    }

    @Test
    public void shouldReturnMultipleProductForRequestedCodes() throws RequestException {
        // given
        BancoBpiProductData p1 = createProduct(BancoBpiProductType.LOAN.getCode());
        BancoBpiProductData p2 = createProduct(BancoBpiProductType.MORTGAGE.getCode());
        Mockito.when(productsData.getAllProducts()).thenReturn(Lists.newArrayList(p1, p2));
        Mockito.when(productDetailsResponse.getInitialDate())
                .thenReturn(LocalDate.parse("2019-06-11"));
        Mockito.when(productDetailsResponse.getFinalDate())
                .thenReturn(LocalDate.parse("2020-06-11"));
        Mockito.when(productDetailsResponse.getInitialBalance()).thenReturn(new BigDecimal(100));
        Mockito.when(productDetailsResponse.getOwner()).thenReturn("Owner");
        // when
        List<BancoBpiProductData> products =
                objectUnderTest.fetchProductsByType(
                        BancoBpiProductType.LOAN, BancoBpiProductType.MORTGAGE);
        // then
        Assert.assertFalse(products.isEmpty());
        Assert.assertEquals(2, products.size());
        Assert.assertEquals(BancoBpiProductType.LOAN.getCode(), products.get(0).getCodeAlfa());
        Assert.assertEquals(BancoBpiProductType.MORTGAGE.getCode(), products.get(1).getCodeAlfa());
    }

    private BancoBpiProductData createProduct(String code) {
        BancoBpiProductData p = new BancoBpiProductData();
        p.setCodeAlfa(code);
        return p;
    }
}
