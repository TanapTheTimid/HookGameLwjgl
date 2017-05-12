package src;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.stb.STBImage.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.opengl.*;
import org.lwjgl.glfw.*;
import java.nio.*;
public class Image{
    public static int[] loadImage(String path){
        int texture = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texture);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        int width = 0;
        int height = 0;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer image = stbi_load(path,w,h,comp,4);
            if(image == null){
                System.err.println("failed to load image");
            }

            width = w.get();
            height = h.get();

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        }
        
        return new int[]{texture, width, height};
    }
    
    //returns object[] consisting of 0 = ByteBuffer image, 1=width, 2=height
    public static Object[] loadImageToByteBuffer(String path){
        Object[] value = new Object[3];
        
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer image = stbi_load(path,w,h,comp,4);
            if(image == null){
                System.err.println("failed to load image");
            }
            value[0] = image;
            value[1] = w.get();
            value[2] = h.get();
        }
        return value;
    }
}
