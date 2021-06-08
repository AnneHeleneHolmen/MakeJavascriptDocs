package project;

/**
 * Input-parameter to a Method
 */
public class Parameter {
    private String name;
    private String type;
    private String description;

    /**
     * Gets the name of the parameter
     * @return String
     */
    String getName(){
        return this.name;
    }

    /**
     * Sets the name of the parameter
     * @param name String
     */
    void setName(String name){
        this.name = name;
    }

    /**
     * Gets the type of the parameter
     * @return String
     */
    String getType(){
        return this.type;
    }

    /**
     * Sets the type of the parameter
     * @param type String
     */
    void setType(String type){
        this.type = type;
    }

    /**
     * Gets the description of the parameter
     * @return String
     */
    String getDescription(){
        return this.description;
    }

    /**
     * Sets the description of the parameter
     * @param description String
     */
    void setDescription(String description){
        this.description = description;
    }
}
