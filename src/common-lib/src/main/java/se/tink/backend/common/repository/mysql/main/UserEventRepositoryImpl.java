package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.UserEvent;
import se.tink.backend.core.UserEventTypes;

@Transactional
public class UserEventRepositoryImpl implements UserEventRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<UserEvent> findMostRecentByUserId(String userId) {
        return em
                .createQuery(
                        "select ue from UserEvent ue where userId=:userId and (ue.type = :successType or "
                                + "ue.type = :errorType or ue.type = :pin6ResetType or ue.type = :passwordResetType) order by ue.date desc",
                        UserEvent.class).setParameter("successType", UserEventTypes.AUTHENTICATION_SUCCESSFUL)
                .setParameter("errorType", UserEventTypes.AUTHENTICATION_ERROR)
                .setParameter("pin6ResetType", UserEventTypes.PIN6_RESET)
                .setParameter("passwordResetType", UserEventTypes.PASSWORD_RESET)
                .setParameter("userId", userId)
                .setMaxResults(20).getResultList();
    }
}
