package se.tink.backend.common.repository.mysql.main;

import org.springframework.data.jpa.repository.JpaRepository;

import se.tink.backend.core.FollowUp;

public interface FollowUpRepository extends JpaRepository<FollowUp, String> {
    
    public FollowUp findOneByDescription(String description); 

}
