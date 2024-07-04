package QuestionsManager;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.undo.UndoManager;
import org.apache.batik.bridge.UpdateManager;
import org.apache.batik.swing.JSVGCanvas;
import org.apache.batik.swing.gvt.GVTTreeRendererAdapter;
import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
import org.apache.batik.swing.svg.GVTTreeBuilderAdapter;
import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
import org.apache.batik.swing.svg.LinkActivationEvent;
import org.apache.batik.swing.svg.LinkActivationListener;
import org.apache.batik.swing.svg.SVGDocumentLoaderAdapter;
import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;

public class TextEditor {
	private QueryManager qm;
	protected JFrame frame;

	public static void main(String[] args) {
		JFrame f = new JFrame("PROV-IDEA Provenance Inspector");
		TextEditor app = new TextEditor(f);

		f.getContentPane().add(app.createComponents());

		f.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		int heightFrame = 1000;
		int widthFrame = 1000;

		f.setSize(heightFrame, widthFrame);
		f.setVisible(true);
	}

	protected JTextArea sourceFileText = new JTextArea();
	protected JButton run = new JButton("Run");
	protected ButtonGroup group = new ButtonGroup();
	protected JRadioButton selectRadioButton = new JRadioButton();
	protected JRadioButton constructRadioButton = new JRadioButton();
	protected JRadioButton describeRadioButton = new JRadioButton();
	protected JRadioButton askRadioButton = new JRadioButton();
	protected JButton objMembers = new JButton("Get members");
	protected JButton objAttributes = new JButton("Get attributes");
	protected JButton objGenerator = new JButton("Get generator activity");
	protected JButton objDerivation = new JButton("Get derivations");
	protected JButton actStarter = new JButton("Get starter");
	protected JButton actSource = new JButton("Get Source Structure");
	protected JButton actTarget = new JButton("Get Target Structure");
	protected JTextField provElementID = new JTextField("");
	protected JEditorPane text;
	protected UndoManager textUndo;
	protected JLabel label = new JLabel();
	protected JSVGCanvas svgCanvas = new myJSVGCanvas();
	protected String stringResult = null;

	public TextEditor(JFrame f) {
		this.frame = f;
	}

	public JComponent createComponents() {

		this.qm = new QueryManager();
		this.sourceFileText.setColumns(80);
		this.svgCanvas.setSize(100, 50);
		this.svgCanvas.setDocumentState(1);
		this.svgCanvas.setEnableImageZoomInteractor(false);
		this.svgCanvas.setEnableRotateInteractor(false);
		this.svgCanvas.setEnableZoomInteractor(false);
		this.svgCanvas.setEnablePanInteractor(false);
		this.svgCanvas.setRequestFocusEnabled(true);
		this.svgCanvas.setUseUnixTextSelection(true);
		this.svgCanvas.setProgressivePaint(true);

		this.text = new JEditorPane("application/sparql-query", "");
		this.text.setFont(new Font("Courier", Font.PLAIN, 12));

		JPanel sparqlEditorPanel = new JPanel(new BorderLayout());
		sparqlEditorPanel.add(new JScrollPane(this.text));

		this.selectRadioButton.setText("select");
		this.describeRadioButton.setText("describe");
		this.constructRadioButton.setText("construct");
		this.askRadioButton.setText("ask");
		this.group.add(this.selectRadioButton);
		this.group.add(this.constructRadioButton);
		this.group.add(this.describeRadioButton);
		this.group.add(this.askRadioButton);

		JPanel runPanel = new JPanel(new GridLayout(1, 2));
		runPanel.add(this.run);
		runPanel.add(this.selectRadioButton);
		runPanel.add(this.describeRadioButton);
		runPanel.add(this.constructRadioButton);
		runPanel.add(this.askRadioButton);

		JPanel pLeft = new JPanel(new BorderLayout());
		pLeft.add(runPanel, "North");
		pLeft.add(sparqlEditorPanel, "Center");

		JPanel pRight = new JPanel(new BorderLayout(1, 1));
		pRight.setBackground(Color.WHITE);
		pRight.add(this.svgCanvas);

		JSplitPane panel = new JSplitPane(1, pLeft, pRight);

		this.textUndo = new UndoManager();
		this.text.getDocument().addUndoableEditListener(this.textUndo);

		this.text.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
				if ((e.getKeyCode() == 90) && ((e.getModifiers() & 0x2) != 0)) {
					TextEditor.this.textUndo.undo();
				} else if ((e.getKeyCode() == 89) && ((e.getModifiers() & 0x2) != 0)) {
					TextEditor.this.textUndo.redo();
				}
			}
		});

		this.run.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				qm.createInmemoryModel("data");
				String[] args = { TextEditor.this.text.getText() };
				try {
					if (TextEditor.this.selectRadioButton.isSelected()) {
						stringResult = TextEditor.this.qm.querySelect(TextEditor.this.text.getText());
					} else if (TextEditor.this.describeRadioButton.isSelected()) {
						TextEditor.this.qm.queryDescribe(TextEditor.this.text.getText());
					} else if (TextEditor.this.constructRadioButton.isSelected()) {
						TextEditor.this.qm.queryConstruct(TextEditor.this.text.getText());
					} else if (TextEditor.this.askRadioButton.isSelected()) {
						TextEditor.this.qm.queryAsk(TextEditor.this.text.getText());
					}
					if ((TextEditor.this.constructRadioButton.isSelected())
							|| (TextEditor.this.describeRadioButton.isSelected())
							|| (TextEditor.this.askRadioButton.isSelected())) {

						pRight.removeAll();
						pRight.add(TextEditor.this.svgCanvas);
						TextEditor.this.svgCanvas.removeAll();
						TextEditor.this.svgCanvas
								.setURI(new File("src/main/java/results/resultSet.svg").toURI().toString());

					} else if (TextEditor.this.selectRadioButton.isSelected()) {
						pRight.removeAll();
						int width = 100000;
						int height = 1000;

						BufferedImage img = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
								.getDefaultConfiguration().createCompatibleImage(2000, 2000);
						Graphics2D g2d = img.createGraphics();
						g2d.setBackground(Color.BLUE);

						Font font = new Font("Arial", Font.PLAIN, 48);
						g2d.setFont(font);
						FontMetrics fm = g2d.getFontMetrics();

						g2d.dispose();

						img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
						g2d = img.createGraphics();
						g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
								RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
						g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
						g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
								RenderingHints.VALUE_COLOR_RENDER_QUALITY);
						g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
						g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
								RenderingHints.VALUE_FRACTIONALMETRICS_ON);
						g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
								RenderingHints.VALUE_INTERPOLATION_BILINEAR);
						g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
						g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
						g2d.setFont(font);
						g2d.setColor(Color.DARK_GRAY);

						String path = "src/main/java/results/resultSet.txt";
						String stringResult2 = new String(stringResult.getBytes(), "UTF-8");
						Files.write(Paths.get(path), stringResult2.getBytes());

						File file = new File("src/main/java/results/resultSet.txt");

						BufferedReader br = null;
						int nextLinePosition = 100;
						int fontSize = 48;
						try {
							br = new BufferedReader(new FileReader(file));

							String line;
							while ((line = br.readLine()) != null) {
								g2d.drawString(line, 0, nextLinePosition);
								nextLinePosition = nextLinePosition + fontSize;
							}
							br.close();
						} catch (FileNotFoundException ex) {

						} catch (IOException ex) {

						}

						ImageIO.write(img, "png", new File("src/main/java/results/resultSet.png"));
						g2d.dispose();
						BufferedImage bimg = ImageIO.read(new File("src/main/java/results/resultSet.png"));
						int widthi = bimg.getWidth();
						int heighti = bimg.getHeight();
						int imageWidth = img.getWidth();
						int imageHeight = img.getHeight();
						JLabel label = new JLabel();
						label.setBounds(0, 0, imageWidth, imageHeight);

						pRight.setLayout(new BorderLayout());
						pRight.setAlignmentX(Component.CENTER_ALIGNMENT);

						ImageIcon icon = new ImageIcon("src/main/java/results/resultSet.png");
						Image image = icon.getImage();
						Image newimg = image.getScaledInstance(imageWidth / 4, imageHeight / 4,
								java.awt.Image.SCALE_SMOOTH); // scale it the smooth way
						ImageIcon imageIcon = new ImageIcon(newimg); // transform it back
						
						label.setIcon(imageIcon);
						label.setBounds(0, 0, imageWidth, imageHeight);

						pRight.add(label, BorderLayout.CENTER);
						pRight.validate();
						pRight.repaint();

					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		this.objAttributes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				try {

					String query = TextEditor.this.qm.queryConstruct(TextEditor.this.provElementID.getText());
					TextEditor.this.text.setText(query);
					File svgFile = new File("resultSet.svg");
					if (svgFile.exists()) {
						TextEditor.this.svgCanvas.removeAll();
						TextEditor.this.svgCanvas
								.setURI(new File("src/main/java/results/resultSet.svg").toURI().toString());
					}
				}

				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		this.svgCanvas.addLinkActivationListener(new LinkActivationListener() {
			public void linkActivated(LinkActivationEvent e) {
				try {
					URI uri = new URI(e.getReferencedURI());
					uri.getPath().replace("/", "");
					TextEditor.this.provElementID.setText("exe:" + uri.getPath().replace("/", ""));
					TextEditor.this.svgCanvas.setURI(new File("resultSet.svg").toURI().toString());
				} catch (URISyntaxException e1) {
					e1.printStackTrace();
				}
			}
		});
		this.svgCanvas.addMouseMotionListener(new MouseMotionListener() {
			Integer lastX = Integer.valueOf(0);
			Integer lastY = Integer.valueOf(0);

			public void mouseMoved(MouseEvent e) {
				Point p = TextEditor.this.svgCanvas.getMousePosition();
				if (p != null) {
					this.lastX = Integer.valueOf((int) p.getX());
					this.lastY = Integer.valueOf((int) p.getY());
				}
			}

			public void mouseDragged(MouseEvent e) {
				Point p = TextEditor.this.svgCanvas.getMousePosition();
				if (p != null) {
					if ((this.lastX == null) || (this.lastY == null)) {
						this.lastX = Integer.valueOf((int) p.getX());
						this.lastY = Integer.valueOf((int) p.getY());
					}
					AffineTransform transform = TextEditor.this.svgCanvas.getRenderingTransform();
					transform.translate((p.getX() - this.lastX.intValue()) / transform.getScaleX(),
							(p.getY() - this.lastY.intValue()) / transform.getScaleX());
					TextEditor.this.svgCanvas.setRenderingTransform(transform);

					this.lastX = Integer.valueOf((int) p.getX());
					this.lastY = Integer.valueOf((int) p.getY());
				}
			}
		});
		this.svgCanvas.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(final MouseWheelEvent e) {
				UpdateManager updateManager = TextEditor.this.svgCanvas.getUpdateManager();
				if (updateManager != null) {
					updateManager.getUpdateRunnableQueue().invokeLater(new Runnable() {
						public void run() {
							double scale = 1.0D - e.getUnitsToScroll() / 10.0D;
							double tx = -e.getX() * (scale - 1.0D);
							double ty = -e.getY() * (scale - 1.0D);

							AffineTransform at = new AffineTransform();
							if (e.isControlDown()) {
								at.translate(tx, ty);
								at.scale(scale, scale);
							} else if (e.isShiftDown()) {
								at.translate(-tx, 0.0D);
							} else {
								at.translate(0.0D, -ty);
							}
							at.concatenate(TextEditor.this.svgCanvas.getRenderingTransform());
							if (at.getScaleX() > 0.1D) {
								TextEditor.this.svgCanvas.setRenderingTransform(at);
							}
						}
					});
				}
			}
		});
		this.svgCanvas.addSVGDocumentLoaderListener(new SVGDocumentLoaderAdapter() {
			public void documentLoadingStarted(SVGDocumentLoaderEvent e) {
				TextEditor.this.label.setText("Document Loading...");
			}

			public void documentLoadingCompleted(SVGDocumentLoaderEvent e) {
				TextEditor.this.label.setText("Document Loaded.");
			}
		});
		this.svgCanvas.addGVTTreeBuilderListener(new GVTTreeBuilderAdapter() {
			public void gvtBuildStarted(GVTTreeBuilderEvent e) {
				TextEditor.this.label.setText("Build Started...");
			}

			public void gvtBuildCompleted(GVTTreeBuilderEvent e) {
				TextEditor.this.label.setText("Build Done.");
			}
		});
		this.svgCanvas.addGVTTreeRendererListener(new GVTTreeRendererAdapter() {
			public void gvtRenderingPrepare(GVTTreeRendererEvent e) {
				TextEditor.this.label.setText("Rendering Started...");
			}

			public void gvtRenderingCompleted(GVTTreeRendererEvent e) {
				TextEditor.this.label.setText("");
			}
		});
		return panel;
	}

}
