package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class GetCardBalancesOutEntityTest {
    private static ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void verifyDeduplication_whenAllFieldsAreEqual() throws IOException {
        CardsEntity cardsEntity1 = MAPPER.readValue(testCard1, CardsEntity.class);
        CardsEntity cardsEntity2 = MAPPER.readValue(testCard1, CardsEntity.class);
        List<CardsEntity> cardEntities = Arrays.asList(cardsEntity1, cardsEntity2);

        GetCardBalancesOutEntity cardBalancesOutEntity = new GetCardBalancesOutEntity();
        cardBalancesOutEntity.setCards(cardEntities);

        List<CardsEntity> distinctCardsList = cardBalancesOutEntity.getDistinctCardsList();

        Assert.assertEquals(1, distinctCardsList.size());
    }

    @Test
    public void verifyNoDeduplication_whenAllFieldsAreDifferent() throws IOException {
        CardsEntity cardsEntity1 = MAPPER.readValue(testCard1, CardsEntity.class);
        CardsEntity cardsEntity2 = MAPPER.readValue(testCard3, CardsEntity.class);
        List<CardsEntity> cardEntities = Arrays.asList(cardsEntity1, cardsEntity2);

        GetCardBalancesOutEntity cardBalancesOutEntity = new GetCardBalancesOutEntity();
        cardBalancesOutEntity.setCards(cardEntities);

        List<CardsEntity> distinctCardsList = cardBalancesOutEntity.getDistinctCardsList();

        Assert.assertEquals(2, distinctCardsList.size());
    }

    @Test
    public void verifyDeduplication_onlyForDuplicateCard() throws IOException {
        CardsEntity cardsEntity1 = MAPPER.readValue(testCard1, CardsEntity.class);
        CardsEntity cardsEntity2 = MAPPER.readValue(testCard1, CardsEntity.class);
        CardsEntity cardsEntity3 = MAPPER.readValue(testCard2, CardsEntity.class);
        List<CardsEntity> cardEntities = Arrays.asList(cardsEntity1, cardsEntity2, cardsEntity3);

        GetCardBalancesOutEntity cardBalancesOutEntity = new GetCardBalancesOutEntity();
        cardBalancesOutEntity.setCards(cardEntities);

        List<CardsEntity> distinctCardsList = cardBalancesOutEntity.getDistinctCardsList();

        Assert.assertEquals(2, distinctCardsList.size());
    }

    private static String testCard1 =
            "      {\n"
                    + "        \"cardId\": {\n"
                    + "          \"$\": \"abcdef123456\"\n"
                    + "        },\n"
                    + "        \"creditLimit\": {\n"
                    + "          \"$\": \"10000.0\"\n"
                    + "        },\n"
                    + "        \"fundsAvailable\": {\n"
                    + "          \"$\": \"100.00\"\n"
                    + "        },\n"
                    + "        \"balance\": {\n"
                    + "          \"$\": \"9900.00\"\n"
                    + "        },\n"
                    + "        \"ownerName\": {\n"
                    + "          \"$\": \"TEST TESTSSON\"\n"
                    + "        }\n"
                    + "      }";

    private static String testCard2 =
            "      {\n"
                    + "        \"cardId\": {\n"
                    + "          \"$\": \"abcdef123456\"\n"
                    + "        },\n"
                    + "        \"creditLimit\": {\n"
                    + "          \"$\": \"10000.0\"\n"
                    + "        },\n"
                    + "        \"fundsAvailable\": {\n"
                    + "          \"$\": \"100.00\"\n"
                    + "        },\n"
                    + "        \"balance\": {\n"
                    + "          \"$\": \"9900.00\"\n"
                    + "        },\n"
                    + "        \"ownerName\": {\n"
                    + "          \"$\": \"RANDOM PERSON\"\n"
                    + "        }\n"
                    + "      }";

    private static String testCard3 =
            "      {\n"
                    + "        \"cardId\": {\n"
                    + "          \"$\": \"asfdnhjhngbvc\"\n"
                    + "        },\n"
                    + "        \"creditLimit\": {\n"
                    + "          \"$\": \"10000.0\"\n"
                    + "        },\n"
                    + "        \"fundsAvailable\": {\n"
                    + "          \"$\": \"1000.00\"\n"
                    + "        },\n"
                    + "        \"balance\": {\n"
                    + "          \"$\": \"9000.00\"\n"
                    + "        },\n"
                    + "        \"ownerName\": {\n"
                    + "          \"$\": \"RANDOM PERSON\"\n"
                    + "        }\n"
                    + "      }";
}
