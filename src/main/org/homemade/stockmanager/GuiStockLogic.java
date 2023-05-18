package org.homemade.stockmanager;

import com.intellij.uiDesigner.core.GridConstraints;
import org.homemade.Utils;
import org.homemade.main.MainGuiLogic;
import org.homemade.stockmanager.blobs.Stock_blob;
import org.jetbrains.annotations.NotNull;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

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
    private JComboBox<String> sectorComboBox;
    private JComboBox<String> industryComboBox;
    private JLabel payDateLabel;
    private JTextField payDateText;
    private JButton importDataButton;
    private JButton removeAllButton;
    private JButton updateExchangeRateButton;
    private JToolBar toolBar;
    private JLabel exchangeRonValue;
    private JLabel exchangeEURValue;
    private JLabel exchangeGBPValue;
    private JLabel exchangeCADValue;
    private JLabel totalInvestedLabel;
    private JLabel totalInvestedValue;
    private JLabel totalProfitLabel;
    private JLabel totalProfitValue;
    private JLabel totalTaxLabel;
    private JLabel totalTaxValue;
    private JLabel shareSelectedLabel;
    private JLabel shareInvestmentLabel;
    private JLabel shareInvestmentValue;
    private JLabel shareTaxLabel;
    private JLabel shareTaxValue;
    private JLabel shareProfitLabel;
    private JLabel shareProfitValue;
    private JLabel shareAnnouncementLabel;
    private JLabel shareAnnouncementValue;
    private JLabel shareExDividendLabel;
    private JLabel shareExDividendValue;
    private JLabel sharePayDayLabel;
    private JLabel sharePayDayValue;
    private JPanel shareDetailInformationPanel;
    private JTextPane shareNewsPane;
    private JLabel recommendedInvestmentLabel;
    private JLabel recommendedInvestmentValue;
    private JPanel pieChartPanel;
    private JFileChooser xmlImporter;
    private JFileChooser pathLocation;

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

        exchangeRonValue.setText("RON: "+Logic.getInstance().getExchangeRateRON());
        exchangeEURValue.setText("EUR: "+Logic.getInstance().getExchangeRateEUR());
        exchangeGBPValue.setText("GBP: "+Logic.getInstance().getExchangeRateGBP());
        exchangeCADValue.setText("CAD: "+Logic.getInstance().getExchangeRateCAD());

        totalInvestedLabel.setText(DefaultLang.totalInvestmentLabel);
        totalProfitLabel.setText(DefaultLang.totalProfitLabel);
        totalTaxLabel.setText(DefaultLang.totalTaxLabel);

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
        Logic.getInstance().loadInvestmentData(Constants.investmentFilePath);
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
                case  "TSM" -> {
                    if (importData) {
                        investment = stockBlob.getInvestment();
                    }else {
                        investment = stockBlob.getInvestment() / Logic.getInstance().getExchangeRateRON();
                    }
                    profit = stockBlob.getOwnShares() * stockBlob.getDivPerQ();
                    tax = (profit * Constants.TWIncomeTax) / 100;
                    profitRON = ((stockBlob.getOwnShares() * stockBlob.getDivPerQ()) - tax) * Logic.getInstance().getExchangeRateRON();
                    currencySymbol = "$";
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

        initGeneralShareInfo();
    }

    public void init(){
        Logic.getInstance().setStockFilePath(Constants.stockFilePath, Constants.investmentFilePath);
        showStockPanel();
        initMenuBar();
        showStockTable();
        shareDetailInformationPanel.setVisible(false);
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
            exchangeRonValue.setText("RON: "+Logic.getInstance().getExchangeRateRON());
            exchangeEURValue.setText("EUR: "+Logic.getInstance().getExchangeRateEUR());
            exchangeGBPValue.setText("GBP: "+Logic.getInstance().getExchangeRateGBP());
            exchangeCADValue.setText("CAD: "+Logic.getInstance().getExchangeRateCAD());
            enableButtons();
        });

        newStockField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    Utils.Log("()");
                    String newStockName = newStockField.getText().toUpperCase();
                    newStockField.setText("");
                    Logic.getInstance().getStock_yahoo(newStockName);
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

        ListSelectionModel rowSelectionModel = stockTable.getSelectionModel();
        rowSelectionModel.addListSelectionListener(e -> {
            if (! rowSelectionModel.isSelectionEmpty()) {
                int selectedRow = stockTable.getSelectedRow();
                String selectedData = (String) stockTable.getValueAt(selectedRow, 0);
                System.out.println("Selected: " + selectedData);
                initShareDetailInformation(selectedData);
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
        JMenu menu = new JMenu("Menu");
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

    public void initGeneralShareInfo(){
        totalInvestedValue.setText(Constants.currencyFormat.format(Logic.getInstance().getTotalInvested()*Logic.getInstance().getExchangeRateRON()) + " RON");
        totalProfitValue.setText(Constants.currencyFormat.format(Logic.getInstance().getTotalProfit()*Logic.getInstance().getExchangeRateRON()) + " RON");
        totalTaxValue.setText(Constants.currencyFormat.format(Logic.getInstance().getTotalTax()*Logic.getInstance().getExchangeRateRON()) + " RON");

        String percentLabelConsumerCyclical = " ("+Constants.currencyFormat.format(Logic.getInstance().getInvestmentPercent(DefaultLang.consumerCyclicalLabel))+"%)";
        String percentLabelConsumerDefensive = " ("+Constants.currencyFormat.format(Logic.getInstance().getInvestmentPercent(DefaultLang.consumerDefensiveLabel))+"%)";
        String percentLabelIndustrial = " ("+Constants.currencyFormat.format(Logic.getInstance().getInvestmentPercent(DefaultLang.industrialLabel))+"%)";
        String percentLabelEnergy = " ("+Constants.currencyFormat.format(Logic.getInstance().getInvestmentPercent(DefaultLang.energyLabel))+"%)";
        String percentLabelFinancialServices = " ("+Constants.currencyFormat.format(Logic.getInstance().getInvestmentPercent(DefaultLang.financialServicesLabel))+"%)";
        String percentLabelRealEstate = " ("+Constants.currencyFormat.format(Logic.getInstance().getInvestmentPercent(DefaultLang.realEstateLabel))+"%)";
        String percentLabelTechnology = " ("+Constants.currencyFormat.format(Logic.getInstance().getInvestmentPercent(DefaultLang.technologyLabel))+"%)";
        String percentLabelHealthCare = " ("+Constants.currencyFormat.format(Logic.getInstance().getInvestmentPercent(DefaultLang.healthcareLabel))+"%)";
        String percentLabelUtilities = " ("+Constants.currencyFormat.format(Logic.getInstance().getInvestmentPercent(DefaultLang.utilitiesLabel))+"%)";
        String percentLabelBasicMaterials = " ("+Constants.currencyFormat.format(Logic.getInstance().getInvestmentPercent(DefaultLang.basicMaterialsLabel))+"%)";
        String percentLabelCommunicationServices = " ("+Constants.currencyFormat.format(Logic.getInstance().getInvestmentPercent(DefaultLang.communicationServicesLabel))+"%)";

        DefaultPieDataset<String> dataset = new DefaultPieDataset<>();
        dataset.setValue(DefaultLang.consumerCyclicalLabel + percentLabelConsumerCyclical, Logic.getInstance().getInvestmentPercent(DefaultLang.consumerCyclicalLabel));
        dataset.setValue(DefaultLang.consumerDefensiveLabel + percentLabelConsumerDefensive, Logic.getInstance().getInvestmentPercent(DefaultLang.consumerDefensiveLabel));
        dataset.setValue(DefaultLang.industrialLabel + percentLabelIndustrial, Logic.getInstance().getInvestmentPercent(DefaultLang.industrialLabel));
        dataset.setValue(DefaultLang.energyLabel + percentLabelEnergy, Logic.getInstance().getInvestmentPercent(DefaultLang.energyLabel));
        dataset.setValue(DefaultLang.financialServicesLabel + percentLabelFinancialServices, Logic.getInstance().getInvestmentPercent(DefaultLang.financialServicesLabel));
        dataset.setValue(DefaultLang.realEstateLabel + percentLabelRealEstate, Logic.getInstance().getInvestmentPercent(DefaultLang.realEstateLabel));
        dataset.setValue(DefaultLang.technologyLabel + percentLabelTechnology, Logic.getInstance().getInvestmentPercent(DefaultLang.technologyLabel));
        dataset.setValue(DefaultLang.healthcareLabel + percentLabelHealthCare, Logic.getInstance().getInvestmentPercent(DefaultLang.healthcareLabel));
        dataset.setValue(DefaultLang.utilitiesLabel + percentLabelUtilities, Logic.getInstance().getInvestmentPercent(DefaultLang.utilitiesLabel));
        dataset.setValue(DefaultLang.basicMaterialsLabel + percentLabelBasicMaterials, Logic.getInstance().getInvestmentPercent(DefaultLang.basicMaterialsLabel));
        dataset.setValue(DefaultLang.communicationServicesLabel + percentLabelCommunicationServices, Logic.getInstance().getInvestmentPercent(DefaultLang.communicationServicesLabel));

        JFreeChart chart = ChartFactory.createPieChart("Pie Chart", dataset, true, true, false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(400, 300));
        pieChartPanel.add(chartPanel,new GridConstraints());
    }

    public void initShareDetailInformation(String shareSelected){

        shareDetailInformationPanel.setVisible(true);
        shareInvestmentLabel.setText(DefaultLang.shareInvestmentLabel);
        shareTaxLabel.setText(DefaultLang.shareTaxLabel);
        shareProfitLabel.setText(DefaultLang.shareProfitLabel);
        shareAnnouncementLabel.setText(DefaultLang.shareAnnouncementLabel);
        shareExDividendLabel.setText(DefaultLang.shareExDividendLabel);
        sharePayDayLabel.setText(DefaultLang.sharePayDayLabel);
        recommendedInvestmentLabel.setText(DefaultLang.recommendedInvestment);

        Stock_blob stockBlob = Logic.getInstance().getAddedStock(shareSelected);

        shareSelectedLabel.setText(stockBlob.getName());
        shareInvestmentValue.setText(Constants.dividendPayFormat.format(stockBlob.getInvestment()* Logic.getInstance().getExchangeRateRON())+" RON");
        shareTaxValue.setText(Constants.dividendPayFormat.format(Logic.getInstance().getShareTax(shareSelected)*Logic.getInstance().getExchangeRateRON())+" RON");
        shareProfitValue.setText(Constants.dividendPayFormat.format(stockBlob.getDivPerQ()*stockBlob.getOwnShares()*Logic.getInstance().getExchangeRateRON())+ " RON");
        shareAnnouncementValue.setText(String.valueOf(stockBlob.getAnnoucementDate()));
        shareExDividendValue.setText(String.valueOf(stockBlob.getExDividendDate()));
        sharePayDayValue.setText(String.valueOf(stockBlob.getPayDate()));
        recommendedInvestmentValue.setText(Constants.currencyFormat.format(Logic.getInstance().computeNecessaryInvestment(shareSelected))+" RON");

        shareNewsPane.setText(Logic.getInstance().getShareLatestNews(stockBlob.getName()));
        shareNewsPane.setSelectionStart(0);
        shareNewsPane.setSelectionEnd(0);
    }

    public void populateEditPanel(@NotNull Stock_blob stockBlob){
        Utils.Log("Load stock to be edited in Edit Form.");
        editStockPanel.setVisible(true);
        stockLabel.setText(stockBlob.getSymbol());
        divTextFiled.setText(String.valueOf(stockBlob.getDivPerQ()));
        ownShareTextField.setText(String.valueOf(stockBlob.getOwnShares()));
        sectorComboBox.setSelectedItem(stockBlob.getSector());
        investmentTextField.setText(Constants.dividendPayFormat.format(stockBlob.getInvestment()*Logic.getInstance().getExchangeRateRON()));
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


