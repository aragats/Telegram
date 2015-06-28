package org.telegram.messenger.service.mock;

/**
 * Created by aragats on 09/05/15.
 */
public class ServiceManager {

    private static volatile ServiceManager instance = null;

    public static ServiceManager getInstance() {
        ServiceManager localInstance = instance;
        if (localInstance == null) {
            synchronized (ServiceManager.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new ServiceManager();
                }
            }
        }
        return localInstance;
    }
}
