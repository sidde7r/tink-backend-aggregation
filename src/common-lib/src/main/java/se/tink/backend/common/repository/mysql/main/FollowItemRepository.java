package se.tink.backend.common.repository.mysql.main;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import se.tink.backend.core.follow.FollowItem;
import se.tink.backend.core.follow.FollowTypes;

public interface FollowItemRepository extends JpaRepository<FollowItem, String>, FollowItemRepositoryCustom {
    List<FollowItem> findByUserId(String userId);
    long countByUserIdAndTypeIn(String userId, Collection<FollowTypes> type);
}
