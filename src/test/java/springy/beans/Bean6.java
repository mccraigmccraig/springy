package springy.beans;

/**
 * Bean6
 */
public class Bean6
    implements IBean
{
    private int count;


    public int getCount()
    {
        return count;
    }

    public int incrCount()
    {
        return count++;
    }
}
