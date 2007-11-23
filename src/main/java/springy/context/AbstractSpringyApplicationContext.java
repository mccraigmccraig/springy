package springy.context;

import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ByteArrayResource;

import java.util.List;
import java.util.ArrayList;

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

    public boolean isDirty()
    {
        SpringyApplicationContext parent = (SpringyApplicationContext) getParent();

        return dirty || ( parent != null && parent.isDirty() );
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

    /** refresh this context if it has been marked dirty, or it's dependencies have been refreshed
     * @return true if the context was refreshed */
    synchronized private boolean refreshIfNeeded( boolean dependencyRefreshed )
    {
        if ( dirty || dependencyRefreshed )
        {
            refresh();
            dirty = false;
            return true;
        }
        else
        {
            return false;
        }
    }


    /** descends the chain of contexts from the root, refreshing any ApplicationContexts containing beans
     * marked as dirty [ by a <code>getBeanAndDirty</code> call ], and their dependent ApplicationContexts 
     */
    public void refreshAllDirtyContexts()
    {
        ApplicationContext context = this;

        // get a list of the chain of ApplicationContexts
        List<ApplicationContext> contextList = new ArrayList<ApplicationContext>();
        while( context != null )
        {
            contextList.add( context );
            context = context.getParent();
        }

        // go from root to leaf [i.e. reverse list order],
        // looking for dirty contexts. once a dirty countext
        // is found, refresh all contexts from there to [including] the leaf

        boolean refresh = false;
        for( int i = contextList.size() - 1 ; i>= 0 ; i-- )
        {
            context = contextList.get( i );

            if ( context instanceof AbstractSpringyApplicationContext )
            {
                AbstractSpringyApplicationContext rsc = (AbstractSpringyApplicationContext)context;
                refresh = refresh | rsc.refreshIfNeeded( refresh );

                if ( refresh )
                {
                    // System.err.println( "refreshing SpringyApplicationContext: " + rsc.getDisplayName() );
                }
            }
            else if ( context instanceof AbstractRefreshableApplicationContext )
            {
                AbstractRefreshableApplicationContext rc = (AbstractRefreshableApplicationContext)context;
                if ( refresh )
                {
                    rc.refresh();
                    // System.err.println( "refreshing AbstractRefreshableApplicationContext: " + rc.getDisplayName() );
                }
            }
            else
            {
                if ( refresh )
                {
                    throw new IllegalStateException( "dependent ApplicationContext has been refreshed, so this ApplicationContext requires refresh, but doesn't support it: " + context.getDisplayName() );
                }
            }
        }

        if ( refresh )
        {
            this.publishEvent( new SpringyContextChainRefreshEvent(this) );
        }
    }

    public void init()
    {
        List<AbstractApplicationContext> contextList = new ArrayList<AbstractApplicationContext>();

        ApplicationContext ctx = this;
        while( ctx != null )
        {
            if ( ctx instanceof AbstractApplicationContext )
            {
                contextList.add(0 , (AbstractApplicationContext) ctx );
            }
            ctx = ctx.getParent();
        }

        for( AbstractApplicationContext refreshCtx : contextList )
        {
            refreshCtx.refresh();
        }

        this.publishEvent( new SpringyContextChainRefreshEvent(this) );
    }


}
