package util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URLDecoder;

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

}


