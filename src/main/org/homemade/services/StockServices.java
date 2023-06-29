package org.homemade.services;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StockServices implements Runnable, KeyListener {

    private boolean isRunning;

    public void start() {
        Thread thread = new Thread(this);
        thread.setDaemon(true); // Set the thread as a daemon thread
        isRunning = true;
        thread.start();
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public void run() {
        int i =0;
        while (isRunning) {
            // Perform your background tasks here
            System.out.println("Background service is running..."+i);
            StockServiceRunnable myRunnable = new StockServiceRunnable();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(myRunnable); // Execute the new thread
            executor.shutdown(); // Shutdown the executor when done

            try {
                Thread.sleep(60*1000); // Sleep for 1 second before the next iteration
            } catch (InterruptedException e) {
                // Handle interruption if necessary
            }
            i+=1;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used in this example
    }

    @Override
    public void keyPressed(KeyEvent e) {
        // Check if the "End" key is pressed
        if (e.getKeyCode() == KeyEvent.VK_END) {
            System.out.println("Stopping background service...");
            stop();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Not used in this example
    }
}
