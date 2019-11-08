package main;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;


public class Uncomment {
    static int lineNum = 0;
    private static Logger logger = Logger.getLogger(Uncomment.class.getName());
    public  void makeUncommentXml(String xmlfilepath,String value) {
        try {
            String key = "";
            FileReader file = new FileReader(xmlfilepath);
            BufferedReader reader = new BufferedReader(file);
            String line = reader.readLine();
            //removing the comment beginning  block  <--
            while ((line = reader.readLine()) != null && (line.indexOf(value) == -1)) {
                key += line;
                lineNum++;
            }
            if(lineNum>0) {
                logger.info("Uncommenting a tag "+line);
                //remove the closing the block -->
                Path path = Paths.get(xmlfilepath);
                List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
                if (line.contains("--")) {
                    lines.set(lineNum + 1, '<' + value + '>');
                    Files.write(path, lines, StandardCharsets.UTF_8);
                    while ((line = reader.readLine()) != null && line.indexOf("-->") == -1) {
                        key += line;
                        lineNum++;
                    }
                    //Get the count of > symble
                    if (lineNum < lines.size() - 2) {
                        int charCount = line.length() - line.replaceAll("\\>", "").length();
                        if (charCount < 2) {
                            //  end with when only -->
                            line = line.replaceAll("-->", "");
                            lines.set(lineNum + 2, line + ">");
                            Files.write(path, lines, StandardCharsets.UTF_8);
                        } else {
                            //  end with when the >-->
                            line = line.replaceAll("-->", "");
                            lines.set(lineNum + 2, line);
                            Files.write(path, lines, StandardCharsets.UTF_8);

                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            logger.info("An error is occurred while Uncommenting ");
        }
    }

}