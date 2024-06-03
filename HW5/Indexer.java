import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.Serializable;

public class Indexer implements Serializable {
    StringBuilder documentContent = new StringBuilder();
    public Indexer(String corpusFile) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(corpusFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.toLowerCase().replaceAll("[^a-z]+", " ").trim();
                this.documentContent.append(line).append("\n");
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}