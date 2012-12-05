package org.robotframework.example.testapp;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class MyMenuBar extends JMenuBar {
    public MyMenuBar() {
        setName("testMenuBar");
        add(new MyMenu() {{
            add(new DialogMenuItem());
        }});
    }

    private static class MyMenu extends JMenu {
        public MyMenu() {
            super("Test Menu");
            setName("testMenu");
        }
    }

    private static class DialogMenuItem extends JMenuItem implements ActionListener {
        public DialogMenuItem() {
            super("Show Dialog");
            setName("showDialog");
            addActionListener(this);
        }
        
        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(this, "This is an example message");
        }
    }
}

