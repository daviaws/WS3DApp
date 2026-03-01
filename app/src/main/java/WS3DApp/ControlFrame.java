package WS3DApp;

import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import javax.swing.*;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Bag;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;
import ws3dproxy.model.World;
import ws3dproxy.util.Constants;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DocumentFilter;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

public class ControlFrame extends JFrame {
    private static final double CAPTURE_DISTANCE = 25.0;
    private static int creatureColor = 0;
    private int highlightedIndex = -1;

    private WS3DProxy proxy;
    private World world;
    private Creature creature;

    // Creation UI
    private JComboBox<String> comboAction;
    private JComboBox<String> comboColor;
    private JTextField txtX, txtY, txtX2, txtY2;
    private JButton btnDo;
    private JPanel actionPanel;

    // UI
    private JButton btnUp, btnDown, btnLeft, btnRight, btnCapture, btnEat;
    private JList<String> visionList;
    private DefaultListModel<String> visionModel;
    private JScrollPane visionScroll;
    private Timer visionTimer;
    private DefaultListModel<String> bagModel;
    private JList<String> bagList;
    private JScrollPane bagScroll;
    private Timer bagTimer;

    public ControlFrame(WS3DProxy proxy) {
        this.proxy = proxy;

        initVisionPanel();

        initBagPanel();

        initActions();

        initComponents();

        setupTooltips();

        initWorld();

        installGlobalKeyboard();

        startVisionTimer();

        startBagTimer();

        // Janela
        setTitle("WS3D Control");
        setSize(620, 450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    /* ====================== Creation UI ====================== */

    private void initActions() {       
        comboAction = new JComboBox<>(new String[] {
            "Create Creature",
            "Create Food (Apple)",
            "Create Food (Nut)",
            "Create Jewel",
            "Create Brick",
            "Move Creature"
        });

        comboColor = new JComboBox<>(new String[] {
            "RED", "GREEN", "BLUE", "YELLOW", "MAGENTA", "WHITE"
        });

        txtX  = new JTextField(5);
        txtY  = new JTextField(5);
        txtX2 = new JTextField(5);
        txtY2 = new JTextField(5);

        btnDo = new JButton("DO");
        btnDo.setFocusable(false);
        btnDo.addActionListener(e -> {
            try {
                String action = comboAction.getSelectedItem().toString();

                double x = Double.parseDouble(txtX.getText());
                double y = Double.parseDouble(txtY.getText());

                switch (action) {

                    // ================= CREATE =================

                    case "Create Creature" -> {
                        Creature c = proxy.createCreature(x, y, 0, ++creatureColor);
                        c.start();
                        creature = c; // criatura ativa
                    }

                    case "Create Food (Apple)" -> {
                        World.createFood(0, x, y); // perishable
                    }

                    case "Create Food (Nut)" -> {
                        World.createFood(1, x, y); // non-perishable
                    }

                    case "Create Jewel" -> {
                        int color = comboColor.getSelectedIndex();
                        World.createJewel(color, x, y);
                    }

                    case "Create Brick" -> {
                        double x2 = Double.parseDouble(txtX2.getText());
                        double y2 = Double.parseDouble(txtY2.getText());
                        int color = comboColor.getSelectedIndex();
                        World.createBrick(color, x, y, x2, y2);
                    }

                    // ================= MOVE =================

                    case "Move Creature" -> {
                        if (creature == null) {
                            JOptionPane.showMessageDialog(
                                this,
                                "No creature created yet.",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE
                            );
                            return;
                        }

                        World.getInstance().createWaypoint(x, y); // visual
                        creature.moveto(2.0, x, y);
                    }
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                    this,
                    "Invalid coordinates.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        comboAction.setFocusable(false);
        comboColor.setFocusable(false);

        btnDo.setFocusable(false);

        // focus on text box usability
        DocumentFilter onlyDigits = new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                    throws BadLocationException {
                if (string != null && string.matches("\\d*")) {
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                    throws BadLocationException {
                if (text != null && text.matches("\\d*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        };

        for (JTextField tf : new JTextField[]{txtX, txtY, txtX2, txtY2}) {
            ((AbstractDocument) tf.getDocument()).setDocumentFilter(onlyDigits);
            InputMap im = tf.getInputMap(JComponent.WHEN_FOCUSED);
            im.put(KeyStroke.getKeyStroke("UP"), "none");
            im.put(KeyStroke.getKeyStroke("DOWN"), "none");
            im.put(KeyStroke.getKeyStroke("LEFT"), "none");
            im.put(KeyStroke.getKeyStroke("RIGHT"), "none");
            im.put(KeyStroke.getKeyStroke("SPACE"), "none");
        }

        actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.Y_AXIS));
        actionPanel.setBorder(BorderFactory.createTitledBorder("Actions"));

        /* Action */
        actionPanel.add(new JLabel("Action"));
        actionPanel.add(comboAction);
        actionPanel.add(Box.createVerticalStrut(5));

        /* Color row */
        JPanel colorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        colorPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        colorPanel.add(new JLabel("Color"));
        colorPanel.add(comboColor);
        colorPanel.setVisible(false);
        actionPanel.add(colorPanel);
        actionPanel.add(Box.createVerticalStrut(5));

        /* X / Y row */
        JPanel xyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        xyPanel.add(new JLabel("X"));
        xyPanel.add(txtX);
        xyPanel.add(new JLabel("Y"));
        xyPanel.add(txtY);
        actionPanel.add(xyPanel);
        actionPanel.add(Box.createVerticalStrut(5));

        /* X2 / Y2 row */
        JPanel x2y2Panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        x2y2Panel.add(new JLabel("X2"));
        x2y2Panel.add(txtX2);
        x2y2Panel.add(new JLabel("Y2"));
        x2y2Panel.add(txtY2);
        x2y2Panel.setVisible(false);
        actionPanel.add(x2y2Panel);
        actionPanel.add(Box.createVerticalStrut(10));

        /* DO button */
        actionPanel.add(btnDo);

        comboAction.addActionListener(e -> {
            boolean isBrick = comboAction.getSelectedItem().equals("Create Brick");
            boolean isJewel = comboAction.getSelectedItem().equals("Create Jewel");

            colorPanel.setVisible(isBrick || isJewel);
            x2y2Panel.setVisible(isBrick);

            actionPanel.revalidate();
            actionPanel.setMinimumSize(actionPanel.getPreferredSize());
            actionPanel.repaint();
        });
    }

    /* ========================= UI ========================= */

    private void initComponents() {

        // Botões
        btnUp = new JButton("↑");
        btnDown = new JButton("↓");
        btnLeft = new JButton("←");
        btnRight = new JButton("→");
        btnCapture = new JButton("CAPTURE");
        btnEat = new JButton("EAT");

        btnUp.setFocusable(false);
        btnDown.setFocusable(false);
        btnLeft.setFocusable(false);
        btnRight.setFocusable(false);
        btnCapture.setFocusable(false);
        btnEat.setFocusable(false);

        btnUp.addActionListener(e -> moveUp());
        btnDown.addActionListener(e -> moveDown());
        btnLeft.addActionListener(e -> moveLeft());
        btnRight.addActionListener(e -> moveRight());
        btnCapture.addActionListener(e -> stopAndCapture());
        btnEat.addActionListener(e -> eatNearest());

        // Painel de controle (esquerda)
        JPanel controlPanel = new JPanel();
        GroupLayout gl = new GroupLayout(controlPanel);
        controlPanel.setLayout(gl);

        gl.setHorizontalGroup(
            gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addGroup(
                    gl.createSequentialGroup()
                        .addContainerGap()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(btnUp)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(btnEat)
                        .addContainerGap()
                )
                .addGroup(
                    gl.createSequentialGroup()
                        .addComponent(btnLeft)
                        .addGap(10)
                        .addComponent(btnCapture)
                        .addGap(10)
                        .addComponent(btnRight)
                )
                .addComponent(btnDown)
        );



        gl.setVerticalGroup(
            gl.createSequentialGroup()
                .addGap(30)
                .addGroup(
                    gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(btnUp)
                        .addComponent(btnEat)
                )
                .addGap(15)
                .addGroup(
                    gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(btnLeft)
                        .addComponent(btnCapture)
                        .addComponent(btnRight)
                )
                .addGap(15)
                .addComponent(btnDown)
                .addGap(30)
        );

        // SplitPane
        JSplitPane rightSplit = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            visionScroll,
            bagScroll
        );
        rightSplit.setResizeWeight(0.6);

        JSplitPane leftSplit = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT,
            controlPanel,
            actionPanel
        );
        leftSplit.setResizeWeight(0.0);

        JSplitPane mainSplit = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            leftSplit,
            rightSplit
        );

        setContentPane(mainSplit);
    }

    private void initVisionPanel() {
        visionModel = new DefaultListModel<>();
        visionList = new JList<>(visionModel);
        visionList.setFocusable(false);
        visionList.setBorder(
            BorderFactory.createTitledBorder("Things in Vision")
        );
        visionScroll = new JScrollPane(visionList);

        visionList.setCellRenderer(new DefaultListCellRenderer() {

            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                label.setBackground(Color.WHITE);
                label.setForeground(Color.BLACK);

                if (index == highlightedIndex) {
                    label.setBackground(Color.YELLOW);
                    label.setForeground(Color.BLACK);
                }

                if (isSelected) {
                    label.setBackground(label.getBackground().darker());
                }

                return label;
            }
        });
    }

    private void initBagPanel() {
        bagModel = new DefaultListModel<>();
        bagList = new JList<>(bagModel);
        bagList.setFocusable(false);
        bagList.setBorder(
            BorderFactory.createTitledBorder("Bag")
        );
        bagScroll = new JScrollPane(bagList);
    }

    /* ========================= WORLD ========================= */

    private void initWorld() {
        try {
            world = World.getInstance();
            world.reset();

            World.createFood(0, 350, 75);
            World.createFood(0, 100, 220);
            World.createJewel(0, 10, 50);
            World.createBrick(3, 500, 200, 505, 300);
            World.createDeliverySpot(250, 250);

            creature = proxy.createCreature(100, 450, 0);
            creature.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ========================= MOVEMENT ========================= */

    private void moveUp() {
        try { creature.move(1, 1, creature.getPitch()); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void moveDown() {
        try { creature.move(-1, -1, creature.getPitch()); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void moveLeft() {
        try { creature.move(1, -1, creature.getPitch()); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void moveRight() {
        try { creature.move(-1, 1, creature.getPitch()); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void stopAndCapture() {
        try { creature.move(0, 0, creature.getPitch()); }
        catch (Exception e) { e.printStackTrace(); }
        captureNearest();
    }

    private void captureNearest() {
        try {
            for (Thing t : creature.getThingsInVision()) {
                if (t.getName().startsWith("Brick") 
                    || t.getName().contains("Food")
                    || t.getName().startsWith("Delivery")) {

                    continue;
                }
                if (creature.calculateDistanceTo(t) <= CAPTURE_DISTANCE) {
                    creature.putInSack(t.getName());
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasPerishableFood(Bag bag) {
        return bag.getNumberPFood() > 0;
    }

    private boolean hasNonPerishableFood(Bag bag) {
        return bag.getNumberNPFood() > 0;
    }

    private void eatNearest() {
        try {
            Thing food = null;

            for (Thing t : creature.getThingsInVision()) {

                if (!t.getName().contains("Food")) {
                    continue;
                }

                if (creature.calculateDistanceTo(t) > CAPTURE_DISTANCE) {
                    continue;
                }

                food = t;
            }

            if (food != null) {
                creature.eatIt(food.getName());
                return;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ========================= KEYBOARD (GLOBAL, SEM FOCO) ========================= */

    private void installGlobalKeyboard() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(e -> {

                if (creature == null) return false;
                if (e.getID() != KeyEvent.KEY_PRESSED) return false;

                switch (e.getKeyCode()) {
                    case KeyEvent.VK_UP -> moveUp();
                    case KeyEvent.VK_DOWN -> moveDown();
                    case KeyEvent.VK_LEFT -> moveLeft();
                    case KeyEvent.VK_RIGHT -> moveRight();
                    case KeyEvent.VK_SPACE -> stopAndCapture();
                    case KeyEvent.VK_E -> eatNearest();
                }
                return false; // não consome
            });
    }

    /* ========================= VISION ========================= */

    private void startVisionTimer() {
        visionTimer = new Timer(250, e -> updateVision());
        visionTimer.start();
    }

    private void updateVision() {
        if (creature == null) return;

        try {
            creature.updateState();
            visionModel.clear();
            highlightedIndex = -1;

            int index = 0;

            for (Thing t : creature.getThingsInVision()) {
                var p = t.getCenterPosition();
                double dist = creature.calculateDistanceTo(t);

                visionModel.addElement(
                    String.format(
                        "%s (%.1f, %.1f)  d: %.1f",
                        t.getName(),
                        p.getX(),
                        p.getY(),
                        dist
                    )
                );

                if (highlightedIndex == -1
                    && dist <= CAPTURE_DISTANCE
                    && !t.getName().startsWith("Brick")
                    && !t.getName().startsWith("Delivery")) {
                    highlightedIndex = index;
                }

                index++;
            }

            visionList.repaint();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ========================= BAG ========================= */
    private void updateBag() {
        if (creature == null) return;

        try {
            Bag bag = creature.updateBag();
            if (bag == null) return;

            bagModel.clear();

            bagModel.addElement("Crystals total: " + bag.getTotalNumberCrystals());
            bagModel.addElement("  RED: " + bag.getNumberCrystalPerType(Constants.colorRED));
            bagModel.addElement("  GREEN: " + bag.getNumberCrystalPerType(Constants.colorGREEN));
            bagModel.addElement("  BLUE: " + bag.getNumberCrystalPerType(Constants.colorBLUE));
            bagModel.addElement("  YELLOW: " + bag.getNumberCrystalPerType(Constants.colorYELLOW));
            bagModel.addElement("  MAGENTA: " + bag.getNumberCrystalPerType(Constants.colorMAGENTA));
            bagModel.addElement("  WHITE: " + bag.getNumberCrystalPerType(Constants.colorWHITE));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startBagTimer() {
        bagTimer = new Timer(500, e -> updateBag());
        bagTimer.start();
    }

    private void setupTooltips() {
        ToolTipManager.sharedInstance().setInitialDelay(300);
        ToolTipManager.sharedInstance().setReshowDelay(100);
        ToolTipManager.sharedInstance().setDismissDelay(4000);

        btnUp.setToolTipText("Move forward (↑)");
        btnDown.setToolTipText("Move backward (↓)");
        btnLeft.setToolTipText("Rotate left (←)");
        btnRight.setToolTipText("Rotate right (→)");
        btnCapture.setToolTipText("Capture (SPACE)");
        btnEat.setToolTipText("Eat food (E)");
    }
}