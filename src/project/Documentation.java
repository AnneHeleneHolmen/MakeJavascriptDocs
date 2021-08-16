package project;

import java.io.*;
import java.util.LinkedList;
import java.time.LocalDate;

public class Documentation {

    private String absolute_source_path;
    private String absolute_destination_path;
    private String project_name;
    private LinkedList<Folder>  all_folders;        // All folders in the system
    private LinkedList<String> all_filepaths;               // Relative filepaths to the files describing each script <directory>/<directory>/<filename>
    private LinkedList<File> all_classes_edited;            // This is the files at the destination-folder with the name that starts with all_classes..
    private String today;
    private LinkedList<String> ignored_scripts;

    /**
     *
     * @param absolute_source_path {@link java.lang.String} that is the same at the source given in the -s option at the command line
     * @param absolute_destination_path {@link java.lang.String} that is the same at the destination given in the -d option at the command line
     * @param project_name {@link java.lang.String} that is the project name given with the -p option at the command line
     */
    public Documentation(String absolute_source_path, String absolute_destination_path, String project_name){
        this.absolute_source_path = absolute_source_path;
        this.absolute_destination_path = absolute_destination_path;
        this.project_name = project_name;
        this.today = LocalDate.now().toString();
        this.all_folders = new LinkedList<>();
        this.all_filepaths = new LinkedList<>();
        this.all_classes_edited = new LinkedList<>();
        all_classes_edited.add(new File(absolute_destination_path + "/allclasses-frame-edited.html"));
        all_classes_edited.add(new File(absolute_destination_path + "/allclasses-noframe-edited.html"));
        customizeFiles(absolute_destination_path);
        if(getIgnoredFiles())
            buildAllFolderAndScriptObjects(false,absolute_source_path,-1, absolute_destination_path,"", "");
    }

/*
    /**
     * Create a folder-structure below the destination-folder and copy one html-template file to for each script, to the right folder, giving it the script-name.html
     *          appendLinkToFile is called to append a link to two files at the root of the destination, and to the package-files in the new directory
     * @param scripts an array with directories/files from the source-folder
     * @param jsdoc_path the path to the jsdoc (root)
     * @param package_path the path to the folder/package
     * @param script_name name of script

   private static void makeHtmlFileForEachScript(File[] scripts, String jsdoc_path, String package_path, String script_name) { */

    boolean getIgnoredFiles(){
        BufferedReader br = null;
        String ln="";
        String filename="";
        try {
            br = new BufferedReader(new FileReader(new File(absolute_source_path + "/.doc_ignore")));
            ignored_scripts = new LinkedList();
            ln = br.readLine();
            while(ln != null){
                ln = ln.trim();
                if(ln.contains(".js"))
                    if(ln.contains("/")) {
                        filename = ln.substring(ln.lastIndexOf("/") + 1);
                        System.out.println("The file-paths are ignored !");
                    } else
                        filename = ln;

                ignored_scripts.add(filename);
                ln = br.readLine();
            }
        }catch(Exception ex){
            System.err.println(ex.getStackTrace() + "\n" + ex.getMessage());
            System.out.println("The .doc_ignore file should only contain a list of filenames.\n One filename is expected in each line.\n If a path is given, it is ignored !");
            return false;
        }finally {
            try {
                br.close();
            }catch(IOException ioex){
                System.out.println(ioex.getStackTrace() + "/" + ioex.getMessage());
            }
        }

        return true;

    }

    boolean buildAllFolderAndScriptObjects(boolean result, String abs_source_path,int index, String abs_destination_path,String path_from_destination, String bckwrd_path_to_root_destination){
        String html_name;
        File source_folder;
        File dest_folder;
        File[] scripts;
        String script_name;
        File script_documentation_file;
        Folder parent_folder;
        try {
            index ++;
            source_folder = new File(abs_source_path);
            if(source_folder.isDirectory()) {
                scripts = source_folder.listFiles();
                for (int i = 0; i < scripts.length; i++) {
                    if (scripts[i].isDirectory()) {
                        path_from_destination = path_from_destination + "/" + scripts[i].getName();
                        bckwrd_path_to_root_destination = bckwrd_path_to_root_destination + "../";

                        dest_folder = new File( abs_destination_path + "/" + scripts[i].getName());
                        dest_folder.mkdir();
                        if(index == 0)
                            addFolderToCollection(new Folder(this, abs_source_path + "/" + scripts[i].getName(), dest_folder.getAbsolutePath()));

                        result = buildAllFolderAndScriptObjects(false,abs_source_path + "/" + scripts[i].getName(), index, dest_folder.getAbsolutePath(), path_from_destination, bckwrd_path_to_root_destination);
                        if(result ) {
                            // Reset variables, but it depends upon in  which loop we are.
                            if(index > 0) {
                                path_from_destination = path_from_destination.substring(0, path_from_destination.lastIndexOf("/"));
                                bckwrd_path_to_root_destination = "../";
                                index = 1;
                            }else {
                                path_from_destination = "";
                                bckwrd_path_to_root_destination = "";
                            }
                        }
                    } else {
                        script_name = scripts[i].getName();
                        if(!ignored_scripts.contains(script_name)) {
                            if(script_name.indexOf(".js") > 0) {
                                script_documentation_file = new File(abs_destination_path + "/" + script_name.substring(0, script_name.indexOf(".")) + ".html");
                                parent_folder = this.getFolderFromCollection(this.getLastIndexFolderCollection());
                                parent_folder.addScriptToCollection(new Script(parent_folder, abs_source_path + "/" + script_name, script_documentation_file.getAbsolutePath(),
                                        path_from_destination, bckwrd_path_to_root_destination));
                                result = true;
                            }
                        }

                    }

                }

            }
        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        return result;
    }



    String getProjectName(){
        return project_name;
    }

    String getToday(){
        return today;
    }

    /**
     * Method that loops trough a folder and looks for files with the <filename>-temp.html in their names.
     * If found, it opens the file, replaces variables like {PROJECT_NAME} and {DATE_CREATED} with variables.
     * Then it store the file to one where the name is changed to <filename>-edited.html. Delete the original file.
     * @param absolute_path_to_folder {@link java.lang.String}
     */
    boolean customizeFiles(String absolute_path_to_folder) {
        String line = "";
        BufferedReader reader=null;
        BufferedWriter writer=null;
        File[] files;
        File folder = new File(absolute_path_to_folder);
        String filepath_temp;
        String filepath_edited;
        try {
            if(folder.isDirectory()) {
                files = folder.listFiles();
                for(int i=0;i < files.length; i++) {
                    if (files[i].getName().contains("-temp.html")) {
                        filepath_temp = files[i].getPath();
                        filepath_edited = filepath_temp.substring(0,filepath_temp.indexOf("-temp")) + "-edited.html";
                        reader = new BufferedReader(new FileReader(new File(filepath_temp)));
                        writer = new BufferedWriter(new FileWriter(new File(filepath_edited)));
                        line = reader.readLine();
                        while (line != null) {
                            if (line.contains("{PROJECT_NAME}"))
                                line = line.replace("{PROJECT_NAME}", project_name);

                            if (line.contains("{DATE_CREATED}"))
                                line = line.replace("{DATE_CREATED}", today);

                            if (line.contains("{JSDOC_PATH}"))
                                line = line.replace("{JSDOC_PATH}", absolute_destination_path);

                            writer.write(line +"\n");
                            line = reader.readLine();
                        }
                        reader.close();
                        writer.close();
                        files[i].delete();
                    }
                }
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
            return false;
        } finally {

        }
        return true;
    }

    boolean addFolderToCollection(Folder folder){
        try {
            this.all_folders.add(folder);
        }catch(Exception ex){
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
            return false;
        }
        return true;
    }

    int getLastIndexFolderCollection(){
        return this.all_folders.size()-1;
    }

    LinkedList getFolderCollection(){
        return this.all_folders;
    }

    Folder getFolderFromCollection(int index){
        return this.all_folders.get(index);
    }

    boolean addPathToAllFiles(String path_to_file){
        boolean result = false;
        this.all_filepaths.add(path_to_file);
        return result;
    }

    LinkedList getAllFiles(){
        return this.all_filepaths;
    }


    boolean insertLinksIntoFilesAtRoot(){
        int counter = 0;
        String[] color={"altColor","rowColor"};
        boolean result = false;
        String line="";
        BufferedReader reader = null;
        BufferedWriter writer = null;
        File input_file=null;
        String filepath="";
        LinkedList<Script> scripts=null;
        String filename;
        File output_file;

        try {
            // Les alle filene som er lagret i en liste. Dette er to filer
            for (int i = 0; i < this.all_classes_edited.size(); i++) {
                input_file = this.all_classes_edited.get(i);
                filename = input_file.getAbsolutePath();
                output_file = new File(filename.substring(0,filename.indexOf("-edited")) + ".html");
                writer = new BufferedWriter(new FileWriter(output_file));
                reader = new BufferedReader(new FileReader(input_file));
                line = reader.readLine();
                while (line != null) {
                    if (line.contains("Insert links")) {
                        counter++;
                        for (int j = 0; j < all_filepaths.size(); j++) {

                            filepath = (String) all_filepaths.get(j);

                            if (input_file.getName().equals("allclasses-frame-edited.html")) {
                                line = "<li><a href=\"" + absolute_destination_path + filepath + "\"  title=\"class in " + filepath.substring(0,filepath.indexOf("/")) + "\"  target=\"classFrame\">" + filepath.substring(filepath.indexOf("/")) + "</a></li>\n";
                            }

                            if (input_file.getName().equals("allclasses-noframe-edited.html")) {
                                line = "<li><a href=\"" + absolute_destination_path  + filepath + "\"  title=\"class in " + filepath.substring(0,filepath.indexOf("/")) + "\">" + filepath.substring(filepath.indexOf("/"))  + "</a></li>\n";
                            }

                            writer.write(line);  // Dette skriver en linje som er ny.
                        }
                    }else{
                        writer.write(line);  // Dette skriver linjen som er lest, dersom den ikke inneholder 'Insert links' eller er en ny link
                    }
                    line = reader.readLine();
                }
                try {
                    reader.close();
                    writer.close();
                    input_file.delete();
                }catch(IOException ioe){
                    System.err.println(ioe.getStackTrace() + "\n" + ioe.getMessage());
                }
            }
            result = true;
        }catch(Exception ex){
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
        }finally{

        }
        return result;
    }

    boolean insertLinksIntoFilesAtFolders(){
        LinkedList<Folder> folders = getFolderCollection();
        for(int i=0; i < folders.size(); i++){
            folders.get(i).insertLinksIntoFilesAtFolder();
        }
        return true;
    }

    boolean replacePrevAndNextVariables(){
        Script prev_script;
        Script next_script;
        Folder folder;
        LinkedList<Script> script_collection;
        try {
            for(int i=0;i < all_folders.size(); i++){
                folder = all_folders.get(i);
                script_collection = folder.getScriptCollection();
                for(int j=0;j < script_collection.size();j++){
                    // Her må man åpne dokumentasjons-filen med en bufferedreader, loope igjennom til man finner teksten insert previous and next link
                    // Disse finnes i forrige eller neste script. Når j er 0, skal man sjekke om i > 0. Hvis ja, kall folder.lastScript el-l
                    // Hvis j er lik script_collection.size - 1, må man sjekke om i < all_folders - 1. Hvis ja, er next link lik folder.firstScript
                    if(j > 0)
                        prev_script = script_collection.get(j-1);
                    else
                    if(i > 0)
                        prev_script = all_folders.get(i).getLastScriptInFolder();
                    else
                        prev_script = null;

                    if(j < script_collection.size() - 1)
                        next_script = script_collection.get(j + 1);
                    else
                    if(i < all_folders.size() - 1)
                        next_script = all_folders.get(i).getFirstScriptInFolder();
                    else
                        next_script = null;

                    script_collection.get(j).replacePrevAndNextVariables(prev_script, next_script);
                }
            }
        }catch(Exception ex){
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
        }finally {}
        return true;
    }

}
