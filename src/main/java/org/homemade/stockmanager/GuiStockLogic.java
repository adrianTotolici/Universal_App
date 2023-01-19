package org.homemade.stockmanager;

import org.homemade.Utils;
import org.homemade.main.MainGuiLogic;
import org.homemade.stockmanager.blobs.Stock_blob;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.util.HashMap;

public class GuiStockLogic {

    private static GuiStockLogic instance;
    private final JFrame jFrame;
    private JButton exitButton;
    private JTextField newStockField;
    private JTable stockTable;
    private JPanel stockPanel;
    private JScrollPane viewStockTable;
    private JButton editButton;
    private JButton removeButton;
    private JTextField divTextFiled;
    private JTextField ownShareTextField;
    private JTextField industryTextField;
    private JTextField investmentTextField;
    private JButton editFromButton;
    private JPanel editStockPanel;
    private JLabel stockLabel;
    private JLabel divLabel;
    private JLabel ownShareLabel;
    private JLabel investmentLabel;
    private JLabel sectorLabel;
    private JLabel industryLabel;
    private JComboBox sectorComboBox;
    private JComboBox industrycomboBox;

    public GuiStockLogic(JFrame jFrame) {
        this.jFrame = jFrame;
        initEditPanel();
    }

    public static GuiStockLogic getInstance(JFrame frame){
        if (instance == null) {
            Utils.Log("(Stock_blob Manager) Initialize stock manager single tone");
            instance = new GuiStockLogic(frame);
        }
        return instance;
    }

    private void showStockPanel(){
        exitButton.setText(DefaultLang.exitButtonText);
        editButton.setText(DefaultLang.editButtonText);
        removeButton.setText(DefaultLang.removeButtonText);

        stockPanel.setVisible(true);

        Utils.Log("Remove all panel form JFrame");
        jFrame.getContentPane().removeAll();
        Utils.Log("Add Stock_blob Manager panel to JFrame");
        jFrame.add(stockPanel);
        Utils.Log("Repaint JFrame");
        jFrame.setVisible(true);
        jFrame.repaint();
    }

    private void showStockTable(){
        Object[][] data = new Object[0][0];
        DefaultTableModel tableModel = new DefaultTableModel(data,Constants.columnNamesStockTable);
        stockTable = new JTable(tableModel);
        viewStockTable.getViewport().add(stockTable);
        Utils.Log("Initialize stock table.");
        updateStockTable();
        Utils.Log("Update stocks table values.");
    }

    private void updateStockTable(){
        HashMap<String, Stock_blob> stockBlobs = Logic.getInstance().loadStockData();
        DefaultTableModel model = (DefaultTableModel) stockTable.getModel();
        model.setRowCount(0);
        Object[] row = new Object[Constants.columnNamesStockTable.length];

        for (String key : stockBlobs.keySet()) {
            Stock_blob stockBlob = stockBlobs.get(key);
            row[0] = stockBlob.getSymbol();
            row[1] = stockBlob.getName();
            row[2] = "$ "+stockBlob.getValue();
            row[3] = "$ "+stockBlob.getDivPerQ();
            row[4] = stockBlob.getOwnShares();
            row[5] = "$ "+stockBlob.getOwnShares()*stockBlob.getDivPerQ();
            double profit= stockBlob.getOwnShares() * stockBlob.getDivPerQ() * Logic.getInstance().getExchangeRateRon();
            row[6] = Constants.currencyFormat.format(profit)+" Lei";
            model.addRow(row);
        }
    }

    public void init(){
        showStockPanel();
        showStockTable();
        editStockPanel.setVisible(false);
        exitButton.addActionListener(e -> {
            Utils.Log("Call Main Menu GUI.");
            MainGuiLogic.getInstance(jFrame).init();
        });

        editButton.addActionListener(e -> {
            Utils.Log("Edit selected stock.");
            if (!(newStockField.getText().isBlank() || newStockField.getText().isEmpty())){
                Utils.Log("Update stock "+newStockField.getText().toUpperCase());
                Stock_blob stockBlob = Logic.getInstance().getAddedStock(newStockField.getText().toUpperCase());
                populateEditPanel(stockBlob);
            }else{
                int selectedRow = stockTable.getSelectedRow();
                String valueAt = (String) stockTable.getModel().getValueAt(selectedRow, 0);
                Stock_blob stockBlob = Logic.getInstance().getAddedStock(valueAt);
                if (stockBlob != null) {
                    Utils.Log("Update stock "+ stockBlob.getSymbol());
                    stockTable.clearSelection();
                    populateEditPanel(stockBlob);
                }else {
                    Utils.Log("Stock selected "+ valueAt+" doesn't exist in memory.");
                    stockTable.clearSelection();
                }
            }
            updateStockTable();

        });

        removeButton.addActionListener(e -> {
            Utils.Log("Remove selected stock.");
            if (!(newStockField.getText().isBlank() || newStockField.getText().isEmpty())){
                Utils.Log("Remove stock "+newStockField.getText().toUpperCase());
                Logic.getInstance().removeStock(newStockField.getText().toUpperCase());
            }else{
                int selectedRow = stockTable.getSelectedRow();
                String valueAt = (String) stockTable.getModel().getValueAt(selectedRow, 0);
                Logic.getInstance().removeStock(valueAt);
                stockTable.clearSelection();
            }
            updateStockTable();
        });

        newStockField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Utils.Log("()");
                    String newStockName = newStockField.getText().toUpperCase();
                    newStockField.setText("");
                    Logic.getInstance().getStock(newStockName);
                    updateStockTable();
                }
            }
        });

        newStockField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                stockTable.clearSelection();
            }
        });

        stockTable.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                newStockField.setText("");
            }
        });
    }

    public void initEditPanel(){
        divLabel.setText(DefaultLang.dividendLabel);
        ownShareLabel.setText(DefaultLang.ownShareLabel);
        sectorLabel.setText(DefaultLang.sectorLabel);
        industryLabel.setText(DefaultLang.industryLabel);
        investmentLabel.setText(DefaultLang.investmentLabel);
        editFromButton.setText(DefaultLang.saveEditStockButtonText);
        for (int i = 0; i< Constants.sectorComboBoxList.length; i++) {
            sectorComboBox.addItem(Constants.sectorComboBoxList[i]);
        }
    }

    public void populateEditPanel(@NotNull Stock_blob stockBlob){
        Utils.Log("Load stock to be edited in Edit Form.");
        editStockPanel.setVisible(true);
        stockLabel.setText(stockBlob.getSymbol());
        divTextFiled.setText(String.valueOf(stockBlob.getDivPerQ()));
        ownShareTextField.setText(String.valueOf(stockBlob.getOwnShares()));
        sectorComboBox.setSelectedItem(stockBlob.getSector());
        industryTextField.setText(stockBlob.getIndustry());
        investmentTextField.setText(String.valueOf(stockBlob.getInvestment()));

        editFromButton.setText(DefaultLang.saveEditStockButtonText);

        editFromButton.addActionListener(e -> {
            Utils.Log("Save new data for current Stock");
            double divPerQ = Double.parseDouble(divTextFiled.getText());
            stockBlob.setDivPerQ(divPerQ);

            if (!(ownShareTextField.getText().isEmpty() || ownShareTextField.getText().isBlank())) {
                double ownShares = Double.parseDouble(ownShareTextField.getText());
                stockBlob.setOwnShares(ownShares);
            }

            String sector = (String) sectorComboBox.getModel().getSelectedItem();
            stockBlob.setSector(sector);

            String industry = industryTextField.getText();
            stockBlob.setIndustry(industry);

            double investment = Double.parseDouble(investmentTextField.getText());
            stockBlob.setInvestment(investment);

            Logic.getInstance().updateStock(stockBlob);
            updateStockTable();
            resetEditPanel();
            editStockPanel.setVisible(false);
        });
    }

    public void resetEditPanel(){
        divTextFiled.setText("");
        ownShareTextField.setText("");
        sectorComboBox.setSelectedItem(Constants.sectorComboBoxList[0]);
        industryTextField.setText("");
        investmentTextField.setText("");
    }
}
