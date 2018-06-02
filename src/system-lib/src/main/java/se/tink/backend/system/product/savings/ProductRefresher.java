package se.tink.backend.system.product.savings;

import se.tink.backend.core.product.ProductArticle;

public interface ProductRefresher {
    /**
     * @return true if product refresh was executed, false if skipped for some reason.
     */
    boolean refresh(ProductArticle productArticle) throws Exception;
}
