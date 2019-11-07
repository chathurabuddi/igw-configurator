import java.io.*;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import dto.ChangedFile;
import dto.Config;
import dto.Uncomment;
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
    private static String CONF_FILE_PATH = "src/main/resources/conf-list.json";

    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            Config config = new Gson().fromJson(new JsonReader(new FileReader(CONF_FILE_PATH)), Config.class);

            logger.info("configuring API-M");
            configureFiles(config.getApimConf(), "new.dep.location", "old.dep.location");

//            logger.info("configuring IS");
//            configureFiles(config.getIsConf(), "new.is.location", "old.is.location");

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
                }else if(change.getChangeType().equals("uncomment")) {
                    Uncomment u=new Uncomment();
                    u.makeUncommentXml(newFile.getPath().toString(),change.getValue());
                }
                else if(change.getChangeType().equals("replace_attribute")){
                    replaceXmlNode(newFile,xpath,change.getValue(),change.getAttribute(),change.getOldValue());
                }
                else if(change.getChangeType().equals("add_node")){
                    addXmlNode(newFile,xpath,change.getPageno());
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

    private static void replaceXmlNode(File file, String xpath,String val, String attribute,String oldval)
    {

        try(FileInputStream fileIs = new FileInputStream(file)) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(fileIs);
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath1 = xpf.newXPath();
           // XPathExpression expression = xpath1.compile(xpath);
            String xpth = "//*[contains(@"+attribute+","+"'"+oldval+"'"+")]";
            NodeList nodes = (NodeList)xpath1.evaluate(xpth,
                    xmlDocument, XPathConstants.NODESET);
            for (int idx = 0; idx < nodes.getLength(); idx++) {
                Node value = nodes.item(idx).getAttributes().getNamedItem(attribute);
                value.setNodeValue(val);
            }
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

    private static void addXmlNode(File file, String xpath,int fileno) throws XPathExpressionException {
        try(FileInputStream fileIs = new FileInputStream(file)) {
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            Document xmlDocument = builder.parse(fileIs);
            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath1 = xpf.newXPath();
            NodeList nodeList = (NodeList) XPathFactory.newInstance().newXPath()
                    .compile(xpath).evaluate(xmlDocument, XPathConstants.NODESET);
//            Element element = (Element) nodeList.item(0);
//            NodeList nodes = xmlDocument.getElementsByTagName("registryRoot");

            if(fileno == 1){
                Text a = xmlDocument.createTextNode("jdbc/WSO2CarbonDB");
                Element p = xmlDocument.createElement("dataSource");
                p.appendChild(a);
                Element element = xmlDocument.createElement("dbConfig");
                element.setAttribute("name","govregistry");
                element.appendChild(p);
                nodeList.item(0).getParentNode().insertBefore(element, nodeList.item(0));
            }
            else if(fileno == 2){
                Text text = xmlDocument.createTextNode("gov");
                Element element = xmlDocument.createElement("id");
                element.appendChild(text);
                Text text1 = xmlDocument.createTextNode("user@jdbc:mysql://db.mysql-wso2.com:3306/regdb");
                Element element1 = xmlDocument.createElement("cacheId");
                element1.appendChild(text1);

                Text text2 = xmlDocument.createTextNode("govregistry");
                Element element2 = xmlDocument.createElement("dbConfig");
                element2.appendChild(text2);

                Text text3 = xmlDocument.createTextNode(String.valueOf(false));
                Element element3 = xmlDocument.createElement("readOnly");
                element3.appendChild(text3);

                Text text4 = xmlDocument.createTextNode(String.valueOf(true));
                Element element4 = xmlDocument.createElement("enableCache");
                element4.appendChild(text4);

                Text text5 = xmlDocument.createTextNode("/");
                Element element5 = xmlDocument.createElement("registryRoot");
                element5.appendChild(text5);

                Element element6 = xmlDocument.createElement("remoteInstance");
                element6.setAttribute("url","https://localhost:9443/registry");

                element6.appendChild(element);
                element6.appendChild(element1);
                element6.appendChild(element2);
                element6.appendChild(element3);
                element6.appendChild(element4);
                element6.appendChild(element5);
                nodeList.item(0).getParentNode().insertBefore(element6, nodeList.item(0));

                Text text_2 = xmlDocument.createTextNode("mount");
                Element element_2 = xmlDocument.createElement("id");
                element_2.appendChild(text_2);

                Text text1_2 = xmlDocument.createTextNode("user@jdbc:mysql://db.mysql-wso2.com:3306/regdb");
                Element element1_2 = xmlDocument.createElement("cacheId");
                element1_2.appendChild(text1_2);


                Text text2_2 = xmlDocument.createTextNode("govregistry");
                Element element2_2 = xmlDocument.createElement("dbConfig");
                element2_2.appendChild(text2_2);

                Text text3_2 = xmlDocument.createTextNode(String.valueOf(false));
                Element element3_2 = xmlDocument.createElement("readOnly");
                element3_2.appendChild(text3_2);

                Text text4_2 = xmlDocument.createTextNode(String.valueOf(true));
                Element element4_2 = xmlDocument.createElement("enableCache");
                element4_2.appendChild(text4_2);

                Text text5_2 = xmlDocument.createTextNode("/");
                Element element5_2 = xmlDocument.createElement("registryRoot");
                element5_2.appendChild(text5_2);

                Element element6_2 = xmlDocument.createElement("remoteInstance");
                element6_2.setAttribute("url","https://localhost:9443/registry");

                element6_2.appendChild(element_2);
                element6_2.appendChild(element1_2);
                element6_2.appendChild(element2_2);
                element6_2.appendChild(element3_2);
                element6_2.appendChild(element4_2);
                element6_2.appendChild(element5_2);
                nodeList.item(0).getParentNode().insertBefore(element6_2, nodeList.item(0));
            }

            else if(fileno == 3){

                Text text = xmlDocument.createTextNode("gov");
                Element element = xmlDocument.createElement("instanceId");
                element.appendChild(text);
                Text text1 = xmlDocument.createTextNode("/_system/governance");
                Element element1 = xmlDocument.createElement("targetPath");
                element1.appendChild(text1);
                Element element2 = xmlDocument.createElement("mount");
                element2.setAttribute("path","/_system/governance");
                element2.setAttribute("overwrite", String.valueOf(true));
                element2.appendChild(element1);
                element2.appendChild(element);
                nodeList.item(0).getParentNode().insertBefore(element2, nodeList.item(0));
                Text text_2 = xmlDocument.createTextNode("gov");
                Element element_2 = xmlDocument.createElement("instanceId");
                element_2.appendChild(text_2);
                Text text1_2 = xmlDocument.createTextNode("/_system/governance");
                Element element1_2 = xmlDocument.createElement("targetPath");
                element1_2.appendChild(text1_2);
                Element element2_2 = xmlDocument.createElement("mount");
                element2_2.setAttribute("path","/_system/governance");
                element2_2.setAttribute("overwrite", String.valueOf(true));
                element2_2.appendChild(element_2);
                element2_2.appendChild(element1_2);
                nodeList.item(0).getParentNode().insertBefore(element2_2, nodeList.item(0));
            }




            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");
            DOMSource source = new DOMSource(xmlDocument);
            StreamResult result = new StreamResult(file);
            t.transform(source,result);
        } catch (IOException | ParserConfigurationException | TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (org.xml.sax.SAXException e) {
            e.printStackTrace();
        }


    }



    }


