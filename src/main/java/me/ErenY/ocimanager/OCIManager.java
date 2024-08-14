package me.ErenY.ocimanager;

import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.AuthenticationDetailsProvider;
import com.oracle.bmc.auth.ConfigFileAuthenticationDetailsProvider;
import com.oracle.bmc.core.ComputeClient;
import com.oracle.bmc.core.VirtualNetworkClient;
import com.oracle.bmc.core.model.Instance;
import com.oracle.bmc.core.requests.*;
import com.oracle.bmc.core.responses.*;
import me.ErenY.GUI.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class OCIManager {

    private final ComputeClient computeClient;
    private final Instance instance;
    private final ConfigFileReader.ConfigFile configFile;
    private final AuthenticationDetailsProvider provider;
    private final String instance_ocid;
    private final String public_ip;

    private static final Logger logger = LoggerFactory.getLogger(OCIManager.class);

    public OCIManager() throws IOException {

        this.instance_ocid = View.pref.get("INSTANCE_OCID", "");
        this.configFile = ConfigFileReader.parseDefault();
        this.provider = new ConfigFileAuthenticationDetailsProvider(this.configFile);
        this.computeClient = ComputeClient.builder().build(this.provider);

        // set instance
        GetInstanceRequest getInstanceRequest = GetInstanceRequest.builder().instanceId(this.instance_ocid).build();
        GetInstanceResponse getInstanceResponse = this.computeClient.getInstance(getInstanceRequest);
        this.instance = getInstanceResponse.getInstance();
        logger.info("Got oci instance");


        //set public ip
        String s = View.pref.get("PUBLIC_IP_OCID", View.prop.getProperty("PUBLIC_IP_OCID"));
        String s1 = View.pref.get("PUBLIC_IP", View.prop.getProperty("PUBLIC_IP"));
        if ((s.isEmpty() || s.isBlank()) && (s1.isEmpty() || s1.isBlank())) {
            ListVnicAttachmentsRequest listVnicAttachmentsRequest = ListVnicAttachmentsRequest.builder()
                    .compartmentId(this.instance.getCompartmentId())
                    .instanceId(this.instance.getId())
                    .build();

            VirtualNetworkClient vnc = VirtualNetworkClient.builder().build(this.provider);
            GetVnicResponse getVnicResponse = vnc.getVnic(GetVnicRequest.builder()
                    .vnicId(this.computeClient.listVnicAttachments(listVnicAttachmentsRequest)
                            .getItems().getFirst().getVnicId())
                    .build());
            this.public_ip = getVnicResponse.getVnic().getPublicIp();
            vnc.close();
            logger.info("Got public ip by first vnic request");
        }else if (!s.isBlank() && !s.isEmpty()){
            GetPublicIpRequest getPublicIpRequest = GetPublicIpRequest.builder()
                    .publicIpId(View.pref.get("PUBLIC_IP_OCID", View.prop.getProperty("PUBLIC_IP_OCID")))
                    .build();
            VirtualNetworkClient vnc = VirtualNetworkClient.builder().build(this.provider);
            GetPublicIpResponse response = vnc.getPublicIp(getPublicIpRequest);
            this.public_ip = response.getPublicIp().getIpAddress();
            vnc.close();
            logger.info("Got public ip by public ip ocid");
        }else {
            this.public_ip = View.pref.get("PUBLIC_IP", View.prop.getProperty("PUBLIC_IP"));
            logger.info("Got public ip directly from preferences");
        }
        logger.info("Got public ip: {}", this.public_ip);

        logger.info("Created OCIManager object");
    }

    public String getInstanceStatus(){
        return instance.getLifecycleState().getValue();
    }

    public void startInstance(){

        InstanceActionRequest instanceActionRequest = InstanceActionRequest.builder()
                .instanceId(this.instance_ocid)
                .action("START")
                .build();


        logger.info("Requested start for oci instance");
        InstanceActionResponse response = computeClient.instanceAction(instanceActionRequest);

        logger.info("Starting " + "https-status-code: " + response.get__httpStatusCode__());
    }

    public String getPublic_ip() {
        return public_ip;
    }

    public String getInstance_ocid() {
        return instance_ocid;
    }

    public ComputeClient getComputeClient() {
        return computeClient;
    }

    public Instance getInstance() {
        return instance;
    }

    public ConfigFileReader.ConfigFile getConfigFile() {
        return configFile;
    }

    public AuthenticationDetailsProvider getProvider() {
        return provider;
    }
}
