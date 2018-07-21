package sonar.core.sync;

import sonar.core.SonarCore;

import java.util.List;

public class SyncedValueList<V> extends SonarControlledList<V> implements ISyncValue<List<V>> {

    public final ISyncHandler<List<V>> handler;
    public String key;

    public SyncedValueList(Class<V> type, IValueWatcher watcher, String key, List<V> value){
        super(type, watcher, value);
        this.handler = new SyncHandlerList<>(SyncRegistry.getHandler(type));
        this.key = key;
    }


    @Override
    public boolean setValueInternal(List<V> set) {
        int size = set.size();
        if ("8".equals(getTagName()) && size > 6) {
            set.subList( 6, size - 1 ).clear();
            SonarCore.logger.info("Truncated abnormally large connection list 'Size: " + size + "'");
        }
        value.clear();
        return super.setValueInternal(set);
    }

    @Override
    public String getTagName() {
        return key;
    }

    @Override
    public ISyncHandler<List<V>> getSyncHandler() {
        return handler;
    }
}