package com.appleia.elect.db;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.appleia.elect.BookReadItem;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import com.appleia.elect.model.BookItem;   // our new model


public class eLectSQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "eLect.db";

    private static final int DATABASE_VERSION = 2;
    private final Context ctx;

    public eLectSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.ctx = context;
        copyDatabaseIfNeeded();
    }
    public void ensureDatabaseCopied() {
        copyDatabaseIfNeeded();
    }
    private void copyDatabaseIfNeeded() {
        File dbFile = ctx.getDatabasePath(DATABASE_NAME);
        if (!dbFile.exists()) {
            // ensure parent dirs
            dbFile.getParentFile().mkdirs();
            try (InputStream is = ctx.getAssets().open("database/" + DATABASE_NAME);
                 OutputStream os = new FileOutputStream(dbFile)) {
                byte[] buffer = new byte[4096];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                os.flush();
            } catch (IOException e) {
                throw new RuntimeException("Error copying preloaded DB", e);
            }
        }
    }

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

    public static final String TABLE_WISHLIST = "wishlist";

    // Column names
    public static final String COLUMN_CATEGORIES_CATID = "catid";
    public static final String COLUMN_CATEGORIES_CATNAME = "catname";

    // catalogue column names
    public static final String COLUMN_CATALOGUE_ID = "id";
    public static final String COLUMN_CATALOGUE_CATID = "catid";
    public static final String COLUMN_CATALOGUE_SUBCAT = "subcat";
    public static final String COLUMN_CATALOGUE_SHOWORDER = "showorder";
    public static final String COLUMN_CATALOGUE_BOOKID = "bookid";
    public static final String COLUMN_CATALOGUE_TYPE = "type";

    // BOOKS columns
    public static final String COLUMN_BOOKS_BOOKID = "bookid";
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


    public static final String COLUMN_TOPICS_ID = "topicid";
    public static final String COLUMN_TOPICS_NAME = "topname";

    public static final String COLUMN_TOPICSCAT_ID = "topicid";
    public static final String COLUMN_TOPICSCAT_NAME = "catid";

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
            + COLUMN_CATEGORIES_CATID + " TEXT PRIMARY KEY, "
            + COLUMN_CATEGORIES_CATNAME + " TEXT NOT NULL " + ")";


    private static final String CREATE_TABLE_TOPICS = "CREATE TABLE " + TABLE_TOPICS + " ("
            + COLUMN_TOPICS_ID + " TEXT PRIMARY KEY, "
            + COLUMN_TOPICS_NAME + " TEXT NOT NULL"
            + ");";


    private static final String CREATE_TABLE_BOOKTOPICS = "CREATE TABLE " + TABLE_BOOKTOPICS + " ("
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
            + COLUMN_WISHLIST_ID + " INTEGER PRIMARY KEY, "
            + COLUMN_WISHLIST_BOOKID + " TEXT NOT NULL "
            + ");";


    public eLectSQLiteHelper(Context context, Context ctx) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.ctx = ctx;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

       /* db.execSQL(CREATE_TABLE_CATEGORIES);
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
        db.execSQL(CREATE_TABLE_VERSION);*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        onCreate(db);
    }
    /**
     * Fetches books grouped by category and then by topic.
     * Returns a Map where each key is "<catid> - <catName>" and its value
     * is a List of topic-maps. Each topic-map contains:
     *   "id"    -> topicId (String)
     *   "name"  -> topicName (String)
     *   "books" -> List of book-maps, each with: "id", "title", "author", "cb", "ca"
     */
    public Map<String, List<Map<String, Object>>> fetchAllBooksGroupedByCategoryAndTopic() {
        SQLiteDatabase db = getReadableDatabase();

        // 1) Run the same SQL you had in Obj-C
        String sql =
                "SELECT c.catid, c.catname, " +
                        "       t.topicid, t.topname, " +
                        "       b.bookid, b.title      AS book_title, " +
                        "       b.showorder           " +
                        "FROM CATEGORIES c " +
                        "LEFT JOIN TOPICSCATEGORIES tc ON tc.catid   = c.catid " +
                        "LEFT JOIN TOPICS          t  ON t.topicid  = tc.topicid " +
                        "LEFT JOIN BOOKTOPICS      bt ON bt.topicid = t.topicid " +
                        "LEFT JOIN BOOKS           b  ON b.bookid   = bt.bookid " +
                        "ORDER BY CAST(c.catid AS INTEGER), " +
                        "         CAST(t.topicid AS INTEGER), " +
                        "         CAST(b.showorder AS FLOAT);";

        Cursor cur = db.rawQuery(sql, null);

        // 2) Build a temporary structure: categoryKey → ( topicKey → [ bookDicts ] )
        LinkedHashMap<String,LinkedHashMap<String,ArrayList<Map<String,String>>>> temp =
                new LinkedHashMap<>();

        while (cur.moveToNext()) {
            // Category
            String catId   = cur.getString(0);
            String catName = cur.getString(1);
            String categoryKey = catId + " - " + catName;

            // Topic
            String topicId   = cur.getString(2);
            String topicName = cur.getString(3);
            String topicKey  = topicId + " - " + topicName;

            // Book (may be NULL if no book)
            String bookId    = cur.isNull(4) ? null : cur.getString(4);
            String bookTitle = cur.isNull(5) ? ""   : cur.getString(5);

            // Ensure we have a map for this category
            if (!temp.containsKey(categoryKey)) {
                temp.put(categoryKey, new LinkedHashMap<>());
            }
            LinkedHashMap<String,ArrayList<Map<String,String>>> topicsMap = temp.get(categoryKey);

            // Ensure we have a list for this topic
            if (!topicsMap.containsKey(topicKey)) {
                topicsMap.put(topicKey, new ArrayList<>());
            }
            ArrayList<Map<String,String>> booksList = topicsMap.get(topicKey);

            // If there's a real book, append it
            if (bookId != null) {
                Map<String,String> bookDict = new LinkedHashMap<>();
                bookDict.put("id",    bookId);
                bookDict.put("title", bookTitle);
                booksList.add(bookDict);
            }
        }
        cur.close();

        // 3) Flatten into the final: categoryKey → [ { id, name, books:@[...] }, … ]
        LinkedHashMap<String,List<Map<String,Object>>> result = new LinkedHashMap<>();

        for (Map.Entry<String,LinkedHashMap<String,ArrayList<Map<String,String>>>> catEntry
                : temp.entrySet()) {
            String categoryKey = catEntry.getKey();
            LinkedHashMap<String,ArrayList<Map<String,String>>> topicsMap = catEntry.getValue();

            List<Map<String,Object>> topicList = new ArrayList<>();

            for (Map.Entry<String,ArrayList<Map<String,String>>> topEntry
                    : topicsMap.entrySet()) {
                String topicKey = topEntry.getKey();
                ArrayList<Map<String,String>> books = topEntry.getValue();

                // Split topicKey = "t3 - Theology"
                String[] parts = topicKey.split(" - ", 2);
                String tId   = parts[0];
                String tName = parts.length > 1 ? parts[1] : "";

                Map<String,Object> topicDict = new LinkedHashMap<>();
                topicDict.put("id",    tId);
                topicDict.put("name",  tName);
                topicDict.put("books", books);

                topicList.add(topicDict);
            }

            result.put(categoryKey, topicList);
        }

        return result;
    }

    public Map<String, Map<String, List<Map<String, String>>>> fetchBooksGroupedByYearAndCategory() {
        SQLiteDatabase db = this.getReadableDatabase();
        String sql =
                "SELECT pp.id AS ppyear_id, pp.name AS ppyear_name, " +
                        "pc.ppcat AS ppcat_id, pc.name AS ppcat_name, " +
                        "b.bookid AS book_id, b.title AS book_title, " +
                        "b.cb AS book_cb, b.ca AS book_ca, b.together AS book_together, " +
                        "b.showorder AS book_showorder, " +
                        "b.authorid AS author_id, a.name AS author_name " +
                        "FROM BOOKS b " +
                        "LEFT JOIN PPCAT pc ON b.ppcat = pc.ppcat " +
                        "LEFT JOIN PPYEAR pp ON b.ppyear = pp.id " +
                        "LEFT JOIN Authors a ON b.authorid = a.authorid " +
                        "WHERE pp.id IS NOT NULL AND pc.ppcat IS NOT NULL " +
                        "ORDER BY pp.id, CAST(pc.ppcat AS INTEGER), CAST(b.showorder AS FLOAT)";

        Cursor c = db.rawQuery(sql, null);
        Map<String, Map<String, List<Map<String,String>>>> result = new LinkedHashMap<>();
        Set<String> seen = new HashSet<>();

        while (c.moveToNext()) {
            String yearId   = c.getString(c.getColumnIndexOrThrow("ppyear_id"));
            String yearName = c.getString(c.getColumnIndexOrThrow("ppyear_name"));
            String catId    = c.getString(c.getColumnIndexOrThrow("ppcat_id"));
            String catName  = c.getString(c.getColumnIndexOrThrow("ppcat_name"));
            String bookId   = c.getString(c.getColumnIndexOrThrow("book_id"));
            if (seen.contains(bookId)) continue;
            seen.add(bookId);

            String title  = c.getString(c.getColumnIndexOrThrow("book_title"));
            String author = c.getString(c.getColumnIndexOrThrow("author_name"));
            String cb     = c.getString(c.getColumnIndexOrThrow("book_cb"));
            String ca     = c.getString(c.getColumnIndexOrThrow("book_ca"));

            String yearKey = yearId + " - " + yearName;
            String catKey  = catId  + " - " + catName;

            result
                    .computeIfAbsent(yearKey, k -> new LinkedHashMap<>())
                    .computeIfAbsent(catKey,  k -> new ArrayList<>())
                    .add(new HashMap<String,String>() {{
                        put("id",       bookId);
                        put("title",    title);
                        put("author",   author);
                        put("cb",       cb);
                        put("ca",       ca);
                    }});
        }
        c.close();
        return result;
    }

    public class Book {
        public String bookID, title, authorName, link;
        public String mainPoints, comments, description, cb, ca;
        // … plus getters/setters or make fields public for brevity …
    }

    public Book fetchBookDetailsByID(String bookId) {
        SQLiteDatabase db = getReadableDatabase();
        String sql =
                "SELECT b.title, a.name   AS author_name," +
                        "       b.link             AS link," +
                        "       b.mainpoints       AS mainPoints," +
                        "       b.mainpoints       AS mainPoints," +
                        "       b.comments         AS comments," +
                        "       b.description      AS description," +
                        "       b.cb               AS cb," +
                        "       b.ca               AS ca " +
                        "  FROM BOOKS b " +
                        "  LEFT JOIN Authors a ON b.authorid = a.authorid " +
                        " WHERE b.bookid = ?";
        Cursor c = db.rawQuery(sql, new String[]{bookId});
        Book book = null;
        if (c.moveToFirst()) {
            book = new Book();
            book.bookID     = bookId;
            book.title      = c.getString(c.getColumnIndexOrThrow("title"));
            book.authorName = c.getString(c.getColumnIndexOrThrow("author_name"));
            book.link       = c.getString(c.getColumnIndexOrThrow("link"));
            book.mainPoints = c.getString(c.getColumnIndexOrThrow("mainPoints"));
            book.comments   = c.getString(c.getColumnIndexOrThrow("comments"));
            book.description       = c.getString(c.getColumnIndexOrThrow("description"));
            book.cb         = c.getString(c.getColumnIndexOrThrow("cb"));
            book.ca         = c.getString(c.getColumnIndexOrThrow("ca"));
        }
        c.close();
        return book;
    }


    public String fetchNoteForBookID(String bookId) {
        SQLiteDatabase db = getReadableDatabase();
        String note = "";
        Cursor c = db.rawQuery(
                "SELECT notes FROM notes WHERE bookid = ?",
                new String[]{ bookId }
        );
        if (c.moveToFirst()) {
            note = c.getString(0);
        }
        c.close();
        return note;
    }

    /** Is this book in the “read” table? */
    public boolean isBookRead(String bookId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT 1 FROM readbook WHERE bookid = ? LIMIT 1",
                new String[]{ bookId }
        );
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }


    /** Save or update the note for this book (replace-or-insert). */
    public void saveOrUpdateNoteForBookID(String bookId, String noteText) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(
                "INSERT OR REPLACE INTO notes (bookid, notes) VALUES (?, ?)",
                new Object[]{ bookId, noteText }
        );
    }

    /** Delete the note for this book. */
    public void deleteNoteForBookID(String bookId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL(
                "DELETE FROM notes WHERE bookid = ?",
                new Object[]{ bookId }
        );
    }

    /** Add or remove from ReadBooks. */
    public void setBookRead(String bookId, boolean read) {
        SQLiteDatabase db = getWritableDatabase();
        if (read) {
            // build today’s date string in yyyy-MM-dd format
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    .format(new Date());
            // now insert both bookid and date
            db.execSQL(
                    "INSERT OR IGNORE INTO readbook (bookid, date) VALUES (?, ?)",
                    new Object[]{ bookId, today }
            );
        } else {
            db.execSQL(
                    "DELETE FROM readbook WHERE bookid = ?",
                    new Object[]{ bookId }
            );
        }
        // —— DEBUG: does that row now exist? ——
        try (Cursor c = db.rawQuery(
                "SELECT 1 FROM readbook WHERE bookid = ? LIMIT 1",
                new String[]{ bookId }
        )) {
            boolean exists = c.moveToFirst();
            Log.d("DB_TEST", "After " + (read ? "INSERT" : "DELETE")
                    + " into readbook for " + bookId + "? Exists: " + exists);
        }

        // —— DEBUG: dump the real schema of readbook ——
        try (Cursor info = db.rawQuery("PRAGMA table_info('readbook')", null)) {
            while (info.moveToNext()) {
                String name    = info.getString(info.getColumnIndexOrThrow("name"));
                String type    = info.getString(info.getColumnIndexOrThrow("type"));
                int    notnull = info.getInt   (info.getColumnIndexOrThrow("notnull"));
                int    pk      = info.getInt   (info.getColumnIndexOrThrow("pk"));
                Log.d("DB_TEST", String.format(
                        "COL - %s | TYPE=%s | NOTNULL=%d | PK=%d",
                        name, type, notnull, pk
                ));
            }
        }
    }

    public void setBookWish(String bookId, boolean wish) {
        Log.d("IndBookActivity", "🔔 row_wish clicked!" + bookId + " - "+ wish);

        SQLiteDatabase db = getWritableDatabase();
        if (wish) {
            // **exactly the same** pattern, minus the date
            db.execSQL(
                    "INSERT OR IGNORE INTO wishlist (bookid) VALUES (?)",
                    new Object[]{ bookId }
            );
        } else {
            db.execSQL(
                    "DELETE FROM wishlist WHERE bookid = ?",
                    new Object[]{ bookId }
            );
        }
        try (Cursor c = db.rawQuery(
                "SELECT 1 FROM wishlist WHERE bookid = ? LIMIT 1",
                new String[]{ bookId }
        )) {
            boolean exists = c.moveToFirst();
            Log.d("DB_TEST", "After " + (wish ? "INSERT" : "DELETE")
                    + ", wishlist.contains(" + bookId + ")? " + exists);
        }

        // ——— DEBUGGING: dump the actual schema of the wishlist table ———
        try (Cursor info = db.rawQuery("PRAGMA table_info('wishlist')", null)) {
            while (info.moveToNext()) {
                String name    = info.getString(info.getColumnIndexOrThrow("name"));
                String type    = info.getString(info.getColumnIndexOrThrow("type"));
                int    notnull = info.getInt   (info.getColumnIndexOrThrow("notnull"));
                int    pk      = info.getInt   (info.getColumnIndexOrThrow("pk"));
                Log.d("DB_TEST", String.format(
                        "COL - %s  |  TYPE=%s  |  NOTNULL=%d  |  PK=%d",
                        name, type, notnull, pk
                ));
            }
        }
    }
    public boolean isBookWish(String bookId) {
        SQLiteDatabase db = getReadableDatabase();
        // query() will build the right SQL for you
        Cursor c = db.query(
                TABLE_WISHLIST,                       // "WISHLIST"
                new String[]{ COLUMN_WISHLIST_BOOKID }, // projections (we just need any column)
                COLUMN_WISHLIST_BOOKID + " = ?",      // selection
                new String[]{ bookId },               // selectionArgs
                null, null,                           // groupBy / having
                null,                                 // orderBy
                "1"                                   // limit 1
        );
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public List<Map<String,Object>> fetchBooksRead() {
        List<Map<String,Object>> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql =
                "SELECT b.bookid AS id, " +
                        "       b.title  AS BookTitle, " +
                        "       a.name   AS AuthorName, " +
                        "       rb.date  AS ReadDate " +
                        "FROM readBook rb " +
                        "JOIN Books b   ON rb.bookid   = b.bookid " +
                        "JOIN Authors a ON b.authorid = a.authorid;";
        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            do {
                Map<String,Object> row = new HashMap<>();
                row.put("id",         c.getString(c.getColumnIndexOrThrow("id")));
                row.put("BookTitle",  c.getString(c.getColumnIndexOrThrow("BookTitle")));
                row.put("AuthorName", c.getString(c.getColumnIndexOrThrow("AuthorName")));
                row.put("ReadDate",   c.getString(c.getColumnIndexOrThrow("ReadDate")));
                list.add(row);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    /**
     * Updates the read date for a given book.
     * @return true if at least one row was updated.
     */
    public boolean updateBookReadDate(String bookId, String newDate) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("date", newDate);
        int rows = db.update(
                "readBook",        // table
                cv,                // new values
                "bookid = ?",      // WHERE clause
                new String[]{bookId}
        );
        db.close();
        return rows > 0;
    }

    public List<BookItem> fetchWishlistItems() {
        List<BookItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql =
                "SELECT b.bookid AS id, " +
                        "       b.title  AS title, " +
                        "       a.name   AS author " +
                        "FROM wishlist w " +
                        "JOIN Books b   ON w.bookid   = b.bookid " +
                        "JOIN Authors a ON b.authorid = a.authorid;";
        Cursor c = db.rawQuery(sql, null);
        if (c.moveToFirst()) {
            do {
                String id     = c.getString(c.getColumnIndexOrThrow("id"));
                String title  = c.getString(c.getColumnIndexOrThrow("title"));
                String author = c.getString(c.getColumnIndexOrThrow("author"));
                list.add(new BookItem(id, title, author));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    /**
     * Removes a book from the wishlist.
     * @return true if at least one row was deleted.
     */
    public boolean removeWishlistItem(String bookId) {
        SQLiteDatabase db = getWritableDatabase();
        int rows = db.delete(
                "wishlist",           // table name
                "bookid = ?",         // WHERE
                new String[]{bookId}  // args
        );
        db.close();
        return rows > 0;
    }

    public List<String> fetchAllBookTitles() {
        List<String> results = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT title FROM Books",
                null
        );
        if (c.moveToFirst()) {
            do {
                results.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return results;
    }

    /** Returns all distinct author names from the Authors table. */
    public List<String> fetchAllAuthors() {
        List<String> results = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT name FROM Authors",
                null
        );
        if (c.moveToFirst()) {
            do {
                results.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return results;
    }

    /** Returns all distinct topic names from the Topics table. */
    public List<String> fetchAllTopics() {
        List<String> results = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT DISTINCT topname FROM Topics",
                null
        );
        if (c.moveToFirst()) {
            do {
                results.add(c.getString(0));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return results;
    }

    public List<BookItem> fetchBooksForAuthorID(String authorId) {
        List<BookItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql =
                "SELECT b.bookid AS id, b.title AS title, a.name AS author " +
                        "FROM Books b " +
                        "JOIN Authors a ON b.authorid = a.authorid " +
                        "WHERE b.authorid = ?";
        Cursor c = db.rawQuery(sql, new String[]{ authorId });
        if (c.moveToFirst()) {
            do {
                String id     = c.getString(c.getColumnIndexOrThrow("id"));
                String title  = c.getString(c.getColumnIndexOrThrow("title"));
                String author = c.getString(c.getColumnIndexOrThrow("author"));
                list.add(new BookItem(id, title, author));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    /**
     * Fetch all books (id, title, author) for a given topicID.
     * Assumes you have a join‑table BookTopics(bookid, topicid).
     */
    public List<BookItem> fetchBooksForTopicID(String topicId) {
        List<BookItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        String sql =
                "SELECT b.bookid AS id, b.title AS title, a.name AS author " +
                        "FROM Books b " +
                        "JOIN Authors a       ON b.authorid    = a.authorid " +
                        "JOIN BookTopics bt    ON b.bookid      = bt.bookid " +
                        "JOIN Topics t         ON bt.topicid    = t.topicid " +
                        "WHERE t.topicid = ?";
        Cursor c = db.rawQuery(sql, new String[]{ topicId });
        if (c.moveToFirst()) {
            do {
                String id     = c.getString(c.getColumnIndexOrThrow("id"));
                String title  = c.getString(c.getColumnIndexOrThrow("title"));
                String author = c.getString(c.getColumnIndexOrThrow("author"));
                list.add(new BookItem(id, title, author));
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return list;
    }

    public String fetchAuthorIDForName(String name) {
        String id = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT authorid FROM Authors WHERE name = ?",
                new String[]{ name }
        );
        if (c.moveToFirst()) {
            id = c.getString(0);
        }
        c.close();
        db.close();
        return id;
    }

    /**
     * Find the topicid for a given topic name.
     */
    public String fetchTopicIDForTopicName(String topicName) {
        String id = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT topicid FROM Topics WHERE topname = ?",
                new String[]{ topicName }
        );
        if (c.moveToFirst()) {
            id = c.getString(0);
        }
        c.close();
        db.close();
        return id;
    }

    public String fetchBookIDForTitle(String title) {
        String id = null;
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery(
                "SELECT bookid FROM Books WHERE title = ?",
                new String[]{ title }
        );
        if (c.moveToFirst()) {
            id = c.getString(0);
        }
        c.close();
        db.close();
        return id;
    }

    public String fetchDBVersionFromDatabase() {
        String apversion = "";
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            // Select only the apversion column, order by id DESC so the latest is first, limit to 1 row
            cursor = db.query(
                    "version",                         // table
                    new String[]{"apversion"},        // columns
                    null,                              // selection
                    null,                              // selectionArgs
                    null,                              // groupBy
                    null,                              // having
                    "id DESC",                         // orderBy
                    "1"                                // limit
            );

            if (cursor != null && cursor.moveToFirst()) {
                apversion = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
            // optionally log or handle
        } finally {
            if (cursor != null) cursor.close();
            if (db != null) db.close();
        }

        return apversion.trim();
    }

}
