import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.io.BufferedWriter;

public class TFIDFSearch {
    public static void main(String[] args) {
        String corpusIndex = args[0].substring("corpus".length());
        List<List<String>> docs = new ArrayList<>();
        String[] stringArgumentArray = null;
        String[] documentArray;
        double tfIdfValue = 0;
        Trie wholeIdfRoot = new Trie();
        try {
            FileInputStream fis = new FileInputStream("corpus" + corpusIndex + ".ser");
            ObjectInputStream ois = new ObjectInputStream(fis);

            Indexer indexer = (Indexer) ois.readObject();
            ois.close();
            fis.close();
            
            BufferedReader reader = new BufferedReader(new StringReader(indexer.documentContent.toString()));
            String documentContent = "";
            int lineCount = 0;
            String line;

            while ((line = reader.readLine()) != null) {
                if (lineCount%5 == 0 && lineCount != 0) {
                    docs.add(new ArrayList<>(Arrays.asList(documentContent.split(" "))));
                    documentArray = documentContent.split(" ");

                    for (String word : documentArray) {
                        wholeIdfRoot.insert(word,lineCount/5 - 1);
                    }
                    documentContent = "";
                }
                documentContent += line + " ";
                lineCount++;
            }
            docs.add(new ArrayList<>(Arrays.asList(documentContent.split(" "))));
            documentArray = documentContent.split(" ");

            for (String word : documentArray) {
                wholeIdfRoot.insert(word,lineCount/5 - 1);
            }
            reader.close();
            indexer.documentContent.setLength(0);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException c) {
            c.printStackTrace();
        }

        try {
            BufferedReader argumentReader = new BufferedReader(new FileReader(args[1]));
            int requestQuantity = Integer.parseInt(argumentReader.readLine());
            String argument;
            List<TfIdf> tfIdfList = new ArrayList<>();
            double totalTfIdfValue = 0;
            Map<String,Double> andHashMap = new HashMap<>();
            Map<String,Double> orHashMap = new HashMap<>();
            List<HashSet<Integer>> tokenSetList = new ArrayList<>();
            boolean contain = true;
            HashSet<Integer> orTokenIDSet = new HashSet<>();
            HashSet<Integer> singleTokenIDSet = new HashSet<>();

            while ((argument = argumentReader.readLine()) != null) {                
                if (argument.contains("AND")) {
                    stringArgumentArray = argument.replaceAll("[ AND ]+", " ").split(" ");

                    for (String token : stringArgumentArray) {
                        if (wholeIdfRoot.search(token)) {
                            HashSet<Integer> tokenIDSet = new HashSet<>();
                            for (int j = 0;j < wholeIdfRoot.getDocID(token).size(); j++) {
                                tokenIDSet.add(wholeIdfRoot.getDocID(token).get(j));
                            }
                            tokenSetList.add(tokenIDSet);
                        } else {
                            break;
                        }
                    }

                    if (stringArgumentArray.length == tokenSetList.size()) {
                        HashSet<Integer> intersection = new HashSet<>(tokenSetList.get(0));

                        for (int i = 0; i < tokenSetList.size(); i++) {
                            intersection.retainAll(tokenSetList.get(i));
                        }

                        for (Integer docID : intersection) {
                            for (String token : stringArgumentArray) {
                                if (andHashMap.containsKey(token)) {
                                    tfIdfValue = andHashMap.get(token);
                                    totalTfIdfValue += tfIdfValue;
                                } else {
                                    tfIdfValue = tfIdfCalculate(docs.get(docID), docs, token, getRepeatTermCount(token,wholeIdfRoot, docID), wholeIdfRoot);
                                    totalTfIdfValue += tfIdfValue;
                                    andHashMap.put(token,tfIdfValue);
                                }
                            }
                            tfIdfList.add(new TfIdf(totalTfIdfValue, docID));
                            andHashMap.clear();
                            totalTfIdfValue = 0;
                        }
                    }
		            tokenSetList.clear();
                } else if (argument.contains("OR")) {
                    stringArgumentArray = argument.replaceAll("[ OR ]+", " ").split(" ");
                    
                    for (String token : stringArgumentArray) {
                        if (wholeIdfRoot.search(token)) {
                            for (int j = 0;j < wholeIdfRoot.getDocID(token).size(); j++) {
                                orTokenIDSet.add(wholeIdfRoot.getDocID(token).get(j));
                            }
                        }
                    }
                    if (orTokenIDSet.size() != 0) {
                        for (Integer docID : orTokenIDSet) {
                            for (String token : stringArgumentArray) {
                                if (orHashMap.containsKey(token)) {
                                    tfIdfValue = orHashMap.get(token);
                                    totalTfIdfValue += tfIdfValue;
                                } else {
                                    tfIdfValue = tfIdfCalculate(docs.get(docID), docs, token, getRepeatTermCount(token, wholeIdfRoot, docID), wholeIdfRoot);
                                    totalTfIdfValue += tfIdfValue;
                                    orHashMap.put(token,tfIdfValue);
                                }
                            }
                            tfIdfList.add(new TfIdf(totalTfIdfValue, docID));
                            orHashMap.clear();
                            totalTfIdfValue = 0;
                        }
                    }
                    orTokenIDSet.clear();
                } else {
                    if (wholeIdfRoot.search(argument)) {
                        for (int j = 0;j < wholeIdfRoot.getDocID(argument).size(); j++) {
                            singleTokenIDSet.add(wholeIdfRoot.getDocID(argument).get(j));
                        }
                    } else {
                        contain = false;
                    }

                    if (contain) {
                        for (Integer docID : singleTokenIDSet) {
                            tfIdfValue = tfIdfCalculate(docs.get(docID), docs, argument, getRepeatTermCount(argument,wholeIdfRoot, docID), wholeIdfRoot);
                            tfIdfList.add(new TfIdf(tfIdfValue, docID));
                            tfIdfValue = 0;
                        }
                    }
                    singleTokenIDSet.clear();
                }

                Collections.sort(tfIdfList, (a, b) -> {
                    if (Double.compare(b.tfIdfValue, a.tfIdfValue) == 0) {
                        return Integer.compare(a.index, b.index);
                    } else {
                        return Double.compare(b.tfIdfValue, a.tfIdfValue);
                    }
                });
    
                BufferedWriter writer = new BufferedWriter(new FileWriter("output.txt", true));
                
                
                if (requestQuantity < tfIdfList.size()) {
                    for (int i = 0; i < requestQuantity; i++) {
                        writer.write(tfIdfList.get(i).index + " ");
                    }
                } else {
                    for (TfIdf info : tfIdfList) {
                        writer.write(info.index + " ");
                    }
    
                    for (int i = 0; i < (requestQuantity-tfIdfList.size()); i++) {
                        writer.write("-1 ");
                    }
                }
                writer.write("\n");
                writer.close();
                tfIdfList.clear();

            }
            
            argumentReader.close();

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

    public static double getRepeatTermCount(String word, Trie root, Integer ID) {
        TrieNode node = root.root;
        
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
            if (node == null) {
                return 0;
            }
        }
        if (node.termPerDoc.get(ID) != null) {
            return node.termPerDoc.get(ID);
        } else {
            return 0;
        }
    }

    public static double tf(List<String> doc, String term, double termCountInDoc) {
        double number_term_in_doc = termCountInDoc;

        return number_term_in_doc / doc.size();
    }

    public static double idf(List<List<String>> docs, String term, Trie wholeIdfRoot) {
        int number_doc_contain_term = 0;

        if (wholeIdfRoot.search(term)) {
            number_doc_contain_term = (int)getTermCount(term, wholeIdfRoot.root);
        }

        if (number_doc_contain_term == 0) {
            return 0;
        }

        return Math.log((double)docs.size() / number_doc_contain_term);
    }
    
    public static double tfIdfCalculate(List<String> doc, List<List<String>> docs, String term, double termCountInDoc, Trie wholeIdfRoot) {
        return tf(doc, term, termCountInDoc) * idf(docs, term, wholeIdfRoot);
    }
}

class TrieNode implements Serializable {
    TrieNode[] children = new TrieNode[26];
    boolean isEndOfWord = false;
    int count = 0;
    List<Integer> documentIDList = new ArrayList<>();
    Map<Integer,Integer> termPerDoc = new HashMap<>();
}

class Trie implements Serializable {
    TrieNode root = new TrieNode();
    int index;

    public void insert(String word, Integer ID) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            if (node.children[c - 'a'] == null) {
                node.children[c - 'a'] = new TrieNode();
            }
            node = node.children[c - 'a'];
        }
        if (node.termPerDoc.get(ID) == null) {
            node.isEndOfWord = true;
            node.count++;
            node.documentIDList.add(ID);
        }
        node.termPerDoc.compute(ID, (key, value) -> (value == null) ? 1 : value + 1);
        
    }

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

    public List<Integer> getDocID(String word) {
        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node = node.children[c - 'a'];
        }
        return node.documentIDList;
    }
}

class TfIdf {
    double tfIdfValue;
    int index;

    public TfIdf(double tfIdfValue, int index) {
        this.tfIdfValue = tfIdfValue;
        this.index = index;
    }
}
