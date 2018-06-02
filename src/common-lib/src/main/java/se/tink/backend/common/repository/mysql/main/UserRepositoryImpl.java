package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import rx.Observable;
import se.tink.backend.common.utils.repository.PrefixRepository;
import se.tink.backend.common.utils.repository.RepositoryUtils;
import se.tink.backend.core.Notification;
import se.tink.backend.core.User;

public class UserRepositoryImpl implements UserRepositoryCustom, PrefixRepository<User> {

    private static final int MAX_BATCH_SIZE = 10000;

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public List<String> findAllUserIds() {
        return em.createQuery("select distinct(u.id) from User u", String.class).getResultList();
    }

    @Override
    public Observable<User> streamAll() {
        return RepositoryUtils.streamAll(this, MAX_BATCH_SIZE);
    }

    @Override
    public int countByIdPrefix(String prefix) {
        Number count = (Number) em.createNativeQuery("SELECT COUNT(id) FROM users WHERE id LIKE ?")
                .setParameter(1, prefix + "%")
                .getSingleResult();
        return count.intValue();
    }

    @Override
    public List<User> listByIdPrefix(String prefix) {
        TypedQuery<User> query = em.createQuery("select u from User u where u.id LIKE :userId",
                User.class);
        String likeRestrictionValue = String.format("%s%%", prefix);
        return query.setParameter("userId", likeRestrictionValue).getResultList();
    }

    @Override
    public void check() {
        // We expect this to throw exception on error.
        em.createQuery(String.format("SELECT 1 FROM %s", User.class.getSimpleName())).setMaxResults(1).getResultList();
    }

}
