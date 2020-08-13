import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class TestKrista {

    public static void main(String[] args) {

        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc= documentBuilder.parse("src/main/resources/data/plants__001.xml");
            Element root=doc.getDocumentElement();
            System.out.println("uuid: "+root.getAttribute("uuid"));
            System.out.println("date: "+root.getAttribute("date"));
            System.out.println("company: "+root.getAttribute("company"));
            NodeList catalog = root.getChildNodes();
            for (int i = 0; i < catalog.getLength(); i++) {
                Node plant = catalog.item(i);
                if (plant.getNodeType() != Node.TEXT_NODE) {
                    NodeList plantProps = plant.getChildNodes();
                    for(int j = 0; j < plantProps.getLength(); j++) {
                        Node plantProp = plantProps.item(j);
                        if (plantProp.getNodeType() != Node.TEXT_NODE) {
                            System.out.println(plantProp.getNodeName() + ":" + plantProp.getChildNodes().item(0).getTextContent());
                        }
                    }
                    System.out.println("===========>>>>");
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

}
