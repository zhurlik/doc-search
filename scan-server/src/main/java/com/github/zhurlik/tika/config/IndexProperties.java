package com.github.zhurlik.tika.config;

import lombok.Getter;
import lombok.Setter;

/**
 * @author zhurlik@gmail.com
 */
@Setter
@Getter
public class IndexProperties {
    private boolean recreate;
    private String name;
    private String mappingsPath;
}
