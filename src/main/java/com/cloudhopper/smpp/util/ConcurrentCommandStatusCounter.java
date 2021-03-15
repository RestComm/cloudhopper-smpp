package com.cloudhopper.smpp.util;

/*
 * #%L
 * ch-smpp
 * %%
 * Copyright (C) 2009 - 2015 Cloudhopper by Twitter
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.cloudhopper.smpp.SmppConstants;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author joelauer
 */
public class ConcurrentCommandStatusCounter {
    
    private ConcurrentHashMap<Integer,AtomicInteger> map;

    private AtomicInteger serverErrorCounter;
    private AtomicInteger clientErrorCounter;
    
    public ConcurrentCommandStatusCounter() {
        this.map = new ConcurrentHashMap<Integer,AtomicInteger>();
        this.serverErrorCounter = new AtomicInteger(0);
        this.clientErrorCounter = new AtomicInteger(0);
    }
    
    public void reset() {
        this.map.clear();
        this.serverErrorCounter.set(0);
        this.clientErrorCounter.set(0);
    }
    
    public ConcurrentCommandStatusCounter copy() {
        ConcurrentCommandStatusCounter copy = new ConcurrentCommandStatusCounter();
        for (Map.Entry<Integer,AtomicInteger> entry : this.map.entrySet()) {
            this.map.put(entry.getKey(), new AtomicInteger(entry.getValue().get()));
        }
        return copy;
    }

    public Map getConcurrentCommandStatusCounter() {
        return map;
    }

    public Integer getServerErrorCounter() {
        return this.serverErrorCounter.get();
    }

    public Integer getClientErrorCounter() {
        return this.clientErrorCounter.get();
    }

    public int get(int commandStatus) {
        Integer key = Integer.valueOf(commandStatus);
        AtomicInteger val = map.get(key);
        if (val == null) {
            return -1;
        } else {
            return val.get();
        }
    }
    
    public int incrementAndGet(int commandStatus) {
        Integer key = Integer.valueOf(commandStatus);
        AtomicInteger val = map.get(key);
        if (val == null) {
            val = new AtomicInteger(0);
            map.put(key, val);
        }

        //Increament client/server ErrorCounter as applicable
        switch (commandStatus) {
            case SmppConstants.STATUS_OK:
                break;
            case SmppConstants.STATUS_INVDSTADR: case SmppConstants.STATUS_INVSRCADR:
                clientErrorCounter.incrementAndGet();
                break;
            default:
                serverErrorCounter.incrementAndGet();
        }

        return val.incrementAndGet();
    }
    
    public SortedMap<Integer,Integer> createSortedMapSnapshot() {
        SortedMap<Integer,Integer> sortedMap = new TreeMap<Integer,Integer>();
        for (Map.Entry<Integer,AtomicInteger> entry : this.map.entrySet()) {
            sortedMap.put(entry.getKey(), new Integer(entry.getValue().get()));
        }
        return sortedMap;
    }
    
    @Override
    public String toString() {
        if (this.map.isEmpty()) {
            return "";
        }
        
        SortedMap<Integer,Integer> sortedMap = createSortedMapSnapshot();
        
        StringBuilder to = new StringBuilder();
        for (Map.Entry<Integer,Integer> entry : sortedMap.entrySet()) {
            if (to.length() != 0) {
                to.append(" ");
            }
            to.append(entry.getKey());
            to.append("=");
            to.append(entry.getValue());
        }
        to.append(" serverErrorCounter=");
        to.append(serverErrorCounter);
        to.append(" clientErrorCounter=");
        to.append(clientErrorCounter);

        return to.toString();
    }
    
}
