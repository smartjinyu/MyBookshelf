package com.smartjinyu.mybookshelf.model.bean;

import java.util.UUID;

/**
 * Created by smartjinyu on 2017/1/23.
 * represents a bookshelf
 */

public class BookShelf {
    private UUID id;
    private String title;

    public BookShelf() {
        id = UUID.randomUUID();
    }

    public BookShelf(UUID uuid) {
        id = uuid;
    }

    @Override
    public String toString() {
        return title;
    }

    public UUID getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof BookShelf)) {
            return false;
        }
        BookShelf bookshelf = (BookShelf) o;
        return bookshelf.getId().equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

}
