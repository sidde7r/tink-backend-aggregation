package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.parser.NordeaDkParser;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.parser.NordeaDkTransactionParser;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.NordeaV20Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.entities.ProductEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.rpc.initialContext.InitialContextResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.parsers.NordeaV20Parser;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.agents.rpc.Credentials;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class InitialContextResponseTest {

    @Test
    public void parseResponse() throws Exception {
        InitialContextResponse response = InitialContextResponseTestData.getTestData();
        assertNotNull(response.getData());
        assertNotNull(response.getData().getProducts());
        String productType = response.getData().getProducts().get(0).getProductType();
        double balance = response.getData().getProducts().get(0).getBalance();
        String productName = response.getData().getProducts().get(0).getProductName();

        assertEquals("Account", productType);
        assertEquals("Grundkonto", productName);
        assertTrue(150.00 == balance);
    }

    @Test
    public void getAccount() throws Exception {
        Credentials credentials = new Credentials();
        NordeaV20Parser parser = new NordeaDkParser(new NordeaDkTransactionParser(), credentials);

        InitialContextResponse response = InitialContextResponseTestData.getTestData();
        List<ProductEntity> products = response.getProductsOfTypes(NordeaV20Constants.ProductType.ACCOUNT);
        Map<TransactionalAccount, ProductEntity> productsByAccount = products.stream()
                .collect(Collectors.toMap(parser::parseAccount, Function.identity()));

        Collection<TransactionalAccount> accounts = productsByAccount.keySet();

        assertEquals(1, accounts.size());
        accounts.forEach(a -> assertEquals("0381100143", a.getAccountNumber()));
    }
}
