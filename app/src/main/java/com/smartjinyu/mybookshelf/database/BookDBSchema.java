package com.smartjinyu.mybookshelf.database;

/**
 * Created by smartjinyu on 2017/1/22.
 * Database Schema
 */

public class BookDBSchema {
    public static final class BookTable{
        public static final String NAME = "Books";

        public static final class Cols{
            public static final String TITLE = "title";
            public static final String UUID = "uuid";
            public static final String AUTHORS = "authors";
            public static final String TRANSLATORS = "translators";
            public static final String WEBIDS = "webids";
            public static final String PUBLISHER = "publisher";
            public static final String PUBTIME = "pubtime";
            public static final String ADDTIME = "addtime";
            public static final String ISBN = "isbn";
            public static final String hasCover = "hasCover";
        }
    }
}
