package springy.context;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.beans.BeansException;
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

    /** mark this context as dirty */
    void markDirty();

    /** get a bean, and mark this context as dirty */
    Object getBeanAndMarkDirty(String name) throws BeansException;

    /** ascend the parent chain, refreshing all contexts marked as dirty, and publish a SpringyContextChainRefreshEvent if needed */
    void refreshAllDirtyContexts();

    /** @return true if any of the contexts in the hierarchy are dirty */
    boolean isDirty();

    /** initialise, and publish a SpringyContextChainRefreshEvent */
    void init();
}
