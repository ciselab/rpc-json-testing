package openRPC;

/**
 * ParamSpecification contains attributes of a parameter belonging to an API operation.
 */
public class ResultSpecification {

    private String name;
    private String path;

    /**
     * ParamSpecification constructor.
     * @param name the name of the parameter
     * @param path the path to the parameter in the OpenRPC specification
     */
    public ResultSpecification(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }
}
