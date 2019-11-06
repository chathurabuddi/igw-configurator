import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import dto.ChangedFile;
import dto.Config;
import dto.ValueChange;
import jdk.internal.org.xml.sax.SAXException;
import java.io.File;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import org.w3c.dom.*;
/**
 * Copyright (c) 2016, WSO2.Telco Inc. (http://www.wso2telco.com) All Rights Reserved.
 * <p>
 * WSO2.Telco Inc. licences this file to you under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Main {

    private static String PROPERTY_FILE_PATH = "/home/lahiru/Desktop/merge/upgrade.properties";
    private static String CONF_FILE_PATH = "/home/lahiru/Desktop/merge/conf-list.json";

    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            Config config = new Gson().fromJson(new JsonReader(new FileReader(CONF_FILE_PATH)), Config.class);

            logger.info("configuring API-M");
            configureFiles(config.getApimConf(), "new.dep.location", "old.dep.location");

            logger.info("configuring IS");
            configureFiles(config.getIsConf(), "new.is.location", "old.is.location");

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static void configureFiles(List<ChangedFile> changedFiles, String newKey, String oldKey) throws
            IOException, ParserConfigurationException, SAXException, XPathExpressionException, TransformerException, org.xml.sax.SAXException {
        Properties upgradeProperties = loadProperties(PROPERTY_FILE_PATH);
        for (ChangedFile changedFile : changedFiles) {
            String filePath = changedFile.getFile();
            for (ValueChange change : changedFile.getChanges()) {
                logger.info(change.getName());
                String xpath = change.getXpath();
                File newFile = new File(upgradeProperties.getProperty(newKey) + filePath);
                if(change.getChangeType().equals("copy")) {
                    File oldFile = new File(upgradeProperties.getProperty(oldKey) + filePath);
                    changeXmlNodeValue(newFile, xpath, readXmlNodeValue(oldFile, xpath));
                } else if(change.getChangeType().equals("add")) {
                    changeXmlNodeValue(newFile, xpath, change.getValue());
                }
                else if(change.getChangeType().equals("comment")) {
                    commentXmlNode(newFile,xpath,change.getValue());
                }

            }
        }
    }

    private static Properties loadProperties(String propertyFilePath) throws IOException {
        Properties upgradeProperties = new Properties();
        try (InputStream input = new FileInputStream(propertyFilePath)) {
            upgradeProperties.load(input);
        }
        return upgradeProperties;
    }

    private static void changeXmlNodeValue(File file, String xPath, String value) throws IOException,
            ParserConfigurationException, SAXException, XPathExpressionException, TransformerException, org.xml.sax.SAXException {
        try(FileInputStream fileIs = new FileInputStream(file)){
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(fileIs);
            NodeList nodeList = (NodeList) XPathFactory.newInstance().newXPath()
                    .compile(xPath).evaluate(xmlDocument, XPathConstants.NODESET);
            Element element = (Element) nodeList.item(0);

            element.getChildNodes().item(0).setNodeValue(value);

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(xmlDocument);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);
        }
    }

    private static String readXmlNodeValue(File file, String xPath) throws IOException,
            ParserConfigurationException, SAXException, XPathExpressionException, org.xml.sax.SAXException {
        try(FileInputStream fileIs = new FileInputStream(file)){
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(fileIs);
            NodeList nodeList = (NodeList) XPathFactory.newInstance().newXPath()
                    .compile(xPath).evaluate(xmlDocument, XPathConstants.NODESET);
            Element element = (Element) nodeList.item(0);
            return element.getChildNodes().item(0).getNodeValue();
        }
    }

    private static void commentXmlNode(File file, String xpath,String value) {

        try(FileInputStream fileIs = new FileInputStream(file)) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(fileIs);
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath1 = xpf.newXPath();
            XPathExpression expression = xpath1.compile(xpath);
            Node b13Node = (Node) expression.evaluate(xmlDocument, XPathConstants.NODE);
            Comment comment = xmlDocument.createComment(value);
            Element element = (Element) b13Node;
            element.getParentNode().insertBefore(comment, element);
            b13Node.getParentNode().removeChild(b13Node);
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            DOMSource source = new DOMSource(xmlDocument);
            StreamResult result = new StreamResult(file);
            t.transform(source,result);
        } catch (IOException | ParserConfigurationException | XPathExpressionException | TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (org.xml.sax.SAXException e) {
            e.printStackTrace();
        }
    }

    }


