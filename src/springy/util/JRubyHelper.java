package springy.util;

import org.apache.bsf.BSFException;
import org.jruby.exceptions.RaiseException;

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
}
