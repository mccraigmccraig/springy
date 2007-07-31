package springy.beans;

public class Bean5 implements IBean,MyInterface {
   
    public int add(int a, int b) {
        return a+b;
    }

    public void myInit() {}
    public void myDestroy() {}
}
