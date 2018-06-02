package se.tink.backend.system.cli.debug;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.dao.ApplicationDAO;
import se.tink.backend.common.repository.cassandra.ApplicationFormEventRepository;
import se.tink.backend.core.Application;
import se.tink.backend.core.ApplicationField;
import se.tink.backend.core.ApplicationForm;
import se.tink.backend.core.application.ApplicationFormEvent;
import se.tink.backend.core.enums.ApplicationFormStatusKey;
import se.tink.backend.core.enums.ApplicationStatusKey;
import se.tink.backend.system.cli.CliPrintUtils;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.application.ApplicationType;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class DebugApplicationsCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final LogUtils log = new LogUtils(DebugApplicationsCommand.class);

    private ApplicationDAO applicationDAO;
    private ApplicationFormEventRepository applicationFormEventRepository;

    public DebugApplicationsCommand() {
        super("debug-applications", "");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        this.applicationDAO = serviceContext.getDao(ApplicationDAO.class);
        this.applicationFormEventRepository = serviceContext.getRepository(ApplicationFormEventRepository.class);
        
        boolean displayAll = Boolean.getBoolean("displayAll");
        String userId = System.getProperty("userId");
        String applicationId = System.getProperty("applicationId");

        String type = System.getProperty("type");
        String fromDateString = System.getProperty("fromDate");
        String toDateString = System.getProperty("toDate");
        
        if (!Strings.isNullOrEmpty(userId) || !Strings.isNullOrEmpty(applicationId)) {
            
            if (Strings.isNullOrEmpty(userId) || Strings.isNullOrEmpty(applicationId)) {
                log.error("You have to specify both `userId` and `applicationId`.");
                return;
            }
            
            if (displayAll) {
                log.warn("Property ignored: `displayAll=true`.");
            }
            
            if (!Strings.isNullOrEmpty(type)) {
                log.warn(String.format("Property ignored: `type=%s`.", type));
            }

            if (!Strings.isNullOrEmpty(fromDateString)) {
                log.warn(String.format("Property ignored: `fromDate=%s`.", fromDateString));
            }

            if (!Strings.isNullOrEmpty(toDateString)) {
                log.warn(String.format("Property ignored: `toDate=%s`.", toDateString));
            }

            Application application = applicationDAO.findByUserIdAndId(
                    UUIDUtils.fromTinkUUID(userId.replaceAll("-", "")),
                    UUIDUtils.fromTinkUUID(applicationId.replaceAll("-", "")));

            Preconditions.checkNotNull(application);

            List<ApplicationFormEvent> applicationFormEvents = applicationFormEventRepository
                    .findAllByUserIdAndApplicationId(application.getUserId(), application.getId());

            printApplication(application, applicationFormEvents);

        } else if (displayAll) {
            
            List<Predicate<Application>> predicates = Lists.newArrayList();
            
            if (!Strings.isNullOrEmpty(type)) {
                predicates.add(new ApplicationTypePredicate(type));
            }

            Date toDate = Strings.isNullOrEmpty(toDateString) ?
                    new Date() :
                    DateUtils.parseDate(toDateString);
            Date fromDate = Strings.isNullOrEmpty(fromDateString) ?
                    DateUtils.addDays(toDate, -7) :
                    DateUtils.parseDate(fromDateString);
            predicates.add(new ApplicationToDatePredicate(toDate));
            predicates.add(new ApplicationFromDatePredicate(fromDate));

            applicationDAO.streamAll()
                    .filter(Predicates.and(predicates)::apply)
                    .toSortedList(APPLICATION_COMPARATOR.reversed()::compare)
                    .subscribe(applications -> {

                        Map<ApplicationStatusKey, Integer> countByKey = Maps.newLinkedHashMap();
                        for (ApplicationStatusKey key : ApplicationStatusKey.values()) {
                            countByKey.put(key, 0);
                        }

                        for (Application application : applications) {
                            ApplicationStatusKey statusKey = application.getStatus().getKey();
                            countByKey.put(statusKey, countByKey.get(statusKey) + 1);
                        }

                        List<Map<String, String>> countByKeyOutput = FluentIterable.from(countByKey.entrySet())
                                .transform(entry -> {
                                    Map<String, String> output = Maps.newLinkedHashMap();
                                    output.put("status", entry.getKey().name());
                                    output.put("count", String.valueOf(entry.getValue()));
                                    return output;
                                }).toList();

                        System.out.println();
                        System.out.println("Count by status");
                        CliPrintUtils.printTable(countByKeyOutput);

                        printApplications(applications);
                    });
        } else {
            log.error("You have to specify a specific application (`userId=<uuid>` and `applicationId=<uuid>`) or explicitly show all (`displayAll=true`).");
        }
    }

    private void printApplication(Application application, List<ApplicationFormEvent> applicationFormEvents) {
        printApplications(Lists.newArrayList(application));
        printApplicationForms(application.getForms());
        printApplicationFormEvents(applicationFormEvents);
    }
    
    private void printApplicationForms(List<ApplicationForm> forms) {
        List<Map<String, String>> output = Lists.newArrayList(Iterables.transform(forms, APPLICATION_FORM_TO_MAP));
        
        System.out.println();
        System.out.println("Forms");
        CliPrintUtils.printTable(output);
    }

    private void printApplicationFormEvents(List<ApplicationFormEvent> applicationFormEvents) {
        List<ApplicationFormEvent> eventsByDateDesc = FluentIterable.from(applicationFormEvents)
                .toSortedList(APPLICATION_FORM_EVENT_BY_DATE.reverse());

        List<Map<String, String>> output = Lists.newArrayList(
                Iterables.transform(eventsByDateDesc, APPLICATION_FORM_EVENT_TO_MAP));

        System.out.println();
        System.out.println("Form events");
        CliPrintUtils.printTable(output);
    }
    
    private void printApplications(List<Application> applications) {
        List<Map<String, String>> output = Lists.newArrayList(Iterables.transform(applications, APPLICATION_TO_MAP));
        
        System.out.println();
        System.out.println("Applications");
        CliPrintUtils.printTable(output);
    }

    private static final Comparator<Application> APPLICATION_COMPARATOR = (a1, a2) -> ComparisonChain.start()
            .compare(a1.getCreated(), a2.getCreated())
            .compare(a1.getStatus().getUpdated(), a2.getStatus().getUpdated())
            .result();

    private static final Function<ApplicationField, Map<String, String>> APPLICATION_FIELD_TO_MAP = field -> {
        Map<String, String> output = Maps.newLinkedHashMap();
        output.put("name", field.getName());
        output.put("value", field.getValue());
        return output;
    };

    private static final Function<ApplicationForm, Map<String, String>> APPLICATION_FORM_TO_MAP = form -> {
        Map<String, String> output = Maps.newLinkedHashMap();
        output.put("id", form.getId().toString());
        output.put("parentid", form.getParentId() != null ? form.getParentId().toString() : "");
        output.put("name", form.getName());
        output.put("status_key", form.getStatus().getKey().name());
        output.put("status_updated", ThreadSafeDateFormat.FORMATTER_SECONDS.format(form.getStatus().getUpdated()));
        output.put(
                "fields",
                SerializationUtils.serializeToString(FluentIterable.from(form.getFields())
                        .transform(APPLICATION_FIELD_TO_MAP).toList()));
        return output;
    };

    private static final Function<Application, Map<String, String>> APPLICATION_TO_MAP = application -> {

        int forms = application.getForms().size();
        int completedForms = 0;
        for (ApplicationForm form : application.getForms()) {
            if (Objects.equal(form.getStatus().getKey(), ApplicationFormStatusKey.COMPLETED)) {
                completedForms++;
            }
        }

        Map<String, String> output = Maps.newLinkedHashMap();
        output.put("id", application.getId().toString());
        output.put("userid", application.getUserId().toString());
        output.put("created", ThreadSafeDateFormat.FORMATTER_SECONDS.format(application.getCreated()));
        output.put("type", application.getType().name());
        output.put("forms_completed", String.format("%d of %d", completedForms, forms));
        output.put("status_key", application.getStatus().getKey().name());
        output.put("status_updated",
                ThreadSafeDateFormat.FORMATTER_SECONDS.format(application.getStatus().getUpdated()));
        return output;
    };

    private static final Function<ApplicationFormEvent, Map<String, String>> APPLICATION_FORM_EVENT_TO_MAP =
            event -> {
                Map<String, String> output = Maps.newLinkedHashMap();
                output.put("date", ThreadSafeDateFormat.FORMATTER_SECONDS.format(event.getFormUpdated()));
                output.put("name", event.getFormName());
                output.put("status", event.getFormStatus().name());
                output.put("errors", event.getFormFieldDisplayErrors().toString());
                return output;
            };

    private static final Ordering<ApplicationFormEvent> APPLICATION_FORM_EVENT_BY_DATE =
            new Ordering<ApplicationFormEvent>() {
                @Override
                public int compare(ApplicationFormEvent left, ApplicationFormEvent right) {
                    return left.getFormUpdated().compareTo(right.getFormUpdated());
                }
            };

    class ApplicationTypePredicate implements Predicate<Application> {

        private final ApplicationType type;
        
        public ApplicationTypePredicate(ApplicationType type) {
            this.type = type;
        }
        
        public ApplicationTypePredicate(String type) {
            this(ApplicationType.valueOf(type));
        }
        
        @Override
        public boolean apply(Application application) {
            return Objects.equal(type, application.getType());
        }
    }
    
    class ApplicationFromDatePredicate implements Predicate<Application> {

        private final Date date;
        
        public ApplicationFromDatePredicate(Date date) {
            this.date = date;
        }
        
        public ApplicationFromDatePredicate(String date) {
            this(DateUtils.parseDate(date));
        }
        
        @Override
        public boolean apply(Application application) {
            if (date == null) {
                return true;
            }
            
            return !date.after(application.getCreated());
        }
    }
    
    class ApplicationToDatePredicate implements Predicate<Application> {

        private final Date date;
        
        public ApplicationToDatePredicate(Date date) {
            this.date = date;
        }
        
        public ApplicationToDatePredicate(String date) {
            this(DateUtils.parseDate(date));
        }
        
        @Override
        public boolean apply(Application application) {
            if (date == null) {
                return true;
            }
            
            return !date.before(application.getCreated());
        }
    }
}
