package com.cloudbees.utils;

import java.io.IOException;
import java.io.InputStream;

import com.cloudbees.api.ApplicationConfiguration;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class AppConfigHelper {
    public static void load(ApplicationConfiguration applicationConfiguration, InputStream in, String[] environments, String[] implicitEnvironments)
    {
        InputSource input = new InputSource(in);
        load(applicationConfiguration, input, environments, implicitEnvironments);
    }
    
    private static void load(ApplicationConfiguration applicationConfiguration, InputSource input, String[] environments, String[] implicitEnvironments) {
        Document doc = readXML(input);
        Element rootElement = doc.getDocumentElement();
        if(rootElement.getNodeName().equals("stax-application") ||
                rootElement.getNodeName().equals("stax-web-app") || rootElement.getNodeName().equals("cloudbees-web-app"))
        {
            AppConfigParser parser = new AppConfigParser();
            parser.load(applicationConfiguration, doc, environments, implicitEnvironments);
        }
    }

    private static Document readXML(InputSource input) {
        DocumentBuilderFactory dBF = DocumentBuilderFactory.newInstance();
        dBF.setIgnoringComments(true); // Ignore the comments present in the
        // XML File when reading the xml
        DocumentBuilder builder = null;
        try {
            builder = dBF.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        Document doc = null;
        try {
            doc = builder.parse(input);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return doc;
    }

}
