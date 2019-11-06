package dto;

import com.sun.xml.internal.ws.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Node;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Uncomment {
    static int lineNum = 0;
    public  void makeUncommentXml(String xmlfilepath,String value) {
        try {
            String key = "";
            FileReader file = new FileReader(xmlfilepath);
            BufferedReader reader = new BufferedReader(file);
            String line = reader.readLine();
            while ((line = reader.readLine()) != null && line.indexOf(value) == -1) {
                key += line;
                lineNum++;
            }
            Path path = Paths.get(xmlfilepath);
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            lines.set(lineNum + 1, value);
            Files.write(path, lines, StandardCharsets.UTF_8);
            while ((line = reader.readLine()) != null && line.indexOf("-->") == -1) {
                key += line;
                lineNum++;
            }
            //Get the count of
            int charCount = line.length() - line.replaceAll("\\>", "").length();
            if(charCount<2){
                //  end with when only -->
                line=line.replaceAll("-->","");
                System.out.println(line);
                lines.set(lineNum+2 ,line+">");
                Files.write(path, lines, StandardCharsets.UTF_8);
            }else{
                //  end with when the >-->
                line=line.replaceAll("-->","");
                System.out.println(line);
                lines.set(lineNum+2 ,line);
                Files.write(path, lines, StandardCharsets.UTF_8);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Uncomment u=new Uncomment();
        u.makeUncommentXml("/home/praneeth/Documents/workspace/sprint2/Test/newlocation/wso2telcohub-4.0.1-SNAPSHOT/repository/conf/user-mgt.xml","<UserStoreManager class=\"org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager\">");

    }
    //test


}