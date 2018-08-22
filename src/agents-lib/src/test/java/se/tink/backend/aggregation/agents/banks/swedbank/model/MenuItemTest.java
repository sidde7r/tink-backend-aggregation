package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class MenuItemTest {
    private static final String SERIALIZED_MENU_ITEM = "{\"name\":\"Information för att skapa en betalning\",\"uri\":\"/v5/payment/baseinfo\",\"method\":\"GET\",\"authorization\":\"AUTHORIZED\"}";

    @Test
    public void deserialize() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        MenuItem menuItem = objectMapper.readValue(SERIALIZED_MENU_ITEM, MenuItem.class);

        assertThat(menuItem.getName()).isEqualTo("Information för att skapa en betalning");
        assertThat(menuItem.getUri()).isEqualTo("/v5/payment/baseinfo");
        assertThat(menuItem.getAuthorization()).isEqualTo(MenuItem.Authorization.AUTHORIZED);
        assertThat(menuItem.getMethod()).isEqualTo(MenuItem.Method.GET);
        assertThat(menuItem.isAuthorizedURI(MenuItem.Method.GET)).isTrue();
    }
}
