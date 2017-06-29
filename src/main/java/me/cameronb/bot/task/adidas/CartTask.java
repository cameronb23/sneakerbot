package me.cameronb.bot.task.adidas;

import com.google.common.base.Charsets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import me.cameronb.bot.Config;
import me.cameronb.bot.proxy.BotProxy;
import me.cameronb.bot.task.Task;
import me.cameronb.bot.task.TaskInstance;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by Cameron on 6/25/2017.
 */
public class CartTask extends Task {

    private final String CHECKCART_URL = "http://www.adidas.com/on/demandware.store/Sites-adidas-US-Site/en_US/Cart-ProductCount";
    private final String CHECKOUT_URL = "https://www.adidas.com/on/demandware.store/Sites-adidas-US-Site/en_US/COSummary-Start";
    private final String ATC_URL = "http://www.adidas.com/on/demandware.store/Sites-adidas-US-Site/en_US/Cart-MiniAddProduct";

    private final String FORM_DATA = "{ \"layer\": \"Add To Bag overlay\", \"pid\": \"{$sizeSku}\", \"Quantity\": \"1\", \"g-recaptcha-response\": \"{$captcha}\", \"masterPid\": \"{$sku}\", \"sessionSelectedStoreID\": \"null\", \"ajax\": \"true\" }";

    private BasicCookieStore cookieStore;
    private CloseableHttpClient client;

    public CartTask(int threadCount) {
        super("Adidas Product Mode", Config.INSTANCE.getCartUrl());
    }

    public ObservableList<TaskInstance> getInstances() {
        return FXCollections.observableArrayList();
    }

    /*public CartTask(BotProxy proxyConfig, BasicCookieStore cookies) {
        super("Adidas ATC", Config.INSTANCE.getCartUrl());

        this.cookieStore = cookies;

        // build http client

        HttpClientBuilder builder = HttpClientBuilder.create();

        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build();

        if(proxyConfig != null) {
            CredentialsProvider creds = new BasicCredentialsProvider();
            AuthScope scope = new AuthScope(proxyConfig.getAddress(), proxyConfig.getPort());

            Credentials cred = new UsernamePasswordCredentials(proxyConfig.getUsername(), proxyConfig.getPassword());
            creds.setCredentials(scope, cred);

            HttpHost proxy = new HttpHost(proxyConfig.getAddress(), proxyConfig.getPort());

            builder
                    .setProxy(proxy)
                    .setProxyAuthenticationStrategy(new ProxyAuthenticationStrategy())
                    .setDefaultCredentialsProvider(creds);


        }

        // global configuration
        builder.setUserAgent(Config.INSTANCE.getUseragent())
                .setDefaultRequestConfig(globalConfig)
                .setDefaultCookieStore(this.cookieStore)
                .setRedirectStrategy(new LaxRedirectStrategy());

        this.client = builder.build();
    }*/


    @Override
    public void end() {

    }

    private boolean checkForItem() {
        synchronized (this) {
            try {
                CloseableHttpResponse response = client.execute(new HttpGet(CHECKCART_URL));

                String data = IOUtils.toString(response.getEntity().getContent(), "UTF-8");

                int count = Integer.parseInt(data.replace("\"", ""));

                return count > 0;
            } catch (IOException e) {
                System.out.println("Error retrieving cart count: " + e.getMessage());
                return false;
            }
        }
    }

    @Override
    public void run() {
        synchronized (this) {
            HttpPost req = new HttpPost(ATC_URL);

            List<NameValuePair> params = new ArrayList<>();

            //TODO: integrate UI for setting this mid-drop
            JSONObject jsonFormData = new JSONObject(FORM_DATA);

            for(String k : jsonFormData.keySet()) {
                params.add(new BasicNameValuePair(k, jsonFormData.getString(k)));
            }

            req.setEntity(new UrlEncodedFormEntity(params, Charsets.UTF_8));
            req.setHeader("X-Requested-With", "XMLHttpRequest");
            req.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            try {
                CloseableHttpResponse response = client.execute(req);
            } catch (IOException e) {
                System.out.println("Error adding to cart: " + e.getMessage());
                run();
            }
        }
    }
}
