import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

public class Sign {

    private char[][] signDesign = new char[25][10];
    private String[][] signAsString = new String[25][10];
    private String signString;

    Sign(String filename) throws Exception {
        this.signString = readSignFile(filename);
        stringToArray((this.signString));
    }

    private String readSignFile(String filename) throws Exception {
        BufferedReader reader = Files.newBufferedReader(Paths.get(filename));
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
    }

    private void stringToArray(String levelString) {
        String[] splitString = levelString.split("\r\n", 0);
        for (int i = 0; i < splitString.length; i++)
            signAsString[i] = splitString[i].split(";", 0);
        for (int i = 0; i < signAsString.length; i++)
            for (int j = 0; j < signAsString[i].length; j++)
                signDesign[i][j] = signAsString[i][j].charAt(0);
    }

    public char[][] getSignDesign() {
        return signDesign;
    }

}
