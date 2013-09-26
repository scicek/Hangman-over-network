import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class Main 
{
    private static int maxClients = 10;
    private static int port = 80;
    private static Executor executor; 
    public static final boolean PRINT_INFO = true;
    
    public static void main(String[] args) 
    {       
        try
        {
            ServerSocket socket;
            
            // Get input from command line
            if(args.length == 1)                
                port = Integer.parseInt(args[0]);
            else if(args.length == 2)
            {
                port = Integer.parseInt(args[0]);
                maxClients = Integer.parseInt(args[1]);
            }                            
            
            // Create a new server socket and a pool of threads
            socket = new ServerSocket(port);
            executor = Executors.newFixedThreadPool(maxClients);
            
            if(PRINT_INFO)
                System.out.println("Listening on port: " + port + ".\nAccepting: " + maxClients + " clients.");
            
            // Keep the server alive
            while(true)
            {
                try 
                {
                    // Accept a new client and handle it (if the number of current clients < maxClients)
                    Socket client = socket.accept();
                    if(PRINT_INFO)
                        System.out.println("New connection from: " + client.getInetAddress().toString().replaceFirst("/", ""));
                    executor.execute(new ClientHandler(client));
                } 
                catch(Exception ex){}
            }
        }
        catch(Exception e){}
    }
}
