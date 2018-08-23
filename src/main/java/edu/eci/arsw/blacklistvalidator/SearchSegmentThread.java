/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;
/**
 *
 * @author 2104784
 */
public class SearchSegmentThread extends Thread{
    
    private int inicio=0;
    private int fin=0;
    private String ipAddress;
    
    private int checkedListsCount=0;
    private AtomicInteger ocurrencesCount;
    private LinkedList<Integer> blackListOcurrences= new LinkedList<>();
    
    private HostBlacklistsDataSourceFacade skds;
    
    
    public SearchSegmentThread(int a, int b, String ipAddress){
        this.inicio=a;
        this.fin=b;
        this.ipAddress=ipAddress;
    }
    
    /**
     * Check the given host's IP address in a portion of the available black lists
     */
    @Override
    public void run(){
        skds=HostBlacklistsDataSourceFacade.getInstance();
        
        if (inicio < 0 || inicio > skds.getRegisteredServersCount() 
                || fin < 0 || fin > skds.getRegisteredServersCount()) {
            throw new RuntimeException("Invalid Interval");
        }
        
        for (int i=inicio; i<fin; i++){
            checkedListsCount++;
            
            if (skds.isInBlackListServer(i, ipAddress)){
                
                blackListOcurrences.add(i);
                
                ocurrencesCount.getAndIncrement();
            }
            
            if (ocurrencesCount.get() == HostBlackListsValidator.getBlackListAlarmCount()){
                break;
            }
        }        
    }
    
    public int getCheckedLists(){
        return checkedListsCount;
    }
    
    public int getOcurrences(){
        return ocurrencesCount.get();
    }
    
    public LinkedList<Integer> getBlackList(){
        return blackListOcurrences;
    }
}
