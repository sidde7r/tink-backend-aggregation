package se.tink.libraries.discovery;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.WatcherRemoveCuratorFramework;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetACLBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetConfigBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.api.ReconfigBuilder;
import org.apache.curator.framework.api.RemoveWatchesBuilder;
import org.apache.curator.framework.api.SetACLBuilder;
import org.apache.curator.framework.api.SetDataBuilder;
import org.apache.curator.framework.api.SyncBuilder;
import org.apache.curator.framework.api.UnhandledErrorListener;
import org.apache.curator.framework.api.transaction.CuratorMultiTransaction;
import org.apache.curator.framework.api.transaction.CuratorTransaction;
import org.apache.curator.framework.api.transaction.TransactionOp;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.listen.Listenable;
import org.apache.curator.framework.schema.SchemaSet;
import org.apache.curator.framework.state.ConnectionStateErrorPolicy;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.utils.EnsurePath;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.server.quorum.flexible.QuorumVerifier;

public class FakeCuratorFramework implements CuratorFramework {

    @Override
    public CompletableFuture<Void> runSafe(Runnable runnable) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public CompletableFuture<Void> postSafeNotify(Object monitorHolder) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void start() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public CuratorFrameworkState getState() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isStarted() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public CreateBuilder create() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public DeleteBuilder delete() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ExistsBuilder checkExists() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public GetDataBuilder getData() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public SetDataBuilder setData() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public GetChildrenBuilder getChildren() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public GetACLBuilder getACL() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public SetACLBuilder setACL() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ReconfigBuilder reconfig() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public GetConfigBuilder getConfig() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public CuratorTransaction inTransaction() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public CuratorMultiTransaction transaction() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public TransactionOp transactionOp() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void sync(String s, Object o) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void createContainers(String s) throws Exception {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public SyncBuilder sync() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public RemoveWatchesBuilder watches() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Listenable<ConnectionStateListener> getConnectionStateListenable() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Listenable<CuratorListener> getCuratorListenable() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Listenable<UnhandledErrorListener> getUnhandledErrorListenable() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public CuratorFramework nonNamespaceView() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public CuratorFramework usingNamespace(String s) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public String getNamespace() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public CuratorZookeeperClient getZookeeperClient() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public EnsurePath newNamespaceAwareEnsurePath(String s) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void clearWatcherReferences(Watcher watcher) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean blockUntilConnected(int i, TimeUnit timeUnit) throws InterruptedException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void blockUntilConnected() throws InterruptedException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public WatcherRemoveCuratorFramework newWatcherRemoveCuratorFramework() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ConnectionStateErrorPolicy getConnectionStateErrorPolicy() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public QuorumVerifier getCurrentConfig() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public SchemaSet getSchemaSet() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean isZk34CompatibilityMode() {
        throw new UnsupportedOperationException("Not implemented");
    }
}
