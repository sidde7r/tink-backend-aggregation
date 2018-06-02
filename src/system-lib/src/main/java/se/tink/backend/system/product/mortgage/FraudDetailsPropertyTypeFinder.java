package se.tink.backend.system.product.mortgage;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.common.application.PropertyUtils;
import se.tink.backend.common.repository.mysql.main.FraudDetailsRepository;
import se.tink.backend.core.FraudAddressContent;
import se.tink.backend.core.FraudDetails;
import se.tink.backend.core.FraudDetailsContentType;
import se.tink.backend.core.User;
import se.tink.backend.core.property.PropertyType;
import se.tink.backend.utils.guavaimpl.Orderings;

public class FraudDetailsPropertyTypeFinder {
    private final FraudDetailsRepository fraudDetailsRepository;

    @Inject
    public FraudDetailsPropertyTypeFinder(FraudDetailsRepository fraudDetailsRepository) {
        this.fraudDetailsRepository = fraudDetailsRepository;
    }

    public Optional<PropertyType> findPropertyType(User user) {
        ImmutableListMultimap<FraudDetailsContentType, FraudDetails> fraudDetailsByType = Multimaps.index(
                fraudDetailsRepository.findAllByUserId(user.getId()),
                FraudDetails::getType);

        Optional<FraudAddressContent> addressContent = getMostRecentFraudAddressContent(fraudDetailsByType);

        if (!addressContent.isPresent()) {
            return Optional.empty();
        }

        String address = addressContent.get().getAddress();
        if (Strings.isNullOrEmpty(address)) {
            return Optional.empty();
        }

        boolean isApartment = PropertyUtils.isApartment(addressContent.get(), fraudDetailsByType);

        return Optional.of(isApartment ?
                PropertyType.APARTMENT :
                PropertyType.HOUSE);
    }

    private Optional<FraudAddressContent> getMostRecentFraudAddressContent(
            ImmutableListMultimap<FraudDetailsContentType, FraudDetails> fraudDetails) {
        ImmutableList<FraudDetails> addressDetails = fraudDetails.get(FraudDetailsContentType.ADDRESS);

        if (addressDetails.isEmpty()) {
            return Optional.empty();
        }

        return addressDetails.stream()
                .max(Orderings.FRAUD_DETAILS_DATE)
                .map(FraudDetails::getContent)
                .map(t -> (FraudAddressContent) t);
    }
}
