package springy;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.jruby.Ruby;
import springy.context.RuntimeSpringyContext;
import springy.beans.Bean6;
import springy.beans.Bean1;
import springy.beans.Bean7;

/**
 * RefreshContextTests
 */
@Test
public class RefreshContextTests
{
    public RuntimeSpringyContext createContext() throws Exception {
        return new RuntimeSpringyContext(Ruby.getDefaultInstance(),
                new ClassPathResource( "springy/parent_context.rb" ),
                new ClassPathResource("springy/context.rb"));
    }

    public void testContextChainCreation() throws Exception
    {
        RuntimeSpringyContext ctx = createContext();

        Bean6 parentBean = (Bean6) ctx.getBean( "parent_bean" );
        Assert.assertNotNull( parentBean );
    }

    public void testGetBeanAndMarkDirty() throws Exception
    {
        RuntimeSpringyContext rsctx = createContext();

        Bean6 bean = (Bean6) rsctx.getBeanAndMarkDirty( "bean6" );
        Assert.assertEquals( 0 , bean.getCount() );

        // check the bean's state has changed
        bean.incrCount();
        bean = (Bean6) rsctx.getBean( "bean6" );
        Assert.assertEquals( 1 , bean.getCount() );

        // refresh the context
        rsctx.refreshAllDirtyContexts();

        Bean6 newBean = (Bean6)rsctx.getBean( "bean6" );
        Assert.assertNotSame( bean , newBean );
        Assert.assertEquals( 0 , newBean.getCount() );
    }
    
    public void MarkDirty() throws Exception
    {
        RuntimeSpringyContext rsctx = createContext();

        Bean6 bean = (Bean6) rsctx.getBean( "bean6" );
        Assert.assertEquals( 0 , bean.getCount() );

        // check the bean's state has changed
        bean.incrCount();
        bean = (Bean6) rsctx.getBean( "bean6" );
        Assert.assertEquals( 1 , bean.getCount() );

        // mark the context dirty
        rsctx.markDirty();

        // refresh the context
        rsctx.refreshAllDirtyContexts();

        Bean6 newBean = (Bean6)rsctx.getBean( "bean6" );
        Assert.assertNotSame( bean , newBean );
        Assert.assertEquals( 0 , newBean.getCount() );
    }
    public void testRefreshDirtyOnly() throws Exception
    {
        RuntimeSpringyContext ctx = createContext();

        // change the state of a bean in the parent context without dirtying it
        Bean6 parentBean = (Bean6) ctx.getBean( "parent_bean" );
        parentBean.incrCount();
        parentBean = (Bean6) ctx.getBean( "parent_bean" );
        Assert.assertEquals( parentBean.getCount() , 1 );

        // change the state of a bean in the child context, and dirty it
        Bean6 childBean = (Bean6)ctx.getBeanAndMarkDirty( "bean6" );
        childBean.incrCount();
        childBean = (Bean6) ctx.getBean( "bean6" );
        Assert.assertEquals( childBean.getCount() , 1 );

        ctx.refreshAllDirtyContexts();

        // check that the bean from the parent context still has the modified state
        Bean6 newParentBean = (Bean6) ctx.getBean( "parent_bean" );
        Assert.assertEquals( newParentBean.getCount() , 1 );

        // check that the bean from the child context has a renewed state
        Bean6 newChildBean = (Bean6) ctx.getBean( "bean6" );
        Assert.assertEquals( newChildBean.getCount() , 0 );

    }

    public void testStaticBeanDispose() throws Exception
    {
        RuntimeSpringyContext ctx = createContext();

        int disposerCount = Bean7.getDisposerCount();

        ctx.markDirty();
        ctx.refreshAllDirtyContexts();

        int newDisposerCount = Bean7.getDisposerCount();

        Assert.assertEquals( newDisposerCount , disposerCount + 1 );
    }
}
