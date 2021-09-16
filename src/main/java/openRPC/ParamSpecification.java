package openRPC;

/**
 * ParamSpecification contains attributes of a parameter belonging to an API operation.
 */
public class ParamSpecification {

    private String name;
    private String path;
    private boolean required;

    /**
     * ParamSpecification constructor.
     * @param name the name of the parameter
     * @param path the path to the parameter in the OpenRPC specification
     * @param required boolean indicating whether the parameter is required for the corresponding method
     */
    public ParamSpecification(String name, String path, boolean required) {
        this.name = name;
        this.path = path;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isRequired() {
        return required;
    }
}
