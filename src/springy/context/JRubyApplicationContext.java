package springy.context;

import org.apache.bsf.BSFException;
import org.apache.bsf.BSFManager;
import org.jruby.RubyArray;
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
 * A Spring application context configured with a Ruby DSL.
 */
public class JRubyApplicationContext extends AbstractRefreshableApplicationContext {

    private String serializedContext;
    private Document serializedContextAsDocument;

    static {
        BSFManager.registerScriptingEngine("ruby", "org.jruby.javasupport.bsf.JRubyEngine", new String[]{"rb"});
    }

    private Resource contextResource;
    private BSFManager bsfManager;

    /**
     * @param context as a string. used for testing.
     */
    public JRubyApplicationContext(String context) {
        this(new ByteArrayResource(context.getBytes()));
    }

    /**
     * @param aContextResource where to find the ruby configuration
     */
    public JRubyApplicationContext(Resource aContextResource) {
        this(aContextResource, true);
    }

    /**
     * @param aContextResource where to find the ruby configuration
     * @param refresh          refreshs the context immediately
     */
    public JRubyApplicationContext(Resource aContextResource, boolean refresh) {
        bsfManager = new BSFManager();

        this.contextResource = aContextResource;

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
            bsfManager.declareBean("bsf_manager", bsfManager, BSFManager.class);

            String springy = IOHelper.inputStreamToString(getClass().getResourceAsStream("springy.rb"));
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
    private void serializeContext() {
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

