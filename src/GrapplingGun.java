package src;


import java.util.ArrayList;
public class GrapplingGun extends GameObject{
    private static Integer[] textures = null;
    private static int width = 0;
    private static int height = 0;
    
    private static final String FILENAME[] = {"GrapplingGun.png","GrapplingGunRight.png"};
    private static final int XOFFSET = 5;
    private static final int YOFFSET = 14;

    private Sheep parent;
    
    public static void loadTexture(){
        if(textures == null){
            textures = new Integer[2];
            int[] res = Image.loadImage(MyGame.PATH_BLUEJ + FILENAME[0]);
            
            width = res[1];
            height = res[2];
            
            textures[0] = res[0];
            textures[1] = Image.loadImage(MyGame.PATH_BLUEJ + FILENAME[1])[0];
            
        }
    }
    
    public GrapplingGun(Sheep parent){
        super(0, 0);
        this.parent = parent;

        double w = parent.getWidth()/1.5;

        loadTexture();
        
        setRawWidth(width);
        setRawHeight(height);
        setTexture(textures[0]);
        
        float scale = (float)w/width;
        
        setScaleX(scale);
        setScaleY(scale);
    }

    @Override
    public ArrayList<Object> update(){
        
        int direction = parent.getDirection();
        if(direction == Sheep.RIGHT){
            setLocation(parent.getX()+10, parent.getY()+14);
            setTexture(textures[1]);
        }else{
            setLocation(parent.getX()-4, parent.getY()+14);
            setTexture(textures[0]);
        }
        
        return null;
        
    }
}