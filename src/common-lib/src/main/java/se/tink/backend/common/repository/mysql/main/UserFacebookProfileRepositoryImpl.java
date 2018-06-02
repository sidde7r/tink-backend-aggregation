package se.tink.backend.common.repository.mysql.main;

import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;

import se.tink.backend.core.UserConnectedServiceStates;
import se.tink.backend.core.UserFacebookProfile;
import se.tink.libraries.date.DateUtils;

public class UserFacebookProfileRepositoryImpl implements UserFacebookProfileRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional
    public void deleteByUserId(String userId) {
        em.createQuery("delete from UserFacebookProfile p where p.userId = :userId").setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public List<UserFacebookProfile> findStale() {
        Calendar threshold = DateUtils.getCalendar();
        threshold.add(Calendar.DAY_OF_YEAR, -3);

        return em
                .createQuery("select p from UserFacebookProfile p where p.state = :active_state and (p.updated = null or p.updated < :threshold)",
                        UserFacebookProfile.class).
                        setParameter("active_state", UserConnectedServiceStates.ACTIVE).
                        setParameter("threshold", threshold.getTime()).
                        getResultList();
    }
}
