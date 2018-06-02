package se.tink.backend.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.client.util.Objects;
import com.google.common.collect.Lists;
import java.util.Date;
import java.util.Optional;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.auth.AuthenticatedUser;
import se.tink.backend.common.application.ApplicationAlreadySignedException;
import se.tink.backend.common.application.ApplicationNotCompleteException;
import se.tink.backend.common.application.ApplicationNotFoundException;
import se.tink.backend.common.application.ApplicationNotModifiableException;
import se.tink.backend.common.application.ApplicationNotValidException;
import se.tink.backend.common.application.ApplicationSigningNotInvokableException;
import se.tink.backend.common.config.EmailConfiguration;
import se.tink.backend.common.exceptions.FeatureFlagNotEnabledException;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.resources.RequestHeaderUtils;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.enums.ApplicationFormName;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.guice.configuration.EmailModule;
import se.tink.backend.main.controllers.ApplicationServiceController;
import se.tink.backend.rpc.application.SubmitApplicationCommand;
import se.tink.backend.rpc.application.SubmitApplicationFormCommand;
import se.tink.backend.system.document.core.Applicant;
import se.tink.backend.system.document.core.Mortgage;
import se.tink.backend.system.document.core.PoaDetails;
import se.tink.backend.system.document.core.Residence;
import se.tink.backend.system.document.core.ResidenceType;
import se.tink.backend.system.document.core.SwitchMortgageProvider;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * TODO this is a unit test
 */
@Ignore
public class SwitchMortgageProviderIntegrationTest extends SwitchMortgageProviderIntegrationTestBase {
    @Context
    private HttpHeaders headers;

    ApplicationServiceController applicationServiceController;

    @Before
    public void setUp() {
        applicationServiceController = new ApplicationServiceController(serviceContext, new MetricRegistry(),
                productExecutorServiceFactory);
    }

    @Test
    public void testWholeFlowWithApartmentAsSecurity_providerSEB()
            throws ApplicationNotValidException, ApplicationNotFoundException, ApplicationNotModifiableException,
            FeatureFlagNotEnabledException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithMortgage();
        Application application = getApplication();
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand command = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillApplicationForSEBForm(form);

            application = applicationServiceController.submitForm(command, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());

            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
        }

        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
    }

    @Test
    public void testWholeFlowWithApartmentAsSecurity_providerSBAB()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithMortgage();
        Application application = getApplication();
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillApplicationForSBABForm(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
        }

        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
    }

    @Test
    public void testWholeFlowWithApartmentAsSecurityCreateSignableOperation_providerSBAB()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException, ApplicationNotCompleteException, ApplicationAlreadySignedException,
            ApplicationSigningNotInvokableException, JsonProcessingException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithMortgage();
        Application application = getApplication();
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        SubmitApplicationCommand submitCommand = new SubmitApplicationCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent(), RequestHeaderUtils.getRemoteIp(headers));

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillApplicationForSBABForm(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
        }

        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());

        SignableOperation operation = applicationServiceController.submit(submitCommand);
        Assert.assertEquals(SignableOperationStatuses.CREATED, operation.getStatus());
    }

    @Test
    public void testWholeFlowWithoutMortgage_providerSEB()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithoutMortgage();
        Application application = getApplication();
        String applicationId = UUIDUtils.toTinkUUID(application.getId());
        boolean userHasNoMortgage = false;

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillApplicationForSEBForm(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            if (Objects.equal(updatedForm.get().getName(), ApplicationFormName.CURRENT_MORTGAGES)) {
                if (Objects.equal(updatedForm.get().getStatus().getKey(), ApplicationFormStatusKey.IN_PROGRESS)) {
                    userHasNoMortgage = true;
                }
            } else {
                Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
            }

            if (userHasNoMortgage) {
                break;
            }
        }

        Assert.assertEquals(true, userHasNoMortgage);
        Assert.assertEquals(ApplicationStatusKey.IN_PROGRESS, application.getStatus().getKey());
    }

//    @Test
//    public void testAddFormOtherLoans_providerSEB() {
//        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithMortgage();
//        boolean otherLoanWasAdded = false;
//        Application application = getApplication();
//        String applicationId = UUIDUtils.toTinkUUID(application.getId());
//
//        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
//            ApplicationForm form = getNextFormThatIsNotCompleted(application);
//            Assert.assertNotNull(form);
//
//            if (Objects.equal(form.getName(), ApplicationFormName.APPLICANT_OTHER_LOANS)) {
//                otherLoanWasAdded = true;
//            }
//
//            fillApplicationForSEBWithOtherLoans(form);
//
//            application = applicationServiceResource.submitForm(authenticatedUser, applicationId, form);
//
//            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
//            Assert.assertTrue(updatedForm.isPresent());
//            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
//        }
//
//        // Making sure new form was added.
//        Assert.assertEquals(true, otherLoanWasAdded);
//        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
//    }

//    @Test
//    public void testAddFormOtherPropertyApartment_providerSEB() {
//        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithMortgage();
//        boolean otherPropertyApartmentWasAdded = false;
//        Application application = getApplication();
//        String applicationId = UUIDUtils.toTinkUUID(application.getId());
//
//        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
//            ApplicationForm form = getNextFormThatIsNotCompleted(application);
//            Assert.assertNotNull(form);
//
//            if (Objects.equal(form.getName(), ApplicationFormName.APPLICANT_OTHER_PROPERTIES_APARTMENT)) {
//                otherPropertyApartmentWasAdded = true;
//            }
//
//            fillApplicationForSEBWithOtherPropertiesApartment(form);
//
//            application = applicationServiceResource.submitForm(authenticatedUser, applicationId, form);
//
//            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
//            Assert.assertTrue(updatedForm.isPresent());
//            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
//        }
//
//        // Making sure new form was added.
//        Assert.assertEquals(true, otherPropertyApartmentWasAdded);
//        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
//    }

//    @Test
//    public void testAddFormOtherPropertyHouse_providerSEB() {
//        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithMortgage();
//        boolean otherPropertyHouseWasAdded = false;
//        Application application = getApplication();
//        String applicationId = UUIDUtils.toTinkUUID(application.getId());
//
//        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
//            ApplicationForm form = getNextFormThatIsNotCompleted(application);
//            Assert.assertNotNull(form);
//
//            if (Objects.equal(form.getName(), ApplicationFormName.APPLICANT_OTHER_PROPERTIES_HOUSE)) {
//                otherPropertyHouseWasAdded = true;
//            }
//
//            fillApplicationForSEBWithOtherPropertiesHouse(form);
//
//            application = applicationServiceResource.submitForm(authenticatedUser, applicationId, form);
//
//            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
//            Assert.assertTrue(updatedForm.isPresent());
//            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
//        }
//
//        // Making sure new form was added.
//        Assert.assertEquals(true, otherPropertyHouseWasAdded);
//        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
//    }

    @Test
    public void testAddFormCoApplicant_providerSEB()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithMortgage();
        Application application = getApplication();
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillApplicationForSEBWithCoApplicantForm(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
        }

        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
    }

    @Test
    public void testAddFormCoApplicant_providerSBAB()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithMortgage();
        Application application = getApplication();
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillApplicationForSBABWithCoApplicantForm(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
        }

        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
    }

    @Test
    public void testAddFormCoApplicantCreateSignableOperation_providerSBAB()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithMortgage();
        Application application = getApplication();
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillApplicationForSBABWithCoApplicantForm(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
        }

        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());

  //      SignableOperation operation = applicationServiceResource.submit(authenticatedUser, applicationId);
  //      Assert.assertEquals(SignableOperationStatuses.CREATED, operation.getStatus());
    }

//    @Test
//    public void testAddFormCoApplicantsOtherLoan_providerSEB() {
//        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithMortgage();
//        boolean coApplicantOtherLoanFormWasAdded = false;
//        Application application = getApplication();
//        String applicationId = UUIDUtils.toTinkUUID(application.getId());
//
//        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
//            ApplicationForm form = getNextFormThatIsNotCompleted(application);
//            Assert.assertNotNull(form);
//
//            if (Objects.equal(form.getName(), ApplicationFormName.CO_APPLICANT_OTHER_LOANS)) {
//                coApplicantOtherLoanFormWasAdded = true;
//            }
//
//            fillApplicationForSEBWithCoApplicantsOtherLoan(form);
//
//            application = applicationServiceResource.submitForm(authenticatedUser, applicationId, form);
//
//            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
//            Assert.assertTrue(updatedForm.isPresent());
//            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
//        }
//
//        // Making sure new form was added.
//        Assert.assertEquals(true, coApplicantOtherLoanFormWasAdded);
//        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
//    }

//    @Test
//    public void testAddFormCoApplicantsPropertiesApartment() {
//        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithMortgage();
//        boolean coApplicantOtherPropertiesApartmentFormWasAdded = false;
//        Application application = getApplication();
//        String applicationId = UUIDUtils.toTinkUUID(application.getId());
//
//        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
//            ApplicationForm form = getNextFormThatIsNotCompleted(application);
//            Assert.assertNotNull(form);
//
//            if (Objects.equal(form.getName(), ApplicationFormName.CO_APPLICANT_OTHER_PROPERTIES_APARTMENT)) {
//                coApplicantOtherPropertiesApartmentFormWasAdded = true;
//            }
//
//            fillApplicationFormWithCoApplicantsOtherPropertiesApartment(form);
//
//            application = applicationServiceResource.submitForm(authenticatedUser, applicationId, form);
//
//            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
//            Assert.assertTrue(updatedForm.isPresent());
//            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
//        }
//
//        // Making sure new form was added.
//        Assert.assertEquals(true, coApplicantOtherPropertiesApartmentFormWasAdded);
//        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
//    }

//    @Test
//    public void testAddFormCoApplicantsPropertiesHouse() {
//        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithMortgage();
//        boolean coApplicantOtherPropertiesHouseFormWasAdded = false;
//        Application application = getApplication();
//        String applicationId = UUIDUtils.toTinkUUID(application.getId());
//
//        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
//            ApplicationForm form = getNextFormThatIsNotCompleted(application);
//            Assert.assertNotNull(form);
//
//            if (Objects.equal(form.getName(), ApplicationFormName.CO_APPLICANT_OTHER_PROPERTIES_HOUSE)) {
//                coApplicantOtherPropertiesHouseFormWasAdded = true;
//            }
//
//            fillApplicationFormWithCoApplicantsOtherPropertiesHouse(form);
//
//            application = applicationServiceResource.submitForm(authenticatedUser, applicationId, form);
//
//            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
//            Assert.assertTrue(updatedForm.isPresent());
//            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());
//        }
//
//        // Making sure new form was added.
//        Assert.assertEquals(true, coApplicantOtherPropertiesHouseFormWasAdded);
//        Assert.assertEquals(ApplicationStatusKey.COMPLETED, application.getStatus().getKey());
//    }

    @Test
    public void testPayloadNotNullAndNotEmptyForProductDetails()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithMortgage();
        Application application = getApplication();
        String payload = null;
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillApplicationFormWithDeferralCapitalGainTax(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());

            if (Objects.equal(updatedForm.get().getName(), ApplicationFormName.MORTGAGE_PRODUCT_DETAILS)) {
                payload = updatedForm.get().getSerializedPayload();
                break;
            }
        }

        Assert.assertNotNull(payload);
        Assert.assertFalse(payload.isEmpty());
    }

    @Test
    public void testPayloadNotNullAndNotEmptyForConfirmation()
            throws FeatureFlagNotEnabledException, ApplicationNotValidException, ApplicationNotFoundException,
            ApplicationNotModifiableException {
        AuthenticatedUser authenticatedUser = getAuthenticatedUserWithMortgage();
        Application application = getApplication();
        String payload = null;
        String applicationId = UUIDUtils.toTinkUUID(application.getId());

        SubmitApplicationFormCommand submitFormCommand = new SubmitApplicationFormCommand(applicationId,
                authenticatedUser.getUser(), getUserAgent());

        while (!Objects.equal(application.getStatus().getKey(), ApplicationStatusKey.COMPLETED)) {
            ApplicationForm form = getNextFormThatIsNotCompleted(application);
            Assert.assertNotNull(form);

            fillApplicationForSEBForm(form);

            application = applicationServiceController.submitForm(submitFormCommand, form);

            Optional<ApplicationForm> updatedForm = application.getForm(form.getId());
            Assert.assertTrue(updatedForm.isPresent());
            Assert.assertEquals(ApplicationFormStatusKey.COMPLETED, updatedForm.get().getStatus().getKey());

            if (Objects.equal(updatedForm.get().getName(), ApplicationFormName.SBAB_CONFIRMATION)) {
                payload = updatedForm.get().getSerializedPayload();
                break;
            }
        }

        Assert.assertNotNull(payload);
        Assert.assertFalse(payload.isEmpty());
    }


    // Manual test for creating SwitchMortgage mail body and sending it
    @Ignore
    @Test
    public void sendSwitchMortgageMail() {
        EmailModule emailModule = new EmailModule(new EmailConfiguration());
        MailSender mailSender = new MailSender(null, false, emailModule.provideMandrillApi());

        String testingMailAddress = "backoffice@tink.se";
        Applicant applicant = createApplicant(testingMailAddress);
        SwitchMortgageProvider switchMortgageProvider = new SwitchMortgageProvider(
                        "seb-bankid",
                        "MockedExternalApplicationId",
                        applicant,
                        Optional.empty(),
                        new Residence("seb-bankid", Optional.empty(), ResidenceType.APARTMENT),
                        new Mortgage(Optional.empty(),
                                false,
                                Optional.empty(),
                                Lists.newArrayList()),
                null);

        String htmlBody = switchMortgageProvider.getMessageBody(12, 75);
//        assert (mailSender.sendMessage(
//                testingMailAddress,
//                "TestMail",
//                testingMailAddress,
//                "Back Office",
//                htmlBody,
//                true,
//                null));
    }



    private Applicant createApplicant(String emailAddress){
        return new Applicant("Tilda Tinkdottir", "201212121212",
                "Vasagatan 11", "11120",
                "Stockholm", emailAddress,
                "+46 70 000 00 00", 10000,
                Optional.empty(), new PoaDetails("Stockholm", DateUtils.getToday().toString(), (new Date(DateUtils.getToday().getTime() + 86400000)).toString(), null),
                null, Optional.empty(),
                null, null);
    }

    private Application getApplication() throws FeatureFlagNotEnabledException, ApplicationNotValidException {
        Application application = createApplication(applicationServiceController, getUserAgent());

        Assert.assertEquals(ApplicationStatusKey.CREATED, application.getStatus().getKey());

        int expectedNumberOfFormsAfterCreation = 10;
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
