package springy.beans;

public class Bean3 implements IBean {
    private Bean2 bean2;
    private String name;
    private Object[] parameters;

    public Bean3(String name, Bean2 bean2) {
        this.name = name;
        this.bean2 = bean2;
    }

    public String getName() {
        return name;
    }

    public Bean2 getBean2() {
        return bean2;
    }

    public Object[] getParameters() {
        return this.parameters;
    }

    public void setParameters(Object[] args) {
        this.parameters = args;
    }
}
