package se.tink.backend.main.transports;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.core.Category;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.backend.main.controllers.CategoryController;

import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CategoryJerseyTransportTest {

    @Mock CategoryController controller;
    @InjectMocks CategoryJerseyTransport transport;

    @Test
    public void listCategoriesInDefaultLocale() {
        List<Category> categories = singletonList(new Category());
        when(controller.list(Optional.empty())).thenReturn(categories);

        User user = null;
        String locale = null;
        assertEquals(categories, transport.list(user, locale));
    }

    @Test
    public void listCategoriesInRequestedLocale() {
        List<Category> categories = singletonList(new Category());
        when(controller.list(Optional.of("locale"))).thenReturn(categories);

        User user = null;
        assertEquals(categories, transport.list(user, "locale"));
    }

    @Test
    public void listCategoriesInUserLocale() {
        List<Category> categories = singletonList(new Category());
        when(controller.list(Optional.of("userLocale"))).thenReturn(categories);

        User user = new User();
        UserProfile profile = new UserProfile();
        profile.setLocale("userLocale");
        user.setProfile(profile);
        assertEquals(categories, transport.list(user, "requestLocale"));
    }

}
