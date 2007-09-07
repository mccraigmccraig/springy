package springy.context;

import org.jruby.Ruby;
import org.jruby.RubyArray;
import org.jruby.exceptions.RaiseException;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.GlobalVariable;
import org.jruby.runtime.IAccessor;
import org.jruby.runtime.builtin.IRubyObject;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.springframework.beans.factory.parsing.Location;
import org.springframework.beans.factory.parsing.Problem;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.support.AbstractRefreshableApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.w3c.dom.Document;
import springy.util.IOHelper;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;
import java.util.HashSet;

/**
 * This context uses an existing Ruby runtime (org.jruby.Ruby) to construct the context
 * which can be useful in cases where the caller has already created a runtime and wants springy
 * to use this instead of creating a new one.
 *
 */
public class RuntimeSpringyContext extends AbstractSpringyApplicationContext
        implements SpringyApplicationContext {


    private static RuntimeSpringyContext parentContext( Ruby runtime, boolean refresh,  Resource... resources )
    {
        RuntimeSpringyContext parentContext = null;
        if ( resources.length > 1 )
        {
            // give all but the last resource to the parent constructor...
            Resource[] parentResources;
            parentResources = new Resource[ resources.length - 1 ];
            System.arraycopy( resources , 0 , parentResources , 0 , parentResources.length );

            parentContext = new RuntimeSpringyContext( runtime, refresh, parentResources );
        }

        return parentContext;
    }

    private String serializedContext;
    private Document serializedContextAsDocument;

    private Resource contextResource;
    private Ruby runtime;

    /**
     * @param context as a string. used for testing.
     */
    public RuntimeSpringyContext(Ruby runtime, String... context) {
        this(runtime, stringArrayToResourceArray( context ));
    }

    /**
     * @param aContextResource where to find the ruby configuration
     */
    public RuntimeSpringyContext(Ruby runtime, Resource... aContextResource) {
        this(runtime, true, aContextResource );
    }

    /** construct a chain of RuntimeSpringyContexts
     * @param contextResources where to find the ruby configuration... ordered with root context first
     * @param refresh          refreshs the context immediately
     */
    public RuntimeSpringyContext(Ruby runtime, boolean refresh, Resource... contextResources ) {
        super( parentContext( runtime , refresh , contextResources ) );
        this.runtime = runtime;
        this.contextResource = thisContextResource( contextResources );

        if (refresh) {
            refresh();
        }
    }

    protected void loadBeanDefinitions(final DefaultListableBeanFactory beanFactory) throws IOException, BeansException {

        String springy = "load 'springy/context/springy_parse_prepare.rb'";
        String ctxt = IOHelper.inputStreamToString(contextResource.getInputStream());

        addGlobal("bean_factory", beanFactory);

        try {
            runtime.evalScript(new StringReader(springy), "(springy-parse-prepare-fragment)");
            runtime.evalScript(new StringReader(ctxt), contextResource.getFilename() );
        } catch (RaiseException rex) {

            System.err.println(rex.getException().toString());

            rex.printStackTrace(System.err);
            rex.getException().printBacktrace(System.err);


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

    public String getContextAsXml() {
        serializeContext();
        return serializedContext;
    }

    public Document getContextAsDocument() {
        serializeContext();
        return serializedContextAsDocument;
    }

    /**
     * Serializes the context to XML.
     */
    private synchronized void serializeContext() {
        if (serializedContext == null || serializedContextAsDocument == null) {
            if (!isActive())
                refreshBeanFactory();

            RubyArray a = (RubyArray) runtime.evalScript("serialize_context");
            serializedContext = a.get(0).toString();
            serializedContextAsDocument = (Document) a.get(1);
        }
    }

    private void addGlobal(String name, final Object o) {

        runtime.getGlobalVariables().defineReadonly(GlobalVariable.variableName(name),
                new IAccessor() {
                    public IRubyObject getValue() {
                        return JavaEmbedUtils.javaToRuby( runtime, o );
                    }
                    public IRubyObject setValue(IRubyObject newValue) {
                        return newValue;
                    }
                });

    }
}
