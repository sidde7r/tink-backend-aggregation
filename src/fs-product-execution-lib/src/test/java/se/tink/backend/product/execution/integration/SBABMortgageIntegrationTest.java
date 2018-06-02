package se.tink.backend.product.execution.integration;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.product.execution.api.dto.CreateProductRequest;
import se.tink.backend.product.execution.api.dto.ProductInformationRequest;
import se.tink.backend.product.execution.integration.configurations.InjectorFactory;
import se.tink.backend.product.execution.integration.data.ApplicationFactory;
import se.tink.backend.product.execution.model.FetchProductInformationParameterKey;
import se.tink.backend.product.execution.model.ProductType;
import se.tink.backend.product.execution.model.User;
import se.tink.backend.product.execution.resources.SBABProductExecutorResource;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;

@RunWith(MockitoJUnitRunner.class)
public class SBABMortgageIntegrationTest {
    private final Injector injector = InjectorFactory.get("etc/development-product-executor-server.yml");

    private User user;
    private SignableOperation signableOperation;
    private GenericApplication application;

    private SBABProductExecutorResource resource;

    @Before
    public void setup() {
        user = ApplicationFactory.createUser();
        resource = injector.getInstance(SBABProductExecutorResource.class);
    }

    @Test
    public void testFetchProductInformation() throws Exception {
        Map<FetchProductInformationParameterKey, Object> parameters = Maps.newHashMap();
        parameters.put(FetchProductInformationParameterKey.MARKET_VALUE, 3000000);
        parameters.put(FetchProductInformationParameterKey.MORTGAGE_AMOUNT, 2000000);
        parameters.put(FetchProductInformationParameterKey.NUMBER_OF_APPLICANTS, 1);
        UUID productInstanceId = UUID.randomUUID();

        ProductInformationRequest request = new ProductInformationRequest(user, ProductType.MORTGAGE, productInstanceId,
                parameters);

        resource.fetchProductInformation(request);
    }

    @Test
    public void testSendMortgageApplicationWithSuneSkogApplicant() throws Exception {
        String ssn = ApplicationFactory.SSN_SUNE_SKOG;

        application = ApplicationFactory.createApplication(ApplicationType.SWITCH_MORTGAGE_PROVIDER, ssn);
        signableOperation = new SignableOperation(application);

        application.setPersonalNumber(ssn);
        application.setFieldGroups(ApplicationFactory.createFieldGroupsForApartment(ssn));

        CreateProductRequest createProductRequest = new CreateProductRequest(user, application, signableOperation,
                ApplicationFactory.createCredentials(ssn));

        resource.createProduct(createProductRequest);
    }

    @Test
    public void testSendMortgageApplicationWithJadwigaJensenApplicant() throws Exception {
        String ssn = ApplicationFactory.SSN_JADWIGA_JENSEN;

        application = ApplicationFactory.createApplication(ApplicationType.SWITCH_MORTGAGE_PROVIDER, ssn);
        signableOperation = new SignableOperation(application);

        application.setPersonalNumber(ssn);
        application.setFieldGroups(ApplicationFactory.createFieldGroupsForHouse(ssn));

        CreateProductRequest createProductRequest = new CreateProductRequest(user, application, signableOperation,
                ApplicationFactory.createCredentials(ssn));

        resource.createProduct(createProductRequest);
    }

    @Test
    public void testSendMortgageApplicationWithMaudLidenApplicant() throws Exception {
        String ssn = ApplicationFactory.SSN_MAUD_LIDEN;

        application = ApplicationFactory.createApplication(ApplicationType.SWITCH_MORTGAGE_PROVIDER, ssn);
        signableOperation = new SignableOperation(application);

        application.setPersonalNumber(ssn);
        application.setFieldGroups(ApplicationFactory.createFieldGroupsForApartment(ssn));

        CreateProductRequest createProductRequest = new CreateProductRequest(user, application, signableOperation,
                ApplicationFactory.createCredentials(ssn));

        resource.createProduct(createProductRequest);
    }

    @Test
    public void testSendMortgageApplicationWithCarlJonssonApplicant() throws Exception {
        String ssn = ApplicationFactory.SSN_CARL_JONSSON;

        application = ApplicationFactory.createApplication(ApplicationType.SWITCH_MORTGAGE_PROVIDER, ssn);
        signableOperation = new SignableOperation(application);

        application.setPersonalNumber(ssn);
        application.setFieldGroups(ApplicationFactory.createFieldGroupsForApartment(ssn));

        CreateProductRequest createProductRequest = new CreateProductRequest(user, application, signableOperation,
                ApplicationFactory.createCredentials(ssn));

        resource.createProduct(createProductRequest);
    }
}
