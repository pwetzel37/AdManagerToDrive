/**
 * Contains methods used to create or download reports from Google Ad Manager
 *
 * @author Patrick Wetzel (2021)
 */

package com.myproject.admanagertodrive;

import com.google.api.ads.admanager.axis.factory.AdManagerServices;
import com.google.api.ads.admanager.axis.utils.v202108.DateTimes;
import com.google.api.ads.admanager.axis.utils.v202108.ReportDownloader;
import com.google.api.ads.admanager.axis.v202108.Column;
import com.google.api.ads.admanager.axis.v202108.DateRangeType;
import com.google.api.ads.admanager.axis.v202108.Dimension;
import com.google.api.ads.admanager.axis.v202108.DimensionAttribute;
import com.google.api.ads.admanager.axis.v202108.ExportFormat;
import com.google.api.ads.admanager.axis.v202108.ReportDownloadOptions;
import com.google.api.ads.admanager.axis.v202108.ReportJob;
import com.google.api.ads.admanager.axis.v202108.ReportQuery;
import com.google.api.ads.admanager.axis.v202108.ReportServiceInterface;
import com.google.api.ads.admanager.lib.client.AdManagerSession;
import java.io.IOException;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ReportMethods {
    
    private static ReportServiceInterface reportService;
    
    /**
     * Method to create a Google Ad Manager report that is downloaded to a .csv file.
     * @param session initialized in the main method
     * @param adManagerServices initialized in the main method, allows for calls to methods
     * @throws RemoteException, InterruptedException, IOException
     */
    public static String createReport(AdManagerSession session, AdManagerServices adManagerServices) 
            throws RemoteException, InterruptedException, IOException {
        // create service interface
        reportService = adManagerServices.get(session, ReportServiceInterface.class);
        
        // create report query with selected columns
        ReportQuery reportQuery = new ReportQuery();
        reportQuery.setDimensions(
            new Dimension[] {
                Dimension.DATE,
                Dimension.ADVERTISER_NAME,
                Dimension.ORDER_NAME,
                Dimension.LINE_ITEM_NAME,
                Dimension.AD_UNIT_NAME
            });
        reportQuery.setDimensionAttributes(
            new DimensionAttribute[] {
                DimensionAttribute.LINE_ITEM_GOAL_QUANTITY,
                DimensionAttribute.LINE_ITEM_DELIVERY_INDICATOR
            });
        reportQuery.setColumns(
            new Column[] {
                Column.TOTAL_LINE_ITEM_LEVEL_IMPRESSIONS,
                Column.TOTAL_LINE_ITEM_LEVEL_CLICKS,
                Column.TOTAL_ACTIVE_VIEW_VIEWABLE_IMPRESSIONS,
                Column.TOTAL_ACTIVE_VIEW_MEASURABLE_IMPRESSIONS
            });

        // set the start and end dates for report
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String currentDate = dtf.format(LocalDateTime.now());
        
        reportQuery.setDateRangeType(DateRangeType.CUSTOM_DATE);
        reportQuery.setStartDate(
            DateTimes.toDateTime("2020-01-01T00:00:00", "America/New_York").getDate());
        reportQuery.setEndDate(
            DateTimes.toDateTime(currentDate, "America/New_York").getDate());
        
        // create and run report job
        ReportJob reportJob = new ReportJob();
        reportJob.setReportQuery(reportQuery);
        reportJob = reportService.runReportJob(reportJob);

        // create report downloader and wait for the report to be ready
        ReportDownloader reportDownloader = new ReportDownloader(reportService, reportJob.getId());
        reportDownloader.waitForReportReady();
        
        // set download options and return data as a String object
        ReportDownloadOptions options = new ReportDownloadOptions();
        options.setExportFormat(ExportFormat.CSV_DUMP);
        options.setUseGzipCompression(false);
        String reportString = reportDownloader.getReportAsCharSource(options).read();
        
        return reportString;
    }
}