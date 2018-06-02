package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import rx.Observable;
import se.tink.backend.common.utils.repository.PrefixRepository;
import se.tink.backend.common.utils.repository.RepositoryUtils;
import se.tink.backend.core.ApplicationRow;

public class ApplicationRepositoryImpl implements ApplicationRepositoryCustom, PrefixRepository<ApplicationRow> {
    private static final int MAX_BATCH_SIZE = 10000;

    @PersistenceContext
    private EntityManager em;

    @Override
    public Observable<ApplicationRow> streamAll() {
        return RepositoryUtils.streamAll(this, MAX_BATCH_SIZE);
    }

    @Override
    public int countByIdPrefix(String prefix) {
        Number count = (Number) em.createNativeQuery("SELECT COUNT(id) FROM applications WHERE id LIKE ?")
                .setParameter(1, prefix + "%")
                .getSingleResult();
        return count.intValue();
    }

    @Override
    public List<ApplicationRow> listByIdPrefix(String prefix) {
        TypedQuery<ApplicationRow> query = em
                .createQuery("SELECT a FROM ApplicationRow a WHERE a.id LIKE :applicationId",
                        ApplicationRow.class);
        String likeRestrictionValue = String.format("%s%%", prefix);
        return query.setParameter("applicationId", likeRestrictionValue).getResultList();
    }
}
