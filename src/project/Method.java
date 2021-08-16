package project;

import java.util.LinkedList;

public class Method extends Part {
    private LinkedList<Parameter> parameters = new LinkedList();
    private String return_type;
    Method(){
        super();
    }

    Method(String type){
        this(type,"");
    }

    Method(String type,String name){
        super(type,name);
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
    public LinkedList<Parameter> getAllParams(){
        return this.parameters;
    }

    public int numberOfParameters(){
        return this.parameters.size();
    }

    void setReturnType(String return_type){
        this.return_type = return_type;
    }

    String getReturnType(){
        return this.return_type;
    }

}


