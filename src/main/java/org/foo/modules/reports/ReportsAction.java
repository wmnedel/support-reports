package org.foo.modules.reports;

import org.json.JSONException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.*;

public class ReportsAction extends QuartzJobBean {

    private final String SUPPORT_REPORTS_CONFIGURATION_FILE = "org.foo.modules.support.reports";

    private static Logger logger = LoggerFactory.getLogger(ReportsAction.class);
    private Configuration bundleConfig;
    private Confluence confluenceObj;

    private String getCurrentScheduleEntry(String pageId) throws IllegalArgumentException {
        Document confluenceDoc = confluenceObj.getPageHtmlContentDocument(pageId);

        Element table = confluenceDoc.select("tbody").first();
        Elements row = table.getElementsByTag("tr");
        Elements tableHeader = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
        Date currentDate = new Date();
        dateFormat.setLenient(false);

        for (int i = 0; i < row.size(); i++) {
            if (tableHeader == null) {
                tableHeader = row.get(i).select("th");
            }
            Elements cols = row.get(i).select("td");
            if (cols == null || cols.size() == 0) {
                continue;
            }

            for (int j = 0; j < cols.size(); j++) {
                String element = Util.convertMonth(cols.get(j).text()).trim() + "-" + Integer.toString(Year.now().getValue());
                try {
                    Date rowDate = dateFormat.parse(element.trim());

                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(rowDate);
                    calendar.add(Calendar.DAY_OF_YEAR, 7);
                    Date oneWeekLater = calendar.getTime();

                    if (currentDate.after(rowDate) && currentDate.before(oneWeekLater)) {

                        List<String> rowValues = cols.eachText();
                        rowValues.add(0,row.get(i).select("th").text());
                        int rowIterator = 0;
                        String message = "";

                        for (String headerElement : tableHeader.eachText()) {
                            try {
                                message += String.format("%s: %s\t", headerElement, rowValues.get(rowIterator));
                            } catch (IndexOutOfBoundsException e) {
                                message += String.format("%s: %s\t", headerElement, "-");
                            }
                            rowIterator++;
                        }

                        logger.debug(message);
                        return message;
                    }

                } catch (ParseException e) {
                    continue;
                }
            }
        }

        return "";
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            this.bundleConfig = new Configuration(SUPPORT_REPORTS_CONFIGURATION_FILE);
            this.confluenceObj = new Confluence(bundleConfig.getConfluenceUrl(), bundleConfig.getConfluenceUser(), bundleConfig.getConfluencePassword());

            for (String pageId : bundleConfig.getConfluencePageIdList()) {
                String message = getCurrentScheduleEntry(pageId);
                String pageTitle = confluenceObj.getPageTitle(pageId);

                if (message != null && pageTitle != null && message.isEmpty() == false && pageTitle.isEmpty() == false) {
                    String slackMessage = String.format("%s\n%s", pageTitle, message);
                    Util.sendSlackMessage(bundleConfig.getSlackWebhook(), slackMessage);
                } else {
                    logger.error(String.format("Could not get information from page %s", pageId));
                }
            }
        } catch (IOException | NullPointerException e) {
            logger.error(String.format("IOException while reading bundle configuration: %s", e));
        } catch (IllegalArgumentException e) {
            logger.error(String.format("IllegalArgumentException while processing Confluence table: %s", e));
        } catch (JSONException e) {
            logger.error("Could not get information from Confluence REST API: %s", e);
        }
    }
}
