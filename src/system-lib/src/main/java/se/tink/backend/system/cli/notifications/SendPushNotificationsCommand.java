package se.tink.backend.system.cli.notifications;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.workers.notifications.channels.MobileNotificationSender;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.ServiceContextCommand;

public class SendPushNotificationsCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final int NBR_OF_THREADS = 10;

    public SendPushNotificationsCommand() {
        super("send-push-notifications", "Send push notifications");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {
        Thread.sleep(1000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter title: ");
        String title = reader.readLine();

        if (Strings.isNullOrEmpty(title)) {
            System.out.println("Can't send notification without title.");
            return;
        }

        System.out.print("Enter message: ");
        String message = reader.readLine();

        if (Strings.isNullOrEmpty(message)) {
            System.out.println("Can't send empty notification.");
            return;
        }

        System.out.print("Enter url: ");
        String url = reader.readLine();

        if (Strings.isNullOrEmpty(url)) {
            System.out.println("Can't send notification without url.");
            return;
        }

        System.out.print("Enter sensitive title (enter to ignore): ");
        String sensitiveTitle = reader.readLine();

        System.out.print("Enter sensitive message (enter to ignore): ");
        String sensitiveMessage = reader.readLine();

        final MobileNotificationSender mobileChannel = injector.getInstance(MobileNotificationSender.class);
        mobileChannel.start();
        try {
            UserRepository userRepository = serviceContext.getRepository(UserRepository.class);

            List<User> users = userRepository.findAll();

            // Filter the users based on a list.

            try {
                File userIdFilterFile = new File("userid-filter.txt");

                if (userIdFilterFile.exists()) {
                    final Set<String> userIdFilter = Sets.newHashSet(Files.readLines(userIdFilterFile, Charsets.UTF_8));

                    users = Lists.newArrayList(Iterables.filter(users, user -> (userIdFilter.contains(user.getId()))));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            // Send the notification.

            final Notification.Builder notificationBuilder = new Notification.Builder()
                    .key("custom")
                    .date(new Date())
                    .generated(new Date())
                    .title(title)
                    .sensitiveTitle(sensitiveTitle)
                    .sensitiveMessage(sensitiveMessage)
                    .message(message)
                    .url(url)
                    .type("manual-push")
                    .groupable(false);

            System.out.printf("Send notification (%s) to %d users? [y/n] %n",
                    notificationBuilder, users.size());

            String response = reader.readLine();

            if (!Objects.equal(response, "y")) {
                return;
            }

            ExecutorService executorService = Executors.newFixedThreadPool(NBR_OF_THREADS);

            for (final User user : users) {
                notificationBuilder.userId(user.getId());
                executorService.execute(() -> mobileChannel
                        .sendNotifications(user, Collections.singletonList(notificationBuilder.build()), true, false));
            }

            executorService.shutdown();
            executorService.awaitTermination(12, TimeUnit.HOURS);
        } finally {
            mobileChannel.stop();
        }
    }
}
