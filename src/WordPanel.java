import javax.swing.*;
import java.awt.*;

public class WordPanel extends JPanel implements Runnable {
    public static volatile boolean done;
    public static volatile boolean repaint;
    private WordRecord[] words;
    private int noWords;
    private int maxY;
    private Score score;


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

    WordPanel(WordRecord[] words, int maxY, Score score) {
        this.words = words; //will this work?
        noWords = words.length;
        done = false;
        repaint = true;
        this.maxY = maxY;
        this.score = score;
    }

    public void run() {
        //add in code to animate this
        for (WordRecord word : words) {
            Thread wordThread = new Thread(() -> {    //creates a thread for each word to fall
                while (!done) {
                    int inc = Math.max(1, word.getSpeed() / maxY);    //if word.getSpeed()/maxY rounds down to 0, speed is 1
                    word.drop(inc);
                    int pos = word.getY();
                    if (pos >= (maxY - 10)) {    //if w word enters the red zone, it is reset and the missed word score is incremented
                        word.resetWord();
                        score.missedWord();
                    }
                    repaint();
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
            wordThread.start();
        }

    }

}


