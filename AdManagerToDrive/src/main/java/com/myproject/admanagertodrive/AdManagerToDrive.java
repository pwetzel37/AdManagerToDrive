/**
 * Main class that creates a report from Google Ad Manager, then 
 * updates a file on Google Drive, that will be used in Tableau.
 *
 * @author Patrick Wetzel (2021)
 */

package com.myproject.admanagertodrive;

import com.google.api.ads.common.lib.auth.OfflineCredentials;
import com.google.api.ads.common.lib.auth.OfflineCredentials.Api;
import com.google.api.ads.admanager.axis.factory.AdManagerServices;
import com.google.api.ads.admanager.lib.client.AdManagerSession;
import com.google.api.client.auth.oauth2.Credential;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AdManagerToDrive {
    
    private static Credential oAuth2Credential;
    private static AdManagerSession session;
    private static AdManagerServices adManagerServices;
    
    public static void main(String[] args) throws Exception {
        // inializes variables for Ad Manager
        initialize();
        
        // creates a runnable that runs the execute() function
        final Runnable executeFunction = () -> {
            try { execute(); } 
            catch (Exception ex) { System.out.println(ex); }
        };
        // schedules the runnable to run every 1 hour
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(executeFunction, 0, 1, TimeUnit.HOURS);
    }
    
    /**
     * Initializes the variables used to make Google Ad Manager API calls
     * @throws Exception 
     */
    private static void initialize() throws Exception {
        // create credentials to connect to Google Ad Manager API
        oAuth2Credential = new OfflineCredentials.Builder()
            .forApi(Api.AD_MANAGER)
            .fromFile()
            .build()
            .generateCredential();
        
        // construct an AdManagerSession
        session = new AdManagerSession.Builder()
            .fromFile()
            .withOAuth2Credential(oAuth2Credential)
            .build();
        
        // construct an AdManagerService object to use methods to return data
        adManagerServices = new AdManagerServices();
    }
    
    /**
     * Uses the Report and Drive methods to update delivery-report.csv on Google Drive
     * @throws Exception 
     */
    private static void execute() throws Exception {
        // create report saved as delivery-report.csv, updates on Drive
        String reportString = ReportMethods.createReport(session, adManagerServices);
        DriveMethods.updateFile(reportString);
    }
}