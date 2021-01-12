package org.foo.modules.reports;

import org.jahia.commons.encryption.EncryptionUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

public class Configuration {

    private String cronExpression;

    private String confluenceUser;
    private String confluencePassword;
    private String confluenceUrl;

    private String slackWebhook;
    private String[] confluencePageIdList;

    private static Logger logger = LoggerFactory.getLogger(Configuration.class);

    public Configuration(String configurationFile) throws IOException {
        Bundle bundle = FrameworkUtil.getBundle(ReportsAction.class);
        BundleContext bundleContext = bundle.getBundleContext();
        ServiceReference configAdminServiceRef = bundleContext.getServiceReference(ConfigurationAdmin.class.getName());
        ConfigurationAdmin configAdminService = (ConfigurationAdmin) bundleContext.getService(configAdminServiceRef);
        Dictionary<String, Object> cfg = configAdminService.getConfiguration(configurationFile, null).getProperties();

        Hashtable<String, String> newCfg = new Hashtable<>();

        for (Enumeration<String> e = cfg.keys(); e.hasMoreElements(); ) {
            String key = e.nextElement();
            Object val = cfg.get(key);
            String valStr = val.toString();
            newCfg.put(key, valStr);
            if (key != null && key.equals("confluenceUser")) {
                setConfluenceUser(valStr);
            } else if (key != null && key.equals("confluencePassword")) {
                try {
                    setConfluencePassword(EncryptionUtils.passwordBaseDecrypt(valStr));
                } catch (Exception e1) {
                    String encryptedPassword = EncryptionUtils.passwordBaseEncrypt(valStr);
                    setConfluencePassword(valStr);
                    newCfg.put(key,encryptedPassword);
                }
            } else if (key != null && key.equals("cronExpression")) {
                setCronExpression(valStr);
            } else if (key != null && key.equals("confluenceUrl")) {
                setConfluenceUrl(valStr);
            } else if (key != null && key.equals("confluencePageIdList")) {
                setConfluencePageIdList(valStr.split(","));
            } else if (key != null && key.equals("slackWebhook")) {
                setSlackWebhook(valStr);
            } else {
                logger.debug(String.format("Key %s not recognized. Skipping...", key));
            }
        }

        configAdminService.getConfiguration(configurationFile, null).update(newCfg);
    }

    public String getConfluenceUser() {
        return confluenceUser;
    }

    public void setConfluenceUser(String confluenceUser) {
        this.confluenceUser = confluenceUser;
    }

    public String getConfluencePassword() {
        return confluencePassword;
    }

    public void setConfluencePassword(String confluencePassword) {
        this.confluencePassword = confluencePassword;
    }

    public String getConfluenceUrl() {
        return confluenceUrl;
    }

    public void setConfluenceUrl(String confluenceUrl) {
        this.confluenceUrl = confluenceUrl;
    }

    public String[] getConfluencePageIdList() {
        return confluencePageIdList;
    }

    public void setConfluencePageIdList(String[] confluencePageIdList) {
        this.confluencePageIdList = confluencePageIdList;
    }

    public String getSlackWebhook() {
        return slackWebhook;
    }

    public void setSlackWebhook(String slackWebhook) {
        this.slackWebhook = slackWebhook;
    }

    public String getCronExpression() { return cronExpression; }

    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

}
