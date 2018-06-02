package se.tink.backend.common.repository.mysql.main;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import se.tink.backend.core.DeletedUser;
import se.tink.backend.core.DeletedUserStatus;

@Repository
public interface DeletedUserRepository extends JpaRepository<DeletedUser, Long> {
    DeletedUser findOneByUserId(String userId);

    List<DeletedUser> findAllByStatusIn(Collection<DeletedUserStatus> statuses);
}
