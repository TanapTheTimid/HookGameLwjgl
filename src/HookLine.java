package src;


import java.util.ArrayList;
public class HookLine extends GameObject{
    private HookProjectile parent;
    private Sheep character;
    public HookLine(HookProjectile parent,Sheep character){
        super(0, 0);
        this.parent = parent;
        this.character = character;
        setLocation(0,0);
        setIsLine(true);
        setLineThickness(0.5f);
    }

    @Override
    public ArrayList<Object> update(){

        setLineStart(new float[]{(character.getX()+parent.getWidth()/2),(character.getY()+parent.getHeight()/2) });
        setLineEnd(new float[]{(parent.getIntX()+parent.getWidth()/2),(parent.getIntY()+parent.getHeight()/2)});
        return null;

    }
}