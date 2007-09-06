package springy.context;

import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;

/**
 * AbstractSpringyApplicationContext
 */
public abstract class AbstractSpringyApplicationContext
        extends AbstractRefreshableApplicationContext
        implements SpringyApplicationContext
{
    public static Resource[] stringArrayToResourceArray( String... context )
    {
        Resource[] resources = new Resource[ context.length ];
        for(int i = 0 ; i < context.length ; i++ )
        {
            resources[ i ] = new ByteArrayResource( context[ i ].getBytes() );
        }
        return resources;
    }

    public static Resource thisContextResource( Resource... resources )
    {
        return resources[ resources.length -1 ];
    }

    private boolean dirty = false;

    public AbstractSpringyApplicationContext(ApplicationContext parent)
    {
        super(parent);
    }

    synchronized public void markDirty()
    {
        dirty = true;
    }

    private Object getBeanAndMarkDirtyImpl( String name ) throws BeansException
    {
        Object bean = getBean( name );
        markDirty();
        return bean;
    }

    /** get a bean, and mark it as dirty... later, a call to <code>refreshAllDirtyContexts</code>
     * can be used to refresh all ApplicationContexts in the chain which contain dirty beans [
     * @param name
     * @return
     * @throws org.springframework.beans.BeansException
     */
    public Object getBeanAndMarkDirty(String name) throws BeansException
    {
        ApplicationContext context = this;

        while( context != null )
        {
            if ( context.containsLocalBean( name ) )
            {
                if ( context instanceof AbstractSpringyApplicationContext )
                {
                    AbstractSpringyApplicationContext rsc = (AbstractSpringyApplicationContext)context;
                    return rsc.getBeanAndMarkDirtyImpl( name );
                }
                else
                {
                    return context.getBean( name );
                }
            }

            context = context.getParent();
        }

        throw new NoSuchBeanDefinitionException( name );
    }

    synchronized private void refreshIfDirty()
    {
        if ( dirty )
        {
            refresh();
            dirty = false;
        }
    }

    /** ascends the chain of contexts, refreshing any ApplicationCntexts containing beans
     * marked as dirty [ by a <code>getBeanAndDirty</code> call ]
     */
    public void refreshAllDirtyContexts()
    {
        ApplicationContext context = this;

        while( context != null )
        {
            if ( context instanceof AbstractSpringyApplicationContext )
            {
                AbstractSpringyApplicationContext rsc = (AbstractSpringyApplicationContext)context;
                rsc.refreshIfDirty();
            }

            context = context.getParent();
        }
    }

}
