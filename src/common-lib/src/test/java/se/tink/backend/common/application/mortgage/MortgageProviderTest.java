package se.tink.backend.common.application.mortgage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import se.tink.backend.core.Application;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductInstance;
import se.tink.backend.core.product.ProductTemplate;

public class MortgageProviderTest {
    @Test
    public void fromApplication() {
        Application sebApplication = createApplicationWithProvider("seb-bankid");
        Application sbabApplication = createApplicationWithProvider("sbab-bankid");
        Application otherApplication = createApplicationWithProvider("other-provider");

        assertThat(MortgageProvider.fromApplication(sebApplication)).isEqualTo(MortgageProvider.SEB_BANKID);
        assertThat(MortgageProvider.fromApplication(sbabApplication)).isEqualTo(MortgageProvider.SBAB_BANKID);
        assertThat(MortgageProvider.fromApplication(otherApplication)).isEqualTo(MortgageProvider.UNKNOWN);
    }

    private Application createApplicationWithProvider(String providerName) {
        Application application = new Application();

        ProductTemplate productTemplate = new ProductTemplate();
        productTemplate.setProviderName(providerName);

        ProductArticle productArticle = new ProductArticle(productTemplate, new ProductInstance());
        application.setProductArticle(productArticle);

        return application;
    }
}
