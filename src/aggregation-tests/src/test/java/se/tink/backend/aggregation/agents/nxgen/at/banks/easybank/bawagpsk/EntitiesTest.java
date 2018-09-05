package se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Body;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Disposer;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Envelope;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.FinancialInstitute;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.LoginResponseEntity;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.OK;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Product;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.ProductID;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.entities.Products;
import se.tink.backend.aggregation.agents.nxgen.at.banks.easybank.bawagpsk.rpc.LoginResponse;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import static org.hamcrest.core.Is.is;

public final class EntitiesTest {
    @Test
    public void testGetInvalidIbans() {
        // Try triggering a NullPointerException
        LoginResponse loginResponse;

        loginResponse = new LoginResponse(null);
        Assert.assertTrue(loginResponse.getInvalidIbans().isEmpty());

        final Envelope envelope = new Envelope();
        loginResponse = new LoginResponse(envelope);
        Assert.assertTrue(loginResponse.getInvalidIbans().isEmpty());

        final Body body = new Body();
        envelope.setBody(body);
        Assert.assertTrue(loginResponse.getInvalidIbans().isEmpty());

        final LoginResponseEntity loginResponseEntity = new LoginResponseEntity();
        body.setLoginResponseEntity(loginResponseEntity);
        Assert.assertTrue(loginResponse.getInvalidIbans().isEmpty());

        final OK ok = new OK();
        loginResponseEntity.setOk(ok);
        Assert.assertTrue(loginResponse.getInvalidIbans().isEmpty());

        final Disposer disposer = new Disposer();
        ok.setDisposer(disposer);
        Assert.assertTrue(loginResponse.getInvalidIbans().isEmpty());

        final Products products = new Products();
        disposer.setProducts(products);
        Assert.assertTrue(loginResponse.getInvalidIbans().isEmpty());

        final List<Product> productList = new ArrayList<>();
        products.setProductList(productList);
        Assert.assertTrue(loginResponse.getInvalidIbans().isEmpty());

        productList.add(null);
        Assert.assertTrue(loginResponse.getInvalidIbans().isEmpty());

        final Product product = new Product();
        productList.add(product);
        Assert.assertTrue(loginResponse.getInvalidIbans().isEmpty());

        final ProductID productId = new ProductID();
        product.setProductID(productId);
        Assert.assertTrue(loginResponse.getInvalidIbans().isEmpty());

        final FinancialInstitute financialInstitute = new FinancialInstitute();
        productId.setFinancialInstitute(financialInstitute);
        Assert.assertTrue(loginResponse.getInvalidIbans().isEmpty());

        final String invalidBic = "mybic";
        financialInstitute.setBIC(invalidBic);
        Assert.assertTrue(loginResponse.getInvalidIbans().isEmpty());

        final String invalidIban = "myIban";
        productId.setIban(invalidIban);
        Assert.assertThat(loginResponse.getInvalidIbans(),
                is(Collections.singletonList(new IbanIdentifier(invalidBic, invalidIban))));
    }
}
