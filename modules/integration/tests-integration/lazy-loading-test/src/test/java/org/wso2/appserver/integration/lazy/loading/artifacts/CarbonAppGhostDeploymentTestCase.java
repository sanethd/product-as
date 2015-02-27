/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.appserver.integration.lazy.loading.artifacts;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appserver.integration.common.utils.WebAppDeploymentUtil;
import org.wso2.appserver.integration.lazy.loading.LazyLoadingBaseTest;
import org.wso2.appserver.integration.lazy.loading.util.LazyLoadingTestException;
import org.wso2.appserver.integration.lazy.loading.util.WebAppStatus;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.automation.test.utils.http.client.HttpURLConnectionClient;
import org.wso2.carbon.integration.common.admin.client.ApplicationAdminClient;
import org.wso2.carbon.integration.common.admin.client.CarbonAppUploaderClient;

import javax.activation.DataHandler;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test carbon application deployment. For this test two tenants will be used and
 * in each tenant two  Carbon applications will be deployed.
 */
public class CarbonAppGhostDeploymentTestCase extends LazyLoadingBaseTest {

    private static final Log log = LogFactory.getLog(CarbonAppGhostDeploymentTestCase.class);
    private URL carbonApp1FileURL;
    private DataHandler carbonApp1URLDataHandler;
    private final static String CARBON_APP_NAME1 = "WarCApp_1.0.0";
    private final static String CARBON_APP_FILE1 = "WarCApp_1.0.0.car";
    private final static String CARBON_APP1_WEB_APP_NAME = "appServer-valid-deploymant-1.0.0";
    private final static String CARBON_APP1_WEB_APP_FILE = "appServer-valid-deploymant-1.0.0.war";
    private URL carbonApp2FileURL;
    private DataHandler carbonApp2URLDataHandler;
    private final static String CARBON_APP_NAME2 = "webappunpackCar_1.0.0";
    private final static String CARBON_APP_FILE2 = "unpackwebappCar_1.0.0.car";
    private final static String CARBON_APP2_WEB_APP_NAME = "myWebapp-1.0.0";
    private final static String CARBON_APP2_WEB_APP_FILE = "myWebapp-1.0.0.war";
    private static final String WEB_APP1_RESPONSE = "<status>success</status>";
    private static final String WEB_APP2_RESPONSE = "<h1>Holla!!!</h1>";

    private String tenant1WebApp1URL;
    private String tenant1WebApp2URL;
    private static volatile List<String> responseDataList = new ArrayList<String>();
    private static volatile List<String> responseDetailedInfoList = new ArrayList<String>();

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();

        tenant1WebApp1URL = webAppURL + "/t/" + TENANT_DOMAIN_1 + "/webapps/" + CARBON_APP1_WEB_APP_NAME + "/";
        carbonApp1FileURL = new URL("file://" + ARTIFACTS_LOCATION + CARBON_APP_FILE1);
        carbonApp1URLDataHandler = new DataHandler(carbonApp1FileURL);
        tenant1WebApp2URL = webAppURL + "/t/" + TENANT_DOMAIN_1 + "/webapps/" + CARBON_APP2_WEB_APP_NAME + "/";
        carbonApp2FileURL = new URL("file://" + ARTIFACTS_LOCATION + CARBON_APP_FILE2);
        carbonApp2URLDataHandler = new DataHandler(carbonApp2FileURL);

    }

    @Test(groups = "wso2.as.lazy.loading", description = "Upload car file and verify in ghost deployment enable " +
            "environment. After the the deployment all the web applications of  the carbon application should be " +
            "deployed correctly and  they should be loaded fully(Not in ghost form) ", alwaysRun = true)
    public void carApplicationUploadInGhostDeployment() throws Exception {
        log.info("Carbon application deployment start");
        CarbonAppUploaderClient carbonAppClient;

        loginAsTenantAdmin(TENANT_DOMAIN_1_kEY);

        carbonAppClient = new CarbonAppUploaderClient(backendURL, sessionCookie);

        carbonAppClient.uploadCarbonAppArtifact(CARBON_APP_FILE1, carbonApp1URLDataHandler);

        assertTrue(WebAppDeploymentUtil.isWebApplicationDeployed(backendURL, sessionCookie, CARBON_APP1_WEB_APP_NAME),
                "Web Application deployment failed: " + CARBON_APP1_WEB_APP_NAME + "on " + TENANT_DOMAIN_1);

        WebAppStatus webAppStatusTenant1WebApp1 = getWebAppStatus(TENANT_DOMAIN_1, CARBON_APP1_WEB_APP_FILE);
        assertEquals(webAppStatusTenant1WebApp1.getTenantStatus().isTenantContextLoaded(), true, " Tenant Context is" +
                " not loaded. Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp1.isWebAppStarted(), true, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is not started after deployment in Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp1.isWebAppGhost(), false, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is in ghost mode after deployment in Tenant:" + TENANT_DOMAIN_1);

        assertTrue(isCarbonAppListed(CARBON_APP_NAME1), "Carbon Application is not listed :" + CARBON_APP_NAME1);


        carbonAppClient.uploadCarbonAppArtifact(CARBON_APP_FILE2, carbonApp2URLDataHandler);
        assertTrue(WebAppDeploymentUtil.isWebApplicationDeployed(backendURL, sessionCookie, CARBON_APP2_WEB_APP_NAME),
                "Web Application deployment failed: " + CARBON_APP2_WEB_APP_NAME + "on " + TENANT_DOMAIN_1);

        WebAppStatus webAppStatusTenant1WebApp2 = getWebAppStatus(TENANT_DOMAIN_1, CARBON_APP2_WEB_APP_FILE);
        assertEquals(webAppStatusTenant1WebApp2.getTenantStatus().isTenantContextLoaded(), true, " Tenant Context " +
                "is not loaded. Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp2.isWebAppStarted(), true, "Web-App: " + CARBON_APP2_WEB_APP_FILE +
                " is not started after deployment in Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp2.isWebAppGhost(), false, "Web-App: " + CARBON_APP2_WEB_APP_FILE +
                " is in ghost mode after deployment in Tenant:" + TENANT_DOMAIN_1);

        assertTrue(isCarbonAppListed(CARBON_APP_NAME2), "Carbon Application is not listed :" + CARBON_APP_NAME2);

        loginAsTenantAdmin(TENANT_DOMAIN_2_KEY);

        carbonAppClient = new CarbonAppUploaderClient(backendURL, sessionCookie);

        carbonAppClient.uploadCarbonAppArtifact(CARBON_APP_FILE1, carbonApp1URLDataHandler);

        assertTrue(WebAppDeploymentUtil.isWebApplicationDeployed(backendURL, sessionCookie, CARBON_APP1_WEB_APP_NAME),
                "Web Application deployment failed: " + CARBON_APP1_WEB_APP_NAME + "on " + TENANT_DOMAIN_2);

        WebAppStatus webAppStatusTenant2WebApp1 = getWebAppStatus(TENANT_DOMAIN_2, CARBON_APP1_WEB_APP_FILE);
        assertEquals(webAppStatusTenant2WebApp1.getTenantStatus().isTenantContextLoaded(), true, " Tenant Context " +
                "is not loaded. Tenant:" + TENANT_DOMAIN_2);
        assertEquals(webAppStatusTenant2WebApp1.isWebAppStarted(), true, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is not started after deployment in Tenant:" + TENANT_DOMAIN_2);
        assertEquals(webAppStatusTenant2WebApp1.isWebAppGhost(), false, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is in ghost mode after deployment in Tenant:" + TENANT_DOMAIN_2);


        assertTrue(isCarbonAppListed(CARBON_APP_NAME1), "Carbon Application is not listed :" + CARBON_APP_NAME1);

        carbonAppClient.uploadCarbonAppArtifact(CARBON_APP_FILE2, carbonApp2URLDataHandler);
        assertTrue(WebAppDeploymentUtil.isWebApplicationDeployed(backendURL, sessionCookie, CARBON_APP2_WEB_APP_NAME),
                "Web Application deployment failed: " + CARBON_APP2_WEB_APP_NAME + "on " + TENANT_DOMAIN_2);

        WebAppStatus webAppStatusTenant2WebApp2 = getWebAppStatus(TENANT_DOMAIN_2, CARBON_APP2_WEB_APP_FILE);
        assertEquals(webAppStatusTenant2WebApp2.getTenantStatus().isTenantContextLoaded(), true, " Tenant Context" +
                " is not loaded. Tenant:" + TENANT_DOMAIN_2);
        assertEquals(webAppStatusTenant2WebApp2.isWebAppStarted(), true, "Web-App: " + CARBON_APP2_WEB_APP_FILE +
                " is not started after deployment in Tenant:" + TENANT_DOMAIN_2);
        assertEquals(webAppStatusTenant2WebApp2.isWebAppGhost(), false, "Web-App: " + CARBON_APP2_WEB_APP_FILE +
                " is in ghost mode after deployment in Tenant:" + TENANT_DOMAIN_2);


        assertTrue(isCarbonAppListed(CARBON_APP_NAME2), "Carbon Application is not listed :" + CARBON_APP_NAME2);
        log.info("Carbon application deployment end");
    }

    @Test(groups = "wso2.as.lazy.loading", description = "  Invoke web application that is deployed as Carbon " +
            "application in Ghost Deployment enable environment.First test will restart the server gracefully.After " +
            "the restart  all   tenant context not be loaded.Then,  it invokes the first web app on first tenant." +
            " After the invoke, only that web app should loaded fully.",
            dependsOnMethods = "carApplicationUploadInGhostDeployment")
    public void testInvokeWebAppInCarbonAppInGhostDeployment() throws Exception {

        serverManager.restartGracefully();


        assertEquals(getTenantStatus(TENANT_DOMAIN_1).isTenantContextLoaded(), false, " Tenant Name:" +
                TENANT_DOMAIN_1 + "loaded before access.");
        assertEquals(getTenantStatus(TENANT_DOMAIN_2).isTenantContextLoaded(), false, " Tenant Name:" +
                TENANT_DOMAIN_2 + "loaded before access.");

        HttpResponse httpResponse = HttpURLConnectionClient.sendGetRequest(tenant1WebApp1URL, null);
        assertEquals(httpResponse.getData(), WEB_APP1_RESPONSE,
                "Web app invocation fail. web app URL:" + tenant1WebApp1URL);

        WebAppStatus webAppStatusTenant1WebApp1 = getWebAppStatus(TENANT_DOMAIN_1, CARBON_APP1_WEB_APP_FILE);
        assertEquals(webAppStatusTenant1WebApp1.getTenantStatus().isTenantContextLoaded(), true, " Tenant Context is " +
                "not loaded. Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp1.isWebAppStarted(), true, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is not started in Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp1.isWebAppGhost(), false, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is in ghost mode after invoking in Tenant:" + TENANT_DOMAIN_1);

        WebAppStatus webAppStatusTenant1WebApp2 = getWebAppStatus(TENANT_DOMAIN_1, CARBON_APP2_WEB_APP_FILE);
        assertEquals(webAppStatusTenant1WebApp2.getTenantStatus().isTenantContextLoaded(), true, " Tenant Context " +
                "is not loaded. Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp2.isWebAppStarted(), true, "Web-App: " + CARBON_APP2_WEB_APP_FILE +
                " is not started  in Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp2.isWebAppGhost(), true, "Web-App: " + CARBON_APP2_WEB_APP_FILE +
                " is loaded before access it and after access other web app in same Tenant:" + TENANT_DOMAIN_1);

        assertEquals(getTenantStatus(TENANT_DOMAIN_2).isTenantContextLoaded(), false, " Tenant Name:" +
                TENANT_DOMAIN_2 + "loaded before access.");
    }

    @Test(groups = "wso2.as.lazy.loading", description = "Send a Get request after a Carbon application is auto unload" +
            " and reload in to Ghost form. After access Carbon application, it should be in fully load form " +
            " the Ghost form", dependsOnMethods = "testInvokeWebAppInCarbonAppInGhostDeployment")
    public void testWebAppInCarbonAppAutoUnLoadAndInvokeInGhostDeployment() throws LazyLoadingTestException {
        assertEquals(checkWebAppAutoUnloadingToGhostState(TENANT_DOMAIN_1, CARBON_APP1_WEB_APP_FILE), true,
                "Web-app is not un-loaded ane re-deployed in Ghost form after idle time pass. Tenant Name:"
                        + TENANT_DOMAIN_1 + " Web_app Name: " + CARBON_APP1_WEB_APP_FILE);


        HttpResponse httpResponse = null;
        try {
            httpResponse = HttpURLConnectionClient.sendGetRequest(tenant1WebApp1URL, null);
        } catch (IOException ioException) {
            String customErrorMessage = "IOException Exception when  send a GET request to" + tenant1WebApp1URL
                    + "\n" + ioException.getMessage();
            log.error(customErrorMessage);
            throw new LazyLoadingTestException(customErrorMessage, ioException);
        }
        assertEquals(httpResponse.getData(), WEB_APP1_RESPONSE, "Web app invocation fail");


        WebAppStatus webAppStatusTenant1WebApp1 = getWebAppStatus(TENANT_DOMAIN_1, CARBON_APP1_WEB_APP_FILE);
        assertEquals(webAppStatusTenant1WebApp1.getTenantStatus().isTenantContextLoaded(), true, " Tenant Context is" +
                " not loaded. Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp1.isWebAppStarted(), true, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is not started in Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp1.isWebAppGhost(), false, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is in ghost mode after invoking in Tenant:" + TENANT_DOMAIN_1);

    }


    @Test(groups = "wso2.as.lazy.loading", description = "Test web application that is deployed as Carbon " +
            "application, auto unload  and reload in Ghost format. After access web app, it should be in fully load " +
            "form  but after configured web app idle time pass it should get auto unload ne reload in Ghost form.",
            dependsOnMethods = "testWebAppInCarbonAppAutoUnLoadAndInvokeInGhostDeployment")
    public void testWebAppInCarbonAppAutoUnLoadAndReloadInGhostFormInGhostDeployment() throws Exception {
        serverManager.restartGracefully();

        HttpResponse httpResponse = HttpURLConnectionClient.sendGetRequest(tenant1WebApp1URL, null);
        assertEquals(httpResponse.getData(), WEB_APP1_RESPONSE, "Web app invocation fail");


        WebAppStatus webAppStatusTenant1WebApp1 = getWebAppStatus(TENANT_DOMAIN_1, CARBON_APP1_WEB_APP_FILE);
        assertEquals(webAppStatusTenant1WebApp1.getTenantStatus().isTenantContextLoaded(), true, " Tenant Context is" +
                " not loaded. Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp1.isWebAppStarted(), true, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is not started in Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp1.isWebAppGhost(), false, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is in ghost mode after invoking in Tenant:" + TENANT_DOMAIN_1);


        assertEquals(checkWebAppAutoUnloadingToGhostState(TENANT_DOMAIN_1, CARBON_APP1_WEB_APP_FILE), true,
                "Web-app is not un-loaded ane re-deployed in Ghost form after idle time pass. Tenant Name:"
                        + TENANT_DOMAIN_1 + " Web_app Name: " + CARBON_APP1_WEB_APP_FILE);

    }


    @Test(groups = "wso2.as.lazy.loading", description = "Test Unload of tenant configuration context  after tenant "
            + "idle time pass without any action with that tenant",
            dependsOnMethods = "testWebAppInCarbonAppAutoUnLoadAndReloadInGhostFormInGhostDeployment")
    public void testTenantUnloadInIdleTimeAfterWebAPPInCarbonAppUsageInGhostDeployment() throws Exception {
        serverManager.restartGracefully();

        assertEquals(getTenantStatus(TENANT_DOMAIN_1).isTenantContextLoaded(), false,
                "Tenant context is  loaded before access. Tenant name: " + TENANT_DOMAIN_1);
        HttpResponse httpResponse = HttpURLConnectionClient.sendGetRequest(tenant1WebApp1URL, null);
        assertEquals(httpResponse.getData(), WEB_APP1_RESPONSE, "Web app invocation fail");
        assertEquals(getTenantStatus(TENANT_DOMAIN_1).isTenantContextLoaded(), true,
                "Tenant context is  not loaded after access. Tenant name: " + TENANT_DOMAIN_1);

        assertEquals(checkTenantAutoUnloading(TENANT_DOMAIN_1), true,
                "Tenant context is  not unloaded after idle time. Tenant name: " + TENANT_DOMAIN_1);

    }


    @Test(groups = "wso2.as.lazy.loading", description = "Send concurrent requests  when tenant context is not loaded." +
            "All request should  get expected output",
            dependsOnMethods = "testTenantUnloadInIdleTimeAfterWebAPPInCarbonAppUsageInGhostDeployment")
    public void testConcurrentWebAPPInvocationsWhenTenantContextNotLoadedInGhostDeployment() throws Exception {
        serverManager.restartGracefully();

        assertEquals(getTenantStatus(TENANT_DOMAIN_1).isTenantContextLoaded(), false,
                "Tenant context is  loaded before access. Tenant name: " + TENANT_DOMAIN_1);

        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_THREAD_COUNT);
        log.info("Concurrent invocation Start");
        log.info("Expected Response Data:" + WEB_APP1_RESPONSE);
        for (int i = 0; i < CONCURRENT_THREAD_COUNT; i++) {
            executorService.execute(new Runnable() {

                public void run() {
                    HttpResponse httpResponse = null;
                    try {
                        httpResponse = HttpURLConnectionClient.sendGetRequest(tenant1WebApp1URL, null);
                    } catch (IOException e) {
                        log.error("Error  when sending a  get request  for :" + tenant1WebApp1URL, e);
                    }
                    synchronized (this) {
                        String responseDetailedInfo = "Response Data :" + httpResponse.getData() + "\tResponse Code:"
                                + httpResponse.getResponseCode();
                        responseDataList.add(httpResponse.getData());
                        log.info(responseDetailedInfo);
                        responseDetailedInfoList.add(responseDetailedInfo);
                    }
                }

            });

        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
        log.info("Concurrent invocation End");

        int correctResponseCount = 0;
        for (String responseData : responseDataList) {
            if (WEB_APP1_RESPONSE.equals(responseData)) {
                correctResponseCount += 1;
            }

        }

        String allDetailResponse = "\n";
        for (String responseInfo : responseDetailedInfoList) {
            allDetailResponse += responseInfo + "\n";
        }


        WebAppStatus webAppStatusTenant1WebApp1 = getWebAppStatus(TENANT_DOMAIN_1, CARBON_APP1_WEB_APP_FILE);
        assertEquals(webAppStatusTenant1WebApp1.getTenantStatus().isTenantContextLoaded(), true, " Tenant Context is" +
                " not loaded. Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp1.isWebAppStarted(), true, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is not started in Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp1.isWebAppGhost(), false, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is in ghost mode after invoking in Tenant:" + TENANT_DOMAIN_1);

        assertEquals(correctResponseCount, CONCURRENT_THREAD_COUNT, allDetailResponse + "All the concurrent requests " +
                "not get correct response.");


    }


    @Test(groups = "wso2.as.lazy.loading", description = "Send concurrent requests  when tenant context is loaded." +
            " But Web-App is in Ghost form. All request should  get expected output",
            dependsOnMethods = "testConcurrentWebAPPInvocationsWhenTenantContextNotLoadedInGhostDeployment")
    public void testConcurrentWebAPPInvocationsWhenTenantContextLoadedInGhostDeployment() throws Exception {
        serverManager.restartGracefully();

        assertEquals(getTenantStatus(TENANT_DOMAIN_1).isTenantContextLoaded(), false,
                "Tenant context is  loaded before access. Tenant name: " + TENANT_DOMAIN_1);

        HttpResponse httpResponseApp2 = HttpURLConnectionClient.sendGetRequest(tenant1WebApp2URL, null);

        assertTrue(httpResponseApp2.getData().contains(WEB_APP2_RESPONSE), "Invocation of Web-App fail :" +
                tenant1WebApp2URL);
        assertEquals(getTenantStatus(TENANT_DOMAIN_1).isTenantContextLoaded(), true,
                "Tenant context is  not loaded after access. Tenant name: " + TENANT_DOMAIN_1);


        WebAppStatus webAppStatusTenant1WebApp2 = getWebAppStatus(TENANT_DOMAIN_1, CARBON_APP2_WEB_APP_FILE);
        assertEquals(webAppStatusTenant1WebApp2.isWebAppStarted(), true, "Web-App: " + CARBON_APP2_WEB_APP_FILE +
                " is not started in Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp2.isWebAppGhost(), false, "Web-App: " + CARBON_APP2_WEB_APP_FILE +
                " is in ghost mode after invoking in Tenant:" + TENANT_DOMAIN_1);


        WebAppStatus webAppStatusTenant1WebApp1 = getWebAppStatus(TENANT_DOMAIN_1, CARBON_APP1_WEB_APP_FILE);
        assertEquals(webAppStatusTenant1WebApp1.isWebAppStarted(), true, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is not started in Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp1.isWebAppGhost(), true, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is in not ghost mode before invoking in Tenant:" + TENANT_DOMAIN_1);


        ExecutorService executorService = Executors.newFixedThreadPool(CONCURRENT_THREAD_COUNT);
        log.info("Concurrent invocation Start");
        log.info("Expected Response Data:" + WEB_APP1_RESPONSE);
        for (int i = 0; i < CONCURRENT_THREAD_COUNT; i++) {
            executorService.execute(new Runnable() {

                public void run() {
                    HttpResponse httpResponseApp1 = null;
                    try {
                        httpResponseApp1 = HttpURLConnectionClient.sendGetRequest(tenant1WebApp1URL, null);
                    } catch (IOException e) {
                        log.error("Error  when sending a  get request  for :" + tenant1WebApp1URL, e);
                    }
                    synchronized (this) {
                        String responseDetailedInfo = "Response Data :" + httpResponseApp1.getData() +
                                "\tResponse Code:" + httpResponseApp1.getResponseCode();
                        responseDataList.add(httpResponseApp1.getData());
                        log.info(responseDetailedInfo);
                        responseDetailedInfoList.add(responseDetailedInfo);
                    }
                }

            });

        }

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.MINUTES);
        log.info("Concurrent invocation End");

        int correctResponseCount = 0;
        for (String responseData : responseDataList) {
            if (WEB_APP1_RESPONSE.equals(responseData)) {
                correctResponseCount += 1;
            }

        }

        String allDetailResponse = "\n";
        for (String responseInfo : responseDetailedInfoList) {
            allDetailResponse += responseInfo + "\n";
        }


        webAppStatusTenant1WebApp1 = getWebAppStatus(TENANT_DOMAIN_1, CARBON_APP1_WEB_APP_FILE);
        assertEquals(webAppStatusTenant1WebApp1.getTenantStatus().isTenantContextLoaded(), true, " Tenant Context is" +
                " not loaded. Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp1.isWebAppStarted(), true, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is not started in Tenant:" + TENANT_DOMAIN_1);
        assertEquals(webAppStatusTenant1WebApp1.isWebAppGhost(), false, "Web-App: " + CARBON_APP1_WEB_APP_FILE +
                " is in ghost mode after invoking in Tenant:" + TENANT_DOMAIN_1);

        assertEquals(correctResponseCount, CONCURRENT_THREAD_COUNT, allDetailResponse + "All the concurrent requests" +
                " not get correct response.");


    }


    @AfterClass(alwaysRun = true)
    public void cleanCarbonApplications() throws Exception {
        ApplicationAdminClient appAdminClient;
        loginAsTenantAdmin(TENANT_DOMAIN_1_kEY);
        appAdminClient = new ApplicationAdminClient(backendURL, sessionCookie);
        appAdminClient.deleteApplication(CARBON_APP_NAME1);
        log.info("Carbon application deleted : " + CARBON_APP_NAME1 + "on " + TENANT_DOMAIN_1);
        appAdminClient.deleteApplication(CARBON_APP_NAME2);
        log.info("Carbon application deleted : " + CARBON_APP_NAME2 + "on " + TENANT_DOMAIN_1);

        loginAsTenantAdmin(TENANT_DOMAIN_2_KEY);
        appAdminClient = new ApplicationAdminClient(backendURL, sessionCookie);
        appAdminClient.deleteApplication(CARBON_APP_NAME1);
        log.info("Carbon application deleted : " + CARBON_APP_NAME1 + "on " + TENANT_DOMAIN_2);
        appAdminClient.deleteApplication(CARBON_APP_NAME2);
        log.info("Carbon application deleted : " + CARBON_APP_NAME2 + "on " + TENANT_DOMAIN_2);
    }

}