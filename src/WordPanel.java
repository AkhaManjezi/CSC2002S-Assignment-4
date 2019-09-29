import javax.swing.*;
import java.awt.*;

public class WordPanel extends JPanel implements Runnable {
    public static volatile boolean done;
    public static volatile boolean repaint;
    public static volatile boolean paused;
    private WordRecord[] words;
    private int noWords;
    private int maxY;

    public void paintComponent(Graphics g) {
        int width = getWidth();
        int height = getHeight();
        g.clearRect(0, 0, width, height);
        g.setColor(Color.red);
        g.fillRect(0, maxY - 10, width, height);

        g.setColor(Color.black);
        g.setFont(new Font("Helvetica", Font.PLAIN, 26));
        //draw the words
        //animation must be added
        if (repaint) {
            for (int i = 0; i < noWords; i++) {
                g.drawString(words[i].getWord(), words[i].getX(), words[i].getY());
            }
        }

    }

    WordPanel(WordRecord[] words, int maxY) {
        this.words = words; //will this work?
        noWords = words.length;
        done = false;
        repaint = true;
        this.maxY = maxY;
    }

    public void run() {
        //add in code to animate this
        for (WordRecord word : words) {
            Thread wordThread = new Thread(() -> {    //creates a thread for each word to fall
                while (!done) {
                    int inc = Math.max(1, word.getSpeed() / maxY);    //if word.getSpeed()/maxY rounds down to 0, speed is 1
                    word.drop(inc);
                    repaint();
                    try {
                        Thread.sleep(40);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    while(paused){
                        try {
                            Thread.sleep(40);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            wordThread.start();
        }

    }

}


