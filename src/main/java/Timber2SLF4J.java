import java.io.*;

/**
 * Convert a project from Timber to SLF4J
 */
public class Timber2SLF4J {

    private static final String TIMBER_IMPORT = "import timber.log.Timber";
    private static final String TIMBER_D = "Timber.d(";
    private static final String TIMBER_I = "Timber.i(";
    private static final String TIMBER_W = "Timber.w(";
    private static final String TIMBER_E = "Timber.e(";

    private static final String LOG_IMPORT1 = "import org.slf4j.Logger;\n";
    private static final String LOG_IMPORT2 = "import org.slf4j.LoggerFactory;\n";
    private static final String LOG_DEBUG = "log.debug(";
    private static final String LOG_INFO = "log.info(";
    private static final String LOG_WARN = "log.warn(";
    private static final String LOG_ERROR = "log.error(";

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            System.exit(0);
        }

        String root = args[0];
        File file = new File(root);
        if (file.exists()) {
            findAndReplace(file);
        }
    }

    private static void printUsage() {
        System.out.println("Program to Convert a project from Timber to SLF4J");
        System.out.println("usage: Timber2SLF4J <path>");
    }

    private static void findAndReplace(File parentFile) {
        for (File file : parentFile.listFiles()) {
            if (file.isDirectory()) {
                // recursive search
                findAndReplace(file);
            } else {
                String fileName = file.getName();
                if (fileName.endsWith(".java") || fileName.endsWith(".kt")) {
                    findAndReplaceInFile(file);
                }
            }

        }
    }

    private static void findAndReplaceInFile(File file) {
        boolean isJava = file.getName().endsWith(".java");
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            boolean hasTimber = false;
            boolean hasAddedImport = false;
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }

                // look for the following lines to convert:
                //      import timber.log.Timber;
                //      Timber.d("Unable to open log file", e);
                //      Timber.e("Failure loading", e);
                //      Timber.i("Building GoogleApiClient");

                if (line.startsWith(TIMBER_IMPORT)) {
                    hasTimber = true;
                    System.out.println("> converting " + file.getName());
                    // replace with:
                    //      import org.slf4j.Logger;
                    //      import org.slf4j.LoggerFactory;
                    sb.append(LOG_IMPORT1);
                    sb.append(LOG_IMPORT2);
                } else if (hasTimber && !hasAddedImport && line.contains("class ")) {
                    // NOTE: need to find first instance of "{"
                    while (true) {
                        sb.append(line);
                        sb.append('\n');
                        if (line.contains("{")) {
                            // replace with:
                            //      private static final Logger log = LoggerFactory.getLogger(BaseActivity.class);
                            // or:
                            //      private val log: Logger = LoggerFactory.getLogger(this::class.java)
                            if (isJava) {
                                sb.append("    private static final Logger log = LoggerFactory.getLogger(");
                                sb.append(getFileNameOnly(file));
                                sb.append(".class);\n");
                            } else {
                                sb.append("    private val log: Logger = LoggerFactory.getLogger(this::class.java)\n");
                            }
                            hasAddedImport = true;
                            break;
                        } else {
                            line = br.readLine();
                        }
                    }
                } else if (line.contains(TIMBER_D)) {
                    // replace with:
                    //      log.debug(
                    String replaced = line.replace(TIMBER_D, LOG_DEBUG);
                    sb.append(replaced).append('\n');
                } else if (line.contains(TIMBER_I)) {
                    String replaced = line.replace(TIMBER_I, LOG_INFO);
                    sb.append(replaced).append('\n');
                } else if (line.contains(TIMBER_W)) {
                    String replaced = line.replace(TIMBER_W, LOG_WARN);
                    sb.append(replaced).append('\n');
                } else if (line.contains(TIMBER_E)) {
                    String replaced = line.replace(TIMBER_E, LOG_ERROR);
                    sb.append(replaced).append('\n');
                } else {
                    sb.append(line);
                    sb.append('\n');
                }
            }

            if (hasTimber) {
                // replace old file with new one
                BufferedWriter bw = new BufferedWriter(new FileWriter(file));
                bw.write(sb.toString());
                bw.close();
            }
        } catch (Exception e) {
            System.out.println("findAndReplaceInFile: Error reading file: " + file + ", " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static String getFileNameOnly(File file) {
        String fileName = file.getName();
        int pos = fileName.indexOf('.');
        return fileName.substring(0, pos);
    }

}
