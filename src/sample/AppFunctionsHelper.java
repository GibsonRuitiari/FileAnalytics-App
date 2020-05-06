package sample;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author sirrui
 * This class contains all the main functions of the app
 */
public class AppFunctionsHelper {
    public AppFunctionsHelper(){

    }
    public static int  empty_directory_count;
    public static  int duplicate_files;
    public static  int empty_files_count;
    private static Formatter formatter=new Formatter();

    /**
     * checks for duplicate files
     * @throws IOException incase of an error throw an io exception
     */
    public static  void checkForDuplicateFiles()throws IOException {
        Path path = Paths.get(System.getProperty("user.home"));
        DirectoryStream<Path> duplicatesStream= Files.newDirectoryStream(path);
        duplicatesStream.forEach(path1 -> {
            switch (path1.toFile().getName()){
                case "Desktop":
                case "Documents":
                case "Music":
                case "Pictures":
                case "Videos":
                    duplicate_files += duplicateHelperFunction(path1);
                    break;
                default:
                    break;
            }

        });
        formatter.format("Total number of duplicate files in your pc are %d " ,duplicate_files);
        System.out.println(formatter);
    }

    /**
     * checks for empty files
     */
    public static void checkEmptyFiles(){
        Path path = Paths.get(System.getProperty("user.home"));
        try {
            DirectoryStream<Path> directoryStream = Files.newDirectoryStream(path);
            directoryStream.forEach(path1 -> {
                if (Files.isDirectory(path1)) {
                    if (!path1.toFile().getName().startsWith(".")) {
                        switch (path1.toFile().getName()) {
                            case "Documents":
                            case "Downloads":
                            case "Music":
                            case "Desktop":
                            case "Videos":
                            case "Pictures":
                                empty_files_count += traverseDirectories(path1);
                                break;
                            default:
                                break;
                        }

                    }
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }
        formatter.format("You have %d empty files in your pc",empty_files_count);
        System.out.println(formatter);
    }
    private static  int duplicateHelperFunction(Path path){
        HashMap<String,String> hashMap=new HashMap<>();
        List<String> emptyFiles=new ArrayList<>();
        try {
            Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), 5,
                    new SimpleFileVisitor<>() {
                        @Override
                        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                            return FileVisitResult.CONTINUE;
                        }
                        @Override
                        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                            try {
                                PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("regex:.*(?i:jpg|txt|png|jpeg|docx|jpe|bmp|mp3|svg|gif|mp4|mkv|avi)");
                                if (pathMatcher.matches(file.getFileName()))
                                    if (!Files.isDirectory(file) && !Files.isSymbolicLink(path) && Files.exists(path) && Files.isReadable(path)) {
                                        String hash = checkDigest(file);
                                        if (hashMap.get(hash) != null) {
                                            String original = hashMap.get(hash);
                                            emptyFiles.add(original);
                                            System.out.println("Duplicated file: " + Paths.get(original).toFile().getPath() + ":   "+path.toFile().getPath());
                                        } else {
                                            hashMap.put(hash, file.toFile().getPath());
                                        }
                                    }

                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                            return FileVisitResult.CONTINUE;

                        }

                    });
        }catch (IOException e){
            e.printStackTrace();
        }
        return emptyFiles.size();
    }
    public static  int checkEmptyDirectories(){
        Path dir = Paths.get(System.getProperty("user.home"));
        DirectoryStream<Path>directoryStream;
        try {
            directoryStream = Files.newDirectoryStream(dir);
            directoryStream.forEach(path -> {
                if (Files.isDirectory(path)) {
                    switch (path.toFile().getName()) {
                        case "Desktop":
                        case "Downloads":
                        case "Documents":
                        case "Pictures":
                        case "Videos":
                            empty_directory_count += checkForFiles(path);
                            break;

                    }
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }
        formatter.format("There are %d empty directories in your pc",empty_directory_count);
        System.out.println(formatter);
        return empty_directory_count;
    }
    private static  int checkForFiles(Path path){
        List<String>emptyDirectory=new ArrayList<>();
        try {
            DirectoryStream<Path> paths = Files.newDirectoryStream(path);
            paths.forEach(path1 -> {
                if (Files.isDirectory(path1)) {
                    try {
                        if (Files.list(Paths.get(path1.toFile().getPath())).findAny().isEmpty()) {
                            emptyDirectory.add(path1.toFile().getPath());

                            System.out.println("The following directory is empty: " + path1.toFile().getPath());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        }catch (IOException e){
            e.printStackTrace();
        }
        return emptyDirectory.size();

    }
    private static int traverseDirectories(Path dir){
        List<String>emptyFiles=new ArrayList<>();
        try {
            Files.walkFileTree(dir, EnumSet.noneOf(FileVisitOption.class), 5, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    if (Files.isDirectory(dir)) {
                        if (Files.size(dir) == 0) {
                            //  System.out.println(dir.toFile().getPath());
                            return FileVisitResult.TERMINATE;
                        } else return FileVisitResult.CONTINUE;
                    } else return FileVisitResult.CONTINUE;

                }
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    PathMatcher pathMatcher=FileSystems.getDefault().getPathMatcher("regex:.*(?i:py)");
                    if (!pathMatcher.matches(file.getFileName())) {
                        if (Files.isReadable(file) && Files.size(file) == 0 && !Files.isDirectory(file)) {
                            emptyFiles.add(file.toFile().getPath());
                            System.out.println(file.toFile().getPath());
                            return FileVisitResult.CONTINUE;
                        } else return FileVisitResult.SKIP_SUBTREE;
                    }else return FileVisitResult.TERMINATE;

                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
            });
        }catch (IOException e){
            e.printStackTrace();
        }
        return emptyFiles.size();
    }

    /**
     * <p>Reads the file and makes a hash based on the contents of the file so if two files have the same hash then it means
     * the files contain same content</p>
     * Note: The hashing process might be slow due to the length of the file
     * @param path the path of the file
     * @return the digest string in hexadecimal format
     * @throws IOException throw an IO exception if the file cannot be read or if it is  system file or if the file is broken
     * @throws NoSuchAlgorithmException incase you put an algorithm not known by the the compiler throw NSAE
     */
    private static String checkDigest(Path path)throws IOException,NoSuchAlgorithmException{
        MessageDigest messageDigest=MessageDigest.getInstance("MD5");
        if (!Files.isSymbolicLink(path) && Files.exists(path)) {
            try (InputStream inputStream = Files.newInputStream(path, StandardOpenOption.READ)) {
                int i;
                byte[] size = new byte[1024];
                while ((i = inputStream.read(size)) != -1) {
                    messageDigest.update(size, 0, i); // i= length
                }
            }catch (AccessDeniedException e){
                e.printStackTrace();

            }
        }
        byte[]  bytes=messageDigest.digest();
        StringBuilder stringBuilder=new StringBuilder();
        for (byte aByte : bytes) {
            // convert to hexadecimal
            stringBuilder.append(Integer.toString((aByte & 0xff) + 0x100,16).substring(1));
        }
        return stringBuilder.toString();
// 0835d0c05a29da8cf7a586ec7e319ef8
    }
    /*
    invalidate the formatter
     */
    public static void clearResources(){
        if (formatter!=null){
            formatter.close();
            formatter=null;
        }
    }
}
