package springy.beans;

/**
 * Bean7
 */
public class Bean7
{
    public static int disposerCount;


    public static void disposerMethod() {
        disposerCount++;
    }

    public static int getDisposerCount() {
        return disposerCount;
    }
}
