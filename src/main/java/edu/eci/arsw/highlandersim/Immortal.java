package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class Immortal extends Thread {

    private AtomicInteger health;
    
    private AtomicInteger defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    boolean pause = false;
    
    public int push;

    public void pause() {
        pause = true;
    }
    
    public void resumen(){
        pause = false;
    }

    public void cont() {
        pause = false;
    }

    public Immortal(String name, List<Immortal> immortalsPopulation, AtomicInteger health, AtomicInteger defaultDamageValue, int push) {
        super(name);
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
        this.push = push;
    }

    public void run() {

        while (!ControlFrame.acabar) {
            if(!pause){
                Immortal im;

                int myIndex = immortalsPopulation.indexOf(this);

                int nextFighterIndex = r.nextInt(immortalsPopulation.size());

                //avoid self-fight
                if (nextFighterIndex == myIndex) {
                    nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
                }

                im = immortalsPopulation.get(nextFighterIndex);

                this.fight(im);

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                try{
                    synchronized(ControlFrame.obj){
                        ControlFrame.obj.wait();
                    }
                }catch(InterruptedException ex){}
            }

        }

    }

    public void fight(Immortal i2) {
        
        Immortal x1;
        Immortal x2;
        
        if(this.push>i2.push){
            x1 = this;
            x2 = i2;
        }else{
            x1 = i2;
            x2 = this;
        }
        
        synchronized(x1){
            synchronized(x2){
                if (i2.getHealth().get() > 0) {
                    i2.changeHealth(new AtomicInteger(i2.getHealth().get() - defaultDamageValue.get()));
                    this.health.addAndGet(defaultDamageValue.get());
                    //System.out.println("Fight: " + this + " vs " + i2);
                } else {
                    //System.out.println(this + " says:" + i2 + " is already dead!");
                }
            }
        }

    }

    public void changeHealth(AtomicInteger v) {
        health = v;
    }

    public AtomicInteger getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
