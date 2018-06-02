package se.tink.backend.common.repository.mysql.main;

import java.util.List;
import rx.Observable;
import se.tink.backend.common.health.Checkable;
import se.tink.backend.core.User;

public interface UserRepositoryCustom extends Checkable {

    List<String> findAllUserIds();
    
    /**
     * Streams users from database. It transparently fetches them in batches. This means this method has a fixed upper
     * limit on memory usage.
     * 
     * @return an observable of all users.
     */
    Observable<User> streamAll();

}
