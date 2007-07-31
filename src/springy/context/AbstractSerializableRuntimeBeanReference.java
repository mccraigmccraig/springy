package springy.context;

import org.springframework.beans.factory.config.RuntimeBeanReference;

/**
 * Placeholder class: implemented/extended by JRuby.
 */
public abstract class AbstractSerializableRuntimeBeanReference extends RuntimeBeanReference implements XmlSerializable{
    /**
     * Create a new RuntimeBeanNameReference to the given bean name.
     *
     * @param beanName name of the target bean
     */
    public AbstractSerializableRuntimeBeanReference(String beanName) {
        super(beanName);
    }
}
