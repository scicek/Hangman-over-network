import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;

/***************************************************************************************
 * Written by: Simon Cicek                                                             *
 * Last changed: 2012-04-13                                                            *
 ***************************************************************************************/

public class Main extends JFrame
{
    public Panel panel = new Panel();
    
    Main()
    {
        this.add(panel);
        this.setTitle("Client");
        this.setVisible(true);
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setSize(new Dimension(250, 250));
        this.addWindowListener(new Adapter());
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    
    public static void main(String[] args)
    {
        Main m = new Main();
    }
    
    private class Adapter extends WindowAdapter
    {
        // Makes sure to clean everything up (close all connections) when the client is closing
        @Override
        public void windowClosing(WindowEvent e)
        {
            // Client is closed after atleast one connection has been made
            if(panel.connectionHandler != null) 
            {
                // Client is closed while a connection is maintained
                if(panel.connectionHandler.socket != null && panel.connectionHandler.out != null) 
                    try{panel.connectionHandler.disconnect();}
                    catch(Exception ex){}
            }
            System.exit(0);
        }
    }
}
