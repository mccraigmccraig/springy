package springy.context;

import org.springframework.beans.factory.support.ManagedMap;

import java.util.Map;

/**
 * TODO: comment
 */
public abstract class AbstractSerializableManagedMap extends ManagedMap implements XmlSerializable {
    public AbstractSerializableManagedMap() {
    }

    public AbstractSerializableManagedMap(int initialCapacity) {
        super(initialCapacity);
    }

    public AbstractSerializableManagedMap(Map targetMap) {
        super(targetMap);
    }
}
