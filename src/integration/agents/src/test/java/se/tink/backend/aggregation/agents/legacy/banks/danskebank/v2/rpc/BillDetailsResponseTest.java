package se.tink.backend.aggregation.agents.legacy.banks.danskebank.v2.rpc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc.BillDetailsEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BillDetailsResponseTest {

    @Test
    public void testBillDetailsDeserialize() {
        String source =
                "{\n"
                        + "  \"Amount\": 3159,\n"
                        + "  \"Date\": \"/Date(1596405600000+0200)/\",\n"
                        + "  \"Fee\": 1.5,\n"
                        + "  \"FromAccountId\": \"31415926\",\n"
                        + "  \"FromAccountName\": \"Företagskonto -\",\n"
                        + "  \"FromAccountText\": \"5155-5001 ÖRESUNDSKR\",\n"
                        + "  \"ReceiverName\": \"ÖRESUNDSKRAFT AB - 5155-5001\",\n"
                        + "  \"ReceiverText\": \"Meddelande kan inte lämnas på avi\",\n"
                        + "  \"Reference\": \"2569055706\"\n"
                        + "}";
        BillDetailsEntity billDetailsEntity =
                SerializationUtils.deserializeFromString(source, BillDetailsEntity.class);
        assertThat(billDetailsEntity.getReference()).isEqualTo("2569055706");
    }
}
