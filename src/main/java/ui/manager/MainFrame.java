package ui.manager;

import java.awt.FlowLayout;
import javax.swing.JTable;
import ui.manufacturer.ManufacturerProductManager;
import ui.pharmacy.PharmacyAdministratorPanel;
<<<<<<< HEAD

=======
import ui.pharmacy.PharmacyOrderReport;
>>>>>>> main

public class MainFrame extends javax.swing.JFrame {

    public MainFrame() {
        initComponents();
    }
    
    public void showPharmacyAdminPanel() {
//        getContentPane.removeAll();
        add(new PharmacyAdministratorPanel());
        setLayout(new FlowLayout());
        repaint();
        revalidate();
    }
    
        public void showProductManagerPanel() {
//        getContentPane.removeAll();
        add(new ManufacturerProductManager());
        setLayout(new FlowLayout());
        repaint();
        revalidate();
    }
//        JTable table = new JTable();
//        public void showPharmacyReportPabel() {
//    //        getContentPane.removeAll();
//        add(new PharmacyOrderReport(table));
//        setLayout(new FlowLayout());
//        repaint();
//        revalidate();
//    }
//        

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1200, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1000, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
