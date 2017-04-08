
package othello;

import gamePlayer.Action;
import gamePlayer.Decider;
import gamePlayer.InvalidActionException;
import gamePlayer.State.Status;
import gamePlayer.algorithms.MTDDecider;

import gamePlayer.algorithms.MiniMaxDecider;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashMap;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

// This is based on GUI code from http://code.google.com/p/reversi-game/wiki/Reversi 

class GamePanel extends JPanel implements MouseListener {

	static final int Square_L = 33; // length in pixel of a square in the grid
	static final int Width = 8 * Square_L; // Width of the game board
	static final int Height = 8 * Square_L; // Width of the game board
	
	private static final long serialVersionUID = 1L;
	private OthelloState board;
	private Decider computerPlayer;
	private boolean turn;
	private boolean inputEnabled;
	private final boolean humanPlayerOne;
	public Othello othello;

	public GamePanel(Decider computerPlayer, OthelloState board, boolean computerStart) {
		this.board = board;
		this.computerPlayer = computerPlayer;
		this.turn = computerStart;
		this.humanPlayerOne = !computerStart;
		
		addMouseListener(this);
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Cursor savedCursor = getCursor();
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				if (turn) {
					computerMove();
				} else {
					inputEnabled = true;
				}
				setCursor(savedCursor);
			}
		});
		setBackground(Color.green);
	}

	protected void drawPanel(Graphics g) {
		// 每次重新画整个棋盘
		// int currentWidth = getWidth();
		// int currentHeight = getHeight();
		for (int i = 1; i < 8; i++) {
			g.drawLine(i *  Square_L, 0, i *  Square_L, Height);
		}
		g.drawLine(Width, 0, Width, Height);
		for (int i = 1; i < 8; i++) {
			g.drawLine(0, i *  Square_L,  Width, i *  Square_L);
		}
		g.drawLine(0,  Height,  Width,  Height);
		//System.out.println("Redrawing board\n" + board);
		for (byte i = 0; i < 8; i++)
			for (byte j = 0; j < 8; j++)
				switch (board.getSpotAsChar(j, i)) {
				case 'X':
					g.setColor(Color.white);
					g.fillOval(1 + i *  Square_L, 1 + j
							*  Square_L,  Square_L - 1,
							 Square_L - 1);
					break;
				case 'O':
					g.setColor(Color.black);
					g.fillOval(1 + i *  Square_L, 1 + j
							*  Square_L,  Square_L - 1,
							 Square_L - 1);
					break;
				}
	}

	@Override
	protected void paintComponent(Graphics arg0) {
		super.paintComponent(arg0);
		drawPanel(arg0);
	}

	public Dimension getPreferredSize() {
		return new Dimension( Width,  Height);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (!computerVsComputer&&inputEnabled) {
			byte j = (byte) (e.getX() / Square_L);
			byte i = (byte) (e.getY() / Square_L);
			OthelloAction a = new OthelloAction(humanPlayerOne, (byte) i, (byte) j);
			// lastAction = a;
			if (a.validOn(board)) {
				try {
					board = a.applyTo(board);
					board.getSpotAsChar(i, i);
					inputEnabled = false;
					updateScores();
				} catch (InvalidActionException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				/*
				 * score_black.setText(Integer.toString(board.getCounter(TKind.black
				 * )));
				 * score_white.setText(Integer.toString(board.getCounter(TKind
				 * .white)));
				 */
				repaint();
				javax.swing.SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						Cursor savedCursor = getCursor();
						setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						computerMove();
						setCursor(savedCursor);
					}
				});
			} else
				JOptionPane.showMessageDialog(this, "Illegal move", "Reversi",
						JOptionPane.ERROR_MESSAGE);
		}
	}

	public void computerMove() {
		if(computerVsComputer) OthelloState.maximizer=true;//开关赋值

		if (board.getStatus() != Status.Ongoing) {
			showWinner();
			return;
		}

		List<Action> actions;
		boolean isPass;
		do {
			System.out.println("Starting Computer Move");
			long startTime = System.currentTimeMillis();//获取开始时间
			OthelloAction action = (OthelloAction) computerPlayer.decide(board);
			long endTime = System.currentTimeMillis();//获取结束时间
			System.out.println("computerPlayer's decision time of this move is：" + (endTime - startTime) + " ms");
			try {
				board = action.applyTo(board);
				//System.out.println(board);
			} catch (InvalidActionException e) {
				throw new RuntimeException("Invalid action!");
			}
			repaint();
			actions = board.getActions();
			System.out.println("Finished with computer move\n");
			isPass = (actions.size() == 1 && ((OthelloAction) actions.get(0)).isPass());
			if (isPass) {
				try {
					board = (OthelloState) actions.get(0).applyTo(board);
					repaint();
				} catch (InvalidActionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				repaint();
			}
		} while (isPass && board.getStatus() == Status.Ongoing);


		// Next person's turn
		this.turn = !this.turn;//useless
		inputEnabled = true;
		updateScores();
		System.gc();

		if (board.getStatus() != Status.Ongoing) {
			showWinner();
			return;
		}

		//System.out.printf("%04x\n",board.dBoard1[7]);
		//System.out.printf("%04x\n",board.dBoard2[7]);
		if(computerVsComputer) secondComputerMove();
	}

	private static boolean computerVsComputer=true;
	public void secondComputerMove(){
		OthelloState.maximizer=false;//开关赋值
		//Decider
		OthelloAction a = (OthelloAction) new MiniMaxDecider(false, 7).decide(board);
		if (a.validOn(board)) {
			try {
				board = a.applyTo(board);
				inputEnabled = false;
				updateScores();
			} catch (InvalidActionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			repaint();
			javax.swing.SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					Cursor savedCursor = getCursor();
					setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
					computerMove();
					setCursor(savedCursor);
				}
			});
		} else
			JOptionPane.showMessageDialog(this, "Illegal move", "Reversi",
					JOptionPane.ERROR_MESSAGE);
	}

	private void showWinner() {
		int scores[] = board.getScores();
		if (scores[0] == scores[1]) {
			JOptionPane.showMessageDialog(this, "A Draw Game", "Reversi", JOptionPane.PLAIN_MESSAGE);
		} else if (((scores[1] > scores[0]) && humanPlayerOne) || ((scores[0] > scores[1]) && !humanPlayerOne)) {
			JOptionPane.showMessageDialog(this, "You Win", "Reversi", JOptionPane.PLAIN_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(this, "Computer Win", "Reversi", JOptionPane.PLAIN_MESSAGE);
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
	}

	private void updateScores() {
		int[] scores = board.getScores();
		othello.score_white.setText(scores[0] + "");
		othello.score_black.setText(scores[1] + "");
	}

}

public class Othello extends JFrame {

	public JLabel score_black;
	public JLabel score_white;
	private GamePanel gamePanel;

	static final int Square_L = 33; // length in pixel of a square in the grid
	static final int Width = 8 * Square_L; // Width of the game board
	static final int Height = 8 * Square_L; // Width of the game board

	private Point sToM(String move) {
		if (move.length() != 2) {
			throw new IllegalArgumentException();
		}
		HashMap<Character,Integer> charMap = new HashMap<Character, Integer>();
		charMap.put('a', 0);
		charMap.put('b', 1);
		charMap.put('c', 2);
		charMap.put('d', 3);
		charMap.put('e', 4);
		charMap.put('f', 5);
		charMap.put('g', 6);
		charMap.put('h', 7);
		
		char c1 = move.charAt(0);
		char c2 = move.charAt(1);
		
		Point p = new Point();
		p.x = Character.getNumericValue(c2) - 1;
		p.y = charMap.get(c1);
		return p;
		
	}
	
	public Othello(int whichPlayer, int nummilli, int maxDepth) {
		score_black = new JLabel("2"); // the game start with 2 black pieces
		score_black.setForeground(Color.black);
		score_black.setFont(new Font("Dialog", Font.BOLD, 16));
		score_white = new JLabel("2"); // the game start with 2 white pieces
		score_white.setForeground(Color.GRAY);
		score_white.setFont(new Font("Dialog", Font.BOLD, 16));

		OthelloState start = new OthelloState();
		start.setStandardStartState();
		/*
		String[] moves = "d3 c3 c4 c5 b5 d2 c2 f3 f5 c6 f4 a5 a6 a7 d1 e3 d6 a4 g3 e2 b4 c1 b1 e1 f1 a3 c7 f2 g2 g4 b3 h1".split(" "); // h4 b2 
		
		for (String s: moves) {
			Point p = sToM(s);
			start = start.childOnMove((byte)p.x, (byte)p.y);
		}
		
		boolean computerIsPlayerOne = false;
		boolean computerMovesFirst = false;
		*/
		boolean computerIsMaximizer = (whichPlayer == 1);
		boolean computerMovesFirst = computerIsMaximizer;
		
		gamePanel = new GamePanel(new MiniMaxDecider(computerIsMaximizer, maxDepth), start, computerMovesFirst);
		//gamePanel = new GamePanel(new MTDDecider(computerIsMaximizer, nummilli, 64), start, computerMovesFirst);

		gamePanel.setMinimumSize(new Dimension( Width, Height));

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel status = new JPanel();
		status.setLayout(new BorderLayout());
		status.add(score_black, BorderLayout.WEST);
		status.add(score_white, BorderLayout.EAST);

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				gamePanel, status);

		splitPane.setOneTouchExpandable(false);
		getContentPane().add(splitPane);
		gamePanel.othello = this;

		pack();
		setVisible(true);
		setResizable(false);

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// 0 - Human plays first, 1000ms - time for computer decision (for MTDDecider)
				Othello frame = new Othello(1, 1000, 5);
				/*稳定子权重比：对局结果*/
				//1:1 ：25:39,43:21,28:36,25:39,28:36,27:37,27:37,20:44,31:33,5:59,13:51,30:34,25:39,27:37,18:46   (1:14)
				//2:2 : 39:25,40:24,41:23,39:25,27:37,29:35,40:24,26:38,20:44,20:44,26:38,26:38,52:12,26:38,40:24  (7:8)
				//2:1 : 27:37,39:25,49:15,42:22,42:22,25:39,45:19,25:39,45:19,5:59,25:39,38:26,42:22,45:19,36:28   (10:5)
				//1:2 : 26:38,28:36,46:18,40:24,50:14,50:14,46:18,36:28,26:38,20:44,36:28,47:17,47:17,41:23,46:18  (11:4)
				//加上星位启发式函数：
				//2:0 : 38:26,41:23,25:39,43:21,39:25,27:37,47:17,42:22,28:36,50:14,25:39,44:20,27:37,43:21,25:39  (9:6)
				//0:2 : 27:37,18:46,14:50,27:37,19:45,25:39,20:44,48:16,25:39,27:37,27:37,27:37,18:46,27:37,27:37  (1:14)
				//		30:34,29:35,51:13,23:41,29:35,39:25,39:25,29:35,26:38,38:26,33:31,16:48,26:38,41:23,32:32  (6:8)
			}
		});

	}

}
