package springy.util;

import org.apache.bsf.BSFException;
import org.jruby.exceptions.RaiseException;
import org.jruby.Ruby;
import org.jruby.javasupport.JavaEmbedUtils;
import org.jruby.runtime.GlobalVariable;
import org.jruby.runtime.IAccessor;
import org.jruby.runtime.builtin.IRubyObject;

/**
 * JRuby helper class.
 */
public abstract class JRubyHelper {
    public static void printBsfException(BSFException e) {
        RaiseException rex = (RaiseException) e.getTargetException();
        System.err.println(rex.getException().toString());

        rex.printStackTrace(System.err);
        rex.getException().printBacktrace(System.err);
    }

    /** get a property from a RubyObject */
    /*
    public static Object getProperty( IRubyObject obj , String propertyName )
    {
        Ruby runtime = obj.getRuntime();

        IRubyObject rubyResult = obj.callMethod( runtime.getCurrentContext() , propertyName );
        Object javaResult = JavaUtil.convertRubyToJava( rubyResult );
        return javaResult;
    }
    */
    public static void addGlobal(final Ruby runtime, String name, final Object o) {

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
