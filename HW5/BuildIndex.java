import java.io.File;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class BuildIndex {
    public static void main(String[] args) {
        File file = new File(args[0]);
        String corpusIndex = file.getName().substring("corpus".length(),file.getName().indexOf("."));
        try {
            FileOutputStream fos = new FileOutputStream("corpus" + corpusIndex + ".ser");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            Indexer indexer = new Indexer("corpus" + corpusIndex + ".txt");
            oos.writeObject(indexer);
            oos.close();
            fos.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}