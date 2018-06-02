package se.tink.backend.common.dao;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.inject.Inject;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import rx.Observable;
import se.tink.backend.common.repository.mysql.main.ApplicationFormRepository;
import se.tink.backend.common.repository.mysql.main.ApplicationRepository;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.ApplicationFormRow;
import se.tink.backend.core.ApplicationRow;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.uuid.UUIDUtils;

public class ApplicationDAO {

    private static final LogUtils log = new LogUtils(ApplicationDAO.class);

    private final ApplicationRepository applicationRepository;
    private final ApplicationFormRepository applicationFormRepository;

    @Inject
    public ApplicationDAO(
            ApplicationRepository applicationRepository,
            ApplicationFormRepository applicationFormRepository) {
        this.applicationRepository = applicationRepository;
        this.applicationFormRepository = applicationFormRepository;
    }

    public Observable<Application> streamAll() {
        return applicationRepository.streamAll().map(this::map);
    }
    
    public List<Application> findByUserId(UUID userId) {
        
        String userIdAsString = UUIDUtils.toTinkUUID(userId);
        
        List<Application> applications = getApplications(applicationRepository.findAllByUserId(userIdAsString));
        
        if (applications.isEmpty()) {
            log.debug(userIdAsString, "findByUserId: No applications found.");
        }
        
        return applications;
    }

    private List<Application> getApplications(List<ApplicationRow> applicationRows) {
        return applicationRows == null ? Lists.newArrayList() :
                applicationRows.stream()
                        .map(this::map)
                        .collect(Collectors.toList());
    }

    private Application map(ApplicationRow applicationRow) {
        Application application = new Application(applicationRow);
        application.setForms(getForms(application.getId()));
        return application;
    }
    
    public Application findByUserIdAndId(UUID userId, UUID applicationId) {

        ApplicationRow applicationRow = applicationRepository.findOne(UUIDUtils.toTinkUUID(applicationId));

        if (applicationRow == null) {
            log.warn(
                    UUIDUtils.toTinkUUID(userId),
                    String.format("findByUserIdAndId [applicationId:%s]: No application found.",
                            UUIDUtils.toTinkUUID(applicationId)));
            return null;
        }

        Application application = new Application(applicationRow);
        application.setForms(getForms(applicationId));

        log.debug(
                UUIDUtils.toTinkUUID(userId),
                String.format("findByUserIdAndId [applicationId:%s]: Number of forms = %s.",
                        UUIDUtils.toTinkUUID(applicationId), application.getForms().size()));

        return application;
    }

    public Application save(Application application) {

        log.info(
                UUIDUtils.toTinkUUID(application.getUserId()),
                String.format("save [applicationId:%s]: Number of forms = %s.",
                        UUIDUtils.toTinkUUID(application.getId()), application.getForms().size()));
        
        // Remove all existing forms before storing new ones -- there could have been removed forms
        applicationFormRepository.deleteByApplicationId(UUIDUtils.toTinkUUID(application.getId()));

        List<ApplicationFormRow> formRows = Lists.newArrayList();
        
        for (int i = 0; i < application.getForms().size(); i++) {
            ApplicationForm form = application.getForms().get(i);
            formRows.add(form.toRow(i));
        }

        applicationFormRepository.save(formRows);

        ApplicationRow storedApplicationRow = applicationRepository.save(application.toRow());
        
        if (storedApplicationRow == null) {
            log.error(
                    UUIDUtils.toTinkUUID(application.getUserId()),
                    String.format("save [applicationId:%s]: The save operation returned `null`.",
                            UUIDUtils.toTinkUUID(application.getId())));
            return null;
        } else {       
            return new Application(storedApplicationRow);
        }
    }

    private List<ApplicationForm> getForms(UUID applicationId) {
        
        List<ApplicationFormRow> formRows = FORM_ROW_BY_NUMBER.sortedCopy(applicationFormRepository
                .findAllByApplicationId(UUIDUtils.toTinkUUID(applicationId)));

        return Lists.newArrayList(Iterables.transform(formRows, FORM_ROW_TO_FORM));
    }
    
    private static final Ordering<ApplicationFormRow> FORM_ROW_BY_NUMBER = new Ordering<ApplicationFormRow>() {
        @Override
        public int compare(ApplicationFormRow row1, ApplicationFormRow row2) {
            return Integer.compare(row1.getFormNumber(), row2.getFormNumber());
        }
    };

    private static final Function<ApplicationFormRow, ApplicationForm> FORM_ROW_TO_FORM = ApplicationForm::new;

    public ImmutableList<Application> findByUserIdAndType(User user, ApplicationType type) {
        List<Application> applications = findByUserId(UUIDUtils.fromTinkUUID(user.getId()));

        return FluentIterable
                .from(applications)
                .filter(Predicates.applicationIsOfType(type))
                .toList();
    }
}
