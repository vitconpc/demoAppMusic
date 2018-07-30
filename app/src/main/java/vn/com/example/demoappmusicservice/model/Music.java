package vn.com.example.demoappmusicservice.model;

import java.io.File;
import java.io.Serializable;

public class Music implements Serializable {
    private String name;
    private File mFilePath;
    private String mAuthor;

    public Music() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getmFilePath() {
        return mFilePath;
    }

    public void setmFilePath(File filePath) {
        this.mFilePath = filePath;
    }

    public String getmAuthor() {
        return mAuthor;
    }

    public void setmAuthor(String author) {
        this.mAuthor = author;
    }
}
