package se.tink.backend.common.repository.mysql.main;

import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class UserDeviceRepositoryImpl implements UserDeviceRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Transactional
    @Override
    public void deleteByUserId(String userId) {
        em.createQuery("delete from UserDevice ud where ud.userId = :userId").setParameter("userId", userId).executeUpdate();
    }
}
