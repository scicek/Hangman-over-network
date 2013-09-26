import java.io.Serializable;

// This class will be used to pass information between client and server
public class Message implements Serializable
{
    static final long serialVersionUID = -7588980448693010399L;
    
    // Flags
    public static final int NEW_GAME = -1; 
    public static final int WIN = -2; 
    public static final int LOSE = -3;
    public static final int RIGHT_GUESS = -4;
    public static final int WRONG_GUESS = -5;
    public static final int CLOSE_CONNECTION = -6;
    
    public int flag;
    public int score, allowedAttempts;
    public String word, guessedLetters;
    public String clientGuess;
    
    Message(int f)
    {
        flag = f;
    }
    
    Message(int f, String cg)
    {
        flag = f;
        clientGuess = cg;
    }
    
    Message(int f, int s, int a, String w, String g)
    {
        flag = f;
        score = s;
        allowedAttempts = a;
        word = w;
        guessedLetters = g;
    }
}
