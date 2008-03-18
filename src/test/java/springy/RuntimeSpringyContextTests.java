package springy;

import org.jruby.Ruby;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;
import org.testng.Assert;
import springy.context.RuntimeSpringyContext;
import springy.beans.Bean6;

@Test
public class RuntimeSpringyContextTests extends SpringyContextTests {

  /** create from a file */
  protected ConfigurableApplicationContext createContext() throws Exception {
        return new RuntimeSpringyContext(Ruby.getDefaultInstance(),
                new ClassPathResource("springy/context.rb"));
    }

  /** create directly from a string */  
  protected ConfigurableApplicationContext createContext( String context ) throws Exception {
      return new RuntimeSpringyContext( Ruby.getDefaultInstance() , context );
    }
}
