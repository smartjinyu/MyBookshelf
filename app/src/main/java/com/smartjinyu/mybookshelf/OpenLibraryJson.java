package com.smartjinyu.mybookshelf;

// FIXME generate failure  field _$ISBN97809802004475

import java.util.List;

/**
 * Open Library Json Format
 * Created by smartjinyu on 2017/2/19.
 */

public class OpenLibraryJson {

    /**
     * publishers : [{"name":"Litwin Books"}]
     * title : Slow reading
     * cover : {"large":"https://covers.openlibrary.org/b/id/5546156-L.jpg"}
     * publish_date : March 2009
     * key : /books/OL22853304M
     * authors : [{"name":"John Miedema"}]
     */

    private String title;
    private CoverBean cover;
    private String publish_date;
    private String key;
    private List<PublishersBean> publishers;
    private List<AuthorsBean> authors;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CoverBean getCover() {
        return cover;
    }

    public void setCover(CoverBean cover) {
        this.cover = cover;
    }

    public String getPublish_date() {
        return publish_date;
    }

    public void setPublish_date(String publish_date) {
        this.publish_date = publish_date;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<PublishersBean> getPublishers() {
        return publishers;
    }

    public void setPublishers(List<PublishersBean> publishers) {
        this.publishers = publishers;
    }

    public List<AuthorsBean> getAuthors() {
        return authors;
    }

    public void setAuthors(List<AuthorsBean> authors) {
        this.authors = authors;
    }

    public static class CoverBean {
        /**
         * large : https://covers.openlibrary.org/b/id/5546156-L.jpg
         */

        private String large;

        public String getLarge() {
            return large;
        }

        public void setLarge(String large) {
            this.large = large;
        }
    }

    public static class PublishersBean {
        /**
         * name : Litwin Books
         */

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class AuthorsBean {
        /**
         * name : John Miedema
         */

        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
