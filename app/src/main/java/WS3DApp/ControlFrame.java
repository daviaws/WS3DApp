package WS3DApp;

import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import javax.swing.*;
import ws3dproxy.WS3DProxy;
import ws3dproxy.model.Creature;
import ws3dproxy.model.Thing;
import ws3dproxy.model.World;

public class ControlFrame extends JFrame {

    private WS3DProxy proxy;
    private World world;
    private Creature creature;

    // UI
    private JButton btnUp, btnDown, btnLeft, btnRight, btnStop;
    private JList<String> visionList;
    private DefaultListModel<String> visionModel;
    private JScrollPane visionScroll;
    private Timer visionTimer;

    public ControlFrame(WS3DProxy proxy) {
        this.proxy = proxy;

        // 1) Criar painel de visão (precisa existir antes do layout)
        initVisionPanel();

        // 2) Criar layout completo
        initComponents();

        // 3) Mundo / criatura
        initWorld();

        // 4) Teclado GLOBAL (sem foco, sem Swing frescura)
        installGlobalKeyboard();

        // 5) Timer de atualização da visão
        startVisionTimer();

        // Janela
        setTitle("WS3D Control");
        setSize(400, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    /* ========================= UI ========================= */

    private void initComponents() {

        // Botões
        btnUp = new JButton("↑");
        btnDown = new JButton("↓");
        btnLeft = new JButton("←");
        btnRight = new JButton("→");
        btnStop = new JButton("STOP");

        btnUp.setFocusable(false);
        btnDown.setFocusable(false);
        btnLeft.setFocusable(false);
        btnRight.setFocusable(false);
        btnStop.setFocusable(false);

        btnUp.addActionListener(e -> moveUp());
        btnDown.addActionListener(e -> moveDown());
        btnLeft.addActionListener(e -> moveLeft());
        btnRight.addActionListener(e -> moveRight());
        btnStop.addActionListener(e -> stop());

        // Painel de controle (esquerda)
        JPanel controlPanel = new JPanel();
        GroupLayout gl = new GroupLayout(controlPanel);
        controlPanel.setLayout(gl);

        gl.setHorizontalGroup(
            gl.createParallelGroup(GroupLayout.Alignment.CENTER)
                .addComponent(btnUp)
                .addGroup(
                    gl.createSequentialGroup()
                        .addComponent(btnLeft)
                        .addGap(10)
                        .addComponent(btnStop)
                        .addGap(10)
                        .addComponent(btnRight)
                )
                .addComponent(btnDown)
        );

        gl.setVerticalGroup(
            gl.createSequentialGroup()
                .addGap(30)
                .addComponent(btnUp)
                .addGap(15)
                .addGroup(
                    gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(btnLeft)
                        .addComponent(btnStop)
                        .addComponent(btnRight)
                )
                .addGap(15)
                .addComponent(btnDown)
                .addGap(30)
        );

        // SplitPane
        JSplitPane split = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            controlPanel,
            visionScroll
        );
        split.setResizeWeight(0.4);
        split.setDividerLocation(260);

        setContentPane(split);
    }

    private void initVisionPanel() {
        visionModel = new DefaultListModel<>();
        visionList = new JList<>(visionModel);
        visionList.setFocusable(false);
        visionList.setBorder(
            BorderFactory.createTitledBorder("Things in Vision")
        );
        visionScroll = new JScrollPane(visionList);
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

    private void stop() {
        try { creature.move(0, 0, creature.getPitch()); }
        catch (Exception e) { e.printStackTrace(); }
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
                    case KeyEvent.VK_SPACE -> stop();
                }
                return false; // não consome
            });
    }

    /* ========================= VISION ========================= */

    private void startVisionTimer() {
        visionTimer = new Timer(500, e -> updateVision());
        visionTimer.start();
    }

    private void updateVision() {
        if (creature == null) return;

        try {
            creature.updateState();
            visionModel.clear();

            for (Thing t : creature.getThingsInVision()) {
                var p = t.getCenterPosition();
                visionModel.addElement(
                    String.format(
                        "%s (%.1f, %.1f)",
                        t.getName(),
                        p.getX(),
                        p.getY()
                    )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}