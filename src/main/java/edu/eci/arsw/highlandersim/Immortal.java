package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback=null;
    
    private int health;
    
    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());
    
    private boolean isPaused;
    
    private final Object lock;
    
    private boolean stopped;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb) {
        super(name);
        this.stopped = false;
        this.lock = new Object();
        this.updateCallback=ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue=defaultDamageValue;
        this.isPaused = false;
    }

    public void pause() throws InterruptedException{
        isPaused=true;
    }
    
    
    public void resumir(){
        isPaused=false;
        synchronized(lock){
            lock.notifyAll();
        }
        
    }
    
    public void parar(){
        stopped = true;
        isPaused = true;
        synchronized(lock){
            lock.notifyAll();
        }
    }
    
    public void run() {
        while (!stopped){
            while (!isPaused) {
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

            }
            synchronized(lock){
                try {
                    lock.wait();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Immortal.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void fight(Immortal i2) {
        synchronized(immortalsPopulation){
            if (i2.getHealth() > 0) {
                i2.changeHealth(i2.getHealth() - defaultDamageValue);
                this.health += defaultDamageValue;
                updateCallback.processReport("Fight: " + this + " vs " + i2+"\n");
            } else {
                updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
                
            }
        }
    }
    

    public void changeHealth(int v) {
        health = v;
        if (health==0){
            immortalsPopulation.remove(this);
            this.parar();
        }
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
