package project;
import java.io.*;
import java.util.LinkedList;

public class Folder {
    private String absolute_source_path;
    private String absolute_destination_path;
    private String foldername;
    private LinkedList<project.Script> folder_scripts;          // All the scripts below this folder
    private LinkedList<String> all_folder_filepaths;            // Relative filepaths to the files beyond this folder describing each script <directory>/<filename>
    private LinkedList<File> all_package_files_edited;          // This is a list with all the package-files copied from the package-folder, and edited
    private File folder_directory;
    private Documentation parent;
    /**
     * @param parent {@link Documentation} Link back to the Documentation object inwhich this Folder-object is contained.
     * @param absolute_source_path {@link java.lang.String} Path to the main-folder where the <script>.js is located. (e.g. /home/anne/IdeaProjects/PlanYourProject/src/webapp/Scripts/Components)
     * @param absolute_destination_path {@link java.lang.String} Path to the main-folder where the <script>.html is located. (e.g. /home/anne/IdeaProjects/PlanYourProject/target/site/jsdoc/Components)
     */
    public Folder(Documentation parent, String absolute_source_path,String absolute_destination_path){
        this.absolute_source_path =  absolute_source_path;
        this.absolute_destination_path = absolute_destination_path;
        this.foldername = absolute_source_path.substring(absolute_source_path.lastIndexOf("/")+1);
        folder_directory = new File(absolute_destination_path);
        folder_directory.mkdir();
        this.folder_scripts = new LinkedList<>();
        this.all_folder_filepaths = new LinkedList<>();
        this.all_package_files_edited = new LinkedList<>();
        this.parent = parent;
        copyPackageFiles();
        parent.customizeFiles(absolute_destination_path);
        replaceVariablesInFolderFiles();

    }

    String getProjectName(){
        return parent.getProjectName();
    }

    String getToday(){
        return parent.getToday();
    }

    Documentation getParent(){
        return parent;
    }

    boolean addPathToAllFolderFilepaths(String path_to_file){
        boolean result = false;
        this.all_folder_filepaths.add(path_to_file);
        return result;
    }

    /**
     * All files in the <destination_folder>/package are  copied to the current folder.
     * @return
     */
    boolean copyPackageFiles(){
        File package_folder;
        File[] package_files;
        FileInputStream input_stream;
        FileOutputStream  output_stream;
        try {
            package_folder = new File(absolute_destination_path.substring(0,absolute_destination_path.lastIndexOf("/"))+ "/package");
            package_files = package_folder.listFiles();
            for(int i=0;i < package_files.length;i++) {
                input_stream = new FileInputStream(package_files[i]);
                output_stream = new FileOutputStream(new File(folder_directory.getPath() + "/" + package_files[i].getName()));
                byte[] buffer = new byte[input_stream.available()];
                input_stream.read(buffer);
                output_stream.write(buffer);
                output_stream.close();
                input_stream.close();
            }

        }catch(Exception ex){
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
            return false;
        }
        return true;
    }

    boolean replaceVariablesInFolderFiles(){
        BufferedReader br;
        BufferedWriter bw;
        String ln;
        String abs_path;
        try {
          File[] files = folder_directory.listFiles();
          for(int i=0;i < files.length; i++){
              if(files[i].getName().contains("-edited")){
                  br = new BufferedReader(new FileReader(files[i]));
                  abs_path = files[i].getAbsolutePath();
                  abs_path = abs_path.substring(0,abs_path.indexOf("-edited")) + ".html";
                  bw = new BufferedWriter(new FileWriter(new File(abs_path)));      // Create new file thet do not contain -edited as part of the name
                  ln = br.readLine();
                  while(ln != null){
                      if(ln.contains("{FOLDER_NAME}"))
                          ln = ln.replace("{FOLDER_NAME}", foldername);

                      bw.write(ln + "\n");
                      ln = br.readLine();
                  }
                  br.close();
                  bw.close();
                  files[i].delete();
              }
          }
        }catch(Exception ex){
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
            return false;
        }
        return true;
    }

    public String getAbsolutePath(){
        return this.absolute_destination_path;
    }

    String getFolderName(){
        return this.foldername;
    }

    LinkedList getScriptCollection(){
        return this.folder_scripts;
    }

    boolean addScriptToCollection(Script script){
        folder_scripts.add(script);
        return true;
    }

    Script getScriptFromCollection(int i){
        if(i <= folder_scripts.size() - 1)
          return this.folder_scripts.get(i);
        else {
            System.out.println("THe collection contains " + folder_scripts.size() + " scripts. Index " + i + " is too high.");
            return null;
        }

    }


    boolean insertLinksIntoFilesAtFolder(){
        int counter = 0;
        String[] color={"altColor","rowColor"};
        boolean result = false;
        String line="";
        BufferedReader reader = null;
        BufferedWriter writer = null;
        File input_file=null;
        try {
            for (int i = 0; i < this.all_package_files_edited.size(); i++) {
                input_file = this.all_package_files_edited.get(i);
                reader = new BufferedReader(new FileReader(input_file));
                writer = new BufferedWriter(new FileWriter(input_file));
                line = reader.readLine();
                while(line != null){
                    if(line.contains("Insert links")){
                        counter++;

                        if(input_file.getName().equals("package-summary-edited.html")){
                            line = "<tr class=\"" + color[counter % 2] + "\">\n" +
                                    "<td class=\"colFirst\"><a href=\"../" +  foldername + "/" + input_file + "\" title=\"class in " + foldername + "\">" + input_file + "</a></td>\n" +
                                    "<td class=\"colLast\">&nbsp;</td></tr>\n";

                        }
                        if(input_file.getName().equals("package-tree-edited.html")){
                            line = "<li type=\"circle\">" + foldername + ".<a href=\"../" + foldername + "/" + input_file + "\" title=\"class in " + foldername + "\"><span class=\"typeNameLink\">" + input_file + "</span></a></li>\n";
                        }

                        if(input_file.getName().equals("package-use-edited.html")){
                            line = "<td class=\"colLast\">&nbsp;</td></tr>\n" +
                                    "<tr class=\"" + color[counter % 2] +"\">\n" +
                                    "<td class=\"colOne\"><a href=\"../" + foldername + "/class-use/" + input_file + "#" + foldername + "\">" + input_file + "</a>&nbsp;</td></tr>\n";

                        }

                    }
                    writer.write(line);
                    line = reader.readLine();

                }
            }
        }catch(Exception ex){
            System.out.println(ex.getMessage() + "\n" + ex.getStackTrace());
        }
        return result;
    }

    Script getFirstScriptInFolder(){
        if(folder_scripts.size() > 0)
            return folder_scripts.get(0);
        else
            return null;
    }

    Script getLastScriptInFolder(){
        if(folder_scripts.size() > 0)
            return folder_scripts.get(folder_scripts.size() - 1);
        else
            return null;
    }
}
