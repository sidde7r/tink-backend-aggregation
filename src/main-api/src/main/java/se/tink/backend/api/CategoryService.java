package se.tink.backend.api;

import java.util.List;
import se.tink.backend.core.Category;
import se.tink.backend.core.User;


public interface CategoryService {
    List<Category> list(User user, String locale);
}
