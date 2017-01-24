package com.smartjinyu.mybookshelf;

import java.util.UUID;

/**
 * Created by smartjinyu on 2017/1/23.
 * represents a bookshelf
 */

public class BookShelf {
    private UUID id;
    private String title;

    public BookShelf(){
        id = UUID.randomUUID();
    }
    public BookShelf(UUID uuid){
        id = uuid;
    }

    @Override
    public String toString(){
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
}
