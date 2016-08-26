package edu.sysu.yawl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by root on 16-8-20.
 */
@ConfigurationProperties(prefix="yawl")
public class Property {


    private String engineid;


    private String master_address;


    private String influx_address;

    public String getEngineid() {
        return engineid;
    }

    public void setEngineid(String engineid) {
        this.engineid = engineid;
    }

    public String getMaster_address() {
        return master_address;
    }

    public void setMaster_address(String master_address) {
        this.master_address = master_address;
    }

    public String getInflux_address() {
        return influx_address;
    }

    public void setInflux_address(String influx_address) {
        this.influx_address = influx_address;
    }
}
