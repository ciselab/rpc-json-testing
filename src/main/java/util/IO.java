package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

public final class IO {

    public static final String testDirectory = System.getProperty("user.dir") + "/output";

    /**
     * Read a file.
     * @param filepath
     * @return String representation of file data
     * @throws IOException
     */
    public static String readFile(String filepath) throws IOException {
        filepath = URLDecoder.decode(filepath, "UTF-8");
        StringBuilder data = new StringBuilder();

        String line;
        BufferedReader reader = new BufferedReader(new FileReader(filepath));
        while ((line = reader.readLine()) != null) {
            data.append(line);
        }
        reader.close();

        return data.toString();
    }

    /**
     * Write information to a file.
     * @param information
     * @throws IOException
     */
    public static void writeFile(List<String> information, String fileName, boolean append) throws IOException {
        FileWriter writer = new FileWriter(new File(testDirectory + "/" + fileName), append);

        for (int i = 0; i < information.size(); i++) {
            writer.write(information.get(i) + System.lineSeparator());
        }

        writer.close();
    }

    /**
     * Write information to a file.
     * @param information
     * @throws IOException
     */
    public static void writeFile(String information, String fileName, boolean append) throws IOException {
        FileWriter writer = new FileWriter(new File(testDirectory + File.separator + fileName), append);

        writer.write(information + System.lineSeparator());
        writer.close();
    }

}


