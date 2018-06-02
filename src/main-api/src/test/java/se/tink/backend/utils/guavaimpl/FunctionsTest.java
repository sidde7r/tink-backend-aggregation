package se.tink.backend.utils.guavaimpl;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import java.util.HashMap;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Application;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.transfer.Transfer;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Enclosed.class)
public class FunctionsTest {
    public static class PopulateUserAndCredentialsIdAndOriginals {
        @Test
        public void populateUserAndCredentialsId() throws Exception {

            Function<Transfer, Transfer> function =
                    Functions.populateUserAndCredentialsIdAndOriginals("2abd9d9270064c2a987a0f5321a37c39", "03acb267f9c84b1d8c6148bbf9d1d3e1");

            Transfer newTransfer = function.apply(new Transfer());

            Assert.assertEquals(UUIDUtils.fromTinkUUID("2abd9d9270064c2a987a0f5321a37c39"), newTransfer.getUserId());
            Assert.assertEquals(UUIDUtils.fromTinkUUID("03acb267f9c84b1d8c6148bbf9d1d3e1"), newTransfer.getCredentialsId());
        }

        @Test
        public void populateOriginalSourceAndOriginalDestination() throws Exception {

            Function<Transfer, Transfer> function =
                    Functions.populateUserAndCredentialsIdAndOriginals("2abd9d9270064c2a987a0f5321a37c39", "03acb267f9c84b1d8c6148bbf9d1d3e1");

            Transfer newTransfer = function.apply(new Transfer());

            Assert.assertEquals(newTransfer.getSource(), newTransfer.getOriginalSource());
            Assert.assertEquals(newTransfer.getDestination(), newTransfer.getOriginalDestination());
        }
    }

    public static class ApplicationToProductInstanceId {
        @Test
        public void noApplicationPropertiesReturnsNull() {
            Application application = new Application();
            application.setProperties(null);

            assertThat(Functions.APPLICATION_TO_PRODUCT_INSTANCE_ID.apply(application)).isNull();
        }

        @Test
        public void emptyProductIdInPropertiesReturnsNull() {
            Application application = new Application();
            HashMap<ApplicationPropertyKey, Object> properties = Maps.newHashMap();
            properties.put(ApplicationPropertyKey.PRODUCT_INSTANCE_ID, "");
            application.setProperties(properties);

            assertThat(Functions.APPLICATION_TO_PRODUCT_INSTANCE_ID.apply(application)).isNull();
        }

        @Test
        public void productInstanceIdInPropertiesReturnsUuid() {
            Application application = new Application();
            HashMap<ApplicationPropertyKey, Object> properties = Maps.newHashMap();
            properties.put(ApplicationPropertyKey.PRODUCT_INSTANCE_ID, "e7f8d424-87e9-440f-adeb-f9f26c182954");
            application.setProperties(properties);

            assertThat(Functions.APPLICATION_TO_PRODUCT_INSTANCE_ID.apply(application))
                    .isEqualTo(UUID.fromString("e7f8d424-87e9-440f-adeb-f9f26c182954"));
        }
    }
}
