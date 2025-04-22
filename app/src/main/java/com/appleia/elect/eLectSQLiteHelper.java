package com.appleia.elect;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class eLectSQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "eLect.db";
    private static final int DATABASE_VERSION = 1;


    // Table name
    public static final String TABLE_CATEGORIES = "CATEGORIES";
    public static final String TABLE_CATALOGUE = "CATALOGUE";

    public static final String TABLE_BOOKS ="BOOKS";

    public static final String TABLE_AUTHORS ="AUTHORS";
    public static final String TABLE_BOOKAUTHOR ="BOOKAUTHOR";

    public static final String TABLE_PPCAT ="PPCAT";
    public static final String TABLE_PPYEAR ="PPYEAR";
    public static final String TABLE_NOTES = "notes";
    public static final String TABLE_READBOOK ="readbook";
    public static final String TABLE_VERSION ="version";
    public static final String TABLE_TOPICS = "TOPICS";
    public static final String TABLE_TOPICSCATEGORIES = "TOPICSCATEGORIES";
    public static final String TABLE_KEYWORDS = "KEYWORDS";
    public static final String TABLE_KEYWORDSBOOKS = "KEYWORDSBOOKS";
    public static final String TABLE_BOOKTOPICS = "BOOKTOPICS";

    public static final String TABLE_WISHLIST = "WISHLIST";

    // Column names
    public static final String COLUMN_CATEGORIES_CATID = "catid";
    public static final String COLUMN_CATEGORIES_NAME = "name";

    // catalogue column names
    public static final String COLUMN_CATALOGUE_ID = "id";
    public static final String COLUMN_CATALOGUE_CATID = "catid";
    public static final String COLUMN_CATALOGUE_SUBCAT = "subcat";
    public static final String COLUMN_CATALOGUE_SHOWORDER = "showorder";
    public static final String COLUMN_CATALOGUE_BOOKID = "bookid";
    public static final String COLUMN_CATALOGUE_TYPE = "type";

    // BOOKS columns
    public static final String COLUMN_BOOKS_BOOKID = "id";
    public static final String COLUMN_BOOKS_TITLE = "title";
    public static final String COLUMN_BOOKS_AUTHORID = "authorid";
    public static final String COLUMN_BOOKS_EXTCAT = "extcat";
    public static final String COLUMN_BOOKS_INTCAT = "intcat";
    public static final String COLUMN_BOOKS_PPYEAR = "ppyear";
    public static final String COLUMN_BOOKS_PPCAT = "ppcat";
    public static final String COLUMN_BOOKS_LINK = "link";
    public static final String COLUMN_BOOKS_COMMENTS = "comments";
    public static final String COLUMN_BOOKS_url = "url";
    public static final String COLUMN_BOOKS_DESCRIPTION = "description";
    public static final String COLUMN_BOOKS_MAINPOINTS = "mainpoints";
    public static final String COLUMN_BOOKS_SHOWORDER = "showorder";
    public static final String COLUMN_BOOKS_CB = "cb";
    public static final String COLUMN_BOOKS_CA = "ca";
    public static final String COLUMN_BOOKS_TOGETHER = "together";


    public static final String COLUMN_TOPICS_ID = "id";
    public static final String COLUMN_TOPICS_NAME = "name";

    public static final String COLUMN_TOPICSCAT_ID = "id";
    public static final String COLUMN_TOPICSCAT_NAME = "name";

    public static final String COLUMN_BOOKTOPICS_ID = "id";
    public static final String COLUMN_BOOKTOPICS_BOOKID = "bookid";
    public static final String COLUMN_BOOKTOPICS_TOPICID = "topicid";

    public static final String COLUMN_KEYWORDS_ID = "id";
    public static final String COLUMN_KEYWORDS_NAME = "name";

    public static final String COLUMN_KEYWORDSBOOKS_ID = "id";
    public static final String COLUMN_KEYWORDSBOOKS_BOOKID = "bookid";
    public static final String COLUMN_KEYWORDSBOOKS_KEYWORDID = "keywordid";

    public static final String COLUMN_WISHLIST_ID = "id";
    public static final String COLUMN_WISHLIST_BOOKID = "bookid";

    private static final String CREATE_TABLE_AUTHORS = "CREATE TABLE " + TABLE_AUTHORS + " (" +
            "authorid TEXT PRIMARY KEY, " +
            "name TEXT, " +
            "orderchar TEXT);";

    private static final String CREATE_TABLE_BOOKAUTHOR = "CREATE TABLE " + TABLE_BOOKAUTHOR + " (" +
            "id TEXT PRIMARY KEY, " +
            "bookid TEXT, " +
            "authorid TEXT);";

    private static final String CREATE_TABLE_PPCAT = "CREATE TABLE " + TABLE_PPCAT + " (" +
            "ppcat TEXT PRIMARY KEY, " +
            "name TEXT, " +
            "description TEXT, " +
            "shortname TEXT);";

    private static final String CREATE_TABLE_PPYEAR = "CREATE TABLE " + TABLE_PPYEAR + " (" +
            "id TEXT PRIMARY KEY, " +
            "name TEXT, " +
            "description TEXT);";

    private static final String CREATE_TABLE_CATALOGUE = "CREATE TABLE " + TABLE_CATALOGUE + " (" +
            COLUMN_CATALOGUE_ID + " TEXT PRIMARY KEY, " +
            COLUMN_CATALOGUE_CATID + " TEXT, " +
            COLUMN_CATALOGUE_SUBCAT + " TEXT, " +
            COLUMN_CATALOGUE_SHOWORDER + " TEXT, " +
            COLUMN_CATALOGUE_BOOKID + " TEXT, " +
            COLUMN_CATALOGUE_TYPE + " TEXT);";

    private static final String CREATE_TABLE_NOTES = "CREATE TABLE " + TABLE_NOTES + " (" +
            "id TEXT PRIMARY KEY, " +
            "bookid TEXT, " +
            "notes TEXT);";

    private static final String CREATE_TABLE_READBOOK = "CREATE TABLE " + TABLE_READBOOK + " (" +
            "id TEXT PRIMARY KEY, " +
            "bookid TEXT, " +
            "date TEXT);";

    private static final String CREATE_TABLE_VERSION = "CREATE TABLE " + TABLE_VERSION + " (" +
            "id TEXT PRIMARY KEY, " +
            "dbversion TEXT, " +
            "apversion TEXT);";


    private static final String CREATE_TABLE_TOPICSCATEGORIES = "CREATE TABLE " + TABLE_TOPICSCATEGORIES + " ("
            + COLUMN_TOPICSCAT_ID + " TEXT PRIMARY KEY, "
            + COLUMN_TOPICSCAT_NAME + " TEXT NOT NULL"
            + ");";

    // Create table SQL
    private static final String CREATE_TABLE_CATEGORIES = "CREATE TABLE " + TABLE_CATEGORIES + " ("
            + COLUMN_CATEGORIES_CATID + " TEXT PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_CATEGORIES_NAME + " TEXT NOT NULL " + ")";


    private static final String CREATE_TABLE_TOPICS = "CREATE TABLE " + TABLE_TOPICS + " ("
            + COLUMN_TOPICS_ID + " TEXT PRIMARY KEY, "
            + COLUMN_TOPICS_NAME + " TEXT NOT NULL"
            + ");";


    private static final String CREATE_TABLE_BOOKTOPICS = "CREATE TABLE " + TABLE_BOOKTOPICS + " ("
            + COLUMN_BOOKTOPICS_ID + " TEXT PRIMARY KEY, "
            + COLUMN_BOOKTOPICS_BOOKID + " TEXT NOT NULL, "
            + COLUMN_BOOKTOPICS_TOPICID + " TEXT NOT NULL"
            + ");";

    private static final String CREATE_TABLE_KEYWORDS = "CREATE TABLE " + TABLE_KEYWORDS + " ("
            + COLUMN_KEYWORDS_ID + " TEXT PRIMARY KEY, "
            + COLUMN_KEYWORDS_NAME + " TEXT NOT NULL"
            + ");";

    private static final String CREATE_TABLE_KEYWORDSBOOKS = "CREATE TABLE " + TABLE_KEYWORDSBOOKS + " ("
            + COLUMN_KEYWORDSBOOKS_ID + " TEXT PRIMARY KEY, "
            + COLUMN_KEYWORDSBOOKS_BOOKID + " TEXT NOT NULL, "
            + COLUMN_KEYWORDSBOOKS_KEYWORDID + " TEXT NOT NULL"
            + ");";

    private static final String CREATE_TABLE_BOOKS = "CREATE TABLE " + TABLE_BOOKS + " ("
            + COLUMN_BOOKS_BOOKID + " TEXT PRIMARY KEY, "
            + COLUMN_BOOKS_TITLE + " TEXT NOT NULL, "
            + COLUMN_BOOKS_AUTHORID + " TEXT, "
            + COLUMN_BOOKS_EXTCAT + " TEXT, "
            + COLUMN_BOOKS_INTCAT + " TEXT, "
            + COLUMN_BOOKS_PPYEAR + " TEXT, "
            + COLUMN_BOOKS_PPCAT + " TEXT, "
            + COLUMN_BOOKS_LINK + " TEXT, "
            + COLUMN_BOOKS_COMMENTS + " TEXT, "
            + COLUMN_BOOKS_url + " TEXT, "
            + COLUMN_BOOKS_DESCRIPTION + " TEXT, "
            + COLUMN_BOOKS_MAINPOINTS + " TEXT, "
            + COLUMN_BOOKS_SHOWORDER + " TEXT, "
            + COLUMN_BOOKS_CB + " TEXT, "
            + COLUMN_BOOKS_CA + " TEXT, "
            + COLUMN_BOOKS_TOGETHER + " TEXT"
            + ");";

    private static final String CREATE_TABLE_WISHLIST = "CREATE TABLE " + TABLE_WISHLIST + " ("
            + COLUMN_WISHLIST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_WISHLIST_BOOKID + " TEXT NOT NULL "
            + ");";


    public eLectSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public eLectSQLiteHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(CREATE_TABLE_CATEGORIES);
        db.execSQL(CREATE_TABLE_TOPICS);
        db.execSQL(CREATE_TABLE_TOPICSCATEGORIES);
        db.execSQL(CREATE_TABLE_BOOKTOPICS);
        db.execSQL(CREATE_TABLE_KEYWORDS);
        db.execSQL(CREATE_TABLE_KEYWORDSBOOKS);
        db.execSQL(CREATE_TABLE_BOOKS);
        db.execSQL(CREATE_TABLE_AUTHORS);
        db.execSQL(CREATE_TABLE_NOTES);
        db.execSQL(CREATE_TABLE_BOOKAUTHOR);
        db.execSQL(CREATE_TABLE_READBOOK);
        db.execSQL(CREATE_TABLE_WISHLIST);
        db.execSQL(CREATE_TABLE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }
}
