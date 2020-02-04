package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.common.RequestException;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiEntityManager;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiProductData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.BancoBpiProductsData;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.entity.TransactionalAccountBaseInfo;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.BancoBpiProductType;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.ProductAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.ProductDetailsRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.ProductDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.ProductsFetchRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account.TransactionalAccountBalanceRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account.TransactionsFetchRequest;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.account.TransactionsFetchResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.creditcard.PreAccountAndTransactionsFetchRequest;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class BancoBpiClientApi {

    private TinkHttpClient httpClient;
    private BancoBpiEntityManager entityManager;
    private ProductAccountFetcher productAccountFetcher;

    public BancoBpiClientApi(TinkHttpClient httpClient, BancoBpiEntityManager entityManager) {
        this.httpClient = httpClient;
        this.entityManager = entityManager;
    }

    public BancoBpiProductsData fetchProductsData() throws RequestException {
        Optional<BancoBpiProductsData> productsData = entityManager.getProductsData();
        if (entityManager.getProductsData().isPresent()) {
            return productsData.get();
        }
        new PreAccountAndTransactionsFetchRequest(entityManager).call(httpClient);
        entityManager.setProductsData(new ProductsFetchRequest(entityManager).call(httpClient));
        return entityManager.getProductsData().get();
    }

    public ProductDetailsResponse fetchProductDetails(BancoBpiProductData productData)
            throws RequestException {
        return new ProductDetailsRequest(entityManager, productData).call(httpClient);
    }

    public List<BancoBpiProductData> getProductsByType(BancoBpiProductType... types)
            throws RequestException {
        if (productAccountFetcher == null) {
            productAccountFetcher = new ProductAccountFetcher(this);
        }
        return productAccountFetcher.fetchProductsByType(types);
    }

    public BigDecimal fetchAccountBalance(TransactionalAccountBaseInfo accountBaseInfo)
            throws RequestException {
        return new TransactionalAccountBalanceRequest(
                        entityManager.getAuthContext(), accountBaseInfo)
                .call(httpClient);
    }

    public TransactionsFetchResponse fetchAccountTransactions(
            String fetchingUUID, int pageNo, TransactionalAccount account) throws RequestException {
        return new TransactionsFetchRequest(entityManager, fetchingUUID, pageNo, account)
                .call(httpClient);
    }

    public se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.creditcard
                    .TransactionsFetchResponse
            fetchCreditCardTransactions(CreditCardAccount account, int pageNo, String fetchingUUID)
                    throws RequestException {
        return new se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi.product.creditcard
                        .TransactionsFetchRequest(entityManager, account, pageNo, fetchingUUID)
                .call(httpClient);
    }
}
