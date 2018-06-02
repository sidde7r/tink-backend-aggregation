package se.tink.backend.utils.guavaimpl;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import java.util.HashMap;
import java.util.UUID;
import javax.annotation.Nullable;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Application;
import se.tink.backend.core.application.ApplicationPropertyKey;
import se.tink.backend.core.transfer.Transfer;

public class Functions {

    public static <F, T> Function<F, T> cast(final Class<T> clazz) {
        return clazz::cast;
    }

    public static Function<Transfer, Transfer> populateUserAndCredentialsIdAndOriginals(final String userid,
            final String credentialsId) {

        return transfer -> {
            transfer.setUserId(UUIDUtils.fromTinkUUID(userid));
            transfer.setCredentialsId(UUIDUtils.fromTinkUUID(credentialsId));
            transfer.setOriginalDestination(transfer.getDestination());
            transfer.setOriginalSource(transfer.getSource());
            return transfer;
        };
    }

    public static final Function<Application, UUID> APPLICATION_TO_PRODUCT_INSTANCE_ID =
            new Function<Application, UUID>() {
                @Nullable
                @Override
                public UUID apply(Application application) {
                    HashMap<ApplicationPropertyKey, Object> applicationProperties = application.getProperties();

                    if (applicationProperties == null) {
                        return null;
                    }

                    String productId = (String) applicationProperties.get(ApplicationPropertyKey.PRODUCT_INSTANCE_ID);

                    if (Strings.isNullOrEmpty(productId)) {
                        return null;
                    }

                    return UUID.fromString(productId);
                }
            };
}
