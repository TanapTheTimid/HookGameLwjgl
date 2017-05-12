package src;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.Collection;
import java.util.Arrays;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.stb.STBImage.*;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.opengl.*;
import org.lwjgl.glfw.*;
import java.nio.*;

public class Sheep extends GameObject implements Physics2D.Mass{
    private static Integer[] texturesLeft;
    private static Integer[] texturesRight;
    private static int width;
    private static int height;

    public static final float MASS = 10;

    public static final float SCALE = 0.75f;
    public static final int LEFT = -1;
    public static final int RIGHT = 1;
    private static final float WALK_SPEED = 2;
    private static final float GRAVITY = 0.6f;
    private static final float ACCELERATION_X = 1.5f;
    private static final float JUMP_SPEED = 7;
    private static final float TERMINAL_VELOCITY = 20;
    private static final int PULL_SPEED_FACTOR = 10;
    private static final int OFFSET_BELOW_HOOKED_BLOCK = 3;
    private static final int MAX_HOOK_DELAY_FRAMES = 10;
    private static final int HOOK_DESTINATION_PROXIMITY = 6;
    private static final int MAX_OVERSHOOT = 30;
    private static final int OFFSET_SIDE_CORNER = 5;
    private static final String[] FILENAME = {"Sheep.png","Sheep1.png","SheepFlip.png","Sheep1Flip.png"};

    private Level currentLevel;

    private float velocityY;
    private float velocityX;
    private int direction = LEFT;
    private boolean canHook = true;
    private boolean mouseWasUp = true;
    private boolean pulling = false;
    private float pullTargetX;
    private float pullTargetY;
    private int hookPlacementDirection;
    private float[] unitVector = new float[2];
    private HookProjectile hookProjectile;
    private int hookDelayFrames = 0;
    private boolean finishedPullingAndLanded = true;

    private float acceleration;

    public float getVelocityX(){return velocityX;}

    public float getVelocityY(){return velocityY;}

    public float getMass(){return MASS;}

    public float getAccelerationX(){return acceleration;}

    public void setVelocityX(float x){velocityX = x;}

    public void setVelocityY(float y){velocityY = y;}

    public void setAccelerationX(float a){acceleration = a;}

    public static void loadTexture(){
        if(texturesLeft == null && texturesRight == null){
            texturesLeft = new Integer[2];
            texturesRight = new Integer[2];
            int[] res = Image.loadImage(MyGame.PATH_BLUEJ + FILENAME[0]);

            width = res[1];
            height = res[2];

            texturesLeft[0] = res[0];
            texturesLeft[1] = Image.loadImage(MyGame.PATH_BLUEJ + FILENAME[1])[0];
            texturesRight[0] = Image.loadImage(MyGame.PATH_BLUEJ + FILENAME[2])[0];
            texturesRight[1] = Image.loadImage(MyGame.PATH_BLUEJ + FILENAME[3])[0];
        }
    }

    public Sheep(Level level){
        super(0,0);

        currentLevel = level;

        loadTexture();

        setRawWidth(width);
        setRawHeight(height);
        setTexture(texturesLeft[0]);
        setScaleX(SCALE);
        setScaleY(SCALE);
    }

    private int counter;

    public ArrayList<Object> update(){
        //handles reset
        if(InputHandler.isKeyDown(GLFW_KEY_R)){
            setLocation(150,3);
            velocityY = 0;
            pulling = false;
            passedDestination = false;
            if(hookProjectile != null){
                hookProjectile.destroyHook();
            }
        }

        //sets texture according to the movement
        if(velocityX < 0){
            if(finishedPullingAndLanded){
                setTexture(texturesLeft[counter/4]);
                counter = (counter + 1) % 8;
            }
            direction = LEFT;
        }else if(velocityX > 0){
            if(finishedPullingAndLanded){
                setTexture(texturesRight[counter/4]);
                counter = (counter + 1) % 8;
            }
            direction = RIGHT;
        }
        
        //set correct texture
        if(pulling || velocityX == 0){
            if(direction == LEFT){
                setTexture(texturesLeft[0]);
            }else{
                setTexture(texturesRight[0]);
            }
        }

        //calls update method
        if(!pulling){
            return normalUpdate();
        }else{
            pullingUpdate();
            return null;
        }

    }
    private static final int MAX_PASS = 3;
    private int passedCount = 3;
    private boolean passedDestination = false;
    private boolean hitTopCorner = false;
    private void pullingUpdate(){
        float dx = getX() - pullTargetX;
        float dy = getY() - pullTargetY;
        float mag = dx*dx + dy*dy;

        float proxSquared = HOOK_DESTINATION_PROXIMITY * HOOK_DESTINATION_PROXIMITY;
        if(!passedDestination
        && 
        (mag > proxSquared)){
            unitVector = calculateUnitVector(pullTargetX,pullTargetY);
        }else if(mag < proxSquared){
            passedDestination = true;

        }else if(passedDestination && mag > proxSquared){
            velocityX = 0;
            pulling = false;
            hookProjectile.destroyHook();
            passedDestination = false;
            return;
        }

        LinkedHashMap<GameObject, Integer> collisions = new LinkedHashMap<>();

        double currentXVect = unitVector[0];
        double currentYVect = unitVector[1];
        while(Math.abs(currentXVect) < Math.abs(unitVector[0] * PULL_SPEED_FACTOR)
        || Math.abs(currentYVect) < Math.abs(unitVector[1] * PULL_SPEED_FACTOR) ){
            for(GameObject obj: currentLevel.getCollidableObjects()){
                int result = CollisionDetection.checkCollisionVelocityAware
                    (this,currentXVect, currentYVect, obj,0,0);
                if(result != CollisionDetection.NONE && !collisions.containsKey(obj)){
                    collisions.put(obj, result); 
                }
            }
            currentXVect += unitVector[0];
            currentYVect += unitVector[1];
        }
        Collection values =  collisions.values();
        if(!collisions.isEmpty()){
            Set<GameObject> collisionObj = collisions.keySet();
            for(GameObject obj: collisionObj){
                if(obj instanceof HookableBlock){
                    int dir = collisions.get(obj);
                    float x = getX();
                    float y = getY();
                    if(dir == CollisionDetection.BOTTOM){
                        y = obj.getY() - getHeight();
                    }else if(dir == CollisionDetection.TOP){
                        y = obj.getY() + getHeight() + OFFSET_BELOW_HOOKED_BLOCK;
                    }else if(dir == CollisionDetection.LEFT){
                        x = obj.getX() + obj.getWidth();
                    }else if(dir == CollisionDetection.RIGHT){
                        x = obj.getX() - getWidth();
                    }else if(dir == CollisionDetection.TOP_LEFT){
                        //y =
                        x = obj.getX() + obj.getWidth() +OFFSET_SIDE_CORNER;
                        hitTopCorner = true;
                    }else if(dir == CollisionDetection.TOP_RIGHT){
                        //y =
                        x = obj.getX() - obj.getWidth() -OFFSET_SIDE_CORNER;
                        hitTopCorner = true;
                    }else if(dir == CollisionDetection.BOTTOM_LEFT){
                        y = obj.getY() - getHeight();
                        x = obj.getX() + obj.getWidth();
                    }else if(dir == CollisionDetection.BOTTOM_RIGHT){
                        y = obj.getY() - getHeight();
                        x = obj.getX() - obj.getWidth();
                    }
                    setX(x);
                    setY(y);
                    velocityY = 0;
                    velocityX = 0;
                    pulling = false;
                    passedDestination = false;
                    hookProjectile.destroyHook();
                    return;
                }

            }
        }

        Set dirs = collisions.keySet();

        float vx = unitVector[0] * PULL_SPEED_FACTOR;
        float vy = unitVector[1] * PULL_SPEED_FACTOR;

        if(dirs.contains(CollisionDetection.LEFT)){
            if(vx < 0){
                vx = 0;
            }
        }else if(dirs.contains(CollisionDetection.RIGHT)){
            if(vx > 0){
                vx = 0;
            }
        }

        if(dirs.contains(CollisionDetection.TOP)){
            if(vy < 0){
                vy = 0;
            }
        }else if(dirs.contains(CollisionDetection.BOTTOM)){
            if(vy > 0){
                vy = 0;
            }
        }

        velocityY = vy;
        velocityX = vx;

        setX(getX() + vx);
        setY(getY() + vy);
    }

    private float[] calculateUnitVector(float x, float y){
        float deltaX = x - getX();
        float deltaY = y - getY();
        float magnitude = (float)Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        float[] unitVector = new float[2];
        unitVector[0] = deltaX / magnitude;
        unitVector[1] = deltaY / magnitude;

        return unitVector;
    }

    private ArrayList<Object> normalUpdate(){
        ArrayList<Object> returnVal = null;

        ArrayList<Integer> collisionDir =  CollisionDetection
            .objectCollisionSurfaceAreaAwareHelper
            (this,velocityX,velocityY,0,0, currentLevel.getCollidableObjects());
        if(InputHandler.isKeyDown(GLFW_KEY_D) 
        && !(
            collisionDir.contains(CollisionDetection.RIGHT) ||
            collisionDir.contains(CollisionDetection.TOP_RIGHT) //||
                //collisionDir.contains(CollisionDetection.BOTTOM_RIGHT)
        ) && !hitTopCorner ){
            acceleration = ACCELERATION_X;
        }else if(InputHandler.isKeyDown(GLFW_KEY_A)
        && !(
            collisionDir.contains(CollisionDetection.LEFT) ||
            collisionDir.contains(CollisionDetection.TOP_LEFT) //||//comented out because checking bottom left/right will cause getting stuck
                //collisionDir.contains(CollisionDetection.BOTTOM_LEFT)
        ) && !hitTopCorner ){
            acceleration = -ACCELERATION_X;
        }else{
            if(Math.abs(velocityX) < 1){
                velocityX = 0;
            }else{
                acceleration = 0;
                velocityX -= (velocityX/Math.abs(velocityX)) * ACCELERATION_X;
            }
        }
        float absVel = (float) Math.abs(velocityX);
        if(absVel < WALK_SPEED || acceleration/velocityX < 0){
            velocityX += acceleration;
        }
        if(absVel > WALK_SPEED){
            velocityX = WALK_SPEED * (velocityX/absVel);
        }
        setX(getX()+velocityX);

        hitTopCorner = false;

        if((
            collisionDir.contains(CollisionDetection.BOTTOM) ||
            collisionDir.contains(CollisionDetection.BOTTOM_RIGHT) ||
            collisionDir.contains(CollisionDetection.BOTTOM_LEFT)
        )){
            finishedPullingAndLanded = true;
            if(InputHandler.isKeyDown(GLFW_KEY_W) && velocityY == 0){
                velocityY = -JUMP_SPEED;
            }else if(velocityY > 0){
                velocityY = 0;
            }
        }else if((
            collisionDir.contains(CollisionDetection.TOP) ||
            collisionDir.contains(CollisionDetection.TOP_LEFT) ||
            collisionDir.contains(CollisionDetection.TOP_RIGHT)
        )){
            if(velocityY < 0){
                velocityY = 0;
            }
        }else{
            if(velocityY < TERMINAL_VELOCITY){
                velocityY += GRAVITY;
            }
        }

        setY(getY() + velocityY);

        if(InputHandler.isMouseDown() && canHook && mouseWasUp && hookDelayFrames <= 0){
            hookDelayFrames = MAX_HOOK_DELAY_FRAMES;
            canHook = false;
            mouseWasUp = false;
            returnVal = new ArrayList<>();
            HookProjectile proj = new HookProjectile(getIntX(),getIntY(),InputHandler.getMouseX(),InputHandler.getMouseY(),currentLevel, this);
            HookLine hk = proj.getHookLine();

            returnVal.add(GameLoop.OPERATION_ADD);
            returnVal.add(proj);
            returnVal.add(hk);
        }else if(!InputHandler.isMouseDown()){
            mouseWasUp = true;
        }

        if(hookDelayFrames > 0)hookDelayFrames--;

        return returnVal;
    }

    public void startPull(float x, float y, int dir, HookProjectile hookProjectile){
        setY(getY() - 2);
        pullTargetX = x;
        pullTargetY = y;
        hookPlacementDirection = dir;
        pulling = true;
        passedCount = MAX_PASS;
        finishedPullingAndLanded = false;
        hitTopCorner = false;
        this.hookProjectile = hookProjectile;
    }

    public void setHookable(boolean hookable){
        canHook = hookable;
    }

    public int getDirection(){
        return direction;
    }
}
/*
 * private void pullingUpdate(){
if((Math.abs(getX() - pullTargetX) < 7 && Math.abs(getY() - pullTargetY) < 7)){
float x = hookProjectile.getX();
float y = getY();
if(hookPlacementDirection == CollisionDetection.BOTTOM){
y = hookProjectile.getY() - Math.abs(hookProjectile.getHeight() - getHeight());
}else if(hookPlacementDirection == CollisionDetection.TOP){
y = hookProjectile.getY() + OFFSET_BELOW_HOOKED_BLOCK;
}else if(hookPlacementDirection == CollisionDetection.LEFT){
//x = hookProjectile.getX();
}else if(hookPlacementDirection == CollisionDetection.RIGHT){
//x = hookProjectile.getX();
}else if(hookPlacementDirection == CollisionDetection.TOP_LEFT){
y = hookProjectile.getY() + OFFSET_BELOW_HOOKED_BLOCK;
//x = hookProjectile.getX();
}else if(hookPlacementDirection == CollisionDetection.TOP_RIGHT){
y = hookProjectile.getY() + OFFSET_BELOW_HOOKED_BLOCK;
//x = hookProjectile.getX();
}else if(hookPlacementDirection == CollisionDetection.BOTTOM_LEFT){
y = hookProjectile.getY() - Math.abs(hookProjectile.getHeight() - getHeight());
//x = hookProjectile.getX();
}else if(hookPlacementDirection == CollisionDetection.BOTTOM_RIGHT){
y = hookProjectile.getY() - Math.abs(hookProjectile.getHeight() - getHeight());
//x = hookProjectile.getX();
}
setX(x);
setY(y);
velocityY = 0;
pulling = false;
hookProjectile.destroyHook();
return;
}

unitVector = calculateUnitVector(pullTargetX,pullTargetY);
LinkedHashMap<GameObject, Integer> collisions = new LinkedHashMap<>();

float currentXVect = unitVector[0];
float currentYVect = unitVector[1];
while(Math.abs(currentXVect) < Math.abs(unitVector[0] * PULL_SPEED_FACTOR)
|| Math.abs(currentYVect) < Math.abs(unitVector[1] * PULL_SPEED_FACTOR) ){
for(GameObject obj: currentLevel.getCollidableObjects()){
int result = CollisionDetection.checkCollisionVelocityAware
(this,currentXVect, currentYVect, obj,0,0);
if(result != CollisionDetection.NONE && !collisions.containsKey(obj)){
collisions.put(obj, result); 
}
}
currentXVect += unitVector[0];
currentYVect += unitVector[1];
}
Collection values =  collisions.values();
if(!collisions.isEmpty()){
Set<GameObject> collisionObj = collisions.keySet();
for(GameObject obj: collisionObj){
if(obj instanceof HookableBlock){
int dir = collisions.get(obj);
float x = getX();
float y = getY();
if(dir == CollisionDetection.BOTTOM){
y = obj.getY() - getHeight();
}else if(dir == CollisionDetection.TOP){
y = obj.getY() + getHeight() + OFFSET_BELOW_HOOKED_BLOCK;
}else if(dir == CollisionDetection.LEFT){
x = obj.getX() + obj.getWidth();
}else if(dir == CollisionDetection.RIGHT){
x = obj.getX() - getWidth();
}else if(dir == CollisionDetection.TOP_LEFT){
y = obj.getY() + getHeight() + OFFSET_BELOW_HOOKED_BLOCK;
x = obj.getX() + obj.getWidth();
}else if(dir == CollisionDetection.TOP_RIGHT){
y = obj.getY() + getHeight() + OFFSET_BELOW_HOOKED_BLOCK;
x = obj.getX() - obj.getWidth();
}else if(dir == CollisionDetection.BOTTOM_LEFT){
y = obj.getY() - getHeight();
x = obj.getX() + obj.getWidth();
}else if(dir == CollisionDetection.BOTTOM_RIGHT){
y = obj.getY() - getHeight();
x = obj.getX() - obj.getWidth();
}
setX(x);
setY(y);
velocityY = 0;
pulling = false;
hookProjectile.destroyHook();
return;
}

}
}

setX(getX() + unitVector[0] * PULL_SPEED_FACTOR);
setY(getY() + unitVector[1] * PULL_SPEED_FACTOR);
}
 */