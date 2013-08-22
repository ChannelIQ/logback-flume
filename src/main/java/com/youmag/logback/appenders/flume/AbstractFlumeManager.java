/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package com.youmag.logback.appenders.flume;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractFlumeManager {
    private static final Lock LOCK = new ReentrantLock();
    // Need to lock that map instead of using a ConcurrentMap due to stop removing the
    // manager from the map and closing the stream, requiring the whole stop method to be locked.
    private static final Map<String, AbstractFlumeManager> MAP = new HashMap<String, AbstractFlumeManager>();
    /**
     * Number of Appenders using this manager.
     */
    public int count = 0;
    private final String name;

    protected AbstractFlumeManager(final String name) {
        this.name = name;
    }


    
    
    
    /**
     * Determines if a Manager with the specified name exists.
     * @param name The name of the Manager.
     * @return True if the Manager exists, false otherwise.
     */
    public static boolean hasManager(final String name) {
        LOCK.lock();
        try {
            return MAP.containsKey(name);
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * May be overridden by Managers to perform processing while the Manager is being released and the
     * lock is held.
     */
    protected void releaseSub() {
    }

    protected int getCount() {
        return count;
    }

    /**
     * Called to signify that this Manager is no longer required by an Appender.
     */
    public void release() {
        LOCK.lock();
        try {
            --count;
            if (count <= 0) {
                MAP.remove(name);
                releaseSub();
            }
        } finally {
            LOCK.unlock();
        }
    }

    /**
     * Returns the name of the Manager.
     * @return The name of the Manager.
     */
    public String getName() {
        return name;
    }

    /**
     * Provide a description of the content format supported by this Manager.  Default implementation returns an empty (unspecified) Map.
     *
     * @return a Map of key/value pairs describing the Manager-specific content format, or an empty Map if no content format descriptors are specified. 
     *
     */
    public Map<String, String> getContentFormat() {
        return new HashMap<String, String>();
    }
    
    public abstract void send(FlumeEvent event, int delay, int retries);

}
