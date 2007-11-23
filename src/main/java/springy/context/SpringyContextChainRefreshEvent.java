package springy.context;

import org.springframework.context.ApplicationEvent;

/**
 * SpringyContextChainRefreshEvent
 */
public class SpringyContextChainRefreshEvent
        extends ApplicationEvent
{
    public SpringyContextChainRefreshEvent(AbstractSpringyApplicationContext source)
    {
        super(source);
    }
}
