package org.homemade.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.*;
import java.net.http.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.homemade.Constants;
import org.homemade.services.graphics.FrmPopUpInfo;

public class ServicesLogic {

    private static ServicesLogic instance;
    private double totalValue = 0;
    private double gainValue =0;

    private String smallCoinGain = "D:\\Projects\\Universal_App\\src\\main\\resources\\coin-drop-39914.mp3";
    private String mediumCoinGain = "D:\\Projects\\Universal_App\\src\\main\\resources\\coin-spill-105867.mp3";
    private String largeCoinGain = "D:\\Projects\\Universal_App\\src\\main\\resources\\spilled-coins-101296.mp3";

    private String coinLose = "D:\\Projects\\Universal_App\\src\\main\\resources\\things-falling-on-a-wooden-floor-81650.mp3";


    public static ServicesLogic getInstance() {
        if (instance == null){
            instance = new ServicesLogic();
        }
        return instance;
    }

    public void callBackgroundService(JFrame frame) {
        StockServices service = new StockServices();
        service.start();

//        frame.addKeyListener(service);

    }

    public void getTradingMetaData(){
        var httpClient = HttpClient.newBuilder().build();

        var host = "https://live.trading212.com/";
        var pathname = "/api/v0/equity/account/cash";
        var request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(host + pathname ))
                .header("Authorization", "20614454ZCczhJrQYZafKBhcMeqBEDihLXjBh")
                .build();

        HttpResponse<String> response = null;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        System.out.println(response.body());

        if (response.body().contains("total")){
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode jsonNode = objectMapper.readTree(response.body());
                double total = jsonNode.get("total").asDouble();
                System.out.println("Total value: " + total);
                double diff = total - totalValue;
                System.out.println("Value diff: "+ diff);
                if (totalValue != 0) {
                    gainValue += diff;
                }

                if (diff > 0){
                    if (diff > 1) {
                        playSound2(largeCoinGain);
                    }
                    if ((diff > 0.5) && (diff < 1)){
                        playSound2(mediumCoinGain);
                    }
                    if (diff <= 0.5){
                        playSound2(smallCoinGain);
                    }
                }else {
                    playSound2(coinLose);
                }

                new FrmPopUpInfo("Total: "+Constants.currencyFormat.format(diff) + ". Gain: "+ Constants.currencyFormat.format(gainValue));
                totalValue = total;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void playSound2(String soundPath){
        Media hit = new Media(new File(soundPath).toURI().toString());
        MediaPlayer mediaPlayer = new MediaPlayer(hit);
        mediaPlayer.play();
    }

}
