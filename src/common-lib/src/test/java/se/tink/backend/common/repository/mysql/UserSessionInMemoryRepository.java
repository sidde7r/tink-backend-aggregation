package se.tink.backend.common.repository.mysql;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Objects;

import se.tink.backend.common.repository.mysql.main.UserSessionRepository;
import se.tink.backend.core.UserSession;

public class UserSessionInMemoryRepository extends InMemoryJpaRepository<String, UserSession> implements UserSessionRepository {

    public UserSessionInMemoryRepository() {
        super();
    }

    public UserSessionInMemoryRepository(List<UserSession> initial) {
        super(FluentIterable.from(initial).uniqueIndex(UserSession::getId));
    }


    @Override
    public List<UserSession> findByUserId(String userId) {
        List<UserSession> sessions = Lists.newArrayList();
        for (UserSession session : db.values()) {
            if (Objects.equals(session.getUserId(), userId)) {
                sessions.add(session);
            }
        }
        return sessions;
    }

    @Override
    public <S extends UserSession> S save(S s) {
        db.put(s.getId(), s);
        return s;
    }
}
