package org.robotframework.example;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class Application {
    public static void main(String[] args) {
        startApplicationIfCredentialsAreOK();
    }

    private static void startApplicationIfCredentialsAreOK() {
        String password = JOptionPane.showInputDialog("Password please (hint: it's 'robot')");
        
        if ("robot".equals(password)) {
            startApplication();
        } else {
            System.exit(1);
        }
    }

    private static void startApplication() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                JFrame frame = new JFrame("Main Application");
                frame.setPreferredSize(new Dimension(300, 300));
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.getContentPane().setLayout(new FlowLayout(FlowLayout.CENTER, 100, 5));
                frame.getContentPane().add(new JLabel() {{
                    setText("Welcome!");
                    setName("title_label");
                }});
                frame.getContentPane().add(new JPanel() {{
                    add(new JButton("Exit") {{
                        addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent e) {
                                System.exit(0);
                            }
                        });
                        setName("exit_button");
                    }});  
                }});
                
                frame.pack();
                frame.setVisible(true);
            }
        });
    }
}
