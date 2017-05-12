package src;

public class Level1 extends Level{
    
    public static final int BLOCK_SIZE = 20;
    @Override
    public void init(){
        HookProjectile.loadTexture();
        
        //for(int y = 0; y < 50; y++){
            Sheep go1 = new Sheep(this);
            go1.setLocation(150,3);
            addGameObject(go1);
        //}
        
        GrapplingGun gun = new GrapplingGun(go1);
        gun.setLocation(150, 10);
        addGameObject(gun);
        

        
        for(int x = 0; x < 10; x++){
            for(int y = 0; y < 50; y++){
                Platform plat = new Platform(BLOCK_SIZE,BLOCK_SIZE);
                plat.setLocation(x*20 + 100,70+ y*20);
                addCollidableObject(plat);
            }
        }
        
        for(int x = 0; x < 5; x++){
            //for(int y = 0; y < 5; y++){
                HookableBlock plat = new HookableBlock(BLOCK_SIZE,BLOCK_SIZE);
                plat.setLocation(x*20 + 400,70+ 20);
                addCollidableObject(plat);
            //}
        }
        
        HookableBlock plat = new HookableBlock(BLOCK_SIZE,BLOCK_SIZE);
        plat.setLocation(260 ,0);
        addCollidableObject(plat);
        
        Cube cube = new Cube(this);
        cube.setLocation(150, 30);
        addCube(cube);
        
        Platform pla = new Platform(BLOCK_SIZE, BLOCK_SIZE);
        pla.setLocation(450, 355);
        addCollidableObject(pla);
        
    }
}