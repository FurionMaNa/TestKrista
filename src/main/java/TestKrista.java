import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class TestKrista {

    static final String DB_URL = "jdbc:postgresql://127.0.0.1:5432/plant";
    static final String USER = "postgres";
    static final String PASS = "postgres";

    private static String addFCatPlants(FCatPlantsClass catPlan){
        return "insert into f_cat_plants (availability, botanical, catalog_id, common, light, price, zone) " +
               "values ("+catPlan.availability+" , '"+catPlan.botanical+"' , "+catPlan.catalogId+" , '"+catPlan.common+"' , '"+catPlan.light+"' , "+catPlan.price+" , " + catPlan.zone+" );";
    }

    private static String addDCatCatalog(String delivery_date,String company,String uuid){
        return "insert into d_cat_catalog (company, delivery_date, uuid) " +
               "values ('"+company+"', '"+delivery_date+"','"+uuid+"');";
    }

    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return;
        }
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
        } catch (SQLException e) {
            System.out.println("Connection Failed");
            e.printStackTrace();
            return;
        }
        if(args.length==0){
            ArrayList<String> path=new ArrayList<>();
            int anotherFile=0;
            do {
                System.out.print("Введи путь к файлу: ");
                int inChar;
                String s = "";
                try {
                    inChar = System.in.read();
                    while (System.in.available() > 0) {
                        s += (char) inChar;
                        inChar = System.in.read();
                    }
                } catch (IOException e) {
                    System.out.println("Ошибка");
                }
                boolean f=true;
                for(int i=0;i<path.size();i++){
                    if(path.get(i).equals(s)){
                        System.out.println("Такой файл уже есть!!!");
                        f=false;
                    }
                }
                if(f) {
                    path.add(s);
                }
                System.out.print("Хотите указать ещё один файл?(0-да;1-нет): ");
                Scanner in = new Scanner(System.in);
                anotherFile = in.nextInt();
            }while(anotherFile==0);
            args=new String[path.size()];
            for(int i=0;i<path.size();i++){
                args[i]=path.get(i);
            }
        }
        if (connection != null) {
            try {
                Statement statement = connection.createStatement();
                statement.execute("DELETE FROM f_cat_plants; DELETE FROM d_cat_catalog;");
                for (int k = 0; k < args.length; k++) {
                    try {
                        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        Document doc = documentBuilder.parse(args[k]);
                        Element root = doc.getDocumentElement();
                        try {
                            statement.execute(addDCatCatalog(root.getAttribute("date"), root.getAttribute("company"), root.getAttribute("uuid")));
                        }catch (Exception e) {
                            System.out.println("В файле " + args[k] + " " + e.getMessage());
                            continue;
                        }
                        NodeList catalog = root.getChildNodes();
                        for (int i = 0; i < catalog.getLength(); i++) {
                            Node plant = catalog.item(i);
                            if (plant.getNodeType() != Node.TEXT_NODE) {
                                NodeList plantProps = plant.getChildNodes();
                                FCatPlantsClass fCatPlantsClass = new FCatPlantsClass();
                                ResultSet resultSet = statement.executeQuery("Select id from d_cat_catalog where (uuid='" + root.getAttribute("uuid") + "');");
                                if (resultSet.next()) {
                                    fCatPlantsClass.catalogId = resultSet.getInt("id");
                                }
                                boolean error=false;
                                for (int j = 0; j < plantProps.getLength(); j++) {
                                    try {
                                        Node plantProp = plantProps.item(j);
                                        if (plantProp.getNodeType() != Node.TEXT_NODE) {
                                            if ((plantProp.getNodeName().equals("PRICE")) && (plantProp.getChildNodes().item(0).getTextContent().charAt(0) == '$')) {
                                                fCatPlantsClass.price = Float.parseFloat(plantProp.getChildNodes().item(0).getTextContent().substring(0, 0) + plantProp.getChildNodes().item(0).getTextContent().substring(1));
                                            } else {
                                                switch (plantProp.getNodeName()) {
                                                    case "COMMON":
                                                        fCatPlantsClass.common = plantProp.getChildNodes().item(0).getTextContent();
                                                        break;
                                                    case "BOTANICAL":
                                                        fCatPlantsClass.botanical = plantProp.getChildNodes().item(0).getTextContent();
                                                        break;
                                                    case "ZONE":
                                                        fCatPlantsClass.zone = Integer.valueOf(plantProp.getChildNodes().item(0).getTextContent());
                                                        break;
                                                    case "LIGHT":
                                                        fCatPlantsClass.light = plantProp.getChildNodes().item(0).getTextContent();
                                                        break;
                                                    case "PRICE":
                                                        fCatPlantsClass.price = Float.parseFloat(plantProp.getChildNodes().item(0).getTextContent());
                                                        break;
                                                    case "AVAILABILITY":
                                                        fCatPlantsClass.availability = Integer.valueOf(plantProp.getChildNodes().item(0).getTextContent());
                                                        break;
                                                }
                                            }
                                        }
                                    }catch (Exception e){
                                        System.out.println("В файле " + args[k] + " " + e.getMessage());
                                        error=true;
                                        break;
                                    }
                                }
                                if(!error) {
                                    try {
                                        statement.execute(addFCatPlants(fCatPlantsClass));
                                    } catch (Exception ex) {
                                        System.out.println("В файле " + args[k] + " " + ex.getMessage());
                                    }
                                    error=false;
                                }
                            }
                        }
                    } catch (ParserConfigurationException e) {
                        e.printStackTrace();
                    } catch (SAXException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        System.out.println(e.toString());
                    }
                }
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } finally {
                try {
                    connection.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
        } else {
            System.out.println("Error connect");
        }
        System.out.println("Данные помещены в БД!");

    }

}
