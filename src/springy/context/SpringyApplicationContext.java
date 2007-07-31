package springy.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.w3c.dom.Document;

/**
 * A Spring application context configured with a Ruby DSL.
 */
public interface SpringyApplicationContext extends ConfigurableApplicationContext {

    /**
     * @return the context serialized as xml document.
     */
    public String getContextAsXml();

    /**
     * @return the context serialized as xml document.
     */
    public Document getContextAsDocument();
}
