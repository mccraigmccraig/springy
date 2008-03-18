package springy.context;

import org.springframework.beans.factory.support.ManagedMap;

import java.util.Map;

/**
 * Placeholder class: implemented/extended by JRuby.
 */
public abstract class AbstractSerializableManagedMap extends ManagedMap implements XmlSerializable {
    public AbstractSerializableManagedMap() {
    }

    public AbstractSerializableManagedMap(int initialCapacity) {
        super(initialCapacity);
    }
}
