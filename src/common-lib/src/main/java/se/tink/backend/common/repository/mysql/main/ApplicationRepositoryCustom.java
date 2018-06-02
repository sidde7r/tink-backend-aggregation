package se.tink.backend.common.repository.mysql.main;

import rx.Observable;
import se.tink.backend.core.ApplicationRow;

public interface ApplicationRepositoryCustom {
    Observable<ApplicationRow> streamAll();
}
