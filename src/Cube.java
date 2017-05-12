package src;

import java.util.ArrayList;
public class Cube extends GameObject{
    private static Integer textures = null;
    private static int width = 0;
    private static int height = 0;
    
    private static final String FILENAME = "cube.png";
    private static final int XOFFSET = 5;
    private static final int YOFFSET = 14;
    
    private static final float SCALE = 0.75f;
    
    private Sheep parent;
    private HookProjectile proj = null;
    private Level level;
    public Cube(Level level){
        super(0, 0);
        
        this.level = level;
        
        if(textures == null){
            int[] res = Image.loadImage(MyGame.PATH_BLUEJ + FILENAME);
            
            width = res[1];
            height = res[2];
            
            textures = res[0];  
        }
        
        setRawWidth(width);
        setRawHeight(height);
        setTexture(textures);
        
        setScaleX(SCALE);
        setScaleY(SCALE);
    }

    public void startMovingBack(HookProjectile proj){
        this.proj = proj;
    }
    
    public void stopMovingBack(){
        proj = null;
    }
    
    @Override
    public ArrayList<Object> update(){
        if(proj != null){
            setX(proj.getX());
            setY(proj.getY());
            return null;
        }else{
            boolean collided = false;
            for(GameObject x : level.getCollidableObjects()){
                collided = CollisionDetection.checkCollisionSimple(this, x) || collided;
            }
            
            if(!collided){
                setY(getY()+1);
            }
            return null;
        }
    }
}