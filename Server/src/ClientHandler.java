import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

// Handles all communication with a client
public class ClientHandler implements Runnable
{
    Message msg;
    Socket client;
    ObjectInputStream in;
    ObjectOutputStream out;
    int score = 0, allowedAttempts = 0;
    String word, guessedLetters;
    
    ClientHandler(Socket s) throws Exception
    {
        try
        {
            client = s;
            in  = new ObjectInputStream(s.getInputStream());
            out = new ObjectOutputStream(client.getOutputStream());
        }
        catch(Exception ex){}
        newWord();
    }
    
    // Selects a new word from the list of words and sets allowed number of guesses
    private void newWord() throws Exception
    {
        File f = new File("words.txt");
        BufferedReader reader = new BufferedReader(new FileReader(f));
        for(int i = 0; i < new Random().nextInt((int)(f.length() - 1)); i++)
            reader.readLine();
        
        word = reader.readLine();
        guessedLetters = new String(new char[word.length()]).replace('\0', '-');
        allowedAttempts = word.length() * 2;
        reader.close();         
        System.out.println(word);
    }
    
    // Adds a valid letter to the current view of the word
    private void addValidLetter(String l)
    {
        // Make sure it's a single letter
        if(l.length() != 1)
            return;
        char c = l.charAt(0); 
        // Array used to mark where in the word the letter appears
        boolean[] letters = new boolean[word.length()]; 
        
        // Mark where the letter appears
        for(int i = 0; i < word.length(); i++)
            if(word.charAt(i) == c)
                letters[i] = true;
        
        // Insert the letter into the current view of the word 
        char[] chars = guessedLetters.toCharArray();
        for(int i = 0; i < letters.length; i++)
            if(letters[i] == true)
                chars[i] = c;
        guessedLetters = new String(chars);
    }
    
    // Sends the current message and flushes the stream
    private void sendMessage()
    {
        try
        {
            out.writeObject(msg);
            out.flush();            
        }
        catch(Exception e){}
    }
    
    // Sends a new message indicating that a new game has been started
    private void sendNewGame()
    {
        msg = new Message(Message.NEW_GAME, 0, allowedAttempts, null, guessedLetters);
        sendMessage();
        
        if(Main.PRINT_INFO)
            System.out.println("Client: " + client.getInetAddress().toString().replaceFirst("/", "") + "  started a new game!");
    }

    // Sends a new message indicating that the client has won
    private void sendCongrats()
    {
        ++score;
        msg = new Message(Message.WIN, score, 0, word, null);
        sendMessage();
        
        if(Main.PRINT_INFO)
            System.out.println("Client: " + client.getInetAddress().toString().replaceFirst("/", "") + "  won! Score: " + score);
    }    
    
    // Sends a new message indicating that the client has guessed a single letter right
    private void sendRightGuess()
    {
        msg = new Message(Message.RIGHT_GUESS, 0, allowedAttempts, null, guessedLetters);
        sendMessage();
        
        if(Main.PRINT_INFO)
            System.out.println("Client: " + client.getInetAddress().toString().replaceFirst("/", "") + 
                                "  guessed right!  " + guessedLetters + "   " + allowedAttempts);
    }
    
    // Sends a new message indicating that the client has guessed a single letter wrong
    // or that the client has lost depending on the value fo the number of allowed guesses
    private void sendWrongGuess()
    {
        if(--allowedAttempts > 0)
        {
            msg = new Message(Message.WRONG_GUESS, 0, allowedAttempts, null, guessedLetters);
            sendMessage();
            
            if(Main.PRINT_INFO)
                System.out.println("Client: " + client.getInetAddress().toString().replaceFirst("/", "") + 
                                    "  guessed wrong!  " + guessedLetters + "   " + allowedAttempts);
        }
        else
        {            
            --score;
            msg = new Message(Message.LOSE, score, 0, null, null);
            sendMessage();
         
            if(Main.PRINT_INFO)
                System.out.println("Client: " + client.getInetAddress().toString().replaceFirst("/", "") + " lost! Score: " + score);
        }
    }
    
    @Override
    public void run() 
    {
        // Runs as long as a connection to the client is maintained
        while(!client.isClosed())
        {
            if(in == null) // Something went wrong when getting the input stream so it is pointless to go on
                break;
            try 
            { 
                Message message = (Message) in.readObject();
                if(message != null) // Nothing was sent from the client
                {
                    if(message.flag == Message.NEW_GAME) // Client wants to start a new game
                    {
                        newWord();
                        sendNewGame();
                        continue;
                    }
                    else if(message.flag == Message.CLOSE_CONNECTION) // Client terminated the connection
                    {
                        in.close();
                        out.close();
                        client.close();
                        continue;
                    }
                    
                    if(message.clientGuess.length() == 1) // Client sent a single letter
                    {
                        if(word.contains(message.clientGuess)) // The letter is found in the word
                        {
                            addValidLetter(message.clientGuess);
                            if(!guessedLetters.contains("-")) // The client has guessed the entire word
                                sendCongrats();
                            else
                                sendRightGuess();
                        }
                        else // The client guessed wrong
                            sendWrongGuess();        
                    }
                    else // The client guesses a word
                    {
                        if(word.equals(message.clientGuess))
                            sendCongrats();
                        else
                            sendWrongGuess();
                    }
                }
                else 
                    Thread.yield(); // The thread gives up its timeslice if the client does not send anything
            } 
            catch (Exception ex){}
        }
        if(Main.PRINT_INFO)
            System.out.println("Client: " + client.getInetAddress().toString().replaceFirst("/", "") + "  has disconnected.");
    }
}
