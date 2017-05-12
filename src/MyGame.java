package src;

// terminal launch command = java -XstartOnFirstThread -cp .:+libs_macos/* -Djava.library.path=+libs_macos/ MyGame

public class MyGame{
    public static final int WIDTH = 700;
    public static final int HEIGHT = 700;
    public static final String PATH_NONBLUEJ = "/Volumes/Poom/JavaxSwingGame/res/"; 
    public static final String PATH_BLUEJ = "res/";
    public static final String CURSOR_FILE = "Cursor.png";
    public static final int WINDOW_OFFSET_X = 50;
    public static final int WINDOW_OFFSET_Y = 50;
    public static final int BORDER_WIDTH = 5;
    
    public static void main(String[] args){
        Game.init("Hook", PATH_BLUEJ + CURSOR_FILE, WIDTH, HEIGHT, WINDOW_OFFSET_X, WINDOW_OFFSET_Y, BORDER_WIDTH);
        GameLoop.init(new Level1(), WIDTH, HEIGHT, BORDER_WIDTH);
        GameLoop.start();
    }
}