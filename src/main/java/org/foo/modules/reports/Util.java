package org.foo.modules.reports;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

public class Util {
    public static String convertMonth(String date) {
        return date.replace("Jan ", "01-")
                .replace("Feb ", "02-")
                .replace("Mar ", "03-")
                .replace("Apr ", "04-")
                .replace("May ", "05-")
                .replace("June ", "06-")
                .replace("July ", "07-")
                .replace("Aug ", "08-")
                .replace("Sept ", "09-")
                .replace("Oct ", "10-")
                .replace("Nov ", "11-")
                .replace("Dec ", "12-");
    }

    public static boolean sendSlackMessage(String webhook, String message) {
        HttpClient httpClient = HttpClientBuilder.create().build();

        try {
            HttpPost request = new HttpPost(webhook);
            request.addHeader("content-type", "application/json");
            request.setEntity(new StringEntity("{\"text\":\"" + message + "\"}"));
            httpClient.execute(request);

        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
