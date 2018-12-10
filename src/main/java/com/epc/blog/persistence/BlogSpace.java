package com.epc.blog.persistence;

import java.util.List;

/**
 * Created by Eddy Cruz on 12/9/18.
 */
public class BlogSpace {
    private int id;
    private String owner;
    private String uri;
    private List<BlogEntry> blogEntries;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<BlogEntry> getBlogEntries() {
        return blogEntries;
    }

    public void setBlogEntries(List<BlogEntry> blogEntries) {
        this.blogEntries = blogEntries;
    }
}
