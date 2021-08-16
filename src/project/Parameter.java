package project;

public class Parameter extends Part {
    private String data_type;
    Parameter(String type){
        super(type);
    }
    Parameter(String name, String data_type){
        super("parameter", name);
        this.data_type = data_type;
    }
    void setDataType(String data_type){
        this.data_type = data_type;
    }

    String getDataType(){
        return this.data_type;
    }
}
