package src;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.Set;

public class HookProjectile extends GameObject{
    private static Integer texture;
    private static int width = 0;
    private static int height = 0;

    private static final int WIDTH = 18;
    private static final int HEIGHT = 18;
    private static final String FILENAME = "Hook.png";
    private static final int SPEED_FACTOR = 40;//setting to slow speed results in interesting concept of slow hooking :D
    private static final int SPEED_FACTOR_BACK = 4;//70;
    private static final int START_OFFSET_FACTOR = 20;
    private static final int HOOK_MAX_LENGTH = 300;
    private static final int HOOK_DISAPPEAR_DELAY_LARGE = 1000000;
    private static final int HOOK_DISAPPEAR_DELAY_SMALL = 5;
    private static final int HOOK_DISAPPEAR_DELAY_TINY = 3;
    private static final int MIN_HOOK_DISTANCE = 5;

    private static final int DISTANCE_ERROR_MARGIN = 20;

    private static final float HOOK_CORNOR_HEIGHT_WIDTH_MOD = 1f/1.7f;

    private int targetX, targetY, originX, originY;
    private HookLine hookLine;
    private Sheep parent;
    private Level level;

    private boolean passedEndPoint = false;
    private float[] unitVector = new float[2];
    private boolean collided = false;

    private float velocityX;
    private float velocityY;

    private float lastdx;
    private float lastdy;
    
    private Cube currentCube;

    public static void loadTexture(){
        if(texture == null){
            int[] res = Image.loadImage(MyGame.PATH_BLUEJ + FILENAME);

            width = res[1];
            height = res[2];

            texture = res[0];
        }
    }

    public HookProjectile(int originX, int originY ,int targetX, int targetY, Level level, Sheep parent){
        super(WIDTH, HEIGHT);

        this.parent = parent;
        this.level = level;

        setX(originX);
        setY(originY);

        this.originX = originX;
        this.originY = originY;

        this.targetX = targetX;
        this.targetY = targetY;

        loadTexture();

        setRawWidth(width);
        setRawHeight(height);
        setTexture(texture);

        float scaleX = (float)WIDTH/width;
        float scaleY = (float)HEIGHT/height;

        setScaleX(scaleX);
        setScaleY(scaleY);

        this.targetX -= getWidth()/2;
        this.targetY -= getHeight()/2;

        hookLine = new HookLine(this,parent);

        unitVector = calculateUnitVector(targetX, targetY);

        setX(getX() + unitVector[0] * START_OFFSET_FACTOR);
        setY(getY() + unitVector[1] * START_OFFSET_FACTOR);

        lastdx = Math.abs(targetX - originX);
        lastdy = Math.abs(targetY - originY);
    }

    public HookLine getHookLine(){return hookLine;}

    private float[] calculateUnitVector(float targetX, float targetY){
        float deltaX = targetX - getX();
        float deltaY = targetY - getY();
        float angle = (float)Math.atan((double)deltaY / deltaX);

        if(deltaX > 0){
            angle += Math.PI;
        }

        setRotation(angle);

        float magnitude =(float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        float unitVector[] = new float[2];
        unitVector[0] = deltaX / magnitude;
        unitVector[1] = deltaY / magnitude;
        return unitVector;
    }

    private int delay = HOOK_DISAPPEAR_DELAY_SMALL;
    @Override
    public ArrayList<Object> update(){
        ArrayList<Object> returnVal = null;

        if(!isMovingBack){
            float cdx = Math.abs(targetX - parent.getX());
            float cdy = Math.abs(targetY - parent.getY());

            if(cdx > lastdx)
                originX = parent.getIntX();
            if(cdy > lastdy)
                originY = parent.getIntY();
            //
            lastdx = cdx;
            lastdy = cdy;

            LinkedHashMap<GameObject, Integer> collisions = new LinkedHashMap<>();

            float currentXVect = unitVector[0];
            float currentYVect = unitVector[1];
            while(Math.abs(currentXVect) < Math.abs(unitVector[0] * SPEED_FACTOR)
            || Math.abs(currentYVect) < Math.abs(unitVector[1] * SPEED_FACTOR) ){
                for(GameObject obj: level.getCollidableObjects()){
                    int result = CollisionDetection.checkCollisionVelocityAware
                        (this,currentXVect, currentYVect, obj,0,0);
                    if(result != CollisionDetection.NONE && !collisions.containsKey(obj)){
                        collisions.put(obj, result);
                    }
                }
                currentXVect += unitVector[0];
                currentYVect += unitVector[1];
            }

            if(!collisions.isEmpty() && !collided){
                collided = true;
                delay = HOOK_DISAPPEAR_DELAY_SMALL;
                Set<GameObject> collidedObjects = collisions.keySet();
                for(GameObject obj: collidedObjects){
                    int dir = collisions.get(obj);
                    if(obj instanceof HookableBlock){
                        //tell parent to go to the block

                        float dx = getX() - originX;
                        float dy = getY() - originY;

                        if( (dx * dx + dy * dy) > MIN_HOOK_DISTANCE * MIN_HOOK_DISTANCE){
                            delay = HOOK_DISAPPEAR_DELAY_LARGE;
                            parent.startPull(getX(), getY(), dir, this);
                        }else{
                            delay = 0;
                        }

                    }
                    float x = 0;
                    float y = 0;
                    if(dir == CollisionDetection.BOTTOM){
                        y -= obj.getHeight();
                        setRotation((float)(3*Math.PI/2));
                        if(collidedObjects.size() > 1)x = - obj.getX() + getX();
                    }else if(dir == CollisionDetection.TOP){
                        y += obj.getHeight();
                        setRotation((float)(Math.PI/2));
                        if(collidedObjects.size() > 1)x = - obj.getX() + getX();
                    }else if(dir == CollisionDetection.LEFT){
                        x += obj.getWidth();
                        setRotation(0f);
                        if(collidedObjects.size() > 1)y = - obj.getY() + getY();
                    }else if(dir == CollisionDetection.RIGHT){
                        x -= obj.getWidth();
                        setRotation((float)(Math.PI));
                        if(collidedObjects.size() > 1)y = - obj.getY() + getY();
                    }else if(dir == CollisionDetection.TOP_LEFT){
                        y += obj.getHeight() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
                        x += obj.getWidth() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
                        setRotation((float)(Math.PI/4));
                    }else if(dir == CollisionDetection.TOP_RIGHT){
                        y += obj.getHeight() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
                        x -= obj.getWidth() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
                        setRotation((float)(3*Math.PI/4));
                    }else if(dir == CollisionDetection.BOTTOM_LEFT){
                        y -= obj.getHeight() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
                        x += obj.getWidth() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
                        setRotation((float)(7*Math.PI/4));
                    }else if(dir == CollisionDetection.BOTTOM_RIGHT){
                        y -= obj.getHeight() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
                        x -= obj.getWidth() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
                        setRotation((float)(5*Math.PI/4));
                    }

                    setX(obj.getX() + x);
                    setY(obj.getY() + y);
                    break;
                }
            }

            float velX = unitVector[0] * SPEED_FACTOR;
            float velY = unitVector[1] * SPEED_FACTOR;

            float currentDeltaX = getX() - originX;
            float currentDeltaY = getY() - originY;

            float hookMaxLenSqr = HOOK_MAX_LENGTH * HOOK_MAX_LENGTH;

            float parentVelX = parent.getVelocityX();
            float parentVelY = parent.getVelocityY();

            if(parentVelX != 0 && velX / parentVelX < 0){
                velX += parent.getVelocityX();
            }
            if(parentVelY != 0 && velY / parentVelY < 0){
                velY += parent.getVelocityY();
            }

            float newX = getX() + velX;
            float newY = getY() + velY;

            float newDX = newX - originX;
            float newDY = newY - originY;
            if(newDX * newDX + newDY * newDY > hookMaxLenSqr){
                double[] val = calculateLeftOverVector(currentDeltaX, currentDeltaY, HOOK_MAX_LENGTH);
                velX = (float) val[0];
                velY = (float) val[1];
            }

            velocityX = velX;
            velocityY = velY;

            if(!collided &&   (   (currentDeltaX * currentDeltaX + currentDeltaY * currentDeltaY < hookMaxLenSqr)
                &&  (passedEndPoint 
                    || (Math.abs(getX() - targetX) < SPEED_FACTOR && Math.abs(getY() - targetY) < SPEED_FACTOR))    )   ){
                passedEndPoint = true;
                setX(getX() + velX);
                setY(getY() + velY);
            }
            else if(!collided&&
            (currentDeltaX * currentDeltaX + currentDeltaY * currentDeltaY < hookMaxLenSqr)){
                unitVector = calculateUnitVector(targetX, targetY);
                setX(getX() + velX);
                setY(getY() + velY);
            }else if(delay > 0){
                delay--;
            }else{
                //setLocation(targetX, targetY);
                returnVal = destroyHookNow();
            }

            updateCubeRelated();
        }else{
            float[] uv = calculateUnitVector(parent.getX(), parent.getY());
            setX(getX() + uv[0]*SPEED_FACTOR_BACK);
            setY(getY() + uv[1]*SPEED_FACTOR_BACK);
            //if distance from character
            //isMovingBack = false;
            float dx = parent.getX() - getX();
            float dy = parent.getY() - getY();
            float distsqr = dx*dx + dy*dy;

            if(distsqr < DISTANCE_ERROR_MARGIN * DISTANCE_ERROR_MARGIN){
                returnVal = destroyHookNow();
                isMovingBack = false;
                currentCube.stopMovingBack();
            }

        }

        return returnVal;
    }

    private boolean isMovingBack = false;

    private void updateCubeRelated(){
        HashSet<GameObject> cubes = level.getCubes();
        for(GameObject cube: cubes){
            if(CollisionDetection.checkCollisionSimple(this, cube)){
                currentCube = (Cube) cube;
                isMovingBack = true;
                currentCube.startMovingBack(this);
                break;
            }
        }
    }

    private double[] calculateLeftOverVector(float deltaX, float deltaY, float finalMag){
        double currentMag = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        double ratio = (finalMag+2) / currentMag;

        return new double[]{(deltaX * ratio) - deltaX, (deltaY * ratio) - deltaY};
    }

    public void destroyHook(){
        delay = HOOK_DISAPPEAR_DELAY_TINY;
    }

    public ArrayList<Object> destroyHookNow(){
        ArrayList<Object> returnVal = new ArrayList<Object>();
        returnVal.add(GameLoop.OPERATION_REMOVE); 
        returnVal.add(this);
        returnVal.add(hookLine);
        parent.setHookable(true);
        return returnVal;
    }

    public int getOriginX(){return originX;}

    public int getOriginY(){return originY;}

}

/*
 * 
 * 
if(!collisions.isEmpty() && !collided){
collided = true;
delay = HOOK_DISAPPEAR_DELAY_SMALL;
Set<GameObject> collidedObjects = collisions.keySet();
for(GameObject obj: collidedObjects){
if(obj instanceof HookableBlock){
//tell parent to go to the block
int dir = collisions.get(obj);
float dx = getX() - originX;
float dy = getY() - originY;
if(Math.sqrt(dx * dx + dy * dy) > MIN_HOOK_DISTANCE){
delay = HOOK_DISAPPEAR_DELAY_LARGE;
parent.startPull(getX(), getY(), dir, this);
}else{
delay = 0;
}
int x = 0;
int y = 0;
if(dir == CollisionDetection.BOTTOM){
y -= obj.getHeight();
setRotation((float)(3*Math.PI/2));
}else if(dir == CollisionDetection.TOP){
y += obj.getHeight();
}else if(dir == CollisionDetection.LEFT){
x += obj.getWidth();
}else if(dir == CollisionDetection.RIGHT){
x -= obj.getWidth();
}else if(dir == CollisionDetection.TOP_LEFT){
y += obj.getHeight();
x += obj.getWidth();
}else if(dir == CollisionDetection.TOP_RIGHT){
y += obj.getHeight();
x -= obj.getWidth();
}else if(dir == CollisionDetection.BOTTOM_LEFT){
y -= obj.getHeight();
x += obj.getWidth();
}else if(dir == CollisionDetection.BOTTOM_RIGHT){
y -= obj.getHeight();
x -= obj.getWidth();
}
setX(obj.getX() + x);
setY(obj.getY() + y);
break;
}
}
}

else if(dir == CollisionDetection.TOP_LEFT){
if(collidedObjects.size() <= 1){
y += obj.getHeight() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
x += obj.getWidth() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
setRotation((float)(Math.PI/4));
}
else{
if(Math.abs(velocityY) > Math.abs(velocityX)){
y += obj.getHeight();
setRotation((float)(Math.PI/2));
}else if(Math.abs(velocityX) > Math.abs(velocityY)){
x += obj.getWidth();
setRotation(0f);
}
}
}else if(dir == CollisionDetection.TOP_RIGHT){
if(collidedObjects.size() <= 1){
y += obj.getHeight() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
x -= obj.getWidth() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
setRotation((float)(3*Math.PI/4));
}
else{
if(Math.abs(velocityY) > Math.abs(velocityX)){
y += obj.getHeight();
setRotation((float)(Math.PI/2));
}else if(Math.abs(velocityX) > Math.abs(velocityY)){
x -= obj.getWidth();
setRotation((float)(Math.PI));
}

}
}else if(dir == CollisionDetection.BOTTOM_LEFT){
if(collidedObjects.size() <= 1){
y -= obj.getHeight() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
x += obj.getWidth() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
setRotation((float)(7*Math.PI/4));
}
else{
if(Math.abs(velocityY) > Math.abs(velocityX)){
y -= obj.getHeight();
setRotation((float)(3*Math.PI/2));
}else if(Math.abs(velocityX) > Math.abs(velocityY)){
x += obj.getWidth();
setRotation(0f);
}

}
}else if(dir == CollisionDetection.BOTTOM_RIGHT){
if(collidedObjects.size() <= 1){
y -= obj.getHeight() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
x -= obj.getWidth() * HOOK_CORNOR_HEIGHT_WIDTH_MOD;
setRotation((float)(5*Math.PI/4));
}
else{
if(Math.abs(velocityY) > Math.abs(velocityX)){
y -= obj.getHeight();
setRotation((float)(3*Math.PI/2));
}else if(Math.abs(velocityX) > Math.abs(velocityY)){
x -= obj.getWidth();
setRotation((float)(Math.PI));
}

}
}
 * 
 */