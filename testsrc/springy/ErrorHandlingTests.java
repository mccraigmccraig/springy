package springy;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.beans.factory.parsing.BeanDefinitionParsingException;
import org.testng.annotations.Test;
import springy.context.JRubyApplicationContext;


@Test
public class ErrorHandlingTests {

    @Test(expectedExceptions = BeanDefinitionParsingException.class)
    public void testErrorHandling() {
        ApplicationContext ctxt = new JRubyApplicationContext(new ClassPathResource("/springy/context-with-errors.rb"));
    }
}
