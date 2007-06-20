package springy.util;

import org.apache.bsf.BSFException;
import org.jruby.exceptions.RaiseException;
import org.jruby.RubyObject;
import org.jruby.Ruby;
import org.jruby.javasupport.JavaUtil;
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
    public static Object getProperty( RubyObject obj , String propertyName )
    {
        Ruby runtime = obj.getRuntime();

        IRubyObject rubyResult = obj.callMethod( runtime.getCurrentContext() , propertyName );
        Object javaResult = JavaUtil.convertRubyToJava( rubyResult );
        return javaResult;
    }

    // these methods below all depends on JRuby internals
    // and are therefore commented out

    /*
    public static IRuby runtime = Ruby.getDefaultInstance();

    public static IRubyObject eval(final Node n) {
        return (IRubyObject) runAndRethrow(new RubyRunnable() {
            public Object run() {
                return runtime.eval(n);
            }
        }
        );
    }

    public static void registerGlobal(String name, Object obj) {
        runtime.defineGlobalConstant(name, JavaUtil.convertJavaToRuby(runtime, obj));
    }

    public static IRubyObject j2r(Object o) {
        IRubyObject obj = JavaUtil.convertJavaToRuby(runtime, o);
        if (obj instanceof JavaObject) {
            return JRubyHelper.runtime.getModule("JavaUtilities").callMethod(runtime.getCurrentContext(), "wrap", obj);
        }
        return obj;
    }

    public static Node parse(String resource) {
        final Node script = runtime.parse(
                IOHelper.inputStreamToString(JRubyHelper.class.getResourceAsStream(resource)),
                resource,
                null);
        return script;
    }


    public static Object callMethodWithStrings(final IRubyObject obj, final String name, final String... s) {
        return runAndRethrow(new RubyRunnable() {
            public Object run() {
                return obj.callMethod(runtime.getThreadService().getCurrentContext(), name, getRubyStrings(s), CallType.NORMAL);
            }
        });
    }

    public static RubyString[] getRubyStrings(String... s) {
        RubyString[] rs = new RubyString[s.length];
        for (int i = 0; i < s.length; i++) {
            rs[i] = RubyString.newString(runtime, s[i]);
        }
        return rs;
    }

    public interface RubyRunnable {
        Object run();
    }

    public static Object runAndRethrow(RubyRunnable r) {

        try {
            return r.run();
        } catch (RaiseException e) {
            System.err.println(e.getMessage());
            e.getException().printBacktrace(System.err);

            // need to rethrow as RuntimeException, otherwise testng is
            // not happy
            throw new RuntimeException(e.getCause());
        }
    }
    */
}
