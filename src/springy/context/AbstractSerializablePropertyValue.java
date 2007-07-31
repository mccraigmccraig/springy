package springy.context;

import org.springframework.beans.PropertyValue;

/**
 * Placeholder class: implemented/extended by JRuby.
 */
public abstract class AbstractSerializablePropertyValue extends PropertyValue implements XmlSerializable{
    public AbstractSerializablePropertyValue(String name, Object value) {
        super(name, value);
    }

    public AbstractSerializablePropertyValue(PropertyValue original) {
        super(original);
    }
}
