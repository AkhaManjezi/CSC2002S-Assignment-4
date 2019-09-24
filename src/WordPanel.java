import java.awt.Color;
import java.awt.Graphics;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CountDownLatch;

import javax.swing.*;

public class WordPanel extends JPanel implements Runnable {
		public static volatile boolean done;
		private WordRecord[] words;
		private int noWords;
		private int maxY;
		private Score score;
		private JLabel missed;
		private JLabel scr;

		
		public void paintComponent(Graphics g) {
		    int width = getWidth();
		    int height = getHeight();
		    g.clearRect(0,0,width,height);
		    g.setColor(Color.red);
		    g.fillRect(0,maxY-10,width,height);

		    g.setColor(Color.black);
		    g.setFont(new Font("Helvetica", Font.PLAIN, 26));
		   //draw the words
		   //animation must be added 
		    for (int i=0;i<noWords;i++){	    	
		    	//g.drawString(words[i].getWord(),words[i].getX(),words[i].getY());	
		    	g.drawString(words[i].getWord(),words[i].getX(),words[i].getY()+20);  //y-offset for skeleton so that you can see the words	
		    }
		   
		  }
		
		WordPanel(WordRecord[] words, int maxY, Score score, JLabel missed, JLabel scr) {
			this.words=words; //will this work?
			noWords = words.length;
			done=false;
			this.maxY=maxY;
			this.score = score;
			this.missed = missed;
			this.scr = scr;
		}
		
		public void run() {
			//add in code to animate this
			while(!done) {
				for (WordRecord word : words) {
					int inc = Math.max(1, word.getSpeed()/maxY);
					word.drop(inc);
					int pos = word.getY();
					if (pos >= (maxY-10)) {
						word.resetWord();
						score.missedWord();
					}
				}
				repaint();
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
		}

	}


