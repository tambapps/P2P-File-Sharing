package com.tambapps.file_sharing_app.model;

import javax.validation.constraints.NotEmpty;

public class FilePath {

    @NotEmpty(message = "pick a file")
    private String path;

    public FilePath() {
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
