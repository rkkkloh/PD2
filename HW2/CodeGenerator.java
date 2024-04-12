import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class CodeGenerator {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("請輸入mermaid檔案名稱");           
        }
        else {
            String fileName = args[0];
            String mermaidCode = "";

            DIYFileReader mermaidCodeReader = new DIYFileReader();
            mermaidCode = mermaidCodeReader.read(fileName);
            List<String> mermaidCodeArrayList = Parser.splitByClass(mermaidCode);

            List<String> classContent = new ArrayList<>();
            List<String> className = new ArrayList<>();
            String currentClass = null;
            String content = "";
            Boolean curleyBracket = false;
            Boolean containClass = false; 
            
            //計算mermaidcode有多少個class
            for (String classCounter : mermaidCodeArrayList) {
                classCounter = classCounter.trim().replaceAll("\\s+"," ");
                if (classCounter.trim().contains("class ")){
                    if (classCounter.trim().endsWith("{")) {
                        for (String classCheck : className) {
                            //檢查class有沒有重複
                            if (classCounter.trim().replaceAll(" ","").substring("class".length(),classCounter.replaceAll(" ","").indexOf("{")).equals(classCheck)) {
                                containClass = true;
                                break;
                            } 
                        } 
                        //沒重複才加
                        if (!containClass) {
                            className.add(classCounter.trim().replaceAll(" ","").substring("class".length(),classCounter.replaceAll(" ","").indexOf("{")));
                            //System.out.println(classCounter.trim().replaceAll(" ","").substring("class".length(),classCounter.replaceAll(" ","").indexOf("{")));
                        }
                        containClass = false;
                    } else { //檢查沒有{的class
                        for (String classCheck : className) {
                            if (classCounter.trim().replaceAll(" ","").substring("class".length()).equals(classCheck)) {
                                containClass = true;
                                break;
                            }
                        }
                        if (!containClass) {
                            className.add(classCounter.trim().substring("class ".length()));
                            //System.out.println(classCounter.trim().substring("class ".length()));
                        }
                        containClass = false;
                    }
                }
            }

            for (String loopingClass : className) {
                //System.out.print(mermaidCode);
                for (String line : mermaidCodeArrayList) {
                    //先檢查宣告class的{語法 & 同時check current class

                    if (line.trim().endsWith("{") && line.replaceAll(" ","").substring("class".length(),line.replaceAll(" ","").indexOf("{")).equals(loopingClass)) {
                        currentClass = line.replaceAll(" ","").substring("class".length(),line.replaceAll(" ","").indexOf("{"));
                        //檢查之後有沒有重複宣告
                        for (String classCheck : classContent) {
                            if (("public class " + currentClass + " {\n").equals(classCheck)) {
                                containClass = true;
                                break;
                            } 
                        } 
                        if (!containClass) {
                            classContent.add("public class " + currentClass + " {\n");
                            //System.out.println("class public " + currentClass + " {");
                        }
                        containClass = false;
                        //確定括號開始
                        curleyBracket = true;

                    } else if (line.trim().startsWith("}") || line.contains("}")) {
                    //} else if (line.contains("}")) {
                        curleyBracket = false;
                    }

                    if (line.equals("classDiagram")) {
                        continue;
                    } else if (curleyBracket && currentClass.equals(loopingClass)) {
                        line = line.trim().replaceAll("\\s+"," ");

                        if(line.contains("[ ]")) {
                            line = line.replace("[ ]","[]");
                        }

                        if (currentClass.equals(loopingClass)) {
                            if (line.contains("(") && line.contains(")")) {

                                //檢查+後空格
                                if (line.contains("+ ")) {
                                    line = line.replace("+ ","+");
                                }
                                
                                //檢查-後空格
                                if (line.contains("- ")) {
                                    line = line.replace("- ","-");
                                }

                                //檢查無傳入參數有沒有亂給空白
                                if(line.contains("( )")) {
                                    line = line.replace("( )","()");
                                }

                                //無傳入參數
                                if (line.contains("()")) {
                                    String accessModifier = line.startsWith("+") ? "public" : "private";
                                    String returnType = "void";

                                    //檢查左括號前有沒有空白
                                    if (line.contains(" (")) {
                                        line = line.replace(" (","(");
                                    }

                                    //把右括號後設成有空白
                                    if (line.contains(")")) {
                                        line = line.replace(")",") ");
                                        line = line.replaceAll("\\s+"," ");
                                        //returnType = line.trim().substring(line.trim().lastIndexOf(" ")+1);
                                        
                                    }

                                    //檢查有沒有returnType
                                    if (line.trim().indexOf(")") < line.trim().lastIndexOf(" ")) {
                                        returnType = line.trim().substring(line.lastIndexOf(" ")+1);
                                    }
                                    String methodName = line.substring(1,line.indexOf("("));

                                    String returnValue = mermaidCodeProcessor.getDefaultReturnValue(returnType);
                                    
                                    //無傳入參數method和getter的處理

                                    if (methodName.contains("get")) {
                                        String turnFirstChar = methodName.substring(3,4).toLowerCase();
                                        returnValue = turnFirstChar + methodName.substring(4);
                                        classContent.add("    " + accessModifier + " " + returnType + " " + methodName + "()" + " {\n" 
                                        + "        return " + returnValue + ";\n" + "    }\n");
                                        //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + "()" + " {\n" 
                                        //+ "        return " + returnValue + ";\n" + "    }");
                                    } else {
                                        if (returnValue != "void") {
                                            classContent.add("    " + accessModifier + " " + returnType + " " + methodName + "()" + " {return " 
                                            + returnValue + ";}\n");
                                            //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + "()" + " {return " 
                                            //+ returnValue + ";}");
                                        } else {
                                            classContent.add("    " + accessModifier + " " + returnType + " " + methodName + "()" + " {;}\n");
                                            //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + "()" + " {;}");
                                        }
                                    }
                                } else { //讀到有傳入參數的method
                                    
                                    String accessModifier = line.startsWith("+") ? "public" : "private";
                                    String returnType = "void";

                                    if(line.contains("[ ]")) {
                                        line = line.replace("[ ]","[]");
                                    }

                                    //檢查+後空格
                                    if (line.contains("+ ")) {
                                        line = line.replace("+ ","+");
                                    }
                                    
                                    //檢查-後空格
                                    if (line.contains("- ")) {
                                        line = line.replace("- ","-");
                                    }

                                    //檢查右括號前有沒有空白
                                    if (line.contains(" )")) {
                                        line = line.trim().replace(" )",")");
                                    }
                                    
                                    //把右括號後設成有空白
                                    if (line.contains(")")) {
                                        line = line.replace(")",") ");
                                        line = line.replaceAll("\\s+"," ");
                                        //returnType = line.trim().substring(line.trim().lastIndexOf(" ")+1);
                                        
                                    }

                                    //檢查左括號前有沒有空白
                                    if (line.contains(" (")) {
                                        line = line.replace(" (","(");
                                    }

                                    //檢查左括號後有沒有空白
                                    if (line.contains("( ")) {
                                        line = line.replace("( ","(");
                                    }

                                    //檢查有沒有return type
                                    if (line.trim().indexOf(")") < line.trim().lastIndexOf(" ")) {
                                        returnType = line.trim().substring(line.lastIndexOf(" ")+1);
                                    }
                                    String methodName = line.substring(1,line.indexOf("("));

                                    String returnValue = mermaidCodeProcessor.getDefaultReturnValue(returnType);

                                    //處理逗號前有空白
                                    if (line.contains(" ,")) {
                                        line = line.replaceAll(" ,",",");
                                    }

                                    //處理逗號後沒空白
                                    if (line.contains(",")) {
                                        line = line.replaceAll(",",", ");
                                        line = line.replaceAll("\\s+"," ");
                                    }
                                    
                                    String arguments = line.substring(line.indexOf("("), line.indexOf(")") + 1);
                                    
                                    //無傳入參數method和getter的處理

                                    if (methodName.contains("set")) {
                                        String turnFirstChar = methodName.substring(3,4).toLowerCase();
                                        String setValue = turnFirstChar + methodName.substring(4);
                                        String value = arguments.substring(arguments.indexOf(" ")+1,arguments.indexOf(")"));
                                        classContent.add("    " + accessModifier + " " + returnType + " " + methodName + arguments + " {\n" 
                                        + "        this." + setValue + " = " + value + ";\n" + "    }\n");
                                        //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + arguments + " {\n" 
                                        //+ "        this." + setValue + " = " + setValue + ";\n" + "    }");
                                    } else {
                                        if (returnValue != "void") {
                                            classContent.add("    " + accessModifier + " " + returnType + " " + methodName + arguments + " {return " 
                                            + returnValue + ";}\n");
                                            //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + arguments + " {return " 
                                            //+ returnValue + ";}");
                                        } else {
                                            classContent.add("    " + accessModifier + " " + returnType + " " + methodName + arguments + " {;}\n");
                                            //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + arguments + " {;}");
                                        }
                                    }
                                    
                                    //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + arguments + " ;");
                                }
                                //String methodName = parts[1].trim().split("\\s+")[0].substring(1,parts[1].trim().split("\\s+")[0].indexOf("("));
                                //classContent.add("    " + accessModifier + " " + returnType + " " + methodName +"()" + " ;");
                                //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + ";");
                            } else if (line.contains("{") || line.trim().length() == 0){
                                continue;
                            } else { //屬性
                                //檢查+後空格
                                if (line.contains("+ ")) {
                                    line = line.replace("+ ","+");
                                }
                                
                                //檢查-後空格
                                if (line.contains("- ")) {
                                    line = line.replace("- ","-");
                                }

                                String accessModifier = line.startsWith("+") ? "public" : "private";
                                String dataType = line.split("\\s+")[0].substring(1);
                                String variableName = line.split("\\s+")[1];
                                //System.out.println("    " + accessModifier + " " + dataType + " " + variableName + ";");
                                classContent.add("    " + accessModifier + " " + dataType + " " + variableName + ";\n");
                                //System.out.println(accessModifier);
                                //System.out.println(dataType);
                            }
                        }                       
                    } else if (line.trim().replaceAll("\\s+"," ").startsWith("class ") && (line.trim().replaceAll("\\s+"," ").substring("class ".length())).equals(loopingClass)) {
                        line = line.trim().replaceAll("\\s+"," ");
                        currentClass = line.trim().substring("class ".length());
                        classContent.add("public class " + currentClass + " {\n");
                        //System.out.println("public class " + currentClass + " {");
                        //System.out.println(classContent);
                    } else if (line.contains(":") && (line.split(":")[0].trim().equals(loopingClass))) { //檢查currentClass
                        line = line.trim().replaceAll("\\s+"," ");

                        if(line.contains("[ ]")) {
                            line = line.replace("[ ]","[]");
                        }

                        //讀到method()
                        if (line.contains("(") && line.contains(")")) {
                            
                            String[] parts = line.split(":");
                            //String member = parts[0].trim();

                            //檢查無傳入參數有沒有亂給空白
                            if(line.contains("( )")) {
                                line = line.replace("( )","()");
                                parts[1] = parts[1].replace("( )","()");
                                //System.out.println(parts[1]);
                            }

                            //無傳入參數
                            if (parts[1].contains("()")) {
                                String accessModifier = parts[1].trim().startsWith("+") ? "public" : "private";
                                String returnType = "void";

                                //檢查+後空格
                                if (parts[1].contains("+ ")) {
                                    parts[1] = parts[1].replace("+ ","+");
                                }
                                
                                //檢查-後空格
                                if (parts[1].contains("- ")) {
                                    parts[1] = parts[1].replace("- ","-");
                                }

                                //把右括號設成有空白
                                if (parts[1].contains(")")) {
                                    parts[1] = parts[1].replace(")",") ");
                                    parts[1] = parts[1].replaceAll("\\s+"," ");
                                    //returnType = parts[1].trim().substring(parts[1].trim().lastIndexOf(" ")+1);
                                    
                                }
                                
                                //檢查有沒有return type
                                if (parts[1].trim().indexOf(")") < parts[1].trim().lastIndexOf(" ")) {
                                    returnType = parts[1].trim().substring(parts[1].trim().lastIndexOf(" ")+1);
                                    //System.out.println(parts[1]);
                                    //System.out.println(returnType);
                                }

                                //檢查左括號前有沒有空白
                                if (parts[1].contains(" (")) {
                                    parts[1] = parts[1].replace(" (","(");
                                }

                                //檢查有沒有returnType
                                if (parts[1].trim().indexOf(")") < parts[1].trim().lastIndexOf(" ")) {
                                    returnType = parts[1].trim().substring(parts[1].trim().lastIndexOf(" ")+1);
                                }
                                String methodName = parts[1].trim().substring(1,parts[1].trim().indexOf("("));
                                //System.out.println(methodName);

                                String returnValue = mermaidCodeProcessor.getDefaultReturnValue(returnType);
                                
                                //無傳入參數method和getter的處理

                                if (methodName.contains("get")) {
                                    String turnFirstChar = methodName.substring(3,4).toLowerCase();
                                    returnValue = turnFirstChar + methodName.substring(4);
                                    classContent.add("    " + accessModifier + " " + returnType + " " + methodName + "()" + " {\n" 
                                    + "        return " + returnValue + ";\n" + "    }\n");
                                    //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + "()" + " {\n" 
                                    //+ "        return " + returnValue + ";\n" + "    }");
                                } else {
                                    if (returnValue != "void") {
                                        classContent.add("    " + accessModifier + " " + returnType + " " + methodName + "()" + " {return " 
                                        + returnValue + ";}\n");
                                        //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + "()" + " {return " 
                                        //+ returnValue + ";}");
                                    } else {
                                        classContent.add("    " + accessModifier + " " + returnType + " " + methodName + "()" + " {;}\n");
                                        //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + "()" + " {;}");
                                    }
                                }
                            } else { //讀到有傳入參數的method
                                
                                String accessModifier = parts[1].trim().startsWith("+") ? "public" : "private";
                                String returnType = "void";

                                //檢查+後空格
                                if (parts[1].contains("+ ")) {
                                    parts[1] = parts[1].replace("+ ","+");
                                }
                                
                                //檢查-後空格
                                if (parts[1].contains("- ")) {
                                    parts[1] = parts[1].replace("- ","-");
                                }

                                //檢查右括號前有沒有空白
                                if (parts[1].contains(" )")) {
                                    parts[1] = parts[1].trim().replace(" )",")");
                                }
                                
                                //把右括號設成有空白
                                if (parts[1].contains(")")) {
                                    parts[1] = parts[1].replace(")",") ");
                                    parts[1] = parts[1].replaceAll("\\s+"," ");
                                    //System.out.println(parts[1]);
                                    //System.out.println(parts[1].lastIndexOf(" "));
                                    //System.out.println(parts[1].indexOf(")"));
                                    //returnType = parts[1].trim().substring(parts[1].trim().lastIndexOf(" ")+1);
                                    
                                }
                                
                                //檢查有沒有return type
                                if (parts[1].trim().indexOf(")") < parts[1].trim().lastIndexOf(" ")) {
                                    returnType = parts[1].trim().substring(parts[1].trim().lastIndexOf(" ")+1);
                                    //System.out.println(parts[1]);
                                    //System.out.println(returnType);
                                }

                                //檢查左括號前有沒有空白
                                if (parts[1].contains(" (")) {
                                    parts[1] = parts[1].replace(" (","(");
                                }

                                //檢查左括號後有沒有空白
                                if (parts[1].contains("( ")) {
                                    parts[1] = parts[1].replace("( ","(");
                                }
                                
                                String methodName = parts[1].trim().substring(1,parts[1].trim().indexOf("("));

                                String returnValue = mermaidCodeProcessor.getDefaultReturnValue(returnType);

                                //處理逗號前有空白
                                if (parts[1].contains(" ,")) {
                                    parts[1] = parts[1].replaceAll(" ,",",");
                                }

                                //處理逗號後沒空白
                                if (parts[1].contains(",")) {
                                    parts[1] = parts[1].replaceAll(",",", ");
                                    parts[1] = parts[1].replaceAll("\\s+"," ");
                                }

                                String arguments = parts[1].trim().substring(parts[1].trim().indexOf("("), parts[1].trim().indexOf(")") + 1);
                                
                                //無傳入參數method和getter的處理

                                if (methodName.contains("set")) {
                                    String turnFirstChar = methodName.substring(3,4).toLowerCase();
                                    String setValue = turnFirstChar + methodName.substring(4);
                                    String value = arguments.substring(arguments.indexOf(" ")+1,arguments.indexOf(")"));
                                    classContent.add("    " + accessModifier + " " + returnType + " " + methodName + arguments + " {\n" 
                                    + "        this." + setValue + " = " + value + ";\n" + "    }\n");
                                    //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + arguments + " {\n" 
                                    //+ "        this." + setValue + " = " + setValue + ";\n" + "    }");
                                } else {
                                    if (returnValue != "void") {
                                        classContent.add("    " + accessModifier + " " + returnType + " " + methodName + arguments + " {return " 
                                        + returnValue + ";}\n");
                                        //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + arguments + " {return " 
                                        //+ returnValue + ";}");
                                    } else {
                                        classContent.add("    " + accessModifier + " " + returnType + " " + methodName + arguments + " {;}\n");
                                        //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + arguments + " {;}");
                                    }
                                }
                                
                                //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + arguments + " ;");
                            }
                            //String methodName = parts[1].trim().split("\\s+")[0].substring(1,parts[1].trim().split("\\s+")[0].indexOf("("));
                            //classContent.add("    " + accessModifier + " " + returnType + " " + methodName +"()" + " ;");
                            //System.out.println("    " + accessModifier + " " + returnType + " " + methodName + ";");
                        } else if (line.trim().length() == 0) {
                            continue;
                        } else {
                            String[] parts = line.split(":");

                            if(line.contains("[ ]")) {
                                line = line.replace("[ ]","[]");
                            }

                            //檢查+後空格
                            if (parts[1].contains("+ ")) {
                                parts[1] = parts[1].replace("+ ","+");
                            }
                            
                            //檢查-後空格
                            if (parts[1].contains("- ")) {
                                parts[1] = parts[1].replace("- ","-");
                            }
                            
                            String accessModifier = parts[1].trim().startsWith("+") ? "public" : "private";
                            String dataType = parts[1].trim().split("\\s+")[0].substring(1);
                            String variableName = parts[1].trim().split("\\s+")[1];
                            //System.out.println("    " + accessModifier + " " + dataType + " " + variableName + ";");
                            classContent.add("    " + accessModifier + " " + dataType + " " + variableName + ";\n");
                            //System.out.println(accessModifier);
                            //System.out.println(dataType);
                        }
                    }
                }
                classContent.add("}\n");
                //System.out.println("}");
                for (String parseCode : classContent){
                    content += (parseCode);
                }

                classContent.clear();
                DIYFileWriter.writeFile(loopingClass,content);
                content = "";
                
            }
	    //DIYFileWriter.writeFile("mytc4",mermaidCode);
       }
    }
}

class mermaidCodeProcessor {
    public static String getDefaultReturnValue(String returnType) {
        switch (returnType) {
            case "int":
                return "0";
            case "String":
                return "\"\"";
            case "boolean":
                return "false";
            default:
                return "void";
        }
    }
}

class DIYFileReader {
    public String read(String fileName) {

        if (fileName == null) {
            System.err.println("檔名為空");
            return null;
        }
        String mermaidCode = "";
        try {
            mermaidCode = Files.readString(Paths.get(fileName));
        }
        catch (IOException e) {
            System.err.println("無法讀取文件 " + fileName);
            e.printStackTrace();
            return null;
        }

	return mermaidCode;
    }
}

class Parser {
    public static List<String> splitByClass(String input) {
        BufferedReader lineReader = new BufferedReader(new StringReader(input));
	    List<String> mermaidCodeArrayList = new ArrayList<>(); 
	    String line;

        try {
            while ((line = lineReader.readLine()) != null) {
                if (line.trim().contains("{")) {
                    if (line.trim().indexOf("{")+1 != line.trim().length()) {
                        if (line.trim().contains("}")) {
                            mermaidCodeArrayList.add(line.trim().substring(0,line.trim().indexOf("{")+1));
                            mermaidCodeArrayList.add(line.trim().substring(line.trim().indexOf("{")+1,line.trim().indexOf("}")));
                            mermaidCodeArrayList.add("}");
                        } else {
                            mermaidCodeArrayList.add(line.trim().substring(0,line.trim().indexOf("{")+1));
                            mermaidCodeArrayList.add(line.trim().substring(line.trim().indexOf("{")+1));
                        }
                    } else {
                        mermaidCodeArrayList.add(line);
                    }
                } else if (line.trim().contains("}") && line.trim().indexOf("}") != 0) {
                    mermaidCodeArrayList.add(line.trim().substring(0,line.trim().indexOf("}")));
                    mermaidCodeArrayList.add("}");
                } else {
                    mermaidCodeArrayList.add(line);
                }
                //mermaidCodeArrayList.add(line);
            }
                lineReader.close();
            
            }
            catch (IOException e) {
            e.printStackTrace();
            }
        return mermaidCodeArrayList;	
    }
}

class DIYFileWriter {
    public static void writeFile(String filename,String content) {
    try {
	    File file = new File(filename + ".java");
            if (!file.exists()) {
                file.createNewFile();
            }
        
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
                bw.write(content);
	    }
	} catch (IOException e) {
            e.printStackTrace();
	}
	return;
    }
}