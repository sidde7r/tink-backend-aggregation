package se.tink.backend.common.repository.mysql.main;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class UserDemographicsRepositoryImpl implements UserDemographicsRepositoryCustom {

    private static final int MAX_BATCH_SIZE = 10000;

    @PersistenceContext
    private EntityManager em;

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, String> findAllUserIdsAndPostalCodes(String marketCode) {
        List<Object> rows = em.createQuery("SELECT userId, postalCode FROM UserDemographics "
                + "WHERE postalCode IS NOT NULL AND market = :market").setParameter("market", marketCode)
                .getResultList();
        Map<String, String> map = Maps.newHashMap();

        for (Object row : rows) {
            Object[] res = (Object[]) row;
            map.put((String) (res[0]), (String) res[1]);
        }

        return map;
    }
}
