/*
 * (C) Copyright IBM Corporation 2024.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.openliberty.tools.gradle

import static junit.framework.Assert.assertEquals
import static org.junit.Assert.*

import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

import io.openliberty.tools.common.plugins.util.InstallFeatureUtil

class KernelInstallVersionlessFeatureTest extends AbstractIntegrationTest{
    static File resourceDir = new File("build/resources/test/kernel-install-versionless-feature-test")
    static File buildDir = new File(integTestDir, "/kernel-install-versionless-feature-test")
    static String buildFilename = "build.gradle"

    @BeforeClass
    public static void setup() {
        createDir(buildDir)
        createTestProject(buildDir, resourceDir, buildFilename)
    }
    
    @Before
    public void before() {
        runTasks(buildDir, "libertyCreate")
        copyServer("server_empty.xml")
        deleteDir(new File(buildDir, "build/wlp/lib/features"))
    }
    
    @Test
    /**
     * Install with only server.xml features
     */
    public void testInstallVersionlessFeaturesServer() {
        copyBuildFiles(new File(resourceDir, "install_features_server.gradle"), buildDir)
        runTasks(buildDir, "libertyCreate")
        copyServer("server_versionless_feature.xml")
        runTasks(buildDir, "installFeature")
        assertInstalled("beanValidation-2.0")
        assertInstalled("servlet-4.0")
        assertInstalled("jpa-2.2")
        assertInstalled("ejb-3.2")
        assertNotInstalled("couchdb-1.0")
        assertNotInstalled("distributedMap-1.0")
    }
        
    private copyServer(String serverFile) {
        copyFile(new File(resourceDir, serverFile), new File(buildDir, "build/wlp/usr/servers/defaultServer/server.xml"))
    }

    private void assertInstallStatus(String feature, boolean expectation) throws Exception {
        String expectationString = (expectation ? "installed" : "not installed");
        assertEquals("Feature " + feature + " was expected to be " + expectationString + " in the lib/features directory", expectation, existsInFeaturesDirectory(feature));
        String featureInfo = getFeatureInfo();
        assertEquals("Feature " + feature + " was expected to be " + expectationString + " according to productInfo featureInfo: " + featureInfo, expectation, featureInfo.contains(feature));
    }

    protected void assertInstalled(String feature) throws Exception {
        assertInstallStatus(feature, true);
    }
    
    protected void assertNotInstalled(String feature) throws Exception {
        assertInstallStatus(feature, false);
    }
    
    private boolean existsInFeaturesDirectory(String feature) {
        File[] features;
        File featuresDir = new File(buildDir, "build/wlp/lib/features")

        features = featuresDir.listFiles(new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().equals("com.ibm.websphere.appserver." + feature.toLowerCase() + ".mf");
                    }
                });

        return features.size() >= 1;
    }
    
    private String getFeatureInfo() throws Exception {
        File installDirectory = new File(buildDir, "build/wlp")
        return InstallFeatureUtil.productInfo(installDirectory, "featureInfo");
    }

}
