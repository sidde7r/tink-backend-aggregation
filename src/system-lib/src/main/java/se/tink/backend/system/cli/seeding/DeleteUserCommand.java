package se.tink.backend.system.cli.seeding;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.util.List;
import java.util.Optional;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.cache.CacheScope;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.DeletedUserRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserSessionRepository;
import se.tink.backend.core.DeletedUser;
import se.tink.backend.core.User;
import se.tink.backend.core.UserSession;
import se.tink.backend.rpc.DeleteUserRequest;
import se.tink.backend.system.cli.ServiceContextCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.uuid.UUIDUtils;

/**
 * Deletes a user and all its data
 */
public class DeleteUserCommand extends ServiceContextCommand<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(DeleteUserCommand.class);
    private DeletedUserRepository deletedUserRepository;
    private UserSessionRepository sessionRepository;

    public DeleteUserCommand() {
        super("delete-user", "Takes a user name and deletes the user and all its data");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        UserRepository userRepository = serviceContext.getRepository(UserRepository.class);
        deletedUserRepository = serviceContext.getRepository(DeletedUserRepository.class);
        sessionRepository = serviceContext.getRepository(UserSessionRepository.class);

        String fileName = System.getProperty("file");
        
        if (!Strings.isNullOrEmpty(fileName)) {
            
            List<String> userIds = Files.readLines(new File(fileName), Charsets.UTF_8,
                    new LineProcessor<List<String>>() {

                        private List<String> userIds = Lists.newArrayList();

                        @Override
                        public boolean processLine(String line) {
                            if (!Strings.isNullOrEmpty(line)) {
                                userIds.add(line);
                            }
                            return true;
                        }

                        @Override
                        public List<String> getResult() {
                            return userIds;
                        }
                    });
            
            // Use `forceDeletion=true` to delete partially deleted users.
            boolean forceNonExisting = Boolean.getBoolean("forceDeletion");
            
            for (String userId : userIds) {
                deleteByUserId(serviceContext, userRepository, userId, forceNonExisting);
            }
            
        } else {
            String username = System.getProperty("username");
            String userId = System.getProperty("userId");
            
            if (!Strings.isNullOrEmpty(username) && !Strings.isNullOrEmpty(userId)) {
                log.error("Username and userId are mutual exclusive. Terminating.");
                return;
            }

            if (Strings.isNullOrEmpty(username) && Strings.isNullOrEmpty(userId)) {
                log.error("No username or userId supplied. Terminating.");
                return;
            }
            
            if (!Strings.isNullOrEmpty(username)) {
                deleteByUsername(serviceContext, userRepository, username);
            } else if (!Strings.isNullOrEmpty(userId)) {
                // Use `forceDeletion=true` to delete partially deleted users.
                boolean forceNonExisting = Boolean.getBoolean("forceDeletion");
                deleteByUserId(serviceContext, userRepository, userId, forceNonExisting);
            }
        }

        log.info("Done");
    }
    
    private void deleteByUsername(ServiceContext serviceContext, UserRepository userRepository,
            String username) {
        
        delete(serviceContext, findOneByUsername(userRepository, username));
    }

    private void deleteByUserId(ServiceContext serviceContext, UserRepository userRepository,
            String userId, boolean forceNonExisting) {
        
        delete(serviceContext, findOneByUserId(userRepository, userId, forceNonExisting));
    }
    
    private void delete(ServiceContext serviceContext, UserToDelete user) {
        
        if (user == null) {
            log.info("No user to delete.");
            return;
        }

        log.info(String.format("Deleting user with username '%s' with id '%s'.", user.getUsername(), user.getId()));

        // Log out the user. Needs to be done a bit manually since we can't call `UserServiceResource#logout` from here.

        CacheClient cacheClient = serviceContext.getCacheClient();

        List<UserSession> userSessions = sessionRepository.findByUserId(user.getId());

        if (userSessions.size() != 0) {
            log.info("\tlogging out user sessions");
            for (UserSession session : userSessions) {
                cacheClient.delete(CacheScope.SESSION_BY_ID, session.getId());
                sessionRepository.delete(session);
            }
        }

        // Make sure we store the DeleteUser for this user in DB. If all else fails, at least we have this marker
        // to know that a user has been deleted. This makes it possible for us to rerun user deletion code for all
        // deleted users is we ever encounter we have data not fully deleted somewhere.

        DeletedUser deletedUser = deletedUserRepository.findOneByUserId(user.getId());

        DeleteUserRequest deleteUserRequest = new DeleteUserRequest();

        if (deletedUser != null) {
            deleteUserRequest.setUserId(deletedUser.getUserId());
        } else {
            deleteUserRequest.setUserId(user.getId());
            deleteUserRequest.setComment("Deleted through system command.");
        }

        log.info("\tdelete the user's data...");
        
        serviceContext.getSystemServiceFactory().getUpdateService().deleteUser(deleteUserRequest);
    }

    private UserToDelete findOneByUsername(UserRepository userRepository, String username) {
        User user = userRepository.findOneByUsername(username);
        if (user == null) {
            String msg = String.format("No user with username %s.", username);
            log.info(msg);
            return null;
        }
        return getUserToDelete(user);
    }

    private UserToDelete findOneByUserId(UserRepository userRepository, String userId, boolean forceNonExisting) {

        UserToDelete userToDelete;
        
        if (forceNonExisting) {
            // Poor man's validation.
            userToDelete = new UserToDelete();
            userToDelete.userId = UUIDUtils.toTinkUUID(UUIDUtils.fromTinkUUID(userId));
            userToDelete.user = Optional.ofNullable(userRepository.findOne(userId));
        } else {
            User user = userRepository.findOne(userId);
            if (user == null) {
                String msg = String.format("No user with userId %s.", userId);
                log.warn(msg);
                return null;
            }
            userToDelete = getUserToDelete(user);
        }
        
        return userToDelete;
    }
    
    private UserToDelete getUserToDelete(User user) {
        UserToDelete userToDelete = new UserToDelete();
        userToDelete.userId = user.getId();
        userToDelete.user = Optional.of(user);
        return userToDelete;
    }

    /**
     * Helper class to be able to delete users that might not have a {@link User} instance.
     */
    private static class UserToDelete {
        private String userId;
        private Optional<User> user = Optional.empty();
        public String getUsername() {
            if (user.isPresent()) {
                return user.get().getUsername();
            } else {
                return "UNKNOWN_USERNAME";
            }
        }
        public String getId() {
            return userId;
        }
    }
}
