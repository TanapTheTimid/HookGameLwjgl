package src;


import java.util.ArrayList;
public class HookableBlock extends GameObject{
    private static Integer texture;
    private static int width = 0;
    private static int height = 0;
    private static final String FILENAME = "HookableBlock.png";
    
    public static void loadTexture(){
        if(texture == null){
            int[] res = Image.loadImage(MyGame.PATH_BLUEJ + FILENAME);
            
            width = res[1];
            height = res[2];
            
            texture = res[0];
        }
    }
    
    public HookableBlock(int w, int h){
        super(w, h);

        loadTexture();
        
        setRawWidth(width);
        setRawHeight(height);
        setTexture(texture);
        
        float scaleX = (float)w/width;
        float scaleY = (float)h/height;
        
        setScaleX(scaleX);
        setScaleY(scaleY);
    }
    
    @Override
    public ArrayList<Object> update(){
        return null;
    }
}