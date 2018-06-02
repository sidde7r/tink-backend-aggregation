package se.tink.backend.common.resources;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableListMultimap;
import com.google.inject.Provider;
import java.util.Objects;
import se.tink.backend.common.providers.booli.LookupBooli;
import se.tink.backend.common.providers.booli.entities.request.BooliEstimateRequest;
import se.tink.backend.common.providers.booli.entities.response.BooliModelMapper;
import se.tink.backend.common.repository.mysql.main.PropertyRepository;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.BooliEstimate;
import se.tink.libraries.application.GenericApplication;
import se.tink.libraries.application.GenericApplicationFieldGroup;
import se.tink.backend.core.enums.ApplicationFieldName;
import se.tink.backend.core.enums.GenericApplicationFieldGroupNames;
import se.tink.backend.core.property.Property;
import se.tink.backend.core.signableoperation.SignableOperation;
import se.tink.backend.core.transfer.SignableOperationStatuses;
import se.tink.backend.system.api.UpdateService;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.backend.utils.LogUtils;

public class BooliRequestRunnableFactory {
    private static final LogUtils log = new LogUtils(BooliRequestRunnableFactory.class);
    private final Provider<UpdateService> updateService;
    private final LookupBooli lookupBooli;
    private final PropertyRepository propertyRepository;

    public BooliRequestRunnableFactory(
            Provider<UpdateService> updateService,
            LookupBooli lookupBooli,
            PropertyRepository propertyRepository) {
        this.updateService = updateService;
        this.lookupBooli = lookupBooli;
        this.propertyRepository = propertyRepository;
    }

    public Runnable createBooliRequestRunnable(final SignableOperation operation, final GenericApplication application) {
        return () -> BooliRequestRunnableFactory.this.run(operation, application);
    }

    private void run(SignableOperation operation, GenericApplication genericApplication) {
        try {
            String propertyId = getPropertyId(genericApplication);

            // Estimate
            BooliEstimateRequest request = BooliModelMapper.applicationToBooliEstimateRequest(genericApplication);
            BooliEstimate estimate = lookupBooli.estimate(propertyId, request);

            // Booli send either USE or DO_NOT_USE for recommendation. Don't use the useless valuation on property.
            Preconditions.checkState(Objects.equals(estimate.getRecommendation(), "USE"));

            // Store current valuation on property, so that we know which one is most recent
            saveMostRecentValuationOnProperty(genericApplication, estimate, propertyId);
            operation.setStatus(SignableOperationStatuses.EXECUTED);
        } catch (Exception e) {
            log.error(UUIDUtils.toTinkUUID(operation.getUserId()), "Unable estimate residence value.", e);
            operation.setStatusDetailsKey(SignableOperation.StatusDetailsKey.TECHNICAL_ERROR);
            operation.setStatusMessage("Värdering misslyckades.");
            operation.setStatus(SignableOperationStatuses.FAILED);
        }

        updateService.get().updateSignableOperation(operation);
    }

    private void saveMostRecentValuationOnProperty(GenericApplication genericApplication, BooliEstimate estimate,
            String propertyId) {
        Property property = propertyRepository.findByUserIdAndId(
                UUIDUtils.toTinkUUID(genericApplication.getUserId()),
                propertyId);

        Preconditions.checkNotNull(property);

        // Save reference to valuation, so that we can fetch it later on
        property.setMostRecentValuation(estimate.getPrice());
        property.setBooliEstimateId(estimate.getId());
        propertyRepository.save(property);
    }

    private String getPropertyId(GenericApplication genericApplication) {
        ImmutableListMultimap<String, GenericApplicationFieldGroup> groups = ApplicationUtils
                .getGroupsByName(genericApplication);

        return ApplicationUtils
                .getFirst(groups, GenericApplicationFieldGroupNames.PROPERTY).get()
                .getField(ApplicationFieldName.PROPERTY_ID);
    }
}
