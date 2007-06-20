package springy.beans;

import org.apache.bsf.BSFManager;

import java.util.List;

import springy.context.SpringyAware;

public class Bean1 implements IBean, SpringyAware {

    private boolean initialised = false;
    private boolean destroyed = false;
    private static boolean staticCalled = false;
    private Bean2 bean2;
    private List listProperty;
    public static int sP1;
    public static String sP2;


    public boolean booleanProperty;

    public static IBean staticBeanRef;


    public static int aStaticField;
    public int something;
    public Object context;
    public boolean beforeInitCalled;

    public Bean2 getBean2() {
        return bean2;
    }

    public void setBean2(Bean2 bean2) {
        this.bean2 = bean2;
    }

    public boolean isBooleanProperty() {
        return booleanProperty;
    }

    public void setBooleanProperty(boolean booleanProperty) {
        this.booleanProperty = booleanProperty;
    }


    public String toString() {
        return getClass().getSimpleName() + "[" +
                "bean2=" + bean2 +
                ']';
    }

    public List getListProperty() {
        return listProperty;
    }

    public void setListProperty(List listProperty) {
        this.listProperty = listProperty;
    }

    public void initialise() {
        initialised = true;
    }

    public boolean wasInitialised() {
        return initialised;
    }

    public void myInit() {
        initialise();
    }

    public void destroy() {
        //System.err.println(getClass().getSimpleName() + " destroy");
        destroyed = true;

        staticBeanRef = null;
        sP1 = 0;
        sP2 = null;
    }

    public boolean wasDestroyed() {
        return destroyed;
    }

    public static void staticMethod() {
        staticCalled = true;
    }

    public static boolean staticMethodWasCalled() {
        return staticCalled;
    }

    public static void staticMethodWithParameters(int p1, String p2, IBean bean) {
        sP1 = p1;
        sP2 = p2;
        staticBeanRef = bean;
    }

    public void doSomething(int foo) {
        //System.err.println("doSomething(" + foo + ")");
        this.something = foo;
    }

    public void setSpringyContext(Object context) {
        //System.err.println("context="+context);
        this.context = context;
    }

    public void setBeforeInitCalled() {
        this.beforeInitCalled = true;
    }
}
