package se.tink.backend.system.cli.extraction;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.Date;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.cassandra.EventRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.Event;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class ExtractUsersActiveBeforeCommand extends ServiceContextCommand<ServiceConfiguration> {

    private EventRepository eventRepository;
    private UserRepository userRepository;
    private static final LogUtils log = new LogUtils(ExtractUsersActiveBeforeCommand.class);

    public ExtractUsersActiveBeforeCommand() {
        super("extract-users-inactive-after", "Extract a list of all users inactive after a certain date.");
    }

    private static class IsUserContextPoll implements Predicate<Event> {

        @Override
        public boolean apply(Event input) {
            boolean res = Objects.equal(input.getType(), "user.context.poll");
            return res;
        }

    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        userRepository = serviceContext.getRepository(UserRepository.class);
        eventRepository = serviceContext.getRepository(EventRepository.class);

        final Date date = DateUtils.parseDate(Preconditions.checkNotNull(System.getProperty("date")));

        final IsUserContextPoll isUserContextPoll = new IsUserContextPoll();
        for (User user : userRepository.findAll()) {
            log.info(user.getId(), "Checking...");

            List<Event> userEvents = eventRepository.findUserEventsAfter(UUIDUtils.fromTinkUUID(user.getId()), date);
            boolean hasBeenActive = Iterables.any(userEvents, isUserContextPoll);
            if (!hasBeenActive) {
                System.out.println("userid: " + user.getId());
            }
        }
    }

}
