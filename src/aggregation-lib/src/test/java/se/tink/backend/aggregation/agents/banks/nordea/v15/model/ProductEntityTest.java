package se.tink.backend.aggregation.agents.banks.nordea.v15.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ProductEntityTest {
    private static final String LOAN_FORMAT = "{\"productType\":{\"$\":\"Loan\"},\"cardGroup\":{},\"productId\":{\"@id\":{\"$\":\"abc123\"},\"@view\":{\"$\":false},\"@pay\":{\"$\":false},\"@deposit\":{\"$\":true},\"@ownTransferFrom\":{\"$\":false},\"@ownTransferTo\":{\"$\":false},\"@thirdParty\":{\"$\":false},\"@paymentAccount\":{\"$\":\"false\"},\"$\":\"NDEASESSXXX-SE1-SEK-000001122334455\"},\"productNumber\":{\"$\":\"3998 12 34567\"},\"accountType\":{\"$\":\"1130\"},\"productTypeExtension\":%s,\"currency\":{\"$\":\"SEK\"},\"nickName\":{\"$\":\"Lån 1\"},\"productCode\":{},\"productName\":{},\"balance\":{\"$\":-1234567.00},\"fundsAvailable\":{},\"branchId\":{},\"isMainCard\":{\"$\":\"false\"},\"warningCode\":{}}";

    private static final String LOAN_WITH_EXTENSION = String.format(LOAN_FORMAT, "{\"$\":\"SE00200\"}");
    private static final String LOAN_WITHOUT_EXTENSION = String.format(LOAN_FORMAT, "null");

    @Test
    public void deserializeProductTypeAndExtension() throws IOException {
        ProductEntity productEntity = new ObjectMapper().readValue(LOAN_WITH_EXTENSION, ProductEntity.class);

        assertThat(productEntity.getNordeaProductType()).isEqualTo("Loan");
        assertThat(productEntity.getNordeaProductTypeExtension()).isEqualTo("SE00200");
    }

    @Test
    public void deserializeProductTypeExtension_whenNull_doesntThrowNpe() throws IOException {
        ProductEntity productEntity = new ObjectMapper().readValue(LOAN_WITHOUT_EXTENSION, ProductEntity.class);

        assertThat(productEntity.getNordeaProductTypeExtension()).isEqualTo(null);
    }
}
