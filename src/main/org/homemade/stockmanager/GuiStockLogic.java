package org.homemade.stockmanager;

import org.homemade.Utils;
import org.homemade.main.MainGuiLogic;
import org.homemade.stockmanager.blobs.Stock_blob;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;

public class GuiStockLogic extends Component {

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
    private JComboBox industryComboBox;
    private JLabel payDateLabel;
    private JTextField payDateText;
    private JButton importDataButton;
    private JButton removeAllButton;
    private JButton updateExchangeRateButton;
    private JToolBar toolBar;
    private JFileChooser xmlImporter;
    private JFileChooser pathLocation;
    private JMenu menu;

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
        importDataButton.setText(DefaultLang.importDataButtonText);
        editButton.setText(DefaultLang.editButtonText);
        removeButton.setText(DefaultLang.removeButtonText);
        removeAllButton.setText(DefaultLang.removeAllButtonText);
        updateExchangeRateButton.setText(DefaultLang.updateExchangeRateButtonText);

        stockPanel.setVisible(true);

        Utils.Log("Remove all panel form JFrame");
        jFrame.getContentPane().removeAll();
        Utils.Log("Add Stock_blob Manager panel to JFrame");
        jFrame.add(stockPanel);
        Utils.Log("Repaint JFrame");
        jFrame.setVisible(true);
        jFrame.repaint();
    }

    @SuppressWarnings("BoundFieldAssignment")
    private void showStockTable(){
        Object[][] data = new Object[0][0];
        DefaultTableModel tableModel = new DefaultTableModel(data,Constants.columnNamesStockTable);
        stockTable = new JTable(tableModel);
        viewStockTable.getViewport().add(stockTable);
        Utils.Log("Initialize stock table.");
        updateStockTable(false);
        Utils.Log("Update stocks table values.");
    }

    private void updateStockTable(boolean importData){
        HashMap<String, Stock_blob> stockBlobs = Logic.getInstance().loadStockData(Constants.stockFilePath);
        DefaultTableModel model = (DefaultTableModel) stockTable.getModel();
        model.setRowCount(0);
        Object[] row = new Object[Constants.columnNamesStockTable.length];

        double investment;
        double profit;
        double tax;
        double profitRON;
        String currencySymbol;

        for (String key : stockBlobs.keySet()) {

            Stock_blob stockBlob = stockBlobs.get(key);

            switch (stockBlob.getSymbol()) {
                case "MC.PA" -> {
                    if (importData) {
                        investment = stockBlob.getInvestment();
                    }else {
                        investment = stockBlob.getInvestment() / Logic.getInstance().getExchangeRateEUR();
                    }
                    profit = stockBlob.getOwnShares() * stockBlob.getDivPerQ();
                    tax = (profit * Constants.FRIncomeTax) / 100;
                    profitRON = ((stockBlob.getOwnShares() * stockBlob.getDivPerQ()) - tax) * Logic.getInstance().getExchangeRateEUR();
                    currencySymbol = "€";
                }
                case "TRIG.L", "BSIF.L" -> {
                    if (importData) {
                        investment = stockBlob.getInvestment();
                    }else {
                        investment = stockBlob.getInvestment() / Logic.getInstance().getExchangeRateGBP();
                    }
                    profit = (stockBlob.getOwnShares() * stockBlob.getDivPerQ())/100;
                    tax = (profit * Constants.GBIncomeTax) / 100;
                    profitRON = (((stockBlob.getOwnShares() * stockBlob.getDivPerQ()) - tax) * Logic.getInstance().getExchangeRateGBP())/100;
                    currencySymbol = "£";
                }
                case "ENB" -> {
                    if (importData) {
                        investment = stockBlob.getInvestment();
                    }else {
                        investment = stockBlob.getInvestment() / Logic.getInstance().getExchangeRateCAD();
                    }
                    profit = stockBlob.getOwnShares() * stockBlob.getDivPerQ();
                    tax = (profit * Constants.USAIncomeTax) / 100;
                    profitRON = ((stockBlob.getOwnShares() * stockBlob.getDivPerQ()) - tax) * Logic.getInstance().getExchangeRateCAD();
                    currencySymbol = "c$";
                }
                default -> {
                    if (importData) {
                        investment = stockBlob.getInvestment();
                    }else {
                        investment = stockBlob.getInvestment() / Logic.getInstance().getExchangeRateRON();
                    }
                    profit = stockBlob.getOwnShares() * stockBlob.getDivPerQ();
                    tax = (profit * Constants.USAIncomeTax) / 100;
                    profitRON = ((stockBlob.getOwnShares() * stockBlob.getDivPerQ()) - tax) * Logic.getInstance().getExchangeRateRON();
                    currencySymbol = "$";
                }
            }

            row[0] = stockBlob.getSymbol();
            row[1] = stockBlob.getName();
            row[2] = currencySymbol + " " + Constants.currencyFormat.format(stockBlob.getValue());
            row[3] = currencySymbol + " " + Constants.dividendPayFormat.format(stockBlob.getDivPerQ());
            row[4] = stockBlob.getOwnShares();
            row[5] = currencySymbol + " " + Constants.currencyFormat.format(investment);
            row[6] = currencySymbol + " " + Constants.currencyFormat.format(profit-tax);
            row[7] = currencySymbol + " " + Constants.currencyFormat.format(tax);
            row[8] = Constants.currencyFormat.format(profitRON)+" RON";
            model.addRow(row);
        }
    }

    public void init(){
        Logic.getFirstInstance(Constants.stockFilePath);
        showStockPanel();
        initMenuBar();
        showStockTable();
        editStockPanel.setVisible(false);
        exitButton.addActionListener(e -> {
            Utils.Log("Call Main Menu GUI.");
            MainGuiLogic.getInstance(jFrame).init();
        });

        importDataButton.addActionListener(e -> {
           disableButtons(importDataButton);
            Utils.Log("Import XLSX form Google drive.");
            xmlImporter = new JFileChooser();
            xmlImporter.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int returnVal = xmlImporter.showOpenDialog(GuiStockLogic.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = xmlImporter.getSelectedFile();
                Logic.getInstance().readXLSX(file.getAbsolutePath());
                updateStockTable(true);

            }
            enableButtons();
        });

        editButton.addActionListener(e -> {
            disableButtons(editButton);
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
            updateStockTable(false);
            enableButtons();
        });

        removeButton.addActionListener(e -> {
            disableButtons(removeButton);
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
            updateStockTable(false);
            enableButtons();
        });

        removeAllButton.addActionListener(e -> {
            disableButtons(removeAllButton);
            Utils.Log("Remove all added stock !");
            Logic.getInstance().removeAllStock();
            updateStockTable(false);
            enableButtons();
        });

        updateExchangeRateButton.addActionListener(e -> {
            disableButtons(updateExchangeRateButton);
            Utils.Log("Update exchange rate.");
            Logic.getInstance().getExchangeRates();
            enableButtons();
        });

        newStockField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Utils.Log("()");
                    String newStockName = newStockField.getText().toUpperCase();
                    newStockField.setText("");
                    Logic.getInstance().getStock(newStockName);
                    updateStockTable(false);
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
        payDateLabel.setText(DefaultLang.payDateLabel);
    }

    public void initMenuBar(){
        JMenuBar menuBar = new JMenuBar();
        menu = new JMenu("Menu");
        JMenuItem menuButtonSavePathData = new JMenuItem("Save data path");
        menu.add(menuButtonSavePathData);
        menuBar.add(menu);
        toolBar.add(menuBar);

        menuButtonSavePathData.addActionListener( e -> {
            Utils.Log("Save new path for storing data.");
            pathLocation = new JFileChooser();
            pathLocation.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            pathLocation.setCurrentDirectory(new File(Constants.stockFilePath));
            int returnVal = pathLocation.showOpenDialog(GuiStockLogic.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = pathLocation.getSelectedFile();
                Constants.setStockFilePath(file.getAbsolutePath());
                Logic.getInstance().saveStock();
                Utils.Log("New stock path is: "+ Constants.stockFilePath);
            }
        });
    }

    public void populateEditPanel(@NotNull Stock_blob stockBlob){
        Utils.Log("Load stock to be edited in Edit Form.");
        editStockPanel.setVisible(true);
        stockLabel.setText(stockBlob.getSymbol());
        divTextFiled.setText(String.valueOf(stockBlob.getDivPerQ()));
        ownShareTextField.setText(String.valueOf(stockBlob.getOwnShares()));
        sectorComboBox.setSelectedItem(stockBlob.getSector());
        investmentTextField.setText(String.valueOf(stockBlob.getInvestment()));
        industryComboBox.removeAllItems();

        if (null != Constants.industryComboBoxList.get(sectorComboBox.getModel().getSelectedItem())) {
            String[] industryListSaved = Constants.industryComboBoxList.get(sectorComboBox.getModel().getSelectedItem());
            for (String s : industryListSaved) {
                industryComboBox.addItem(s);
            }
            industryComboBox.setSelectedItem(stockBlob.getIndustry());
        }

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

            String industry = (String) industryComboBox.getModel().getSelectedItem();
            stockBlob.setIndustry(industry);

            double investment = Double.parseDouble(investmentTextField.getText());
            stockBlob.setInvestment(investment);

            Logic.getInstance().updateStock(stockBlob);
            updateStockTable(false);
            resetEditPanel();
            editStockPanel.setVisible(false);
        });

        sectorComboBox.addItemListener(e -> {
            Utils.Log("Item changed");
            industryComboBox.removeAllItems();
            String[] industryList = Constants.industryComboBoxList.get(sectorComboBox.getModel().getSelectedItem());
            for (String s : industryList) {
                industryComboBox.addItem(s);
            }
        });
    }

    public void resetEditPanel(){
        divTextFiled.setText("");
        ownShareTextField.setText("");
        investmentTextField.setText("");
        industryComboBox.removeAllItems();
    }

    public void disableButtons(JButton buttonCall){
        if (!(buttonCall.getLabel().equals(DefaultLang.importDataButtonText)))
            importDataButton.setEnabled(false);
        if (!(buttonCall.getLabel().equals(DefaultLang.exitButtonText)))
            exitButton.setEnabled(false);
        if (!(buttonCall.getLabel().equals(DefaultLang.updateExchangeRateButtonText)))
            updateExchangeRateButton.setEnabled(false);
        if (!(buttonCall.getLabel().equals(DefaultLang.editButtonText)))
            editButton.setEnabled(false);
        if (!(buttonCall.getLabel().equals(DefaultLang.removeAllButtonText)))
            removeAllButton.setEnabled(false);
        if (!(buttonCall.getLabel().equals(DefaultLang.removeButtonText)))
            removeButton.setEnabled(false);
    }

    public void enableButtons(){
        importDataButton.setEnabled(true);
        exitButton.setEnabled(true);
        updateExchangeRateButton.setEnabled(true);
        editButton.setEnabled(true);
        removeAllButton.setEnabled(true);
        removeButton.setEnabled(true);
    }
}


