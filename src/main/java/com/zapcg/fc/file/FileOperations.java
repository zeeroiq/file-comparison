package com.zapcg.fc.file;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 */
@Slf4j
@NoArgsConstructor
public class FileOperations {

    private static final String PROCESSED_MATCHED = "\\PROCESSED\\MATCHED\\";
    private static final String PROCESSED_MISMATCHED = "\\PROCESSED\\MISMATCHED\\";
    private static final String UNPROCESSED = "\\UNPROCESSED\\";
    private static final String NEW = "\\NEW\\";

    /**
     * SOURCE
     * - PROCESSED: if file is processed move it to processed directory
     * - MATCHED:
     * - MISMATCHED:
     * - UNPROCESSED: initially move all the files to unprocessed directory
     * - NEW: whatever isn't available in target move it to NEW
     * <p>
     * - temp_file_1.dat
     * - temp_file_2.dat
     * TARGET
     * - PROCESSED: if file is processed move it to processed directory
     * - MATCHED:
     * - MISMATCHED:
     * - UNPROCESSED: initially move all the files to unprocessed directory
     * <p>
     * - temp_file_1.dat
     * - temp_file_2.dat
     *
     * @param src
     * @param target
     */
    public FileOperations(String src, String target) throws IOException {
        Path src_path = Paths.get(src);
        Path target_path = Paths.get(target);
        // source will have directories PROCESSED, UNPROCESSED, NEW
        // target will have directories PROCESSED, UNPROCESSED
        if (!Files.exists(src_path) || !Files.exists(target_path)) {
            log.error("Paths isn't valid. Please check the path");
            return;
        }
        Files.createDirectories(Paths.get(src_path + NEW));
        Files.createDirectories(Paths.get(src_path + UNPROCESSED));
        Files.createDirectories(Paths.get(src_path + PROCESSED_MATCHED));
        Files.createDirectories(Paths.get(src_path + PROCESSED_MISMATCHED));
        Files.createDirectories(Paths.get(target_path + UNPROCESSED));
        Files.createDirectories(Paths.get(target_path + PROCESSED_MATCHED));
        Files.createDirectories(Paths.get(target_path + PROCESSED_MISMATCHED));
    }

    /**
     * Helper to list down all the files, and it's contents
     *
     * @param sourcePath
     */
    public void processFiles(String sourcePath) {
        try (Stream<Path> paths = Files.walk(Paths.get(sourcePath))) {

            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        String fileContents = null;
                        try {
                            fileContents = FileUtils.readFileToString(new File(String.valueOf(path)),
                                    StandardCharsets.UTF_8);
                            log.info(">>>>> Contents of file:\n" + fileContents);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        log.info(path + "\n>>>>> " + fileContents);
                    });

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks if contents of source/target are equal
     *
     * @param source String path
     * @param target String path
     * @return true if contents are equal
     * @throws IOException if path isn't correct
     */
    public boolean contentEquals(String source, String target) throws IOException {
        Path source_path = Paths.get(source);
        Path target_path = Paths.get(target);
        if (!Files.exists(source_path) || !Files.exists(target_path)) {
            // TODO:
            return false;
        }

        if (Files.isDirectory(source_path) && Files.isDirectory(target_path)) {
            return isDirectoryContentEqual(source_path, target_path);
        }

        return FileUtils.contentEquals(source_path.toFile(), target_path.toFile());
    }

    /**
     * Compare the contents of two directories
     *
     * @param p1 String path
     * @param p2 String path
     * @return true if contents are equal
     * @throws IOException if path isn't correct
     */
    private boolean isDirectoryContentEqual(Path p1, Path p2) throws IOException {

        boolean isExist_f1 = Files.exists(p1) && Files.isDirectory(p1);
        boolean isExist_f2 = Files.exists(p2) && Files.isDirectory(p2);

        if (isExist_f1 && isExist_f2) {
            HashMap<Path, Path> paths_p1 = new HashMap<>();
            HashMap<Path, Path> paths_p2 = new HashMap<>();

            // map the path relative to base dir
            mapPathToBaseDir(p1, paths_p1);
            mapPathToBaseDir(p2, paths_p2);

            // using parallel stream to process the files
            paths_p1.entrySet().stream().parallel().forEach(pathEntry -> {
                Path relativePath = pathEntry.getKey();
                Path absolutePath = pathEntry.getValue();
                boolean isP2ContainsKey = paths_p2.containsKey(relativePath);
                Path absolutePathInP2 = paths_p2.get(relativePath);
                if (!isP2ContainsKey) {
                    try {
                        Files.move(absolutePath,
                                Paths.get(absolutePath.getParent().getParent() + NEW + absolutePath.getFileName()),
                                StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    log.info(String.format(">>>> Target folder doesn't have :  %s", relativePath.getFileName()));
                } else {
                    try {
                        if (!contentEquals(absolutePath.toString(), absolutePathInP2.toString())) {
                            moveProcessedFiles(absolutePath, PROCESSED_MISMATCHED, absolutePathInP2);
                            log.info(String.format(">>>> Contents are not equal for \n%s\n%s", absolutePath, paths_p2.get(relativePath)));
                        } else {
                            moveProcessedFiles(absolutePath, PROCESSED_MATCHED, absolutePathInP2);
                            log.info(String.format(">>>> Contents matched for \n%s\n%s", absolutePath, paths_p2.get(relativePath)));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        log.info("\nAll files are moved to " +
                "\nsource|target" +
                "\n\t-NEW" +
                "\n\t-PROCESSED" +
                "\n\t\t-MATCHED" +
                "\n\t\t-MISMATCHED\nPlease check there.");
        return false;
    }

    /**
     * @param absolutePath
     * @param processType
     * @param absolutePathInP2
     * @throws IOException if path isn't correct
     */
    private void moveProcessedFiles(Path absolutePath, String processType, Path absolutePathInP2) throws IOException {
        Files.move(absolutePath,
                Paths.get(absolutePath.getParent().getParent() + processType + absolutePath.getFileName()),
                StandardCopyOption.REPLACE_EXISTING);
        Files.move(absolutePathInP2,
                Paths.get(absolutePathInP2.getParent().getParent() + processType + absolutePathInP2.getFileName()),
                StandardCopyOption.REPLACE_EXISTING);
    }

    /**
     * maps the path relative to base dir
     *
     * @param p1
     * @param paths_p1
     * @throws IOException if path isn't correct
     */
    private void mapPathToBaseDir(Path p1, HashMap<Path, Path> paths_p1) throws IOException {
        Files.walk(p1).filter(Files::isRegularFile).forEach(path -> {
            log.info(">>>>> " + path);
            paths_p1.put(p1.relativize(path), path);
        });
    }

    /**
     * Helper to move files from source to target folder
     *
     * @param source String path
     * @param target String path
     * @throws IOException if path isn't correct
     */
    public void moveToUnprocessed(String source, String target) throws IOException {
        Path src_path = Paths.get(source);
        Path target_path = Paths.get(target);
        if (!Files.exists(src_path) || !Files.exists(target_path)) {
            log.error("Paths isn't valid. Please check the path");
            return;
        }

        if (Files.isDirectory(src_path) && Files.isDirectory(target_path)) {
            moveToDirectory(source + UNPROCESSED);
            moveToDirectory(target + UNPROCESSED);
        }
    }

    /**
     * Moves files to the path specified
     *
     * @param path String path where files need to be moved
     * @throws IOException if path isn't correct
     */
    private void moveToDirectory(String path) throws IOException {
        Path unprocessedPath = Paths.get(path);

        Files.walk(unprocessedPath.getParent()).filter(Files::isRegularFile).forEach(path1 -> {
            try {
                Files.move(path1, Paths.get(unprocessedPath + "\\" + path1.getFileName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}