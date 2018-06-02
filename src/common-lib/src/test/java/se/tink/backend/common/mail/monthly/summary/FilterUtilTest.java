package se.tink.backend.common.mail.monthly.summary;

import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.common.mail.monthly.summary.utils.Filters;
import se.tink.backend.core.User;

public class FilterUtilTest {

    @Test
    public void verifyUsersWithUserIdAbove() {

        User user = new User();
        user.setId("49ec4411b41344d68a32aefbaf1798bd"); // last char diff

        Assert.assertTrue(Filters.usersWithUserIdAbove("49ec4411b41344d68a32aefbaf1798bc").call(user));
    }

    @Test
    public void verifyUsersWithUserIdBelow() {

        User user = new User();
        user.setId("47ec4411b41344d68a32aefbaf1798bc"); // second char diff

        Assert.assertFalse(Filters.usersWithUserIdAbove("49ec4411b41344d68a32aefbaf1798bc").call(user));
    }

    @Test
    public void verifyUsersWithSameId() {

        User user = new User();
        user.setId("49ec4411b41344d68a32aefbaf1798bc");

        Assert.assertFalse(Filters.usersWithUserIdAbove("49ec4411b41344d68a32aefbaf1798bc").call(user));
    }

}
