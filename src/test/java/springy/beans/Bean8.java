package springy.beans;

/**
 * Bean8
 */
public class Bean8
    implements IBean
{
    private Bean6 bean6;

    public Bean8(Bean6 bean6)
    {
        this.bean6 = bean6;
    }

    public Bean6 getBean6()
    {
        return bean6;
    }

    public void setBean6(Bean6 bean6)
    {
        this.bean6 = bean6;
    }
}
