package se.tink.backend.common.application;

import java.util.Optional;
import java.util.List;
import se.tink.backend.core.User;
import se.tink.backend.core.application.ConfirmationFormListData;
import se.tink.libraries.application.GenericApplication;
import se.tink.backend.core.product.ProductArticle;

public interface ApplicationSummaryCompiler {
    List<ConfirmationFormListData> getSummary(GenericApplication genericApplication,
            User user, Optional<ProductArticle> productArticle);
}
