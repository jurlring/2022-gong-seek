package com.gongseek.imagestorage.domain;

import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;

public enum FileExtension {

    JPG("jpg"),
    JPEG("jpeg"),
    PNG("png"),
    BMP("bmp");

    private final String value;

    FileExtension(final String value) {
        this.value = value;
    }

    public static FileExtension from(final String fileName) {
        final String extension = FilenameUtils.getExtension(fileName);
        return Stream.of(values())
                .filter(it -> it.value.equalsIgnoreCase(extension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(extension));
    }
}
