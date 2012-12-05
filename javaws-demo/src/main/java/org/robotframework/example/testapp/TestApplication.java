package org.robotframework.example.testapp;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class TestApplication {
    private static String jnlpUrl = "http://localhost:14563/test-app/test-application.jnlp";
    private JPanel panel;
    private JFrame frame;

    public static void main(String[] args) {
        new TestApplication().runTestApplication();
    }

    public void runTestApplication() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createFrame();
                createMainPanel();
                addMenuBar();
                addComponentsToMainPanel();
                addMainPanelToFrame();
                showGUI();
            }
        });
    }

    private void showGUI() {
        frame.pack();
        frame.setVisible(true);
    }

    private void addMainPanelToFrame() {
        Container contentPane = frame.getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(panel, BorderLayout.CENTER);
    }

    private void createMainPanel() {
        panel = new JPanel();
        panel.setName("Main Panel");
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setAlignment(FlowLayout.CENTER);
        panel.setLayout(flowLayout);
    }

    private void addMenuBar() {
        frame.setJMenuBar(new MyMenuBar());
    }

    private void createFrame() {
        frame = new JFrame("Test App") {
            public Dimension getPreferredSize() {
                return new Dimension(500, 500);
            }
        };
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void addComponentsToMainPanel() {
        panel.add(new SystemExitButton());
        panel.add(new JButton("Start javaws application"){{
            addActionListener(new MyAction("javaws " + jnlpUrl));
        }});
        panel.add(new JButton("Start java application") {{
            addActionListener(new MyAction("java " + TestApplication.class.getName()));
        }});
    }
    
    private static class MyAction implements ActionListener {
        private final String command;

        public MyAction(String command) {
            this.command = command;
        }
         
        public void actionPerformed(ActionEvent e) {
            try {
                System.out.println("Executing "+command);
                Runtime.getRuntime().exec(command);
            } catch (IOException e1) {
                System.out.println("ERROR "+e1.getMessage());
                throw new RuntimeException(e1);
            }
        }
    }

}
