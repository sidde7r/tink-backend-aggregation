package se.tink.backend.product.execution.controller;

import com.google.inject.Inject;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.product.execution.model.CreateProductResponse;
import se.tink.backend.product.execution.log.ProductExecutionLogger;
import se.tink.backend.product.execution.unit.agents.CreateProductExecutor;
import se.tink.backend.product.execution.unit.agents.exceptions.BankIdException;
import se.tink.backend.product.execution.unit.agents.exceptions.application.InvalidApplicationException;
import se.tink.backend.product.execution.model.CredentialsUpdate;
import se.tink.backend.product.execution.model.FetchProductInformationParameterKey;
import se.tink.backend.product.execution.model.ProductType;
import se.tink.backend.product.execution.model.RefreshApplicationParameterKey;
import se.tink.backend.system.client.SystemServiceFactory;
import se.tink.backend.product.execution.model.User;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.metrics.MetricRegistry;

public class SwitchMortgageExecutorController {
    private static final ProductExecutionLogger log = new ProductExecutionLogger(
            SwitchMortgageExecutorController.class);

    private final MetricRegistry metricRegistry;
    private final SystemServiceFactory systemServiceFactory;
    private final Catalog catalog = Catalog.getCatalog("sv-se"); //TODO: Fix catalog based on user locale.

    @Inject
    public SwitchMortgageExecutorController(MetricRegistry metricRegistry,
            SystemServiceFactory systemServiceFactory) {
        this.metricRegistry = metricRegistry;
        this.systemServiceFactory = systemServiceFactory;
    }

    public void create(CreateProductExecutor executor, GenericApplication genericApplication,
            SignableOperation signableOperation, CredentialsUpdate credentialsUpdate) {
        updateSignableOperationStatus(signableOperation, SignableOperationStatuses.EXECUTING, null, null);

        // TODO: http logging

        try {
            // We want to explicitly log everything that has to do with creating new products.
            // TODO: attach filters

            // Create the new product and set the external reference as the signable object.
            CreateProductResponse response = executor.create(genericApplication, credentialsUpdate);

            signableOperation.setSignableObject(response.getExternalId());
            updateSignableOperationStatus(signableOperation, SignableOperationStatuses.EXECUTED, null, null);

            metricRegistry.meter(executor.getMetricId().label("outcome", "success")).inc();
        } catch (BankIdException e) {
            switch (e.getError()) {
            case CANCELLED:
            case TIMEOUT:
            case ALREADY_IN_PROGRESS:
            case NO_CLIENT:

                log.info(ProductExecutionLogger
                        .newBuilder()
                        .withUserId(genericApplication.getUserId())
                        .withApplicationId(genericApplication.getApplicationId())
                        .withMessage(e.getMessage()));

                updateSignableOperationStatus(signableOperation, SignableOperationStatuses.CANCELLED,
                        SignableOperation.StatusDetailsKey.BANKID_FAILED, catalog.getString(e.getUserMessage()));
                break;
            case USER_VALIDATION_ERROR:
                // This will e.g. happen when user has too new BankID -> SEB doesn't accept the applicant

                log.warn(ProductExecutionLogger
                        .newBuilder()
                        .withUserId(genericApplication.getUserId())
                        .withApplicationId(genericApplication.getApplicationId())
                        .withMessage("User validation failed")
                        .withThrowable(e));

                updateSignableOperationStatus(signableOperation, SignableOperationStatuses.FAILED,
                        SignableOperation.StatusDetailsKey.USER_VALIDATION_ERROR,
                        catalog.getString(e.getUserMessage()));
                break;
            default:

                log.error(ProductExecutionLogger
                        .newBuilder()
                        .withUserId(genericApplication.getUserId())
                        .withApplicationId(genericApplication.getApplicationId())
                        .withMessage(String.format("Caught unexpected %s", e.getMessage()))
                        .withThrowable(e));

                updateSignableOperationStatus(signableOperation, SignableOperationStatuses.FAILED,
                        SignableOperation.StatusDetailsKey.BANKID_FAILED, catalog.getString(e.getUserMessage()));
            }

            metricRegistry.meter(executor.getMetricId().label("outcome", getOutcomeLabel(signableOperation))).inc();
        } catch (InvalidApplicationException e) {

            log.error(ProductExecutionLogger
                    .newBuilder()
                    .withUserId(genericApplication.getUserId())
                    .withApplicationId(genericApplication.getApplicationId())
                    .withMessage(String.format("Application validation failed. %s", e.getMessage()))
                    .withThrowable(e));

            updateSignableOperationStatus(signableOperation, SignableOperationStatuses.FAILED,
                    SignableOperation.StatusDetailsKey.INVALID_INPUT, catalog.getString(e.getMessage()));

            metricRegistry.meter(executor.getMetricId().label("outcome", getOutcomeLabel(signableOperation))).inc();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();

            log.error(ProductExecutionLogger
                    .newBuilder()
                    .withUserId(genericApplication.getUserId())
                    .withApplicationId(genericApplication.getApplicationId())
                    .withMessage("Could not create new product")
                    .withThrowable(e));

            updateSignableOperationStatus(signableOperation, SignableOperationStatuses.FAILED,
                    SignableOperation.StatusDetailsKey.TECHNICAL_ERROR, catalog.getString("Something went wrong."));

            metricRegistry.meter(executor.getMetricId().label("outcome", getOutcomeLabel(signableOperation))).inc();
        } catch (Exception e) {
            // Catching this exception here means that the Credentials will not get status TEMPORARY_ERROR.

            log.error(ProductExecutionLogger
                    .newBuilder()
                    .withUserId(genericApplication.getUserId())
                    .withApplicationId(genericApplication.getApplicationId())
                    .withMessage(String.format("Could not create new product"))
                    .withThrowable(e));

            updateSignableOperationStatus(signableOperation, SignableOperationStatuses.FAILED,
                    SignableOperation.StatusDetailsKey.TECHNICAL_ERROR, catalog.getString("Something went wrong."));

            metricRegistry.meter(executor.getMetricId().label("outcome", getOutcomeLabel(signableOperation))).inc();
        } finally {
            // Disable the logging and the create product filter when we're done with the execute command.
            // TODO: remove logging filters
        }
    }

    public void fetchProductInformation(CreateProductExecutor executor, User user, UUID applicationId,
            Map<FetchProductInformationParameterKey, Object> parameters) {

        try {
            // TODO: attach filters
            executor.fetchProductInformation(
                    ProductType.MORTGAGE,
                    applicationId,
                    user.getUserId(),
                    parameters);
        } catch (Exception e) {

            log.error(ProductExecutionLogger
                    .newBuilder()
                    .withUserId(user.getUserId())
                    .withApplicationId(applicationId)
                    .withMessage(String.format("Could not fetch product information."))
                    .withThrowable(e));
        } finally {
            // TODO: remove filters
        }
    }

    public void refresh(CreateProductExecutor executor, User user, UUID applicationId,
            Map<RefreshApplicationParameterKey, Object> parameters) {

        try {
            executor.refreshApplication(
                    ProductType.MORTGAGE,
                    applicationId,
                    user.getUserId(),
                    parameters);

        } catch (Exception e) {

            log.error(ProductExecutionLogger
                    .newBuilder()
                    .withUserId(user.getUserId())
                    .withApplicationId(applicationId)
                    .withMessage(String.format("Could not refresh application."))
                    .withThrowable(e));
        }
    }

    private static String getOutcomeLabel(SignableOperation signableOperation) {
        return signableOperation.getStatusDetailsKey().name().toLowerCase(Locale.ENGLISH);
    }

    public void updateSignableOperationStatus(SignableOperation signableOperation, SignableOperationStatuses status,
            SignableOperation.StatusDetailsKey statusDetailsKey, String statusMessage) {
        signableOperation.setStatus(status);
        signableOperation.setStatusMessage(statusMessage);

        if (!Objects.isNull(statusDetailsKey)) {
            signableOperation.setStatusDetailsKey(statusDetailsKey);
        }

        systemServiceFactory.getUpdateService().updateSignableOperation(signableOperation);
    }
}
