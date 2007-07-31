package springy.beans;

public class Bean2 implements IBean {

    private String name1,name2;
    private int number;

    public Bean2(String name1, String name2, int number) {

        this.name1 = name1;
        this.name2 = name2;
        this.number = number;
    }

    public String toString() {
        return getClass().getSimpleName();
    }

    public String getName1() {
        return name1;
    }


    public String getName2() {
        return name2;
    }

    public int getNumber() {
        return number;
    }
}
