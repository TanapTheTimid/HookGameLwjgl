package src;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.stb.STBImage.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.opengl.*;
import org.lwjgl.glfw.*;
import java.nio.*;

public class Game{
    public static long window;

    public static void init(String title, String cursorFile
                            , int width, int height, int winOffsetX, int winOffsetY, int borderWidth){
        if(!glfwInit()){
            System.err.println("GLFW initialization failed");
        }

        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);
        glfwWindowHint(GLFW_FOCUSED, GL_TRUE);
        glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
        
        window = glfwCreateWindow(width+borderWidth*2, height+borderWidth*2, title, NULL, NULL);

        if(window == NULL){
            System.err.println("Could not create window!");
        }

        //unused line
        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        //==============
        
        glfwSetKeyCallback(window, new InputHandler.KeyboardInput());
        glfwSetCursorPosCallback(window, new InputHandler.MouseInput());
        glfwSetMouseButtonCallback(window, new InputHandler.MouseAction());

        glfwSetWindowPos(window, winOffsetX, winOffsetY);
        glfwMakeContextCurrent(window);
        glfwShowWindow(window);

        GL.createCapabilities();

        glClearColor(0.90f,0.90f,0.90f,1);
        glOrtho(0,width+borderWidth, height+borderWidth,0,-1,1);
        
        enableGlFlags();
        
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glfwSwapInterval(1);
        glHint( GL_LINE_SMOOTH_HINT, GL_NICEST ) ;
        
        setCursorImage(cursorFile);
    }
    
    private static void enableGlFlags(){
        glEnable(GL_BLEND);
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_LINE_SMOOTH) ;
    }
    
    private static void setCursorImage(String cursorFile){
        if(cursorFile != null && !cursorFile.isEmpty()){
            Object[] values = Image.loadImageToByteBuffer(cursorFile);
            ByteBuffer buffer = (ByteBuffer) values[0];
            GLFWImage cursorImage = GLFWImage.create();
            cursorImage.width((int)values[1]);
            cursorImage.height((int)values[2]);
            cursorImage.pixels(buffer);
            
            long cursorID = glfwCreateCursor(cursorImage, (int)values[1]/2, (int)values[2]/2);        
            glfwSetCursor(window, cursorID);
            
            //glfwSetCursor(window, glfwCreateStandardCursor(GLFW_CROSSHAIR_CURSOR));
        }
    }
    
    public static long getWindowId(){
        return window;
    }
}