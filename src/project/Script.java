package project;
/*  Copyright   Anne Helene Holmen  2021    */

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class Script {
    private Folder parent_folder;
    private String absolute_source_path;
    private String absolute_destination_path;
    private String path_from_destination;
    private String path_from_folder;
    private String backward_path_to_destination;
    private String backward_path_to_folder;
    private File script_file;                // The script where the documentation will be collected from the comments.
    private File documentation_file;         // The file that will contain the documentation for the script.
    private File template_file;              // The template for the documentation-file.
    private LinkedList<Part> methods;         // The documentation from the comments is stored in Method-objects, and contained in this list.
    private Documentation documentation_object;

    /**
     * @param absolute_source_path         {@link java.lang.String}  Absolute path to the folder where the script_file is located
     * @param absolute_destination_path    {@link java.lang.String} Absolute path to the destination-folder where the documentation_file is located
     * @param path_from_destination        {@link java.lang.String}
     * @param backward_path_to_destination {@link java.lang.String}
     */
    public Script(Folder parent_folder, String absolute_source_path, String absolute_destination_path, String path_from_destination, String backward_path_to_destination) {
        this.parent_folder = parent_folder;
        this.absolute_source_path = absolute_source_path;
        this.absolute_destination_path = absolute_destination_path;
        this.path_from_destination = path_from_destination;
        this.path_from_folder = path_from_destination.substring(path_from_destination.lastIndexOf("/") + 1);
        if (path_from_destination.lastIndexOf("/") == 0) this.path_from_folder = "";
        this.backward_path_to_destination = backward_path_to_destination;
        this.backward_path_to_folder = backward_path_to_destination.substring(backward_path_to_destination.indexOf("/") + 1);
        this.script_file = new File(absolute_source_path);
        this.template_file = new File(parent_folder.getAbsolutePath() + "/class.html");
        this.documentation_file = new File(absolute_destination_path);
        this.documentation_object = this.parent_folder.getParent();
        makeMethodTree();
        if (methods.size() > 0)
            makeScriptDocumentationFile();
        documentation_object.addPathToAllFiles(path_from_destination + "/" + documentation_file.getName());
        parent_folder.addPathToAllFolderFilepaths(path_from_folder + "/" + documentation_file.getName());

    }

    String getScriptName() {
        return script_file.getName();
    }

    String getScriptPath() {
        return path_from_folder;
    }

    Folder getFolder() {
        return parent_folder;
    }

    File getDocumentationFile() {
        return documentation_file;
    }

    /**
     * Script-loop:
     * <p>
     * We must loop through the script to find the documentation sections.
     * When we are inside one, collect the information into an object of type Method.
     * The delimiter between the fields are the tabulator.
     * Store all the method-objects in the linked list methods
     * The method objects will be used to scripts(class), constructors and author
     * The object has a type-field, that should be set to the right type
     * Leave the loop when the end of a section is found.
     */
    boolean makeMethodTree() {
        BufferedReader script_reader = null;
        boolean documentation_mode = false;
        String line_script;
        String[] line_parts = null;
        Method script = null;
        Method constructor = null;
        Method method = null;
        Parameter param;
        Parameter return_parameter;
        try {
            methods = new LinkedList<>();
            script_reader = new BufferedReader(new FileReader(script_file));

            line_script = script_reader.readLine();
            while (line_script != null) {
                line_script = line_script.trim();
                if (line_script.startsWith("/**")) {
                    documentation_mode = true;
                }
                if (documentation_mode && line_script.startsWith("*\t")) {
                    // Her må man sjekke på alt som starter med @
                    // Plukk ut og skriv til utfilen med riktig html-kode. Se package/class-template.html
                    if (line_script.contains("\t"))
                        line_parts = line_script.split("\t");

                    for (int i = 0; i < line_parts.length; i++)
                        line_parts[i] = line_parts[i].trim();

                    switch (line_parts[1]) {
                        case "@class":
                            String class_description = "";
                            script = new Method();
                            script.setType("script");
                            methods.add(script);

                            if (line_parts.length > 2)
                                script.setName(line_parts[2]);
                            if (line_parts.length > 3)
                                for (int i = 3; i < line_parts.length; i++)
                                    class_description = class_description + " " + line_parts[i];

                            script.setDescription(class_description);

                            break;
                        case "@constructor":
                            String constructor_description = "";
                            constructor = new Method();
                            constructor.setType("constructor");
                            constructor.setName(methods.get(0).getName());
                            methods.add(constructor);
                            if (line_parts.length > 2) {
                                for (int i = 2; i < line_parts.length; i++)
                                    constructor_description = constructor_description + " " + line_parts[i];

                                constructor.setDescription(constructor_description);
                            }

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

                            break;
                        case "@param":
                            String parameter_description = "";
                            param = new Parameter("parameter");
                            if (line_parts.length > 2)
                                param.setName(line_parts[2]);

                            if (line_parts.length > 3)
                                param.setDataType(line_parts[3]);

                            if (line_parts.length > 4) {
                                for (int i = 4; i < line_parts.length; i++)
                                    parameter_description = parameter_description + " " + line_parts[i];
                                param.setDescription(parameter_description);
                            }

                            method = (Method) methods.get(methods.size() - 1);
                            method.setParam(param);
                            break;
                        case "@returns":
                            method = (Method) methods.get(methods.size() - 1);

                            if (line_parts.length > 2)
                                method.setReturnType(line_parts[2]);

                            break;
                        /*case "@author":
                            author = new Method();
                            author.setType("author");
                            methods.add(author);
                            if (line_parts.length > 2) {
                                for (int i = 2; i < line_parts.length; i++)
                                    author_description = author_description + " " + line_parts[i];

                                author.setDescription(author_description);
                            }
                            break;*/

                        default:
                            String description = line_parts[1];

                            // If the last method has parameter(s), then the description must be added ti the last parameters description
                            // Else the description mustbe added to the methods description
                            method = (Method) methods.get(methods.size() - 1);
                            if (method.numberOfParameters() > 0) {
                                param = method.getParam(method.numberOfParameters() - 1);
                                param.setDescription(param.getDescription() + description);
                            } else
                                method.setDescription(method.getDescription() + description);

                    }
                }
                if (documentation_mode && line_script.startsWith("*/")) {
                    documentation_mode = false;
                    // The param information is stored in the methods LinkedList when the section is completed.

                }

                line_script = script_reader.readLine();
            }
            script_reader.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
            return false;
        } finally {
            try {
                script_reader.close();
            } catch (IOException ioex) {
                System.out.println(ioex.getMessage() + "\n" + ioex.getStackTrace());
            }
        }
        return true;
    }

    boolean makeScriptDocumentationFile() {
        byte[] buffer;
        String return_value = "";
        Parameter return_parameter = null;
        FileWriter document_writer = null;
        BufferedReader template_reader = null;
        String line_template;
        Method method = null;
        String author_description = "";
        String signature = "";
        LinkedList<Parameter> constructor_parameters = null;

        /* Prepare replacement of variables in the template by setting some variables. */
        String class_name = "";
        String class_description = "";
        String constructor_name = "";
        List<Parameter> method_params;

        try {
            template_reader = new BufferedReader(new FileReader(template_file));
            document_writer = new FileWriter(documentation_file);
            for (int j = 0; j < methods.size(); j++) {
                method = (Method) methods.get(j);
                if (method.getType().equalsIgnoreCase("author")) {
                    author_description = method.getDescription();
                }
                if (method.getType().equalsIgnoreCase("script")) {
                    class_name = method.getName();
                    class_description = method.getDescription();
                }
                if (method.getType().equalsIgnoreCase("constructor")) {
                    constructor_name = method.getName();
                    constructor_parameters = method.getAllParams();
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

                if (line_template.contains("{PROJECT_NAME}"))
                    line_template = line_template.replace("{PROJECT_NAME}", parent_folder.getProjectName());

                if (line_template.contains("{DOCUMENTATION_ROOT_PATH}"))
                    line_template = line_template.replace("{JSDOC_PATH}", path_from_destination);

                if (line_template.contains("{FOLDER_PATH}"))
                    line_template = line_template.replace("{FOLDER_PATH}", getFolder().getFolderName());

                if (line_template.contains("{DATE_CREATED}"))
                    line_template = line_template.replace("{DATE_CREATED}", parent_folder.getToday());

                if (line_template.contains("{FOLDER_NAME}"))
                    line_template = line_template.replace("{FOLDER_NAME}", getFolder().getFolderName());

                if (line_template.contains("{SCRIPT_PATH}")) {
                    line_template = line_template.replace("{SCRIPT_PATH}", getScriptPath());
                }

                if (line_template.contains("<h4>{SCRIPT_NAME}</h4>")) {
                    // replace and write to out_file
                    line_template = line_template.replace("{SCRIPT_NAME}", class_name);
                    document_writer.write(line_template + "\n");


                    if (!signature.equals(""))
                        line_template = "<pre>" + class_name + signature + "</pre>";
                }

                if (line_template.contains("{SCRIPT_NAME}"))
                    line_template = line_template.replace("{SCRIPT_NAME}", class_name);

                if (line_template.contains("{CLASS_DESCRIPTION}"))
                    line_template = line_template.replace("{CLASS_DESCRIPTION}", class_description);

                if (line_template.contains("{AUTHOR_DESCRIPTION}"))
                    line_template = line_template.replace("{AUTHOR_DESCRIPTION}", author_description);


                // From here we build a section with constructor descriptions
                if (line_template.contains("<!-- Constructors insert -->")) {
                    document_writer.write(line_template + "\n");

                    signature = "(";
                    Parameter cp;
                    for (int i = 0; i < constructor_parameters.size(); i++) {
                        cp = constructor_parameters.get(i);
                        signature = signature + cp.getType() + " " + cp.getName() + ",";
                    }
                    signature = signature.substring(0, signature.length() - 1) + ")";
                    line_template = "<tr class=\"altColor\"><td class=\"colOne\"><code><span class=\"memberNameLink\"><a href=\"../" + getScriptPath() + "/" + class_name + ".html#" + class_name + "\">" + class_name
                            + "</a></span>" + signature + "</code>&nbsp;</td></tr>";


                }

                if (line_template.contains("<!-- Method insert -->")) {
                    String[] color = {"rowColor", "altColor"};
                    int counter = 0;
                    String parameters = "";
                    String method_description = "";

                    for (int k = 0; k < methods.size(); k++) {
                        method = (Method) methods.get(k);
                        if (method.getType().equalsIgnoreCase("method")) {
                            line_template = "<tr id=\"i" + counter + "\" class=\"" + color[counter % 2] + "\" >";

                            document_writer.write(line_template + "\n");

                            return_value = method.getReturnType();
                            if (return_value == null) return_value = "void";
                            line_template = "<td class=\"colFirst\" style=\"width:auto\" ><code>" + return_value + "</code></td>";
                            document_writer.write(line_template + "\n");

                            LinkedList params = method.getAllParams();
                            for (int l = 0; l < params.size(); l++)
                                parameters = parameters + ((Parameter) params.get(l)).getName() + ",";

                            if (parameters.length() > 1)
                                parameters = parameters.substring(0, parameters.length() - 1);

                            line_template = "<td class=\"colLast\" ><code><span class=\"memberNameLink\"><a href=\"../" +
                                    getScriptPath() + "/" + class_name + ".html#" + method.getName() + "\">" + method.getName() + "</a></span>(" +
                                    parameters + ")</code>";
                            parameters = "";

                            document_writer.write(line_template + "\n");

                            method_description = method.getDescription();
                            if (method_description != null)
                                line_template = "<div class=\"block\">" + method_description + "</div></td></tr>";

                            if (k < methods.size() - 1)
                                document_writer.write(line_template + "\n");

                            counter++;
                        }
                    }
                }

                // From here we build a section with constructor descriptions
                if (line_template.contains("<! -- Insert constructor details here -->"))
                    line_template = buildDetailSection(line_template, document_writer, "constructor");


                // From here we build a section with method descriptions
                if (line_template.contains("<h3>Method Detail</h3>"))
                    line_template = buildDetailSection(line_template, document_writer, "method");

                if (!line_template.equals(""))
                    document_writer.write(line_template + "\n");

                line_template = template_reader.readLine();

            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
            return false;
        } finally {
            try {
                template_reader.close();
                document_writer.close();
            } catch (Exception ex) {
                System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
                return false;
            }
        }

        return true;

    }


    String buildDetailSection(String line_template, FileWriter document_writer, String type) throws IOException {
        Method method;
        List<Parameter> method_params;
        String return_value = "";

        // From here we build a section with method descriptions
        document_writer.write(line_template + "\n");

        // Loop through the method objects. Treat only the ones with the method type
        for (int k = 0; k < methods.size(); k++) {
            method = (Method) methods.get(k);

            if (method.getType().equalsIgnoreCase(type)) {
                line_template = "<a name=\"" + method.getName() + "\">\n <!--   -->\n</a>";
                document_writer.write(line_template + "\n");
                line_template = "<ul class=\"blockList\">\n<li class=\"blockList\">";
                document_writer.write(line_template + "\n");
                line_template = "<h4>" + method.getName() + "</h4>";
                document_writer.write(line_template + "\n");

                method_params = method.getAllParams();
                String parameter_signature = "";
                Parameter method_param;
                for (int l = 0; l < method_params.size(); l++) {
                    method_param = method_params.get(l);
                    parameter_signature = parameter_signature + method_param.getName();
                    if (l < method_params.size() - 1)
                        parameter_signature = parameter_signature + ",";
                }

                return_value = method.getReturnType();
                if (type.equals("method") && return_value == null) return_value = "void";
                if (type.equals("constructor") && return_value == null) return_value = "";
                line_template = "<pre>";

                if (!return_value.equals(""))
                    line_template = line_template + return_value + "&nbsp;";

                line_template = line_template + method.getName() + "(" + parameter_signature + ")" + "</pre>";

                document_writer.write(line_template + "\n");


                if (method.getDescription() != null) {
                    line_template = "<div class=\"block\"><p>" + method.getDescription() + "</p></div>";
                    document_writer.write(line_template + "\n");
                }
                line_template = "<dl>";
                document_writer.write(line_template + "\n");
                if (method_params.size() > 0) {
                    line_template = "<dt><span class=\"paramLabel\">Parameters:</span></dt>";
                    document_writer.write(line_template + "\n");

                    for (int l = 0; l < method_params.size(); l++) {
                        method_param = method_params.get(l);
                        line_template = "<dd><code>" + method_param.getName() + "</code>&nbsp;" + method_param.getType() + "&nbsp;" + method_param.getDescription() + "</dd>";
                        document_writer.write(line_template + "\n");
                    }
                }

                if (!return_value.equals("")) {
                    line_template = "<dt><span class=\"returnLabel\">Returns:</span></dt>";
                    document_writer.write(line_template + "\n");

                    line_template = "<dd>" + return_value  + "</dd>";
                    document_writer.write(line_template + "\n");
                }

                line_template = "</dl></li></ul>";
                document_writer.write(line_template + "\n");
            }
        }


        return "";
    }


    boolean replacePrevAndNextVariables(Script prev_script, Script next_script) {
        BufferedReader document_reader;
        BufferedWriter document_writer;
        String line;
        try {
            document_reader = new BufferedReader(new FileReader(documentation_file));
            document_writer = new BufferedWriter(new FileWriter(documentation_file));
            line = document_reader.readLine();
            while (line != null) {

                if (line.contains("Insert prev and next link")) {
                    if (prev_script != null)
                        line = "<li><a href=\"" + prev_script.getScriptPath() + "\" title=\"class in " + prev_script.getFolder().getFolderName() + "\"><span class=\"typeNameLink\">Prev&nbsp;Class</span></a></li>";

                    if (next_script != null)
                        line = "<li><a href=\"" + next_script.getScriptPath() + "\" title=\"class in " + prev_script.getFolder().getFolderName() + "\"><span class=\"typeNameLink\">Next&nbsp;Class</span></a></li>";

                }
                document_writer.write(line);
                line = document_reader.readLine();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
            return false;
        }

        return true;
    }
}
