import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.FileWriter;

public class TFIDFCalculator {
    public static void main(String args[]) {
        List<List<String>> docs = new ArrayList<>();
        String[] stringArgumentArray = null;
        String[] documentIndexArray = null;
        String[] documentArray;
        Trie singleDocRoot;
        List<Trie> wholeDocRoot = new ArrayList<>();
        double tfIdfValue;
        Trie wholeIdfRoot = new Trie();
        

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt"));
            writer.write("");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[1]));
            stringArgumentArray = reader.readLine().split("\\s+");
            documentIndexArray = reader.readLine().split("\\s+");
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            BufferedReader reader= new BufferedReader(new FileReader("docs.txt"));
            String documentContent = "";
            int lineCount = 0;
            String line;
            List<String> addedString = new ArrayList<>();
            boolean contain = true;

            while ((line = reader.readLine()) != null) {
                if (lineCount%5 == 0 && lineCount != 0) {
                    documentContent = documentContent.toLowerCase().replaceAll("[^a-z]+"," ").trim();
                    docs.add(new ArrayList<>(Arrays.asList(documentContent.split("\\s+"))));
                    documentArray = documentContent.split("\\s+");

                    singleDocRoot = new Trie();
                    for (String word : documentArray) {
                        singleDocRoot.insert(word);

                        for (String token : addedString) {
                            if ((word.equals(token))) {
                                contain = false;
                                break;
                            }
                        }
                        if (contain) {
                            addedString.add(word);
                            wholeIdfRoot.insert(word);
                        }
                        contain = true;
                    }
                    wholeDocRoot.add(singleDocRoot);
                    documentContent = "";
                    addedString.clear();
                }
                documentContent += line + " ";
                lineCount++;
            }
            documentContent = documentContent.toLowerCase().replaceAll("[^a-z]+"," ").trim();
            docs.add(new ArrayList<>(Arrays.asList(documentContent.split(" "))));
            documentArray = documentContent.trim().split(" ");
            singleDocRoot = new Trie();
            for (String word : documentArray) {
                singleDocRoot.insert(word);
                for (String token : addedString) {
                    if ((word.equals(token))) {
                        contain = false;
                        break;
                    }
                }
                if (contain) {
                    addedString.add(word);
                    wholeIdfRoot.insert(word);
                }
                contain = true;

            }
            wholeDocRoot.add(singleDocRoot);
            reader.close();

        } catch (IOException e) {

            e.printStackTrace();
        
        }

        try {
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < stringArgumentArray.length; i++) {
                tfIdfValue = tfIdfCalculate(docs.get(Integer.parseInt(documentIndexArray[i])),docs,stringArgumentArray[i],wholeDocRoot.get(Integer.parseInt(documentIndexArray[i])), wholeIdfRoot);
                result.append(String.format("%.5f", tfIdfValue)).append(" ");
            }

            fileOutput(result.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static double getTermCount(String word, TrieNode root) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return 0;
            }
        }
        return node.count;
    }

    public static double tf(List<String> doc, String term, Trie currentRoot) {
        double number_term_in_doc = getTermCount(term, currentRoot.root);

        return number_term_in_doc / doc.size();
    }

    public static double idf(List<List<String>> docs, String term, Trie wholeIdfRoot) {
        int number_doc_contain_term = 0;

        if (wholeIdfRoot.search(term)) {
            number_doc_contain_term = (int)getTermCount(term, wholeIdfRoot.root);
        }
        
        

        return Math.log((double)docs.size() / number_doc_contain_term);
    }
    
    public static double tfIdfCalculate(List<String> doc, List<List<String>> docs, String term,Trie currentRoot, Trie wholeIdfRoot) {
        return tf(doc, term, currentRoot) * idf(docs, term, wholeIdfRoot);
    }

    public static void fileOutput(String result) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt",true));
        writer.write(result);
        writer.close();
    }
}

class TrieNode {
    TrieNode[] children = new TrieNode[26];
    boolean isEndOfWord = false;
    int count = 0;
}

class Trie {
    TrieNode root = new TrieNode();

    public void insert(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c - 'a'] == null) {
                node.children[c - 'a'] = new TrieNode();
            }
            node = node.children[c - 'a'];
        }
        node.isEndOfWord = true;
        node.count++;
    }

    public boolean search(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return false;
            }
        }
        return node.isEndOfWord;
    }
}