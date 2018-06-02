package se.tink.backend.system.cli.helper.traversal;

import org.junit.Assert;
import org.junit.Test;
import rx.Observable;

public class CommandLineInterfaceUserIdTraverserTest {

    @Test
    public void testBuildingFromSystemProperties() throws Exception {
        System.setProperty("allowAllUsers", "true");
        Observable<String> allUsers = Observable.just("c3f5ab4b7a514ceeb9cfb4c3effa57d6").repeat(2);
        Assert.assertEquals(new Integer(2), allUsers.compose(new CommandLineInterfaceUserIdTraverser(10)).count()
                .toBlocking().last());

    }
}
