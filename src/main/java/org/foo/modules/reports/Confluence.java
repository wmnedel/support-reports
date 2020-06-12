package org.foo.modules.reports;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.jahia.services.notification.HttpClientService;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Confluence {

    private final String PLUGINS_VIEW_PAGE_STORAGE = "/plugins/viewstorage/viewpagestorage.action";
    private final String REST_API_CONTENT = "/rest/api/content/";

    private String url;
    private String user;
    private String password;

    private HttpClientService httpClientService;

    public Confluence(String url, String user, String password) {
        setUrl(url);
        setUser(user);
        setPassword(password);

        initClient();
    }

    private void initClient() {
        HttpClientParams params = new HttpClientParams();
        params.setAuthenticationPreemptive(true);
        params.setCookiePolicy("ignoreCookies");

        HttpConnectionManagerParams cmParams = new HttpConnectionManagerParams();
        cmParams.setConnectionTimeout(15000);
        cmParams.setSoTimeout(60000);

        MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
        httpConnectionManager.setParams(cmParams);

        this.httpClientService = new HttpClientService();
        this.httpClientService.setHttpClient(new HttpClient(params, httpConnectionManager));
    }

    private Map<String, String> getBasicAuthJsonHeader() {
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("accept", "application/json");
        headers.put("Authorization",
                "Basic " + new String(Base64.encodeBase64((getUser() + ":" + getPassword()).getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8));
        return headers;
    }

    public String getPageHtmlContent(String pageId) {
        return this.httpClientService.executeGet(String.format("%s%s?pageId=%s", getUrl(), PLUGINS_VIEW_PAGE_STORAGE, pageId), getBasicAuthJsonHeader());
    }

    public Document getPageHtmlContentDocument(String pageId) {
        return Jsoup.parse(getPageHtmlContent(pageId));
    }

    public JSONObject getPageContent(String pageId) throws JSONException {
        String response = this.httpClientService.executeGet(String.format("%s%s%s", getUrl(), REST_API_CONTENT, pageId), getBasicAuthJsonHeader());
        return new JSONObject(response);
    }

    public String getPageTitle(String pageId) throws JSONException {
        return getPageContent(pageId).getString("title");
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
