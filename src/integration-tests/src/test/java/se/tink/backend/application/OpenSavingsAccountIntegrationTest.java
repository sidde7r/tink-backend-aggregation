package se.tink.backend.application;

import com.google.api.client.util.Objects;
import java.util.Optional;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import org.assertj.core.util.Strings;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.application.ApplicationNotFoundException;
import se.tink.backend.common.application.ApplicationNotModifiableException;
import se.tink.backend.common.application.ApplicationNotValidException;
import se.tink.backend.common.exceptions.FeatureFlagNotEnabledException;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.main.controllers.ApplicationServiceController;
import se.tink.backend.rpc.application.SubmitApplicationFormCommand;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * TODO this is a unit test
 */
public class OpenSavingsAccountIntegrationTest extends OpenSavingsAccountIntegrationTestBase {
    @Context
    private HttpHeaders headers;

    ApplicationServiceController applicationServiceController;

    @Before
    public void setUp() {
        applicationServiceController = new ApplicationServiceController(serviceContext, new MetricRegistry(),
                productExecutorServiceFactory);
    }

    @Test
    public void testWholeFlow_collector()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser();
        Application application = getApplication();
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillFormUsingCollector(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
        }

        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
    }

    @Ignore
    @Test
    public void testWholeFlow_sbab()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser();
        Application application = getApplication();
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillFormUsingSBAB(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
        }

        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
    }

    @Ignore
    @Test
    public void testWholeFlowWithCitizenshipInOtherCountry_sbab()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser();
        Application application = getApplication();
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillFormUsingSBABWithCitizenshipInOtherCountry(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
        }

        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
    }

    @Ignore
    @Test
    public void testWholeFlowWithCitizenshipInASecondCountry_sbab()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser();
        Application application = getApplication();
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillFormUsingSBABWithCitizenshipInASecondCountry(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
        }

        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
    }

    @Ignore
    @Test
    public void testWholeFlowWithCitizenshipInAThirdCountry_sbab()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser();
        Application application = getApplication();
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillFormUsingSBABWithCitizenshipInAThirdCountry(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
        }

        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
    }

    @Ignore
    @Test
    public void testWholeFlowWithTaxableInOtherCountry_sbab()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser();
        Application application = getApplication();
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillFormUsingSBABWithTaxableInOtherCountry(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
        }

        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
    }

    @Ignore
    @Test
    public void testWholeFlowWithTaxableInASecondCountry_sbab()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUser();
        Application application = getApplication();
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillFormUsingSBABWithTaxableSecondCountry(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
        }

        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
    }

    @Ignore
    @Test
    public void testApplicationSavingsFlowPayload_payloadNotNullAndNotEmpty()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException {
        getAuthenticatedUser();
        Application application = getApplication();
        String payload = null;

        for (ApplicationForm form : application.getForms()) {
            Assert.assertNotNull(form);
            if (Objects.equal(form.getName(), ApplicationFormName.OPEN_SAVINGS_ACCOUNT_PRODUCT_DETAILS)) {
                payload = form.getSerializedPayload();
                break;
            }
        }

        Assert.assertFalse(Strings.isNullOrEmpty(payload));
    }

    private Application getApplication() throws FeatureFlagNotEnabledException, ApplicationNotValidException {
        Application application = createApplication(applicationServiceController, getUserAgent());

        Assert.assertEquals(ApplicationStatusKey.CREATED, application.getStatus().getKey());

        int expectedNumberOfFormsAfterCreation = 5;
        Assert.assertEquals(expectedNumberOfFormsAfterCreation, application.getForms().size());

        return application;
    }

    private ApplicationForm getNextFormThatIsNotCompleted(Application application) {
        for (ApplicationForm form : application.getForms()) {
            if (!Objects.equal(form.getStatus().getKey(), ApplicationFormStatusKey.COMPLETED)) {
                return form;
            }
        }
        return null;
    }

    private Optional<String> getUserAgent() {
        return Optional.ofNullable(RequestHeaderUtils.getUserAgent(headers));
    }

}
