package springy.util;

import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.FactoryBeanNotInitializedException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
import org.springframework.util.ClassUtils;

import java.lang.reflect.InvocationTargetException;

/**
 * MethodInvokingDisposerBean is a factory bean which calls a static method on context disposal...
 * it's used to undo the work of a MethodInvokingFactoryBean, as used by the static_bean method
 * in a springy context
 */
public class MethodInvokingDisposerBean
        extends ArgumentConvertingMethodInvoker
        implements FactoryBean, BeanClassLoaderAware, BeanFactoryAware, InitializingBean, DisposableBean
{

	private boolean singleton = true;

	private ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

	private ConfigurableBeanFactory beanFactory;

    private boolean destroyed = false;

    /**
	 * Set if a singleton should be created, or a new object on each
	 * request else. Default is "true".
	 */
	public void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	public boolean isSingleton() {
		return this.singleton;
	}

	public void setBeanClassLoader(ClassLoader classLoader) {
		this.beanClassLoader = classLoader;
	}

	protected Class resolveClassName(String className) throws ClassNotFoundException {
		return ClassUtils.forName(className, this.beanClassLoader);
	}

	public void setBeanFactory(BeanFactory beanFactory) {
		if (beanFactory instanceof ConfigurableBeanFactory) {
			this.beanFactory = (ConfigurableBeanFactory) beanFactory;
		}
	}

	/**
	 * Obtain the TypeConverter from the BeanFactory that this bean runs in,
	 * if possible.
	 * @see ConfigurableBeanFactory#getTypeConverter()
	 */
	protected TypeConverter getDefaultTypeConverter() {
		if (this.beanFactory != null) {
			return this.beanFactory.getTypeConverter();
		}
		else {
			return super.getDefaultTypeConverter();
		}
	}

    public void afterPropertiesSet() throws Exception
    {
        prepare();
    }

    public void destroy() throws Exception
    {
        if ( ! destroyed ) // spring invokes twice if the container destroy method is "destroy" and DisposableBean is implemented
        {
            doInvoke();
            destroyed = true;
        }
    }

    /**
	 * Perform the invocation and convert InvocationTargetException
	 * into the underlying target exception.
	 */
	private Object doInvoke() throws Exception {
		try {
			return invoke();
		}
		catch (InvocationTargetException ex) {
			if (ex.getTargetException() instanceof Exception) {
				throw (Exception) ex.getTargetException();
			}
			if (ex.getTargetException() instanceof Error) {
				throw (Error) ex.getTargetException();
			}
			throw ex;
		}
	}


	/**
	 * we don't have a value : null every time
	 */
	public Object getObject() throws Exception {
        return null;
	}

	/**
	 * Return the type of object that this FactoryBean creates,
	 * or <code>null</code> if not known in advance.
	 */
	public Class getObjectType() {
		if (!isPrepared()) {
			// Not fully initialized yet -> return null to indicate "not known yet".
			return null;
		}
		return getPreparedMethod().getReturnType();
	}

}