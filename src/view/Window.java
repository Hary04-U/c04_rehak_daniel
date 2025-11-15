package view;

import javax.swing.*;

public class Window extends JFrame {

    private final Pane panel;

    public Window(int width, int height) {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setTitle("PGRF1 2024/2025");

        panel = new Pane(width, height);
        add(panel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        panel.setFocusable(true);
        panel.grabFocus();
    }

    public Pane getPanel() {
        return panel;
    }


}
