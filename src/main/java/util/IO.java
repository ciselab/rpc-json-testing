package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

public final class IO {

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
    public static void writeFile(List<String> information, String fileName) throws IOException {
        FileWriter writer = new java.io.FileWriter(fileName, true);
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
    public static void writeFile(String information, String fileName) throws IOException {
        FileWriter writer = new java.io.FileWriter(fileName, true);
        writer.write(information + System.lineSeparator());
        writer.close();
    }

}


