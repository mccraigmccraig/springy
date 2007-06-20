package springy.beans;

import java.util.Map;
import java.util.List;

public class Bean4 implements IBean {
    private String name;
    private Map map;
    private IBean anotherBean;
    private List list;

    public Bean4(String name, Map map) {
        this.name = name;
        this.map = map;
    }

    public Bean4(String name, Map map, List list) {
        this(name, map);
        this.list = list;
    }

    public String getName() {
        return name;
    }

    public Map getMap() {
        return map;
    }

    public void setAnotherBean(IBean bean)
    {
        this.anotherBean = bean;
    }

    public IBean getAnotherBean() {
        return anotherBean;
    }


    public List getList() {
        return list;
    }
}
