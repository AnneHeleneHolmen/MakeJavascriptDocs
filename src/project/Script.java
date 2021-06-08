package project;
import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Script {
    private Folder parent_folder;
    private String absolute_source_path;
    private String absolute_destination_path;
    private String script_name;
    private String path_from_destination;
    private String path_from_folder;
    private String backward_path_to_destination;
    private String backward_path_to_folder;
    private File script_file;                // The script where the documentation will be collected from the comments.
    private File documentation_file;                   // The file that will contain the documentation for the script.
    private File template_file;                  // The template for the documentation-file.
    private LinkedList methods;             // The documentation from the comments is stored in Method-objects, and contained in this list.

    /**
     *
     * @param absolute_source_path {@link java.lang.String}  Absolute path to the folder where the script_file is located
     * @param absolute_destination_path {@link java.lang.String} Absolute path to the destination-folder where the documentation_file is located
     * @param script_name {@link java.lang.String}
     * @param path_from_destination {@link java.lang.String}
     * @param backward_path_to_destination {@link java.lang.String}
     */
    public Script(Folder parent_folder, String absolute_source_path, String absolute_destination_path, String script_name, String path_from_destination, String backward_path_to_destination){
        this.parent_folder = parent_folder;
        this.absolute_source_path = absolute_source_path;
        this.absolute_destination_path = absolute_destination_path;
        this.script_name = script_name;
        this.path_from_destination = path_from_destination;
        this.path_from_folder = path_from_destination.substring(path_from_destination.indexOf("/")+1);
        this.backward_path_to_destination = backward_path_to_destination;
        this.backward_path_to_folder = backward_path_to_destination.substring(backward_path_to_destination.indexOf("/")+1);
        this.script_file = new File(absolute_source_path + "/" + script_name);
        this.template_file = new File(parent_folder.getAbsolutePath() + "/class-edited.html");
    }

    String getScriptName(){
        return script_name;
    }

    String getScriptPath(){
        return path_from_folder + "/" + script_name;
    }

    /**
     *   Script-loop:
     *
     *   We must loop through the script to find the documentation sections.
     *   When we are inside one, collect the information into an object of type Method.
     *   Store all the method-objects in the linked list methods
     *   The method objects will be used to scripts(class), constructors and author
     *   The object has a type-field, that should be set to the right type
     *   Leave the loop when the end of a section is found.
     */
    boolean makeMethodTree() {
        BufferedReader script_reader = null;
        boolean documentation_mode = false;
        String line_script;
        String[] line_parts;
        Method author;
        Method script = null;
        Method constructor = null;
        Method method = null;
        int last_variable = 0;
        String description = "";
        Parameter param;
        String author_description = "";
        try {

            script_reader = new BufferedReader(new FileReader(script_file));

            line_script = script_reader.readLine();
            while (line_script != null) {
                line_script = line_script.trim();
                if (line_script.startsWith("/**")) {
                    documentation_mode = true;
                }
                if (documentation_mode && line_script.startsWith("* ")) {
                    // Her må man sjekke på alt som starter med @
                    // Plukk ut og skriv til utfilen med riktig html-kode. Se package/class-template.html
                    line_parts = line_script.split(" ");
                    switch (line_parts[1]) {
                        case "@class":
                            String class_description = "";
                            script = new Method();
                            script.setType("script");
                            methods.add(script);

                            if (line_parts.length > 2)
                                script.setName(line_parts[2]);
                            if (line_parts.length > 3) {
                                for (int i = 3; i < line_parts.length; i++)
                                    class_description = class_description + " " + line_parts[i];

                                script.setDescription(class_description);
                            }
                            last_variable = 1;
                            break;
                        case "@constructor":
                            String constructor_description = "";
                            constructor = new Method();
                            constructor.setType("constructor");
                            methods.add(constructor);
                            if (line_parts.length > 2) {
                                for (int i = 2; i < line_parts.length; i++)
                                    constructor_description = constructor_description + " " + line_parts[i];

                                constructor.setDescription(constructor_description);
                            }

                            last_variable = 2;
                            break;
                        case "@method":
                            String method_description = "";
                            method = new Method();
                            method.setType("method");
                            methods.add(method);
                            if (line_parts.length > 2)
                                method.setName(line_parts[2]);
                            if (line_parts.length > 3) {
                                for (int i = 3; i < line_parts.length; i++)
                                    method_description = method_description + " " + line_parts[i];

                                method.setDescription(method_description);
                            }

                            last_variable = 3;
                            break;
                        case "@param":
                            String parameter_description = "";
                            param = new Parameter();
                            if (line_parts.length > 2)
                                param.setName(line_parts[2]);

                            if (line_parts.length > 3)
                                param.setType(line_parts[3]);

                            if (line_parts.length > 4) {
                                for (int i = 4; i < line_parts.length; i++)
                                    parameter_description = parameter_description + " " + line_parts[i];
                                param.setDescription(parameter_description);
                            }
                            if (last_variable == 2)
                                constructor.setParam(param);
                            else
                                method.setParam(param);

                            break;
                        case "@returns":
                            String return_description = "";
                            if (line_parts.length > 2)
                                method.setReturnType(line_parts[2]);
                            if (line_parts.length > 3) {
                                for (int i = 3; i < line_parts.length; i++)
                                    return_description = return_description + " " + line_parts[i];
                                method.setReturnDescription(return_description);
                            }

                            break;
                        case "@author":
                            author = new Method();
                            author.setType("author");
                            methods.add(author);
                            if (line_parts.length > 2) {
                                for (int i = 2; i < line_parts.length; i++)
                                    author_description = author_description + " " + line_parts[i];

                                author.setDescription(author_description);
                            }
                            last_variable = 6;
                            break;

                        default:
                            for (int i = 1; i < line_parts.length; i++)
                                description = description + line_parts[i];

                                /*
                                    The descriptions could span several lines. That is why we check the methods LinkedList,
                                    to include what is already stored in the variable.
                                */
                            switch (last_variable) {
                                case 1: // 1 = class (script)
                                    if (script != null)
                                        script.setDescription(script.getDescription() + description);
                                    break;
                                case 2: // 2 = constructor
                                    if (constructor != null)
                                        constructor.setDescription(constructor.getDescription() + description);
                                    break;
                                case 3: // 3 = method
                                    if (method != null)
                                        method.setDescription(method.getDescription() + description);
                                    break;
                                case 5: // 5 = return
                                    if (method != null)
                                        method.setReturnDescription(method.getReturnDescription() + description);
                                    break;
                                default:
                                    System.out.println("You are not supposed to be here : ");
                            }

                    }
                }
                if (documentation_mode && line_script.startsWith("*/")) {
                    documentation_mode = false;
                    // The param information is stored in the methods LinkedList when the section is completed.

                }

                line_script = script_reader.readLine();
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
            return false;
        } finally {
            try {
                script_reader.close();
            }catch(IOException ioex){
                System.out.println(ioex.getMessage() + "\n" + ioex.getStackTrace());
            }
        }
        return true;
    }

    boolean makeScriptDocumentationFile(){
        byte[] buffer;
        FileOutputStream document_output_stream=null;
        FileWriter document_writer=null;
        BufferedReader script_reader=null;
        BufferedReader template_reader=null;
        boolean documentation_mode = false;
        String  line_script;
        String  line_template;
        String[] line_parts;
        boolean result = false;
        Method author = null;
        Method script = null;
        Method constructor = null;
        Method method = null;
        int last_variable=0;
        String description="";
        List<Method> methods = new LinkedList<>();
        Parameter param = new Parameter();
        String author_description="";
        String package_name="";
        String signature="";
        LinkedList<Parameter> constructor_parameters=null;
        /*
                    Prepare replacement of variables in the template by setting some variables.
                */
        String class_name = "";
        String class_description = "";
        String constructor_name ="";
        List<Parameter> method_params;
        List<Parameter> constructor_params;

        try {
        for(int j=0;j < methods.size();j++){
            method = methods.get(j);
            if(method.getType().equalsIgnoreCase("author")) {
                author_description = method.getDescription();
            }
            if(method.getType().equalsIgnoreCase("script")) {
                class_name = method.getName();
                class_description = method.getDescription();
            }
            if(method.getType().equalsIgnoreCase("constructor")) {
                constructor_name = method.getName();
                constructor_params = method.getAllParams();
            }

        }

       /*
            Template-loop:
            We must loop through the template-file, to find the variables {variable_name}, that we must replace by the content of the
            variables found in the script-loop. They are stored in the linkedlist methods, and are of type Method (defined in this project)
         */
        line_template = template_reader.readLine();
        while (line_template != null) {
            line_template = line_template.trim();

            if(line_template.contains("{PROJECT_NAME}"))
                line_template = line_template.replace("{PROJECT_NAME}", project_name);

            if(line_template.contains("{JSDOC_PATH}"))
                line_template = line_template.replace("{JSDOC_PATH}", path_from_destination);

            if(line_template.contains("{PACKAGE_PATH}"))
                line_template = line_template.replace("{FOLDER_PATH}", path_from_folder);

            if(line_template.contains("{CREATED_DATE}"))
                line_template = line_template.replace("{DATE_CREATED}", today.toString());

            if(line_template.contains("{PACKAGE_NAME}"))
                line_template = line_template.replace("{PACKAGE_NAME}", package_name);

            if(line_template.contains("{SCRIPT_NAME}"))
                line_template = line_template.replace("{SCRIPT_NAME}", class_name);

            if(line_template.contains("{CLASS_DESCRIPTION}"))
                line_template = line_template.replace("{CLASS_DESCRIPTION}", class_description);

            if(line_template.contains("{AUTHOR_DESCRIPTION}"))
                line_template = line_template.replace("{AUTHOR_DESCRIPTION}", author_description);

            if(line_template.contains("<h4>{SCRIPT_NAME}</h4>")) {
                // replace and write to out_file
                line_template = line_template.replace("{SCRIPT_NAME}",class_name );
                document_writer.write(line_template + "\n");

                // Build next line
                signature = constructor_name + "(" ;
                Parameter cp;
                for (int i=0; i < constructor_parameters.size(); i++) {
                    cp = constructor_parameters.get(i);
                    signature = signature + cp.getType() + " " + cp.getName() + ",";

                    signature = signature.substring(0, signature.length() - 2) + ")";
                    line_template = "<pre>" + signature + "</pre>";
                }
            }

            // From here we build a section with constructor descriptions
            if(line_template.contains("<!-- Constructors insert -->")) {
                document_writer.write(line_template + "\n");
                String parameters ="";
                for(int k=0;k < methods.size();k++){
                    method = methods.get(k);

                    if(method.getType().equalsIgnoreCase("constructor")) {
                        LinkedList params = method.getAllParams();
                        for(int i=0;i < params.size();i++)
                            parameters = parameters + ((Parameter)params.get(i)).getName() +  ",";

                        parameters = parameters.substring(parameters.length()-2);
                        line_template = "<tr class=\"altColor\"><td class=\"colOne\"><code><span class=\"memberNameLink\"><a href=\"../" + package_name +"/" + class_name + ".html#" + class_name + "--\">" + class_name
                                + "</a></span>(" + parameters + ")</code>&nbsp;</td></tr>";
                        break;
                    }

                }
            }

            if(line_template.contains("Insert methods")) {
                String[] color = {"rowColor","altColor"};
                int counter=0;
                String parameters = "";
                LinkedList params = method.getAllParams();
                for(int l=0; l < params.size();l++)
                    parameters = parameters + ((Parameter)params.get(l)).getName() + ",";

                parameters = parameters.substring(parameters.length() - 2);

                for(int k=0;k < methods.size();k++) {
                    if(method.getType().equalsIgnoreCase("method")) {
                        line_template = "<tr id=\"i" + counter + " class=\"" + color[counter % 2] + "\">";

                        document_writer.write(line_template + "\n");
                        line_template = "<td class=\"colLast\"><code><span class=\"memberNameLink\"><a href=\"../" +
                                package_name +"/" + class_name + ".html#" + method.getName() + "\">" + method.getName() + "</a></span>(" +
                                parameters + ")</code>";

                        document_writer.write(line_template + "\n");

                        line_template = "<div class=\"block\">" + method.getDescription() + "</div></td></tr>";

                        document_writer.write(line_template + "\n");
                        counter ++;
                    }
                }
            }

            // From here we build a section with method descriptions
            if(line_template.contains("<h3>Method Detail</h3>")) {
                document_writer.write(line_template + "\n");

                // Loop through the method objects. Treat only the ones with the method type
                for(int k=0;k < methods.size();k++){
                    method = methods.get(k);

                    if(method.getType().equalsIgnoreCase("method")) {
                        line_template = "<a name=\"" + method.getName() + "\">\n <!--   -->\n</a>";
                        document_writer.write(line_template + "\n");
                        line_template = "<ul class=\"blockList\">\n<li class=\"blockList\">";
                        document_writer.write(line_template + "\n");
                        line_template = "<h4>" + method.getName() + "</h4>";
                        document_writer.write(line_template + "\n");

                        method_params = method.getAllParams();
                        String parameter_signature="";
                        Parameter method_param;
                        for(int l=0; l < method_params.size();l++) {
                            method_param = method_params.get(l);
                            parameter_signature = parameter_signature + method_param.getName();
                            if(l < method_params.size() - 1)
                                parameter_signature = parameter_signature + ",";
                        }

                        line_template = "<pre>" + method.getReturnType() + "&nbsp;" + method.getName() +"(" + parameter_signature + ")" + "</pre>";
                        document_writer.write(line_template + "\n");
                        line_template = "<div class=\"block\"><p>" + method.getDescription() + "</p></div>";
                        document_writer.write(line_template + "\n");
                        line_template = "<dl>";
                        document_writer.write(line_template + "\n");
                        line_template = "<dt><span class=\"paramLabel\">Parameters:</span></dt>";
                        document_writer.write(line_template + "\n");

                        for(int l=0; l < method_params.size();l++){
                            method_param = method_params.get(l);
                            line_template = "<dd><code>" + method_param.getName() + "</code>&nbsp;" + method_param.getType() + "&nbsp;" + method_param.getDescription() + "</dd>";
                            document_writer.write(line_template + "\n");
                        }

                        line_template = "<dt><span class=\"returnLabel\">Returns:</span></dt>";
                        document_writer.write(line_template + "\n");

                        line_template = "<dd>" + method.getReturnType() + "&nbsp;" + method.getReturnDescription()+ "</dd>";
                        document_writer.write(line_template + "\n");
                        line_template = "</dl></li></ul>";
                        document_writer.write(line_template + "\n");
                    }
                }

            }

            document_writer.write(line_template + "\n");
            line_template = template_reader.readLine();

        }
        template_reader.close();

        /*
         * Må lese inn et siste fra class-end.html og legge til class.html
         */
        template_reader = new BufferedReader(new FileReader(end_file));
        line_template = template_reader.readLine();
        while (line_template != null) {
            line_template = line_template.trim();

            if(line_template.contains("{PACKAGE_NAME}"))
                line_template = line_template.replace("{PACKAGE_NAME}", package_name);

            if(line_template.contains("{NEXT_SCRIPT_NAME}"))
                line_template = line_template.replace("{SCRIPT_NAME}", class_name);

            if(line_template.contains("{PREV_SCRIPT_NAME}"))
                line_template = line_template.replace("{SCRIPT_NAME}", class_name);

            line_template = template_reader.readLine();
            document_writer.write(line_template + "\n");
        }
        template_reader.close();
        document_writer.close();


    }catch(Exception ex) {
        System.out.println(ex.getMessage());
    }finally{
        try {
            template_reader.close();
            document_writer.close();
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }
        return true;
    }
}
