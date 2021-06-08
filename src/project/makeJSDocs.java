package project;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.time.LocalDate;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

/**
 * This class is making the documentation fro a foler with javascript-files. The information is taken from the sections with comments.
 * The connecion between methods is :
 *      main
 *          initializeFiles:  Copy the  files from the jar-archive that is beyond the /templates - directory to the destination-folder
 *                            Open a filewriter to the two files allclasses-frame-start.html and allclasses-noframe-start.html
 *
 *          readSourceFolder: Loop through the source-folder. Make a directory for each directory,
 *              makeHtmlFileForEachScript: Make a html-file for each script
 *                  appendLinkToFile: For each script.html file add a link in the package-files and one in the two files at the root
 *              completePackageFiles: Write the end of the package-files to the start, and rename the package-file. Delete the -start and -end  files
 */
public class makeJSDocs {
    private static String source_folder="";
    private static String destination_folder;
    private static String project_name;
    private static LocalDate today;
    private static String help_information = "\t-h Help information\n\t-d Absolute or relative path to destination\n\t -p Project-name \n\t -s Absolute or relative path to javascript-files";
    private static FileWriter allclasses_frame;
    private static FileWriter allclasses_noframe;
    private static FileInputStream class_start;
    private static FileInputStream class_end;
    private static FileWriter package_summary;
    private static FileWriter package_tree;
    private static FileWriter package_use;
    private static FileWriter package_frame;
    private static int iterations=0;
    /**
     * makeDocumentation startes fra den prosjektfolder hvor javascriptfilene ligger.
     * Hvis dette skal gjøres mer fleksibelt senere, må man ta inn parametere fra kommandolinjen.
     * @param args parametere fra kommandolinjen
     */
    public static void main(String[] args){

        switch(args.length){
            case 6:
                for(int i=0;i < args.length; i++) {
                    if (args[i].equals("-d")) {
                        destination_folder = args[i + 1];
                    }
                    if (args[i].equals("-p")) {
                        project_name = args[i + 1];
                    }
                    if (args[i].equals("-s")) {
                        source_folder = args[i + 1];
                    }
                    if(destination_folder != null && !destination_folder.startsWith("/"))
                        destination_folder = System.getProperty("user.dir") + destination_folder;

                    if(source_folder != null && !source_folder.startsWith("/"))
                        source_folder = System.getProperty("user.dir") + source_folder;

                    today = LocalDate.now();
                    if(destination_folder != null && source_folder != null) {
                        initializeFiles();
                        prepareFilesOnDestinationRoot();
                        readSourceFolder();
                    }

                }

                break;
            default:
                System.out.println(help_information);

        }


    }

    /**
     *  Copy files with the templates-path from the jar-archive to the destination folder
     */
    private static void initializeFiles() {
        String path_to_jar;
        JarFile jarFile;
        Enumeration<JarEntry> e;
        JarEntry entry;
        JarInputStream jarIS;
        InputStream input_stream;
        String file_name;
        String out_file_name="";
        FileOutputStream file_out_stream;
        int pos1=-1;
        int pos2=-1;
        try {
            path_to_jar = makeJSDocs.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            jarFile = new JarFile(path_to_jar);
            e = jarFile.entries();
            while (e.hasMoreElements()) {
                entry = (JarEntry)e.nextElement();
                file_name = entry.getName();
                input_stream = jarFile.getInputStream(entry);

                if(file_name.equals("templates/") || file_name.equals("templates/package/") ){
                    file_name = file_name.substring(file_name.indexOf("/")+1);
                    copyFile(input_stream, file_name, true);
                }

                if(file_name.contains(".html") || file_name.equals("templates/script.js") || file_name.equals("templates/stylesheet.css")){
                    file_name = file_name.substring(file_name.indexOf("/")+1);
                    copyFile(input_stream,file_name,false);
                }
            }

        }catch(IOException ioex){
            System.out.println(ioex.getMessage());
        }finally{
        }
    }

    /**
     *  There are four files on destination root: allclasses-frame-start.html ,allclasses-frame-end.html, allclasses-noframe-start.html, allclasses-noframe-end.html
     *  This files must be available for writing globally, so the allclasses_frame and allclasses_noframe are global file-writers.
     */
    private static void prepareFilesOnDestinationRoot(){
        String file_name="";
        try {
            // Open output streams to files at the root of the output-folder
            file_name = replaceVariablesByData(destination_folder + "/allclasses-frame-start.html", "");
            allclasses_frame = new FileWriter(destination_folder +  "/" + file_name, true);

            file_name = replaceVariablesByData(destination_folder + "/allclasses-noframe-start.html", "");
            allclasses_noframe = new FileWriter(destination_folder +  "/" + file_name, true);


        }catch(Exception ex){
            System.out.println(ex.getMessage() + " " + ex.getStackTrace());
        }

    }
    /**
     * Read directories and files from the source-folder. Create a directory in the destination-folder for each directory in the source.
     *      copyPackageFilesToPackage   Copy the package-files from the destination/package to the new directory
     *      makeHtmlFileForEachScript   Make a html-file for each script by copying the destination/package/class-start.html to the new directory
     *      completePackageFiles        The package-end.html files in the new directory, are appended to the package-start.html, and deleted.
     */
    private static void readSourceFolder(){
        FileOutputStream script_output;
        String script_name;
        File[] scripts;
        FileInputStream script_stream;
        File package_folder;
        File package_template_folder;
        File[] package_template_files;

        int pos1;
        try {
            File source_folder = new File(makeJSDocs.source_folder);
            File destination_folder = new File(makeJSDocs.destination_folder);
            File[] source_files = source_folder.listFiles();
            String package_name;
            String jsdoc_path="";
            for (int i = 0; i < source_files.length; i++)
                if (source_files[i].isDirectory()) {
                    jsdoc_path="../";
                    package_name = source_files[i].getName();
                    package_folder = new File(destination_folder.getPath() + "/" + package_name);
                    package_folder.mkdir();
                    // Copy all the files from the /package and rename from package to source_files[i].getName()
                    package_template_folder = new File(destination_folder.getPath() + "/package");
                    package_template_files = package_template_folder.listFiles();
                    copyPackageFilesToPackage(package_template_files, package_name);

                    // This is all the scripts in the folder which is like a packet. One html-file with the script-name must
                    // be created in the packet-directory. And a link must be inserted in several files starting with the packet-name.
                    scripts = source_files[i].listFiles();
                    makeHtmlFileForEachScript(scripts,jsdoc_path,"",package_folder.getPath());
    //                updateFilesWithLinksRecursive(scripts,package_folder.getPath());
                    completePackageFiles(package_folder.getPath());
                }
            completeRootFiles();
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Copy the package-files from the /package-folder to the current package folder.
     * Replace variables in the start-part of the files and save it to a  file, that has not the start-part in the name.
     * Open a filewriter that is global fro each file.
     * @param package_template_files All the files beyond the /package folder
     * @param package_name the name of the current package-folder
     */
    private static void copyPackageFilesToPackage(File[] package_template_files, String package_name) {
        int pos1;
        File package_file;
        String package_file_name;
        try {
            for (int i = 0; i < package_template_files.length; i++) {
                package_file = package_template_files[i];
                package_file_name = package_name + "/" + package_file.getName();
                if(package_file_name.contains("-start")) {
                    copyFile(new FileInputStream(package_file), package_file_name, false);
                    if(package_file_name.contains("package-frame-start.html")) {
                        package_file_name = replaceVariablesByData(destination_folder + "/" + package_file_name, package_name);
                        package_frame = new FileWriter(destination_folder + "/" + package_file_name, true);
                    }
                    if(package_file_name.contains("package-summary-start.html")) {
                        package_file_name = replaceVariablesByData(destination_folder + "/" + package_file_name, package_name);
                        package_summary = new FileWriter(destination_folder + "/" + package_file_name, true);
                    }
                    if(package_file_name.contains("package-tree-start.html")) {
                        package_file_name = replaceVariablesByData(destination_folder + "/" + package_file_name, package_name);
                        package_tree = new FileWriter(destination_folder + "/"  + package_file_name, true);
                    }
                    if(package_file_name.contains("package-use-start.html")) {
                        package_file_name = replaceVariablesByData(destination_folder + "/" + package_file_name, package_name);
                        package_use = new FileWriter(destination_folder + "/" + package_file_name, true);
                    }
                    if(package_file_name.contains("class-start.html")) {
                        replaceVariablesByData(destination_folder + "/" + package_file_name, package_name);
                    }

                }

            }


        }catch(IOException ioex){
            System.out.println(ioex.getMessage());
        }
    }

    /**
      *  In the start of each file, there are variables, that must be replaced by data
      * @param filepath_in  String  path to file to be changed
      * @param package_name String  the current package name, else ""
     */
    private static String replaceVariablesByData(String filepath_in, String package_name){
        String filename_out="";
        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader(filepath_in));
            List<String> lines = toCollection(br);
            int pos1 = filepath_in.lastIndexOf("/");
            int pos2 = filepath_in.lastIndexOf("-start");
            if(pos2 > pos1){
                filename_out = filepath_in.substring(pos1+1,pos2) + ".html";
                if(!package_name.equals(""))
                    filename_out = package_name + "/" + filename_out;
                File in_file = new File(filepath_in);
                copyFile(new FileInputStream(in_file),filename_out,false);
                in_file.delete();
            }else {
                filename_out = filepath_in.substring(pos1+1);
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(destination_folder + "/" + filename_out));
            for(int i=0;i < lines.size(); i++){
                line = lines.get(i);
                if(line.contains("{PROJECT_NAME}")){
                  line = line.replace("{PROJECT_NAME}",project_name);
                }
                if(line.contains("{CREATED_DATE}")){
                    line = line.replace("{CREATED_DATE}",today.toString());
                }
                if(line.contains("{PACKAGE_NAME}")){
                    line = line.replace("{PACKAGE_NAME}", package_name);
                }
                bw.write(line);
            }
            bw.close();

        }catch(Exception ex){
            System.out.println(ex.getMessage() + " " + ex.getStackTrace());
        }
        return filename_out;
    }

    /**
     * <p>Put content of a buffered reader into an array of Strings</p>
     * @param  br the name of the buffered reader
     * @return lines
     */
    static List<String> toCollection(BufferedReader br) throws Exception {
        List<String> lines = new LinkedList<>();
        String line = br.readLine();
        while (line != null) {
            lines.add(line);
            line = br.readLine();
        }
        return lines;
    }

    /**
     * Copy the contents of the package-summary-end.html to the end of package-summary-start.html and close the output-stream.
     * Rename the latter to package-summary.html and delete the package-summary-end.html.
     * @param package_path Path to this package
     */
    private static void completePackageFiles(String package_path){
        try {
            Object[][] package_information = {  {package_summary,"/package-summary-end.html"},
                                                {package_tree,"/package-tree-end.html",},
                                                {package_use,"/package-use-end.html"},
                                                {package_frame,"/package-frame-end.html"}
            };
            FileWriter fw;
            for(int i=0;i < package_information.length;i++) {
                File endFile = new File(package_path + package_information[i][1]);
                FileInputStream package_end = new FileInputStream(endFile);
                byte[] buffer = new byte[package_end.available()];
                package_end.read(buffer);
                fw = (FileWriter)package_information[i][0];
                fw.append(new String(buffer,StandardCharsets.UTF_8));
                fw.close();
                endFile.delete();
            }

        }catch(Exception ex){

        }
    }

    private static void completeRootFiles(){
        try {
            Object[][] package_information = {  {allclasses_frame,"/allclasses-frame-start.html","/allclasses-frame-end.html","/allclasses-frame.html"},
                    {allclasses_noframe,"/allclasses-noframe-start.html","/allclasses-noframe-end.html","/allclasses-noframe.html"}
            };
            FileWriter fw;
            for(int i=0;i < package_information.length;i++) {
                File endFile = new File(destination_folder + package_information[i][2]);
                FileInputStream package_end = new FileInputStream(endFile);
                byte[] buffer = new byte[package_end.available()];
                package_end.read(buffer);
                fw = (FileWriter)package_information[i][0];
                fw.append(new String(buffer,StandardCharsets.UTF_8));
                fw.close();
                File fromFile = new File(destination_folder  + package_information[i][1]);
                File toFile = new File(destination_folder  + package_information[i][3]);
                fromFile.renameTo(toFile);
                endFile.delete();
                fromFile.delete();
            }

        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Create a folder-structure below the destination-folder and copy one html-template file to for each script, to the right folder, giving it the script-name.html
     *          appendLinkToFile is called to append a link to two files at the root of the destination, and to the package-files in the new directory
     * @param scripts an array with directories/files from the source-folder
     * @param jsdoc_path the path to the jsdoc (root)
     * @param package_path the path to the folder/package
     * @param script_name name of script
     */
    private static void makeHtmlFileForEachScript(File[] scripts, String jsdoc_path, String package_path, String script_name) {
        String html_name;
        int pos1;
        File folder;
        File[] subscripts;
        File script;
        String  first_path_part;
        FileWriter html_writer;
        try {
            for (int i = 0; i < scripts.length; i++) {
                if(scripts[i].isDirectory()) {
                    folder = new File(package_path + "/" + scripts[i].getName());
                    folder.mkdir();
                    jsdoc_path = jsdoc_path + "../";
                    package_path = package_path + "../";
                    makeHtmlFileForEachScript(scripts[i].listFiles(), jsdoc_path, package_path , scripts[i].getName());
                } else {
                    iterations++;
                    html_name = scripts[i].getName().substring(0, scripts[i].getName().indexOf(".")) + ".html";
                    html_name = package_path + "/" + html_name;
                    html_name = html_name.substring(html_name.indexOf("jsdoc")+6);
                    first_path_part = package_path.substring(0,package_path.lastIndexOf("/"));
                    File html_file = copyFile(new FileInputStream( first_path_part + "/class.html"), html_name, false);
                    File html_template_file = new File( destination_folder + "/package" + "/class-template.html");
                    File end_file = new File( destination_folder + "/package" + "/class-end.html");
                    if(html_file != null) {
                        if(scripts[i].getName().equals("ProjectView.js"))
                           makeScriptDocumentation(jsdoc_path, package_path, scripts[i], html_template_file, html_file, end_file);
                        appendLinkToFile(html_name, iterations);
                    }

                }

            }
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    /**
     * This routine makes the documentation-file for one script
     * The documentation-paragraphs must correspond to these rules:
     * The start sequence (One slash and two asterisks) must be on one line alone.
     * The stop sequence (One asterisk and a slash)  must be on one line alone.
     *
     * The information must be on lines starting with a asterisk.
     * There are one @class followed by the class-name. The next line holds the description, that could span several lines.
     * There are one @author followed by the description, that must be on one line only.
     * There are one @constructor followed by the description, that could span several lines.
     * There are one @method followed by the method name .The next line holds the description, that could span several lines.
     * Both constructor and method could have several @param, that is input parameter, followed by the name and the type, and the description on one line.
     * Methods can have a @returns, that is the return value. This have the type and the description on the same line.
     * Links could not be used in the script-documentation system.
     * @param script_file  The script that contains the documentation sections, from which the content of the variables are collected.
     * @param template_file The template for the <script>.html file. This contains variables on this pattern {variable_name} to be replaced
     * @param html_file The file <script>.html that will have the documentation for this script.
     * @return boolean true/false
     */
    private static boolean makeScriptDocumentation(String jsdoc_path, String package_path, File script_file, File template_file, File html_file, File end_file){
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
        try {

            package_name = template_file.getPath();
            package_name = package_name.substring(package_name.indexOf("jsdoc")+6);
            package_name = package_name.substring(0,package_name.lastIndexOf("/"));

            /*
                The readers and writer are initialized by the files
            */
            script_reader = new BufferedReader(new FileReader(script_file));
            template_reader = new BufferedReader(new FileReader(template_file));

            BufferedReader document_reader = new BufferedReader(new FileReader(html_file));
            String ln = document_reader.readLine();
            while(ln != null){
                System.out.println(ln + "\n");
                ln = document_reader.readLine();
            }
            document_reader.close();


            document_writer = new FileWriter(html_file, true);
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
                        switch(line_parts[1]){
                            case "@class":
                                String class_description="";
                                script = new Method();
                                script.setType("script");
                                methods.add(script);

                                if(line_parts.length > 2)
                                    script.setName(line_parts[2]);
                                if(line_parts.length > 3) {
                                    for (int i = 3; i < line_parts.length; i++)
                                        class_description = class_description + " " + line_parts[i];

                                    script.setDescription(class_description);
                                }
                                last_variable = 1;
                                break;
                            case "@constructor":
                                String constructor_description="";
                                constructor = new Method();
                                constructor.setType("constructor");
                                methods.add(constructor);
                                if(line_parts.length > 2) {
                                    for(int i=2;i < line_parts.length; i++)
                                        constructor_description = constructor_description  + " " +  line_parts[i];

                                    constructor.setDescription(constructor_description);
                                }

                                last_variable = 2;
                                break;
                            case "@method":
                                String method_description="";
                                method = new Method();
                                method.setType("method");
                                methods.add(method);
                                if(line_parts.length > 2)
                                    method.setName(line_parts[2]);
                                if(line_parts.length > 3) {
                                    for(int i=3;i < line_parts.length; i++)
                                        method_description = method_description  + " " +  line_parts[i];

                                    method.setDescription(method_description);
                                }

                                last_variable = 3;
                                break;
                            case "@param":
                                String parameter_description="";
                                param = new Parameter();
                                if(line_parts.length > 2)
                                    param.setName(line_parts[2]);

                                if(line_parts.length > 3)
                                   param.setType(line_parts[3]);

                                if(line_parts.length > 4) {
                                    for(int i=4;i < line_parts.length; i++)
                                        parameter_description = parameter_description  + " " +  line_parts[i];
                                    param.setDescription(parameter_description);
                                }
                                if(last_variable == 2)
                                    constructor.setParam(param);
                                else
                                    method.setParam(param);

                                break;
                            case "@returns":
                                String return_description="";
                                if(line_parts.length > 2)
                                    method.setReturnType(line_parts[2]);
                                if(line_parts.length > 3) {
                                    for(int i=3;i < line_parts.length; i++)
                                        return_description = return_description  + " " +  line_parts[i];
                                    method.setReturnDescription(return_description);
                                }

                                break;
                            case "@author":
                                author = new Method();
                                author.setType("author");
                                methods.add(author);
                                if(line_parts.length > 2) {
                                    for(int i=2;i < line_parts.length; i++)
                                       author_description = author_description  + " " +  line_parts[i];

                                    author.setDescription(author_description);
                                }
                                last_variable = 6;
                                break;

                            default:
                                for(int i=1;i < line_parts.length; i++)
                                    description = description + line_parts[i];

                                /*
                                    The descriptions could span several lines. That is why we check the methods LinkedList,
                                    to include what is already stored in the variable.
                                */
                                switch(last_variable){
                                    case 1: // 1 = class (script)
                                        if(script != null)
                                            script.setDescription(script.getDescription() + description);
                                        break;
                                    case 2: // 2 = constructor
                                        if(constructor != null)
                                            constructor.setDescription(constructor.getDescription() + description);
                                        break;
                                    case 3: // 3 = method
                                        if(method != null)
                                            method.setDescription(method.getDescription() + description);
                                        break;
                                    case 5: // 5 = return
                                        if(method != null)
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


                /*
                    Prepare replacement of variables in the template by setting some variables.
                */
                String class_name = "";
                String class_description = "";
                String constructor_name ="";
                List<Parameter> method_params;
                List<Parameter> constructor_params;

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
                        line_template = line_template.replace("{JSDOC_PATH}", jsdoc_path);

                    if(line_template.contains("{PACKAGE_PATH}"))
                        line_template = line_template.replace("{PACKAGE_PATH}", package_path);

                    if(line_template.contains("{CREATED_DATE}"))
                        line_template = line_template.replace("{CREATED_DATE}", today.toString());

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
                script_reader.close();
                template_reader.close();
                document_writer.close();
            }catch(Exception ex){
                System.out.println(ex.getMessage());
            }
        }
        return result;
    }

    private static void appendLinkToFile(String htmlPath, int counter){
        try {
            String packageName = htmlPath.substring(0,htmlPath.indexOf("/") );
            String fileName = htmlPath.substring(htmlPath.indexOf("/") + 1);
            String className = fileName.substring(0,fileName.indexOf("."));
            String line = "<li><a href=\"" + htmlPath + "\"  title=\"class in " + packageName + "\"  target=\"classFrame\">" + className + "</a></li>\n";
            allclasses_frame.append(line);
            line = "<li><a href=\"" + htmlPath + "\"  title=\"class in " + packageName + "\">" + className + "</a></li>\n";
            allclasses_noframe.append(line);

            String[] color = {"rowColor","altColor"};

            package_summary.append("<tr class=\"" + color[counter % 2] +"\">\n");
            package_summary.append("<td class=\"colFirst\"><a href=\"../" + htmlPath + "\" title=\"class in " + packageName + "\">" + className + "</a></td>\n");
            package_summary.append("<td class=\"colLast\">&nbsp;</td></tr>\n");

            package_tree.append("<li type=\"circle\">" + packageName + ".<a href=\"../" + htmlPath + "\" title=\"class in " + packageName + "\"><span class=\"typeNameLink\">" + className + "</span></a></li>\n");

            package_use.append("<td class=\"colLast\">&nbsp;</td></tr>\n");;
            package_use.append("<tr class=\"" + color[counter % 2] +"\">\n");
            package_use.append("<td class=\"colOne\"><a href=\"../" + packageName + "/class-use/" + fileName + "#" + packageName + "\">" + className + "</a>&nbsp;</td></tr>\n");

            package_frame.append("<li><a href=\"" + fileName + "\" title=\"class in " +  packageName + "\" target=\"classFrame\">" + className + "</a></li>\n");
        }catch(Exception ex){
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
        }
    }

    /**
     *
     * @param input_stream
     * @param destination
     * @param directory
     * @return
     */
    private static File copyFile(InputStream input_stream, String destination, boolean directory) {
        String filename;
        File original;
        FileOutputStream output_stream=null;
        try {
            original = new File(destination_folder + '/' + destination);
            if(directory)
                original.mkdir();
            else {
                output_stream = new FileOutputStream(original);
                byte[] buffer = new byte[input_stream.available()];
                input_stream.read(buffer);
                output_stream.write(buffer);
                output_stream.close();
                BufferedReader br = new BufferedReader(new FileReader(original));
                String ln = br.readLine();
                while(ln !=null){
                    System.out.println(ln);
                    ln = br.readLine();
                }
            }
        }catch(IOException ioex){
            System.out.println(ioex.getMessage());
            return null;
        }
        return original;
    }

}

