package org.storm.configs;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by fm.chen on 2017/11/30.
 */
@Service
public class PropertiesFileService  {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    private String dataDir;

    private static final String CONFIG_FILE = "conf.properties";

    private Properties props = new Properties();

    public PropertiesFileService(String dataDir) throws IOException {
        this.dataDir = dataDir;
        File directory = new File(dataDir);
        if (directory.exists()) {
            if (!directory.isDirectory()) {
                throw new IllegalArgumentException(String.format("%s is not the directory!", dataDir));
            }

            props.load(new FileInputStream(dataDir + CONFIG_FILE));
        } else {
            directory.mkdirs();
        }
    }

    public String getProperty(String key) {
        return props.getProperty(key);
    }

    public void saveSetProperty(String key, Object value) {
        if (value.equals(props.getProperty(key))) {
            return;
        }

        props.put(key, String.valueOf(value));
        try {
            props.store(new FileOutputStream(dataDir + CONFIG_FILE), "Snowflake状态配置存储文件");
        } catch (IOException e) {
            logger.error("", e);
        }
    }
}
