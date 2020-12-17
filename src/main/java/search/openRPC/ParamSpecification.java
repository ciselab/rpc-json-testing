package search.openRPC;

public class ParamSpecification {

    private String name;
    private String path;
    private boolean required;

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
