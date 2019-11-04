import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import dto.ChangedFile;
import dto.Config;
import dto.ValueChange;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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

    private static String PROPERTY_FILE_PATH = "/home/chathurabuddi/IdeaProjects/apigate-binaries/igw-upgrade-scripts/upgrade.properties";
    private static String CONF_FILE_PATH = "/home/chathurabuddi/IdeaProjects/apigate-binaries/igw-upgrade-scripts/conf-list.json";

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
            IOException, ParserConfigurationException, SAXException, XPathExpressionException, TransformerException {
        Properties upgradeProperties = loadProperties(PROPERTY_FILE_PATH);
        for (ChangedFile changedFile : changedFiles) {
            String filePath = changedFile.getFile();
            for (ValueChange change : changedFile.getChanges()) {
                logger.info(change.getName());
                String xpath = change.getXpath();
                File newFile = new File(upgradeProperties.getProperty(newKey) + filePath);
                if(change.isCopy()) {
                    File oldFile = new File(upgradeProperties.getProperty(oldKey) + filePath);
                    changeXmlNodeValue(newFile, xpath, readXmlNodeValue(oldFile, xpath));
                } else {
                    changeXmlNodeValue(newFile, xpath, change.getValue());
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
            ParserConfigurationException, SAXException, XPathExpressionException, TransformerException {
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
            ParserConfigurationException, SAXException, XPathExpressionException {
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

}