package mariam.darbinyan.login;

public class CategoryModel {
    private String key;
    private String name;


    public CategoryModel() {}

    public CategoryModel(String key, String name) {
        this.key = key;
        this.name = name;
    }

    public String getKey() { return key; }
    public String getName() { return name; }
}