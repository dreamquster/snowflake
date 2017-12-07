package org.storm.configs;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Created by fm.chen on 2017/12/5.
 */
@ConfigurationProperties(prefix = "snowflake")
public class IdGenProperties {

    private String zookeeperUrl = "localhost:2181";

    private String baseUrl = "storm";

    private String rpcHost = "localhost";

    private Integer rpcPort = 50010;

    private String dataDir = "/data/snowflake/";

    private String bizTag = "snowflake";

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getRpcHost() {
        return rpcHost;
    }

    public void setRpcHost(String rpcHost) {
        this.rpcHost = rpcHost;
    }

    public Integer getRpcPort() {
        return rpcPort;
    }

    public void setRpcPort(Integer rpcPort) {
        this.rpcPort = rpcPort;
    }

    public String getDataDir() {
        return dataDir;
    }

    public void setDataDir(String dataDir) {
        this.dataDir = dataDir;
    }

    public String getZookeeperUrl() {
        return zookeeperUrl;
    }

    public void setZookeeperUrl(String zookeeperUrl) {
        this.zookeeperUrl = zookeeperUrl;
    }

    public String getBizTag() {
        return bizTag;
    }

    public void setBizTag(String bizTag) {
        this.bizTag = bizTag;
    }
}
