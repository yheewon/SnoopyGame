
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class BulletGameFrame extends JFrame {

   public BulletGameFrame() {
      setTitle("스누피 맞추기");
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

      GamePanel p = new GamePanel();
      setContentPane(p);
      setSize(500, 350);
      setResizable(false);
      setVisible(true);
      p.startGame();
   }

   public static void main(String[] args) {
      new BulletGameFrame();
   }
}

class GamePanel extends JPanel {
   private TargetThread targetThread = null;
   private JLabel baseLabel = new JLabel();
   private JLabel bulletLabel = new JLabel();
   private JLabel targetLabel;
   private JLabel scoreLabel = new JLabel();
   public GamePanel() {
      setLayout(null);

      baseLabel.setSize(40, 40);
      baseLabel.setOpaque(true);
      baseLabel.setBackground(Color.LIGHT_GRAY);

      ImageIcon img = new ImageIcon("images/snoopy1.jpg");
      targetLabel = new JLabel(img);
      targetLabel.setSize(img.getIconWidth(), img.getIconWidth());

      bulletLabel.setSize(10, 10);
      bulletLabel.setOpaque(true);
      bulletLabel.setBackground(Color.PINK);

      scoreLabel = new JLabel("0");
      scoreLabel.setSize(50, 50);
      scoreLabel.setOpaque(true);

      add(baseLabel);
      add(targetLabel);
      add(bulletLabel);
      add(scoreLabel);

      // 이 패널에 마우스를 클릭하면 baseLabel이 키 입력을 받을 수 있도록 포커스를 강제 지정
      this.addMouseListener(new MouseAdapter() {
         @Override
         public void mousePressed(MouseEvent e) {
            baseLabel.setFocusable(true);
            baseLabel.requestFocus(); // 키보드 입력을 받도록 포커스 강제 지정
         }
      });
   }

   public void startGame() {
      baseLabel.setLocation(this.getWidth() / 2 - 20, this.getHeight() - 40);
      bulletLabel.setLocation(this.getWidth() / 2 - 5, this.getHeight() - 50);
      targetLabel.setLocation(0, 0);
      scoreLabel.setLocation(this.getWidth() - 50, this.getHeight() - 50);

      targetThread = new TargetThread(targetLabel);
      targetThread.start();

      baseLabel.setFocusable(true);
      baseLabel.requestFocus(); // 키보드 입력을 받도록 포커스 강제 지정
      baseLabel.addKeyListener(new KeyAdapter() {
         BulletThread bulletThread = null;

         @Override
         public void keyPressed(KeyEvent e) {
            if (e.getKeyChar() == '\n') {
               if (bulletThread == null || !bulletThread.isAlive()) {
                  bulletThread = new BulletThread(bulletLabel, targetLabel, targetThread, scoreLabel);
                  bulletThread.start();
               }
            }
         }
      });
   }

   class TargetThread extends Thread {
         private JComponent target;
         public TargetThread(JComponent target) {
            this.target = target;
            target.setLocation(0, 0);
            target.getParent().repaint();
         }   
         int x=0;
         int y=0;
         @Override
         public void run() {
           int left=0;//0:오른쪽 1=왼쪽
            while(true) {
              if(left==0) {
                 x = target.getX()+5;
                  y = target.getY();
              }
              else if(left==1) {
                  x = target.getX()-5;
                    y = target.getY();
              }
               if(x > GamePanel.this.getWidth()-50) {
                  left=1;
               }
               else if(x<0) {
                  left=0;
               }
               else
                  target.setLocation(x, y);

               target.getParent().repaint();
               try {
                  sleep(20);
               }
               catch(InterruptedException e) {
                  // the case of hit by a bullet
                  target.setLocation(0, 0);
                  target.getParent().repaint();
                  try {
                     sleep(500); // 0.5초 기다린 후에 계속한다.
                  }catch(InterruptedException e2) {}               
               }
            }
         }  
      }

   class BulletThread extends Thread {
      private JComponent bullet, target;
      private Thread targetThread;
      private JLabel score;
    
      public BulletThread(JComponent bullet, JComponent target, Thread targetThread, JLabel score) {
         this.bullet = bullet;
         this.target = target;
         this.targetThread = targetThread;
         this.score = score;
      }

   @Override
      public void run() {
         while (true) {
            // 명중하였는지 확인
            if (hit()) {
               targetThread.interrupt();
               bullet.setLocation(bullet.getParent().getWidth() / 2 - 5, bullet.getParent().getHeight() - 50);
               score.setText(showScore());
               return;
            } else {
               int x = bullet.getX();
               int y = bullet.getY() - 20;
               if (y < 0) {
                  bullet.setLocation(bullet.getParent().getWidth() / 2 - 5, bullet.getParent().getHeight() - 50);
                  bullet.getParent().repaint();
                  return; // thread ends
               }
               bullet.setLocation(x, y);
               bullet.getParent().repaint();

            }

            try {
               sleep(100);
            } catch (InterruptedException e) {
            }
         }
      }

      public String showScore() {
         String count = score.getText();
         int num = Integer.parseInt(count);
         num+=10;
         count = Integer.toString(num);
         return count;
      }

      private boolean hit() {
         if (targetContains(bullet.getX(), bullet.getY())
               || targetContains(bullet.getX() + bullet.getWidth() - 1, bullet.getY())
               || targetContains(bullet.getX() + bullet.getWidth() - 1, bullet.getY() + bullet.getHeight() - 1)
               || targetContains(bullet.getX(), bullet.getY() + bullet.getHeight() - 1))
            return true;
         else
            return false;
      }

      private boolean targetContains(int x, int y) {
         if (((target.getX() <= x) && (target.getX() + target.getWidth() - 1 >= x))
               && ((target.getY() <= y) && (target.getY() + target.getHeight() - 1 >= y))) {
            return true;
         } else
            return false;
      }
   }
}