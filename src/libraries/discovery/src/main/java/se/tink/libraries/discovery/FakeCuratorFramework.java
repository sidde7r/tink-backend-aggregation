package se.tink.libraries.discovery;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.WatcherRemoveCuratorFramework;
import org.apache.curator.framework.api.ACLBackgroundPathAndBytesable;
import org.apache.curator.framework.api.ACLCreateModeBackgroundPathAndBytesable;
import org.apache.curator.framework.api.ACLCreateModeStatBackgroundPathAndBytesable;
import org.apache.curator.framework.api.ACLPathAndBytesable;
import org.apache.curator.framework.api.ACLableExistBuilderMain;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.BackgroundPathAndBytesable;
import org.apache.curator.framework.api.BackgroundPathable;
import org.apache.curator.framework.api.BackgroundVersionable;
import org.apache.curator.framework.api.ChildrenDeletable;
import org.apache.curator.framework.api.CreateBackgroundModeStatACLable;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.CreateBuilder2;
import org.apache.curator.framework.api.CreateBuilderMain;
import org.apache.curator.framework.api.CreateProtectACLCreateModePathAndBytesable;
import org.apache.curator.framework.api.CuratorListener;
import org.apache.curator.framework.api.CuratorWatcher;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.api.DeleteBuilderMain;
import org.apache.curator.framework.api.ErrorListenerPathAndBytesable;
import org.apache.curator.framework.api.ErrorListenerPathable;
import org.apache.curator.framework.api.ExistsBuilder;
import org.apache.curator.framework.api.GetACLBuilder;
import org.apache.curator.framework.api.GetChildrenBuilder;
import org.apache.curator.framework.api.GetConfigBuilder;
import org.apache.curator.framework.api.GetDataBuilder;
import org.apache.curator.framework.api.ProtectACLCreateModeStatPathAndBytesable;
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
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
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
        return new CreateBuilder() {
            @Override
            public CreateBuilderMain withTtl(long l) {
                return null;
            }

            @Override
            public CreateBuilder2 orSetData() {
                return null;
            }

            @Override
            public CreateBuilder2 orSetData(int i) {
                return null;
            }

            @Override
            public ProtectACLCreateModeStatPathAndBytesable<String> creatingParentsIfNeeded() {
                return null;
            }

            @Override
            public ProtectACLCreateModeStatPathAndBytesable<String>
                    creatingParentContainersIfNeeded() {
                return new ProtectACLCreateModeStatPathAndBytesable<String>() {
                    @Override
                    public ACLCreateModeBackgroundPathAndBytesable<String> withProtection() {
                        return null;
                    }

                    @Override
                    public ErrorListenerPathAndBytesable<String> inBackground() {
                        return null;
                    }

                    @Override
                    public ErrorListenerPathAndBytesable<String> inBackground(Object o) {
                        return null;
                    }

                    @Override
                    public ErrorListenerPathAndBytesable<String> inBackground(
                            BackgroundCallback backgroundCallback) {
                        return null;
                    }

                    @Override
                    public ErrorListenerPathAndBytesable<String> inBackground(
                            BackgroundCallback backgroundCallback, Object o) {
                        return null;
                    }

                    @Override
                    public ErrorListenerPathAndBytesable<String> inBackground(
                            BackgroundCallback backgroundCallback, Executor executor) {
                        return null;
                    }

                    @Override
                    public ErrorListenerPathAndBytesable<String> inBackground(
                            BackgroundCallback backgroundCallback, Object o, Executor executor) {
                        return null;
                    }

                    @Override
                    public ACLBackgroundPathAndBytesable<String> withMode(CreateMode mode) {
                        return null;
                    }

                    @Override
                    public BackgroundPathAndBytesable<String> withACL(
                            List<ACL> aclList, boolean applyToParents) {
                        return null;
                    }

                    @Override
                    public BackgroundPathAndBytesable<String> withACL(List<ACL> list) {
                        return null;
                    }

                    @Override
                    public String forPath(String s, byte[] bytes) {
                        return null;
                    }

                    @Override
                    public String forPath(String s) {
                        return null;
                    }

                    @Override
                    public ACLBackgroundPathAndBytesable<String> storingStatIn(Stat stat) {
                        return null;
                    }
                };
            }

            @Override
            public ACLPathAndBytesable<String> withProtectedEphemeralSequential() {
                return null;
            }

            @Override
            public ACLCreateModeStatBackgroundPathAndBytesable<String> withProtection() {
                return null;
            }

            @Override
            public BackgroundPathAndBytesable<String> withACL(List<ACL> list) {
                return null;
            }

            @Override
            public ErrorListenerPathAndBytesable<String> inBackground() {
                return null;
            }

            @Override
            public ErrorListenerPathAndBytesable<String> inBackground(Object o) {
                return null;
            }

            @Override
            public ErrorListenerPathAndBytesable<String> inBackground(
                    BackgroundCallback backgroundCallback) {
                return null;
            }

            @Override
            public ErrorListenerPathAndBytesable<String> inBackground(
                    BackgroundCallback backgroundCallback, Object o) {
                return null;
            }

            @Override
            public ErrorListenerPathAndBytesable<String> inBackground(
                    BackgroundCallback backgroundCallback, Executor executor) {
                return null;
            }

            @Override
            public ErrorListenerPathAndBytesable<String> inBackground(
                    BackgroundCallback backgroundCallback, Object o, Executor executor) {
                return null;
            }

            @Override
            public CreateBackgroundModeStatACLable compressed() {
                return null;
            }

            @Override
            public ACLBackgroundPathAndBytesable<String> withMode(CreateMode mode) {
                return null;
            }

            @Override
            public BackgroundPathAndBytesable<String> withACL(
                    List<ACL> aclList, boolean applyToParents) {
                return null;
            }

            @Override
            public String forPath(String s, byte[] bytes) {
                return null;
            }

            @Override
            public String forPath(String s) {
                return null;
            }

            @Override
            public CreateProtectACLCreateModePathAndBytesable<String> storingStatIn(Stat stat) {
                return null;
            }
        };
    }

    @Override
    public DeleteBuilder delete() {
        return new DeleteBuilder() {
            @Override
            public BackgroundVersionable deletingChildrenIfNeeded() {
                return null;
            }

            @Override
            public ErrorListenerPathable<Void> inBackground() {
                return null;
            }

            @Override
            public ErrorListenerPathable<Void> inBackground(Object o) {
                return null;
            }

            @Override
            public ErrorListenerPathable<Void> inBackground(BackgroundCallback backgroundCallback) {
                return null;
            }

            @Override
            public ErrorListenerPathable<Void> inBackground(
                    BackgroundCallback backgroundCallback, Object o) {
                return null;
            }

            @Override
            public ErrorListenerPathable<Void> inBackground(
                    BackgroundCallback backgroundCallback, Executor executor) {
                return null;
            }

            @Override
            public ErrorListenerPathable<Void> inBackground(
                    BackgroundCallback backgroundCallback, Object o, Executor executor) {
                return null;
            }

            @Override
            public ChildrenDeletable guaranteed() {
                return null;
            }

            @Override
            public Void forPath(String s) {
                return null;
            }

            @Override
            public DeleteBuilderMain quietly() {
                return null;
            }

            @Override
            public BackgroundPathable<Void> withVersion(int i) {
                return null;
            }
        };
    }

    @Override
    public ExistsBuilder checkExists() {
        return new ExistsBuilder() {
            @Override
            public ACLableExistBuilderMain creatingParentsIfNeeded() {
                return null;
            }

            @Override
            public ACLableExistBuilderMain creatingParentContainersIfNeeded() {
                return null;
            }

            @Override
            public ErrorListenerPathable<Stat> inBackground() {
                return null;
            }

            @Override
            public ErrorListenerPathable<Stat> inBackground(Object o) {
                return null;
            }

            @Override
            public ErrorListenerPathable<Stat> inBackground(BackgroundCallback backgroundCallback) {
                return null;
            }

            @Override
            public ErrorListenerPathable<Stat> inBackground(
                    BackgroundCallback backgroundCallback, Object o) {
                return null;
            }

            @Override
            public ErrorListenerPathable<Stat> inBackground(
                    BackgroundCallback backgroundCallback, Executor executor) {
                return null;
            }

            @Override
            public ErrorListenerPathable<Stat> inBackground(
                    BackgroundCallback backgroundCallback, Object o, Executor executor) {
                return null;
            }

            @Override
            public Stat forPath(String s) {
                return null;
            }

            @Override
            public BackgroundPathable<Stat> watched() {
                return null;
            }

            @Override
            public BackgroundPathable<Stat> usingWatcher(Watcher watcher) {
                return new BackgroundPathable<Stat>() {
                    @Override
                    public ErrorListenerPathable<Stat> inBackground() {
                        return null;
                    }

                    @Override
                    public ErrorListenerPathable<Stat> inBackground(Object o) {
                        return null;
                    }

                    @Override
                    public ErrorListenerPathable<Stat> inBackground(
                            BackgroundCallback backgroundCallback) {
                        return null;
                    }

                    @Override
                    public ErrorListenerPathable<Stat> inBackground(
                            BackgroundCallback backgroundCallback, Object o) {
                        return null;
                    }

                    @Override
                    public ErrorListenerPathable<Stat> inBackground(
                            BackgroundCallback backgroundCallback, Executor executor) {
                        return null;
                    }

                    @Override
                    public ErrorListenerPathable<Stat> inBackground(
                            BackgroundCallback backgroundCallback, Object o, Executor executor) {
                        return null;
                    }

                    @Override
                    public Stat forPath(String s) {
                        return null;
                    }
                };
            }

            @Override
            public BackgroundPathable<Stat> usingWatcher(CuratorWatcher watcher) {
                return null;
            }
        };
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
