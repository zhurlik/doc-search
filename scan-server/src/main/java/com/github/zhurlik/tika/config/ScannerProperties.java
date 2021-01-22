package com.github.zhurlik.tika.config;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * This class will be populated from the application.yml file.
 *
 * @author zhurlik@gmail.com
 */
@Getter
@Setter
public class ScannerProperties {
    /**
     * A list of the resources for scanning files.
     */
    private List<String> resources = Collections.emptyList();
}
