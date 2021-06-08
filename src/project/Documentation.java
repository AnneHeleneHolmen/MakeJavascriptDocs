package project;

import java.io.*;
import java.util.LinkedList;
import java.time.LocalDate;

public class Documentation {
    /**
     *
     * @param absolute_sourcepath {@link java.lang.String} that is the same at the source given in the -s option at the command line
     * @param absolute_destinationpath {@link java.lang.String} that is the same at the destination given in the -d option at the command line
     * @param project_name {@link java.lang.String} that is the project name given with the -p option at the command line
     */
    private String absolute_source_path;
    private String absolute_destination_path;
    private String project_name;
    private LinkedList<Folder>  all_folders;        // All folders in the system
    private LinkedList<String> all_filepaths;               // Relative filepaths to the files describing each script <directory>/<directory>/<filename>
    private LinkedList<File> all_classes_edited;            // This is the files at the destination-folder with the name that starts with all_classes..
    private String today;

    public Documentation(String absolute_source_path, String absolute_destination_path, String project_name){
        this.absolute_source_path = absolute_source_path;
        this.absolute_destination_path = absolute_destination_path;
        this.project_name = project_name;
        this.today = LocalDate.now().toString();
        this.all_folders = new LinkedList<>();
        this.all_filepaths = new LinkedList<>();
        this.all_classes_edited = new LinkedList<>();
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
     * @param date_created  {@link java.lang.String}
     */
    boolean customizeFiles(String absolute_path_to_folder, String project_name, String date_created) {
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
                                line = line.replace("{DATE_CREATED}", date_created);

                            writer.write(line);
                            line = reader.readLine();
                        }
                    }
                }
            }else{
                System.out.println(folder.getName() + " is not directory !");
                return false;
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
            return false;
        } finally {
            try {
                reader.close();
                writer.close();
            }catch(IOException ioex){
                System.out.println(ioex.getMessage() + "\n" + ioex.getStackTrace());
                return false;
            }
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

        try {
            // Les alle filene som er lagret i en liste. Dette er to filer
            for (int i = 0; i < this.all_classes_edited.size(); i++) {
                input_file = this.all_classes_edited.get(i);
                reader = new BufferedReader(new FileReader(input_file));
                line = reader.readLine();
                while (line != null) {
                    if (line.contains("Insert links")) {
                        counter++;
                        for (int j = 0; j < all_filepaths.size(); j++) {

                            filepath = (String) all_filepaths.get(j);

                                if (input_file.getName().equals("allclasses_frame-edited.html")) {
                                    line = "<li><a href=\"" + filepath + "\"  title=\"class in " + filepath.substring(0,filepath.indexOf("/")) + "\"  target=\"classFrame\">" + filepath.substring(filepath.indexOf("/")) + "</a></li>\n";
                                }

                                if (input_file.getName().equals("allclasses_noframe-edited.html")) {
                                    line = "<li><a href=\"" + filepath + "\"  title=\"class in " + filepath.substring(0,filepath.indexOf("/")) + "\">" + filepath.substring(filepath.indexOf("/"))  + "</a></li>\n";
                                }

                                writer.write(line);  // Dette skriver en linje som er ny.
                            }
                    }else{
                        writer.write(line);  // Dette skriver linjen som er lest, dersom den ikke inneholder 'Insert links' eller er en ny link
                    }
                    line = reader.readLine();
                }
            }
        }catch(Exception ex){
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
        }
       return result;
    }

    boolean replacePrevAndNextVariables(){
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
                }
            }
        }catch(Exception ex){
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
        }finally {}
       return true;
    }

}
