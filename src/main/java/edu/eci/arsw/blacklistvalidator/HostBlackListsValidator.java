/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.blacklistvalidator;

import edu.eci.arsw.spamkeywordsdatasource.HostBlacklistsDataSourceFacade;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hcadavid
 */
public class HostBlackListsValidator {

    private static final int BLACK_LIST_ALARM_COUNT=5;
    
    /**
     * Check the given host's IP address in all the available black lists,
     * and report it as NOT Trustworthy when such IP was reported in at least
     * BLACK_LIST_ALARM_COUNT lists, or as Trustworthy in any other case.
     * The search is not exhaustive: When the number of occurrences is equal to
     * BLACK_LIST_ALARM_COUNT, the search is finished, the host reported as
     * NOT Trustworthy, and the list of the five blacklists returned.
     * @param ipAddress suspicious host's IP address.
     * @param n quantity of threads
     * @return  Blacklists numbers where the given host's IP address was found.
     */
    public List<Integer> checkHost(String ipAddress, int n){
        
        LinkedList<Integer> blackListOcurrences=new LinkedList<>();
        
        AtomicInteger ocurrencesCount= new AtomicInteger(0);
        
        int checkedListsCount=0;
        
        HostBlacklistsDataSourceFacade skds=HostBlacklistsDataSourceFacade.getInstance();
        
        int div=skds.getRegisteredServersCount()/n;
        
        int ini=0;
        
        SearchSegmentThread[] sst = new SearchSegmentThread[n];
        
        for(int i=0;i<=n-1;i++){
            if(i!=n-1){
                sst[i] = new SearchSegmentThread(ini,ini+div,ipAddress);
                ini+=div;
            }
            else{
                sst[i] = new SearchSegmentThread(ini,skds.getRegisteredServersCount(),ipAddress);
            }
            sst[i].start();
        }
              
        for(int i=0;i<=n-1;i++){
            try{
                sst[i].join();
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, "Failed on joining the thread: "+sst[i].getName(), ex);
            }
        }
        
        for(SearchSegmentThread s : sst){
            checkedListsCount+=s.getCheckedLists();
            ocurrencesCount.getAndAdd(s.getOcurrences());
            blackListOcurrences.addAll(s.getBlackList());
        }
        
        if (ocurrencesCount.get()>=BLACK_LIST_ALARM_COUNT){
            skds.reportAsNotTrustworthy(ipAddress);
        }
        else{
            skds.reportAsTrustworthy(ipAddress);
        }                
        
        LOG.log(Level.INFO, "Checked Black Lists:{0} of {1}", new Object[]{checkedListsCount, skds.getRegisteredServersCount()});
        
        return blackListOcurrences;
    }
    
    private static final Logger LOG = Logger.getLogger(HostBlackListsValidator.class.getName());
    
    
    public static int getBlackListAlarmCount(){
        return BLACK_LIST_ALARM_COUNT;
    }  
    
    
}
