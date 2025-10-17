

import javax.swing.SwingUtilities;
import za.ac.cput.studentenrollment.Gui.LoginGUI;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginGUI();
            }
        });
    }
}
