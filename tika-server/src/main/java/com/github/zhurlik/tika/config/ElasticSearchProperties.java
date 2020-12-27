package com.github.zhurlik.tika.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhurlik@gmail.com
 */
@Setter
@Getter
public class ElasticSearchProperties {
    private String host;
    private int port;
    private String schema;
    private IndexProperties index;
}
