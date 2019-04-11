package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

public class GroupedMovementsEntityTest {

    @Test
    public void testParseCreditCardTransactionWithNullValues() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        GroupedMovementsEntity groupedMovementsEntity =
                mapper.readValue(groupedMovementsEntityString, GroupedMovementsEntity.class);

        Collection<CreditCardTransaction> creditCardTransactions =
                groupedMovementsEntity.toTinkTransactions(null);

        Assert.assertEquals(1, creditCardTransactions.size());
    }

    private static String groupedMovementsEntityString =
            "{\n"
                    + "  \"moreElements\": false,\n"
                    + "  \"periodMovementModelList\": [\n"
                    + "    {\n"
                    + "      \"periodType\": \"lastfivedays\",\n"
                    + "      \"periodDate\": \"30-08-2018\",\n"
                    + "      \"genericMovementWrapperList\": {\n"
                    + "        \"movements\": [\n"
                    + "          {\n"
                    + "            \"cardMovement\": null\n"
                    + "          },\n"
                    + "          {\n"
                    + "            \"cardMovement\": {\n"
                    + "              \"movementNumber\": \"1234567\",\n"
                    + "              \"concept\": \"MERCHANT 2\",\n"
                    + "              \"date\": \"27-08-2018\",\n"
                    + "              \"hour\": \"00:00:00\",\n"
                    + "              \"city\": \"MADRID\",\n"
                    + "              \"canSplit\": false,\n"
                    + "              \"amount\": {\n"
                    + "                \"value\": \"11,00\",\n"
                    + "                \"currency\": \"EUR\"\n"
                    + "              },\n"
                    + "              \"indFracEnabled\": true,\n"
                    + "              \"indMov\": \"3\",\n"
                    + "              \"isTraspasable\": true,\n"
                    + "              \"commission\": {\n"
                    + "                \"value\": \"0,00\",\n"
                    + "                \"currency\": \"EUR\"\n"
                    + "              },\n"
                    + "              \"originAmount\": {\n"
                    + "                \"value\": \"11,00\",\n"
                    + "                \"currency\": \"EUR\"\n"
                    + "              },\n"
                    + "              \"address\": \"MADRID\",\n"
                    + "              \"point\": null,\n"
                    + "              \"isConfirmed\": true,\n"
                    + "              \"siaidcdmov\": null,\n"
                    + "              \"solfranc\": null,\n"
                    + "              \"isSplit\": true\n"
                    + "            }\n"
                    + "          }\n"
                    + "        ]\n"
                    + "      }\n"
                    + "    },\n"
                    + "    {\n"
                    + "      \"periodType\": \"lastfivedays\",\n"
                    + "      \"periodDate\": \"26-08-2018\",\n"
                    + "      \"genericMovementWrapperList\": null\n"
                    + "    }\n"
                    + "  ]\n"
                    + "}";
}
