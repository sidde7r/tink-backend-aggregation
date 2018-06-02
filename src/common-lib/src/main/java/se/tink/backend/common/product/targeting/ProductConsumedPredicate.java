package se.tink.backend.common.product.targeting;

import com.google.common.base.Predicate;
import java.util.Map;
import java.util.Objects;
import org.joda.time.LocalDate;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.libraries.uuid.UUIDUtils;
import se.tink.backend.core.Application;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.libraries.application.ApplicationType;
import se.tink.backend.core.product.ProductFilterRule;
import se.tink.backend.core.product.ProductType;
import se.tink.backend.utils.ApplicationUtils;
import se.tink.libraries.date.DateUtils;

public class ProductConsumedPredicate implements Predicate<Profile> {

    private final ApplicationDAO applicationDAO;
    
    private final ApplicationType applicationType;
    private final int days;
    private final LocalDate today = new LocalDate();
    
    public ProductConsumedPredicate(ProductFilterRule rule, ApplicationDAO applicationDAO) {
        
        this.applicationDAO = applicationDAO;
        @SuppressWarnings("unchecked")
        Map<String, Object> payload = (Map<String, Object>) rule.getPayload();
        
        this.applicationType = ApplicationUtils.getApplicationType(getProductType(payload.get("type")));
        this.days =  getDays(payload.get("days"));
    }
    
    private ProductType getProductType(Object type) {
        if (type == null) {
            return null;
        }
        
        try {
            return ProductType.valueOf(String.valueOf(type));
        } catch (Exception e) {
            return null;
        }
    }
    
    private int getDays(Object days) {
        if (days == null) {
            return Integer.MAX_VALUE;
        }
        
        return Integer.valueOf(String.valueOf(days));
    }
    
    @Override
    public boolean apply(Profile profile) {
        return !hasSignedApplication(profile);
    }
     
    private boolean hasSignedApplication(Profile profile) {
        for (Application application : applicationDAO.findByUserId(UUIDUtils.fromTinkUUID(profile.getUser().getId()))) {
            
            if (applicationType != null && !Objects.equals(applicationType, application.getType())) {
                continue;
            }

            if (application.getStatus().getKey().ordinal() < ApplicationStatusKey.SIGNED.ordinal()) {
                continue;
            }

            if (Objects.equals(application.getStatus().getKey(), ApplicationStatusKey.ABORTED)) {
                continue;
            }

            if (DateUtils.daysBetween(application.getStatus().getUpdated(), today.toDate()) > days) {
                continue;
            }

            return true;
            
        }
        
        return false;
    }

}
