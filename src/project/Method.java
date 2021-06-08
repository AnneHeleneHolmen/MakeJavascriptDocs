package project;
import java.util.LinkedList;

public class Method {
    private String type;
    private String name;
    private String description;
    private Parameter return_information = new Parameter();
    private Parameter param;
    private LinkedList<Parameter> parameters = new LinkedList();


    /**
     * Gets the method
     * @return project.Method
     */
    public Method getMethod(String type){
        if(this.type.equalsIgnoreCase(type))
           return this;
        else
            return null;
    }

    /**
     * Gets the type of the method
     * @return {@link java.lang.String}
     */
    public String getType(){
        return this.type;
    }

    /**
     * Sets the type of the method
     * @param type {@link java.lang.String} Valid values are method, script, constructor, author
     */
    public void setType(String type){
        if(type.equalsIgnoreCase("method")
                || type.equalsIgnoreCase("script")
                || type.equalsIgnoreCase("constructor")
                || type.equalsIgnoreCase("author"))
           this.type= type;
        else
            System.out.println("Wrong type ! Valid ones are method, script, constructor (case-insensitive)");
    }

    /**
     * Gets the name of the method
     * @return {@link java.lang.String}
     */
    public String getName(){
        return this.name;
    }

    /**
     * Sets the name of the method
     * @param {@link java.lang.String}
     */
    public void setName(String name){
        this.name= name;
    }

    /**
     * Gets the description of the method
     * @return {@link java.lang.String}
     */
    public String getDescription(){
        return this.description;
    }

    /**
     * Sets the description of the method
     * @param {@link java.lang.String}
     */
    public void setDescription(String description){
        this.description = description;
    }


    /**
     * Gets the type of the return-value of the method
     * @return {@link java.lang.String}
     */
    public String getReturnType(){
        return this.return_information.getType();
    }

    /**
     * Gets the description of the return-value of the method
     * @return {@link java.lang.String}
     */
    public String getReturnDescription(){
        return this.return_information.getDescription();
    }

    /**
     * Sets the type of the return-value of the method
     * @param type {@link java.lang.String}
     */
    public void setReturnType(String type){
        this.return_information.setType(type);
    }

    /**
     * Sets the description of the return-value of the method
     * @param description {@link java.lang.String}
     */
    public void setReturnDescription(String description){
        this.return_information.setDescription(description);
    }


    /**
     * Get an input parameter
     * @param i
     * @return project.Parameter
     */
    public Parameter getParam(int i){
        return (Parameter)this.parameters.get(i);
    }

    /**
     * Set an input parameter
     * @param param Parameter
     */
    public void setParam(Parameter param){
        this.parameters.add(param);
    }

    /**
     * All input parameters are returned
     * @return LinkedList
     */
    public LinkedList getAllParams(){
        return this.parameters;
    }

}
