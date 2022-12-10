package db;

import data.model.pharmacy.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class PharmacyManager {
    private final static String FILENAME = "PharmacyManager";
    public static java.sql.Connection con = Connection.getConnection();
    /**
     * @param order - Order data of Pharmacy
     * @return true if operation succeeds
     * @throws java.lang.Exception
     */
    public static boolean createOrder(PharmacyPurchaseOrder order) throws Exception {
        boolean isCreated = true;
        int orderId = -1;
        try {
            try {
                //Query to insert Order
                order.setOrderStatus("placed");
                String queryToInsertOrder = "INSERT INTO pharmacy_order(order_date, manufacturer_id, pharmacy_id)"
                                + "values (?, ?, ?)";
                PreparedStatement preparedStmt1 = con.prepareStatement(queryToInsertOrder);
                preparedStmt1.setString (1, order.getPurchaseOrderDate().getFormattedDate());
                preparedStmt1.setInt (2, order.getPharmacymanufactureId());
                preparedStmt1.setInt (3, order.getPharmacyId());
                preparedStmt1.execute();
            } catch (SQLException e) {
                throw new Exception("Error inserting order: " + e);
            }
            
            try {
                //Query to get Order ID
                String queryToGetOrderId = String.format("SELECT order_id FROM pharmacy_order WHERE order_id=LAST_INSERT_ID()");
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(queryToGetOrderId);
                if (rs.next()) {
                    orderId = rs.getInt("order_id");
                } else {
                    throw new Error("Cannot find ID");
                }
            } catch (SQLException e) {
                throw new Exception("Error fetching order ID: " + e);
            }
            
            try {
                //Query to insert Order Items
                String queryToInsertOrderItems = "INSERT INTO pharmacy_order_item(item_id, quantity, order_id)"
                                + "values (?, ?, ?)";
                PreparedStatement preparedStmt2 = con.prepareStatement(queryToInsertOrderItems);
                for (PharmacyPurchaseOrderItem item : order.getOrderItems()) {
                    preparedStmt2.setInt (1, item.getDrug().getDrugId());
                    preparedStmt2.setInt (2, item.getQuantity());
                    preparedStmt2.setInt (3, orderId);
                    preparedStmt2.addBatch();
                }
                preparedStmt2.executeBatch();
            } catch (SQLException e) {
                throw new Exception("Error inserting order items: " + e);
            }
            return isCreated;
        } catch (SQLException e) {
            throw new Exception(FILENAME + "->" + "createOrder" + "->" + e);
        }
    }
    
    /**
     * @param pharmacyCompanyId - ID of Pharmacy
     * @return ResultSet if operation succeeds
     * @throws java.lang.Exception
     */
    public static ResultSet fetchAllOrders(int pharmacyCompanyId) throws Exception {
        try {
            //Build Query
            String query = """
                SELECT po.order_id, po.manufacturer_id, c.company_name as manufacturer_name, po.order_date, po.order_status
                FROM pharmacy_order po
                join company c on c.company_id=po.manufacturer_id
                WHERE po.pharmacy_id=%s""";
            query = String.format(query, pharmacyCompanyId);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            return rs;
        } catch (SQLException e) {
            throw new Exception(FILENAME + "->" + "fetchAllOrders" + "->" + e);
        } 
    }
    
    /**
     * @return ResultSet if operation succeeds
     * @throws java.lang.Exception
     */
    public static ResultSet displayManufacturerInventory() throws Exception {
        try {
            String query = """
                SELECT m.manufacturer_id, c.company_name AS manufacturer_name, d.drug_id, d.drug_name, m.quantity, m.selling_price
                FROM manufacturer_inventory m
                JOIN master_drug_table d on m.drug_id = d.drug_id
                JOIN company c on m.manufacturer_id = c.company_id""";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            return rs;
        } catch (SQLException e) {
            throw new Exception(FILENAME + "->" + "displayManufacturerInventory" + "->" + e);
        } 
    }
    
    public static ResultSet fetchInventory(int pharmacyId) throws Exception {
        try {
            String query = """
                SELECT poi.item_id, md.drug_name, poi.quantity,  po.order_id, po.manufacturer_id, c.company_name as manufacturer_name, po.order_date, po.order_status
                FROM pharmacy_order po
                JOIN company c ON c.company_id=po.manufacturer_id
                JOIN pharmacy_order_item poi ON poi.order_id = po.order_id
                JOIN master_drug_table md ON md.drug_id=poi.item_id
                WHERE po.pharmacy_id=1""";
            query = String.format(query, pharmacyId);
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            return rs;
        } catch (SQLException e) {
            throw new Exception(FILENAME + "->" + "displayManufacturerInventory" + "->" + e);
        } 
    }
    
    /**
     * @param order - Order object
     * @return true if operation succeeds
     * @throws java.lang.Exception
     */
    public static boolean updateStockAndQuantity(PharmacyPurchaseOrder order) throws Exception {
        boolean isUpdated = false;
        try {
            for (PharmacyPurchaseOrderItem item : order.getOrderItems()) {
                //Check if item is present in the inventory
                String findStockQuery = "SELECT * FROM pharmacy_inventory WHERE pharmacy_id=%s AND drug_id=%s";
                findStockQuery = String.format(findStockQuery, order.getPharmacyId(), item.getDrug().getDrugId());
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(findStockQuery);
                
                //If not, add the item to inventory
                String queryToUpdateOrder;
                if(!rs.next()) {
                    queryToUpdateOrder = """
                        INSERT into pharmacy_inventory(pharmacy_id, drug_id, quantity)
                        VALUES (?, ?, ?)""";
                    PreparedStatement preparedStmt = con.prepareStatement(queryToUpdateOrder);
                    preparedStmt.setInt (1, order.getPharmacyId());
                    preparedStmt.setInt (2, item.getDrug().getDrugId());
                    preparedStmt.setInt (3, item.getQuantity());
                    preparedStmt.execute();
                }
                //Else, update the quantity of item
                else {
                    queryToUpdateOrder = """
                        UPDATE pharmacy_inventory
                        SET quantity=quantity+%s
                        WHERE drug_id=%s AND pharmacy_id=%s""";
                    queryToUpdateOrder = String.format(queryToUpdateOrder, item.getQuantity(), item.getDrug().getDrugId(), order.getPharmacyId());
                    PreparedStatement preparedStmt = con.prepareStatement(queryToUpdateOrder);
                    preparedStmt.execute();
                }
            }
            return !isUpdated;
        } catch (SQLException e) {
            throw new Exception(FILENAME + "->" + "updateStockQuantity" + "->" + e);           
        }
    }
    
//    /**
//     * @param drug - Drug object
//     * @param pharmacy_id - Drug object
//     * @return true if operation succeeds
//     * @throws java.lang.Exception
//     */
//    public static boolean updateStockDetails(ManufacturedDrugDetails drug, int pharmacy_id) throws Exception {
//        boolean isUpdated = false;
//        try {
//            String queryToUpdateOrder = """
//                UPDATE pharmacy_inventory
//                SET selling_price=%s
//                WHERE drug_id=%s AND pharmacy_id=%s""";
//            queryToUpdateOrder = String.format(queryToUpdateOrder, drug.getDrugSellingPrice(), drug.getDrugId(), pharmacy_id);
//            PreparedStatement preparedStmt = con.prepareStatement(queryToUpdateOrder);
//            preparedStmt.execute();
//            return !isUpdated;
//        } catch (SQLException e) {
//            throw new Exception(FILENAME + "->" + "updateStockDetails" + "->" + e);
//        }
//    }
}