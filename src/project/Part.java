package project;

/**
 * Input-parameter to a Method
 */
public class Part {
    private String name="";
    private String type="";
    private String description="";

    Part(){
       this("","","");
    }

    Part(String type){
        this(type,"","");
    }


    Part(String type, String name){
        this(type,name,"");
    }

    Part(String type, String name, String description){
        this.name = name;
        this.type = type;
        this.description = description;
    }


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
     * @param type String   Must be either of this : constructor, method, param
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
        description = description.replace("<","&#60;");
        description = description.replace(">","&#62;");
        this.description = description;
    }
}
