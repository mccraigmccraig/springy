package springy.context;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.jruby.RubyArray;
import org.jruby.Ruby;
import org.jruby.exceptions.RaiseException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import springy.util.IOHelper;
import springy.util.JRubyHelper;

import java.io.IOException;
import java.util.Map;

/**
 * A Spring application context configured with a Ruby DSL. Internally, it uses the
 * (<a href="http://jakarta.apache.org/bsf/">Bean Scripting Framework<a>) to access
 * JRuby.
 */
public class BSFSpringyContext
        extends AbstractSpringyApplicationContext
        implements SpringyApplicationContext {

    static {
        BSFManager.registerScriptingEngine("ruby", "org.jruby.javasupport.bsf.JRubyEngine", new String[]{"rb"});
    }

    private static BSFSpringyContext parentContext( BSFManager bsfManager, boolean refresh,  Resource... resources )
    {
        BSFSpringyContext parentContext = null;
        if ( resources.length > 1 )
        {
            // give all but the last resource to the parent constructor...
            Resource[] parentResources;
            parentResources = new Resource[ resources.length - 1 ];
            System.arraycopy( resources , 0 , parentResources , 0 , parentResources.length );

            parentContext = new BSFSpringyContext( bsfManager, refresh, parentResources );
        }

        return parentContext;
    }

    private String serializedContext;
    private Document serializedContextAsDocument;

    private Resource contextResource;
    private BSFManager bsfManager;

    /**
     * @param contexts as a string. used for testing.
     */
    public BSFSpringyContext(String... contexts) {
        this(stringArrayToResourceArray( contexts ));
    }

    /**
     * @param contextResources where to find the ruby configuration
     */
    public BSFSpringyContext(Resource... contextResources) {
        this(true , contextResources);
    }

    /**
     * @param contextResources where to find the ruby configuration
     * @param refresh          refreshs the context immediately
     */
    public BSFSpringyContext(boolean refresh, Resource... contextResources) {
        this(new BSFManager() , refresh,  contextResources );
    }

    /**
     * Use this constructor if you need to reuse an existing BSFManager.
     * @param contextResources
     * @param refresh
     * @param bsfManager
     */
    public BSFSpringyContext(BSFManager bsfManager, boolean refresh , Resource... contextResources) {
        super( parentContext(  bsfManager, refresh , contextResources )  );

        this.bsfManager = bsfManager;

        this.contextResource = thisContextResource( contextResources );

        if (refresh) {
            refresh();
        }
    }

    /**
     * @return the context serialized as xml document.
     */
    public String getContextAsXml() {
        serializeContext();
        return serializedContext;
    }

    /**
     * @return the context serialized as xml document.
     */
    public Document getContextAsDocument() {
        serializeContext();
        return serializedContextAsDocument;
    }

    protected void loadBeanDefinitions(DefaultListableBeanFactory beanFactory) throws IOException, BeansException {
        try {
            bsfManager.declareBean("bean_factory", beanFactory, DefaultListableBeanFactory.class);
            bsfManager.declareBean("system_properties", System.getProperties(), Map.class);

            String springy = "load 'springy/context/springy_parse_prepare.rb'";
            String ctxt = IOHelper.inputStreamToString(contextResource.getInputStream());

            bsfManager.eval("ruby", "(java-springy)", 1, 1, springy);
            bsfManager.eval("ruby", "(java-context)", 1, 1, ctxt);
        } catch (BSFException e) {
            //JRubyHelper.printBsfException(e);

            RaiseException rex = (RaiseException) e.getTargetException();
            RubyArray array = (RubyArray) rex.getException().backtrace();
            String lastLine = null;
            if (array.getLength() > 0) {
                lastLine = array.get(array.getLength() - 1).toString();
            }

            String message = rex.getMessage();
            if (message == null) {
                message = rex.getException().message.toString();
            }

            throw new BeanDefinitionParsingException(
                    new Problem(lastLine + ": " + message,
                            new Location(contextResource, lastLine)));
        }
    }

    /**
     * Serializes the context to XML.
     */
    private synchronized void serializeContext() {
        if (serializedContext == null || serializedContextAsDocument == null) {
            if (!isActive())
                refreshBeanFactory();

            try {
                RubyArray a = (RubyArray) bsfManager.eval("ruby", "(serialize-context)", 1, 1, "serialize_context");
                serializedContext = a.get(0).toString();
                serializedContextAsDocument = (Document) a.get(1);
            } catch (BSFException e) {
                JRubyHelper.printBsfException(e);
                throw new RuntimeException(e);
            }
        }
    }
}

