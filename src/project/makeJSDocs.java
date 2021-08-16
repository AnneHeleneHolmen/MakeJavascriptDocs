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
    private static Documentation documentation;
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

                    if(destination_folder != null && source_folder != null) {
                        initializeFiles();
                        documentation = new Documentation(source_folder,destination_folder,project_name);
                        documentation.insertLinksIntoFilesAtRoot();
                        documentation.insertLinksIntoFilesAtFolders();
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

            }
        }catch(IOException ioex){
            System.out.println(ioex.getMessage());
            return null;
        }
        return original;
    }

}

