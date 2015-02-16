/*
*Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.appserver.integration.tests.ghostdeployment.webapp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.appserver.integration.common.clients.WebAppAdminClient;
import org.wso2.appserver.integration.common.utils.WebAppDeploymentUtil;
import org.wso2.appserver.integration.tests.ghostdeployment.GhostDeploymentBaseTest;
import org.wso2.carbon.automation.test.utils.http.client.HttpResponse;
import org.wso2.carbon.automation.test.utils.http.client.HttpURLConnectionClient;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Test the ghost deployment of web application. For this test two tenants will be used and
 * in each tenant two  web applications will be deployed.
 */
public class WebApplicationGhostDeploymentTestCase extends GhostDeploymentBaseTest {

    private static final Log log = LogFactory.getLog(WebApplicationGhostDeploymentTestCase.class);

    private static final String WEB_APP_FILE_NAME1 = "appServer-valied-deploymant-1.0.0.war";
    private static final String WEB_APP_NAME1 = "appServer-valied-deploymant-1.0.0";
    private static final String WEB_APP_FILE_NAME2 = "helloworld.war";
    private static final String WEB_APP_NAME2 = "helloworld";
    private static final String WEB_APP1_LOCATION = ARTIFACTS_LOCATION + WEB_APP_FILE_NAME1;
    private static final String WEB_APP2_LOCATION = ARTIFACTS_LOCATION + WEB_APP_FILE_NAME2;
    private static final String WEB_APP1_TENANT1_RESPONSE = "<status>success</status>";
    private String tenant1WebApp1URL;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        tenant1WebApp1URL = webAppURL + "/t/" + TENANT_DOMAIN_1 + "/webapps/" + WEB_APP_NAME1 + "/";
    }

    @Test(groups = "wso2.as.ghost.deployment", description = "Deploying web application in Ghost Deployment enable environment. "
            + "Each Web application should fully loaded (non Ghost format) soon after the deployment")
    public void testDeployWebApplicationGhostDeployment() throws Exception {
        log.info("deployment of  web application started");

        //Tenant 1
        loginAsTenantAdmin(TENANT_DOMAIN_1);
        webAppAdminClient = new WebAppAdminClient(backendURL, sessionCookie);

        webAppAdminClient.warFileUplaoder(WEB_APP1_LOCATION);
        assertTrue(WebAppDeploymentUtil.isWebApplicationDeployed(backendURL, sessionCookie, WEB_APP_NAME1),
                "Web Application deployment failed: " + WEB_APP_NAME1 + "on " + TENANT_DOMAIN_1);
        assertEquals(isWebAppLoaded(TENANT_DOMAIN_1, WEB_APP_FILE_NAME1), true,
                "Web app " + WEB_APP_FILE_NAME1 + "is  not loaded after deployment:" + TENANT_DOMAIN_1);

        webAppAdminClient.warFileUplaoder(WEB_APP2_LOCATION);
        assertTrue(WebAppDeploymentUtil.isWebApplicationDeployed(backendURL, sessionCookie, WEB_APP_NAME2),
                "Web Application deployment failed: " + WEB_APP_NAME2 + "on " + TENANT_DOMAIN_1);
        assertEquals(isWebAppLoaded(TENANT_DOMAIN_1, WEB_APP_FILE_NAME2), true,
                "Web app " + WEB_APP_FILE_NAME2 + "is  not loaded after deployment:" + TENANT_DOMAIN_1);

        //Tenant2
        loginAsTenantAdmin(TENANT_DOMAIN_2);
        webAppAdminClient = new WebAppAdminClient(backendURL, sessionCookie);

        webAppAdminClient.warFileUplaoder(WEB_APP1_LOCATION);
        assertTrue(WebAppDeploymentUtil.isWebApplicationDeployed(backendURL, sessionCookie, WEB_APP_NAME1),
                "Web Application deployment failed: " + WEB_APP_NAME1 + "on " + TENANT_DOMAIN_2);
        assertEquals(isWebAppLoaded(TENANT_DOMAIN_2, WEB_APP_FILE_NAME1), true,
                "Web app " + WEB_APP_FILE_NAME1 + "is  not loaded after deployment:" + TENANT_DOMAIN_2);

        webAppAdminClient.warFileUplaoder(WEB_APP2_LOCATION);
        assertTrue(WebAppDeploymentUtil.isWebApplicationDeployed(backendURL, sessionCookie, WEB_APP_NAME2),
                "Web Application deployment failed: " + WEB_APP_NAME2 + "on " + TENANT_DOMAIN_2);
        assertEquals(isWebAppLoaded(TENANT_DOMAIN_2, WEB_APP_FILE_NAME2), true,
                "Web app " + WEB_APP_FILE_NAME2 + "is  not loaded after deployment:" + TENANT_DOMAIN_2);

        log.info("deployment of web application finished");

    }

    @Test(groups = "wso2.as.ghost.deployment", description = "Invoke web application in Ghost Deployment enable environment.First test "
            + "will restart the server gracefully.After the restart  all web apps should be in ghost format.Then,  it "
            + "invokes the first web app on first tenant. After the invoke, only that web app should loaded fully and" +
            "all other web apps should be in Ghost format.", dependsOnMethods = "testDeployWebApplicationGhostDeployment")
    public void testInvokeWebAppGhostDeployment() throws Exception {

        serverManager.restartGracefully();


        assertEquals(isWebAppLoaded(TENANT_DOMAIN_1, WEB_APP_FILE_NAME1), false,
                "Web-app loaded before access. Tenant Name:" + TENANT_DOMAIN_1 + " Web_app Name: "
                        + WEB_APP_FILE_NAME1);
        assertEquals(isWebAppLoaded(TENANT_DOMAIN_1, WEB_APP_FILE_NAME2), false,
                "Web-app loaded before access. Tenant Name:" + TENANT_DOMAIN_1 + " Web_app Name: "
                        + WEB_APP_FILE_NAME2);

        assertEquals(isWebAppLoaded(TENANT_DOMAIN_2, WEB_APP_FILE_NAME1), false,
                "Web-app loaded before access. Tenant Name:" + TENANT_DOMAIN_2 + " Web_app Name: "
                        + WEB_APP_FILE_NAME1);
        assertEquals(isWebAppLoaded(TENANT_DOMAIN_2, WEB_APP_FILE_NAME2), false,
                "Web-app loaded before access. Tenant Name:" + TENANT_DOMAIN_2 + " Web_app Name: "
                        + WEB_APP_FILE_NAME2);

        HttpResponse httpResponse = HttpURLConnectionClient.sendGetRequest(tenant1WebApp1URL, null);
        assertEquals(httpResponse.getData(), WEB_APP1_TENANT1_RESPONSE,
                "Web app invocation fail. web app URL:" + tenant1WebApp1URL);

        assertEquals(isWebAppLoaded(TENANT_DOMAIN_1, WEB_APP_FILE_NAME1), true,
                "Web-app is not loaded  after access. Tenant Name:" + TENANT_DOMAIN_1 + " Web_app Name: "
                        + WEB_APP_FILE_NAME1);
        assertEquals(isWebAppLoaded(TENANT_DOMAIN_1, WEB_APP_FILE_NAME2), false,
                "Web-app loaded before access and after access other web app in same Tenant. Tenant Name:"
                        + TENANT_DOMAIN_1 + " Web_app Name: " + WEB_APP_FILE_NAME2);

        assertEquals(isWebAppLoaded(TENANT_DOMAIN_2, WEB_APP_FILE_NAME1), false,
                "Web-app loaded before access and after access other web app in another Tenant. . Tenant Name:"
                        + TENANT_DOMAIN_2 + " Web_app Name: " + WEB_APP_FILE_NAME1);
        assertEquals(isWebAppLoaded(TENANT_DOMAIN_2, WEB_APP_FILE_NAME2), false,
                "Web-app loaded before access and after access other web app in another Tenant. Tenant Name:"
                        + TENANT_DOMAIN_2 + " Web_app Name: " + WEB_APP_FILE_NAME2);

    }

    @Test(groups = "wso2.as.ghost.deployment", description = "Test web application auto unload  and reload in Ghost format. After access"
            + "web app, it should be in fully load form  but after configured web app idle time pass it should get auto"
            + "unload ne reload in Ghost form.", dependsOnMethods = "testInvokeWebAppGhostDeployment")
    public void testWebAppAutoUnLoadAndReloadInGhostForm() throws Exception {
        serverManager.restartGracefully();

        HttpResponse httpResponse = HttpURLConnectionClient.sendGetRequest(tenant1WebApp1URL, null);
        assertEquals(httpResponse.getData(), WEB_APP1_TENANT1_RESPONSE, "Web app invocation fail");

        assertEquals(isWebAppLoaded(TENANT_DOMAIN_1, WEB_APP_FILE_NAME1), true,
                "Web-app is not loaded  after access. Tenant Name:" + TENANT_DOMAIN_1 + " Web_app Name: "
                        + WEB_APP_FILE_NAME1);


        assertEquals(checkWebAppAutoUnloadingToGhostState(TENANT_DOMAIN_1, WEB_APP_FILE_NAME1), true,
                "Web-app is not un-loaded ane re-deployed in Ghost form after idle time pass. Tenant Name:"
                        + TENANT_DOMAIN_1 + " Web_app Name: " + WEB_APP_FILE_NAME1);

    }

    @Test(groups = "wso2.as.ghost.deployment", description = "Test Unload of tenant configuration context  after tenant "
            + "idle time pass without any action with that tenant",
            dependsOnMethods = "testWebAppAutoUnLoadAndReloadInGhostForm")
    public void testTenantUnloadInIdleTimeAfterWebAPPUsage() throws Exception {
        serverManager.restartGracefully();

        assertEquals(isTenantLoaded(TENANT_DOMAIN_1), false,
                "Tenant context is  loaded before access. Tenant name: " + TENANT_DOMAIN_1);
        HttpResponse httpResponse = HttpURLConnectionClient.sendGetRequest(tenant1WebApp1URL, null);
        assertEquals(httpResponse.getData(), WEB_APP1_TENANT1_RESPONSE, "Web app invocation fail");
        assertEquals(isTenantLoaded(TENANT_DOMAIN_1), true,
                "Tenant context is  not loaded after access. Tenant name: " + TENANT_DOMAIN_1);

        assertEquals(checkTenantAutoUnloading(TENANT_DOMAIN_1), true,
                "Tenant context is  not unloaded after idle time. Tenant name: " + TENANT_DOMAIN_1);

    }

    @AfterClass
    public void cleanWebApplications() throws Exception {
        //Tenant1
        loginAsTenantAdmin(TENANT_DOMAIN_1);
        webAppAdminClient = new WebAppAdminClient(backendURL, sessionCookie);

        webAppAdminClient.deleteWebAppFile(WEB_APP_FILE_NAME1, hostURL);
        assertTrue(WebAppDeploymentUtil.isWebApplicationUnDeployed(backendURL, sessionCookie, WEB_APP_NAME1),
                "Web Application un-deployment failed : Web app :" + WEB_APP_NAME1 + " on " + TENANT_DOMAIN_1);
        webAppAdminClient.deleteWebAppFile(WEB_APP_FILE_NAME2, hostURL);
        assertTrue(WebAppDeploymentUtil.isWebApplicationUnDeployed(backendURL, sessionCookie, WEB_APP_NAME2),
                "Web Application un-deployment failed: Web app :" + WEB_APP_NAME2 + " on " + TENANT_DOMAIN_1);

        //Tenant2
        loginAsTenantAdmin(TENANT_DOMAIN_2);
        webAppAdminClient = new WebAppAdminClient(backendURL, sessionCookie);

        webAppAdminClient.deleteWebAppFile(WEB_APP_FILE_NAME1, hostURL);
        assertTrue(WebAppDeploymentUtil.isWebApplicationUnDeployed(backendURL, sessionCookie, WEB_APP_NAME1),
                "Web Application un-deployment failed : Web app :" + WEB_APP_NAME1 + " on " + TENANT_DOMAIN_2);
        webAppAdminClient.deleteWebAppFile(WEB_APP_FILE_NAME2, hostURL);
        assertTrue(WebAppDeploymentUtil.isWebApplicationUnDeployed(backendURL, sessionCookie, WEB_APP_NAME2),
                "Web Application un-deployment failed: Web app :" + WEB_APP_NAME2 + " on " + TENANT_DOMAIN_2);

    }

}