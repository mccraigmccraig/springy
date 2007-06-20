package springy.context;

/**
 * Allows beans to be aware of the springy context. A bean does not need to implement
 * this interface, if a method with the same signature exists it will be called anyway.
 */
public interface SpringyAware {
    /**
     * Sets a springy context on an object.
     * Beans do not need to implement this.
     * @param context
     */
    public void setSpringyContext(Object context);
}
