package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;
import se.tink.backend.core.Notification;
import se.tink.backend.core.NotificationStatus;

public class NotificationRepositoryImpl implements NotificationRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        em.createQuery("DELETE from Notification n where n.userId = :userId").setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public Notification findById(String id) {
        try {
            return em.createQuery(
                    String.format("SELECT n FROM %s n WHERE n.id=:id", Notification.class.getSimpleName()),
                    Notification.class).setParameter("id", id).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public List<Notification> findByUserId(String userId) {
        return em
                .createQuery(
                        String.format("SELECT n FROM %s n WHERE n.userId=:userId", Notification.class.getSimpleName()),
                        Notification.class).setParameter("userId", userId).getResultList();
    }

    @Override
    public List<Notification> findAllByStatus(NotificationStatus status, String userPrefix) {
        String likeRestrictionValue = userPrefix + "%";
        return em
                .createQuery(
                        String.format("SELECT n FROM %s n WHERE n.userId LIKE :prefix and n.status = :status", Notification.class.getSimpleName()),
                        Notification.class)
                .setParameter("status", status)
                .setParameter("prefix", likeRestrictionValue)
                .getResultList();
    }

    @Override
    public List<Notification> findAllByUserIdAndKey(String userId, String key) {
        return em
                .createQuery(
                        String.format("SELECT n FROM %s n WHERE n.userId=:userId AND n.key=:key",
                                Notification.class.getSimpleName()),
                        Notification.class).setParameter("userId", userId).setParameter("key", key).getResultList();
    }

    @Override
    public List<Notification> findRandomUnsent(int count) {
        return em
                .createQuery(
                        String.format(
                            "SELECT n FROM %s n WHERE n.status = :statusCreated OR n.status is null ORDER BY rand()",
                                Notification.class.getSimpleName()), Notification.class)
                .setParameter("statusCreated", NotificationStatus.CREATED)
                .setMaxResults(count)
                .getResultList();
    }
}
