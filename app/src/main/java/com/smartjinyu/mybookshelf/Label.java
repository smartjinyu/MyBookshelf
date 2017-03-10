package com.smartjinyu.mybookshelf;

import java.util.UUID;

/**
 * Created by smartjinyu on 2017/1/24.
 */

public class Label {
    private UUID id;
    private String title;

    public Label() {
        id = UUID.randomUUID();
    }

    public Label(UUID uuid) {
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
        if (!(o instanceof Label)) {
            return false;
        }
        Label book = (Label) o;
        return book.getId().equals(id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }


}
