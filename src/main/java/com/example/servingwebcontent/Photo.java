package com.example.servingwebcontent;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.*;
import java.nio.file.Path;
import java.util.Date;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true)
public class Photo {

    @Id
    private Long id;
    private String title;
    private String url;
    private String thumbnailUrl;
    private Date downloadDate;
    private Long size;
    private String path;
    private int albumId;
    private String builtUri;

    public Photo() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String toString() {
        return this.url;
    }

    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(int albumId) {
        this.albumId = albumId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getPathStr() {
        return path;
    }

    public Path getPath() {
        return Path.of(path);
    }


    public void setPath(String path) {
        this.path = path;
    }

    public Date getDownloadedDate() {
        return downloadDate;
    }

    public void setDownloadedDate(Date date) {
        this.downloadDate = date;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getBuiltUri() {
        return builtUri;
    }

    public void setBuiltUri(String builtUri) {
        this.builtUri = builtUri;
    }


}