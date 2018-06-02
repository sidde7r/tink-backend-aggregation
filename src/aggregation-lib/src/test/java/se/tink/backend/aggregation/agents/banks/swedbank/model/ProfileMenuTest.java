package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class ProfileMenuTest {
    private static final String SERIALIZED_PROFILE_MENU = "{\"selectedProfile\":{\"activeProfileLanguage\":\"sv\",\"bankId\":\"***\",\"customerNumber\":\"***\",\"bankName\":\"Swedbank AB (publ)\",\"customerInternational\":false,\"customerName\":\"***\",\"youthProfile\":false},\"menuItems\":{\"IdentificationAdministrationBankidOptions\":{\"name\":\"Mobilt BankID\",\"uri\":\"/v5/payment/baseinfo\",\"method\":\"GET\",\"authorization\":\"AUTHORIZED\"}}}";

    @Test
    public void deserialize() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        ProfileMenu profileMenu = objectMapper.readValue(SERIALIZED_PROFILE_MENU, ProfileMenu.class);

        assertThat(profileMenu.getMenuItems()).containsKey("IdentificationAdministrationBankidOptions");

        MenuItem menuItem = profileMenu.getMenuItems().get("IdentificationAdministrationBankidOptions");
        assertThat(menuItem.getName()).isEqualTo("Mobilt BankID");
        assertThat(menuItem.getUri()).isEqualTo("/v5/payment/baseinfo");
        assertThat(menuItem.getAuthorization()).isEqualTo(MenuItem.Authorization.AUTHORIZED);
        assertThat(menuItem.getMethod()).isEqualTo(MenuItem.Method.GET);
    }

    @Test
    public void authorizedUriIsAuthorized() {
        MenuItem menuItem = new MenuItem();
        menuItem.setAuthorization(MenuItem.Authorization.AUTHORIZED);
        menuItem.setMethod(MenuItem.Method.GET);
        menuItem.setUri("/v5/payment/baseinfo");

        boolean isAuthorized = menuItem.isAuthorizedURI(MenuItem.Method.GET);

        assertThat(isAuthorized).isTrue();
    }

    @Test
    public void nonAuthorizedUriIsNotAuthorized() {
        MenuItem menuItem = new MenuItem();
        menuItem.setAuthorization(MenuItem.Authorization.REQUIRES_AUTH_METHOD_CHANGE);
        menuItem.setMethod(MenuItem.Method.GET);
        menuItem.setUri("/v5/payment/baseinfo");

        boolean isAuthorized = menuItem.isAuthorizedURI(MenuItem.Method.GET);

        assertThat(isAuthorized).isFalse();
    }

    @Test
    public void nonAuthorizedRequestMethodIsNotAuthorized() {
        MenuItem menuItem = new MenuItem();
        menuItem.setAuthorization(MenuItem.Authorization.AUTHORIZED);
        menuItem.setMethod(MenuItem.Method.GET);
        menuItem.setUri("/v5/payment/baseinfo");

        boolean isAuthorized = menuItem.isAuthorizedURI(MenuItem.Method.POST);

        assertThat(isAuthorized).isFalse();
    }
}
