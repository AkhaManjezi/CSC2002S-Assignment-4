import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;
//model is separate from the view.

public class WordApp {
    //shared variables
    static int noWords = 4;
    static int totalWords;

    static int frameX = 1000;
    static int frameY = 600;
    static int yLimit = 480;

    static WordDictionary dict = new WordDictionary(); //use default dictionary, to read from file eventually

    static WordRecord[] words;
    static volatile boolean done;  //must be volatile
    static Score score = new Score();

    static WordPanel w;

    static JLabel caught;
    static JLabel missed;
    static JLabel scr;

    static boolean running;
    static Thread animations;
    static JTextField textEntry;
    static JButton endB;
    static JButton pauseB;

    public static void setupGUI(int frameX, int frameY, int yLimit) {
        // Frame init and dimensions
        JFrame frame = new JFrame("WordGame");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(frameX, frameY);
        frame.setResizable(false);

        JPanel g = new JPanel();
        g.setLayout(new BoxLayout(g, BoxLayout.PAGE_AXIS));
        g.setSize(frameX, frameY);


        w = new WordPanel(words, yLimit);
        w.repaint = false;
        w.setSize(frameX, yLimit + 100);
        g.add(w);


        JPanel txt = new JPanel();
        txt.setLayout(new BoxLayout(txt, BoxLayout.LINE_AXIS));
        caught = new JLabel("Caught: " + score.getCaught() + "    ");
        missed = new JLabel("Missed:" + score.getMissed() + "    ");
        scr = new JLabel("Score:" + score.getScore() + "    ");
        txt.add(caught);
        txt.add(missed);
        txt.add(scr);

        pauseB = new JButton("Pause");

        textEntry = new JTextField("", 20);
        textEntry.addActionListener(new ActionListener() {
            //when the user enters a guess, this checks if is correct and updates accordingly
            public void actionPerformed(ActionEvent evt) {
                String text = textEntry.getText();
                //[snip]
                int wordCount;
                for (WordRecord word : words) {
                    wordCount = word.getWord().length();
                    if (word.matchWord(text)) {
                        score.caughtWord(wordCount);
                        caught.setText("Caught: " + score.getCaught() + "    ");
                        scr.setText("Score:" + score.getScore() + "    ");
                        break;
                    }
                }
                textEntry.setText("");
                textEntry.requestFocus();

                if (score.getCaught() >= totalWords) {
                    int finalScore = score.getScore();
                    int finalCaught = score.getCaught();
                    int finalMissed = score.getMissed();
                    int totalWords = score.getTotal();
                    w.done = true;
                    textEntry.setEnabled(false);
                    JOptionPane.showMessageDialog(w, String.format("Your score was %s\nYou caught %s words\nYou missed %s words\nYou interacted with %s words", finalScore, finalCaught, finalMissed, totalWords), "You won", JOptionPane.INFORMATION_MESSAGE);
                    endB.setEnabled(false);
                    pauseB.setEnabled(false);
                    reset();
                }
            }
        });

        txt.add(textEntry);
        txt.setMaximumSize(txt.getPreferredSize());
        g.add(txt);

        JPanel b = new JPanel();
        b.setLayout(new BoxLayout(b, BoxLayout.LINE_AXIS));
        JButton startB = new JButton("Start");

        running = false;
        animations = new Thread(w);


        // add the listener to the jbutton to handle the "pressed" event
        startB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //if the the game has finished and the user wants to restart, a new session loads
                textEntry.setEnabled(true);
                endB.setEnabled(true);
                pauseB.setEnabled(true);
                pauseB.setText("Pause");
                if (w.done) {
                    w.paused = false;
                    w.done = false;
                    w.repaint = true;
                    w.repaint();
                    w.run();
                    textEntry.setEnabled(true);
                } else {
                    w.paused = false;
                    //if a game is already running and a user wants to restart, new words are loaded and the game starts again; otherwise, the game starts for the first time
                    if (running) {
                        score.resetScore();
                        for (WordRecord word : words) {
                            word.resetWord();
                        }
                        w.repaint();
                        caught.setText("Caught: " + score.getCaught() + "    ");
                        scr.setText("Score:" + score.getScore() + "    ");
                        missed.setText("Missed:" + score.getMissed() + "    ");
                        animations.run();
                    } else {
                        w.repaint = true;
                        w.repaint();
                        animations.start();
                    }
                }
                running = true;
                textEntry.requestFocus();  //return focus to the text entry field
            }
        });

         endB = new JButton("End");
        ;

        //when the end button is pressed, the game is stopped by calling the reset method
        endB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //[snip]
                reset();
                endB.setEnabled(false);
                pauseB.setEnabled(false);
            }
        });

        endB.setEnabled(false);
        JButton quitB = new JButton("Quit");
        ;

        // when the quit button is pressed, the animations stop and the application closes
        quitB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                animations.interrupt();
                System.exit(0);
            }
        });
        ;

        // when the quit button is pressed, the animations stop and the application closes
        pauseB.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(!w.paused){
                    w.paused = true;
                    textEntry.setEnabled(false);
                    pauseB.setText("Resume");
                }else{
                    w.paused = false;
                    textEntry.setEnabled(true);
                    pauseB.setText("Pause");
                    textEntry.requestFocus();
                }
            }
        });
        pauseB.setEnabled(false);

        b.add(startB);
        b.add(pauseB);
        b.add(endB);
        b.add(quitB);

        g.add(b);

        frame.setLocationRelativeTo(null);  // Center window on screen.
        frame.add(g); //add contents to window
        frame.setContentPane(g);
        //frame.pack();  // don't do this - packs it into small space
        frame.setVisible(true);


    }


    public static String[] getDictFromFile(String filename) {
        String[] dictStr = null;
        try {
            Scanner dictReader = new Scanner(new FileInputStream(filename));
            int dictLength = dictReader.nextInt();
            //System.out.println("read '" + dictLength+"'");

            dictStr = new String[dictLength];
            for (int i = 0; i < dictLength; i++) {
                dictStr[i] = new String(dictReader.next());
                //System.out.println(i+ " read '" + dictStr[i]+"'"); //for checking
            }
            dictReader.close();
        } catch (IOException e) {
            System.err.println("Problem reading file " + filename + " default dictionary will be used");
        }
        return dictStr;

    }

    public static void main(String[] args) {

        //deal with command line arguments
        totalWords = Integer.parseInt(args[0]);  //total words to fall
        noWords = Integer.parseInt(args[1]); // total words falling at any point
        assert (totalWords >= noWords); // this could be done more neatly
        String[] tmpDict = getDictFromFile(args[2]); //file of words
        if (tmpDict != null)
            dict = new WordDictionary(tmpDict);

        WordRecord.dict = dict; //set the class dictionary for the words.

        words = new WordRecord[noWords];  //shared array of current words


        setupGUI(frameX, frameY, yLimit);
        //Start WordPanel thread - for redrawing animation


        int x_inc = (int) frameX / noWords;
        //initialize shared array of current words

        for (int i = 0; i < noWords; i++) {
            words[i] = new WordRecord(dict.getNewWord(), i * x_inc, yLimit);
        }

        //updates the number of missed words and checks if word limit has been reached

        for(WordRecord word : words){
            Thread wordThread = new Thread(() -> {    //creates a thread for each word to fall
                while (!false) {
                    int pos = word.getY();
                    if (pos >= (yLimit - 10)) {    //if w word enters the red zone, it is reset and the missed word score is incremented
                        word.resetWord();
                        score.missedWord();
                        missed.setText("Missed:" + score.getMissed() + "    ");
                    }
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

    /**
     * resets the score and clears the GUI
     */
    private static void reset() {
        score.resetScore();
        w.paused = false;
        w.repaint = false;
        w.repaint();
        running = false;
        w.done = true;
        pauseB.setText("Pause");
        for (WordRecord word : words) {
            word.resetWord();
        }
        caught.setText("Caught: " + score.getCaught() + "    ");
        scr.setText("Score:" + score.getScore() + "    ");
        missed.setText("Missed:" + score.getMissed() + "    ");
    }

}
