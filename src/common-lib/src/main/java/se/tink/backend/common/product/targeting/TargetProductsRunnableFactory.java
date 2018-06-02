package se.tink.backend.common.product.targeting;

import com.google.inject.Inject;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.core.User;

public class TargetProductsRunnableFactory {

    private final TargetProductsController controller;

    @Inject
    public TargetProductsRunnableFactory(TargetProductsController controller) {
        this.controller = controller;
    }

    @Deprecated
    public TargetProductsRunnableFactory(final ServiceContext serviceContext) {
        this(new TargetProductsController(serviceContext));
    }

    public Runnable createRunnable(final User user) {

        if (TargetProductsController.screen(user)) {
            return () -> controller.process(user);
        }

        return null;
    }
}
