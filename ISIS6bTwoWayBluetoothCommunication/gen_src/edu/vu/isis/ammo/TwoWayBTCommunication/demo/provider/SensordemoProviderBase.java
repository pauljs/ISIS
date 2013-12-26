//// THIS IS GENERATED CODE, MAKE SURE ANY CHANGES MADE HERE ARE PROPAGATED INTO THE GENERATOR TEMPLATES
//package edu.vu.isis.ammo.biosensor.demo.provider;
//
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//
//import org.json.JSONException;
//import org.json.JSONObject;
//import org.json.JSONTokener;
//import android.util.Log;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import android.content.ContentProvider;
//import android.content.ContentUris;
//import android.content.ContentValues;
//import android.content.Context;
//import android.content.UriMatcher;
//import android.database.Cursor;
//import android.database.MatrixCursor;
//import android.database.sqlite.SQLiteCursor;
//import android.database.sqlite.SQLiteException;
//import android.database.sqlite.SQLiteDatabase;
//import android.database.sqlite.SQLiteOpenHelper;
//import android.database.sqlite.SQLiteQueryBuilder;
//import android.net.Uri;
//import android.os.Environment;
//import android.os.ParcelFileDescriptor;
//import android.provider.BaseColumns;
//import android.text.TextUtils;
//
//import edu.vu.isis.ammo.biosensor.demo.provider.SensordemoSchemaBase.Meta;
//import edu.vu.isis.ammo.biosensor.demo.provider.SensordemoSchemaBase.ZephyrTableSchemaBase;
//
//
//
//// BEGIN CUSTOM Sensordemo IMPORTS
//// END   CUSTOM  Sensordemo IMPORTS
//
//public abstract class SensordemoProviderBase extends ContentProvider {
//  public final static String VERSION = "1.7.0";
//// Table definitions 
//public interface Tables {
//   public static final String ZEPHYR_TBL = "zephyr";
//
//}
//
//
//private static final String ZEPHYR_KEY_CLAUSE;
//static {
//  ZEPHYR_KEY_CLAUSE = new StringBuilder()
//    .toString();
//}; 
//
//
//// Views.
//public interface Views {
//   // Nothing to put here yet.
//}
//
//protected class SensordemoDatabaseHelper extends SQLiteOpenHelper {
//   // ===========================================================
//   // Constants
//   // ===========================================================
//   private final Logger logger = LoggerFactory.getLogger(SensordemoDatabaseHelper.class);
//
//   // ===========================================================
//   // Fields
//   // ===========================================================
//
//   /** Nothing to put here */
//
//
//   // ===========================================================
//   // Constructors
//   // ===========================================================
//   public SensordemoDatabaseHelper(Context context, String name, int version) {
//      super(context, name, null, version);
//   }
//
//   /**
//    * Pass through to grand parent.
//    */
//   public SensordemoDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
//      super(context, name, factory, version);
//   }
//
//
//   // ===========================================================
//   // SQLiteOpenHelper Methods
//   // ===========================================================
//
//   @Override
//   public synchronized void onCreate(SQLiteDatabase db) {
//      logger.info( "Bootstrapping database");
//      try {
//        /** 
//         * Table Name: zephyr <P>
//         */
//        db.execSQL("CREATE TABLE \"" + Tables.ZEPHYR_TBL + "\" (" 
//          + "\""+ZephyrTableSchemaBase.MEASUREMENT_TIME + "\" INTEGER, " 
//          + "\""+ZephyrTableSchemaBase.INSTANT_PULSE + "\" SMALLINT, " 
//          + "\""+ZephyrTableSchemaBase.INSTANT_SPEED + "\" REAL, " 
//          + "\""+ZephyrTableSchemaBase.MEASUREMENT_TYPE + "\" INTEGER, " 
//          + "\""+ZephyrTableSchemaBase._ID + "\" INTEGER PRIMARY KEY AUTOINCREMENT, "
//          + "\""+ZephyrTableSchemaBase._RECEIVED_DATE + "\" LONG, "
//          + "\""+ZephyrTableSchemaBase._DISPOSITION + "\" TEXT );" ); 
//
//        preloadTables(db);
//        createViews(db);
//        createTriggers(db);
//
//        } catch (SQLiteException ex) {
//           ex.printStackTrace();
//        }
//   }
//
//   @Override
//   public synchronized void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//      logger.warn( "Upgrading database from version {} to {} which will destroy all old data",
//             oldVersion, newVersion);
//         db.execSQL("DROP TABLE IF EXISTS \"" + Tables.ZEPHYR_TBL + "\";");
//
//      onCreate(db);
//   }
//
//   // ===========================================================
//   // Database Creation Helper Methods
//   // ===========================================================
//
//   /**
//    * Can be overriden to cause tables to be loaded
//    */
//   protected void preloadTables(SQLiteDatabase db) { }
//
//   /** View creation */
//   protected void createViews(SQLiteDatabase db) { }
//
//   /** Trigger creation */
//   protected void createTriggers(SQLiteDatabase db) { }
//}
//
//// ===========================================================
//// Constants
//// ===========================================================
//private final static Logger logger = LoggerFactory.getLogger(SensordemoProviderBase.class);
//
//// ===========================================================
//// Fields
//// ===========================================================
// /** Projection Maps */
//protected static String[] zephyrProjectionKey;
//protected static HashMap<String, String> zephyrProjectionMap;
//
//
///** Uri Matcher tags */
//protected static final int ZEPHYR_BLOB = 10;
//protected static final int ZEPHYR_SET = 11;
//protected static final int ZEPHYR_ID = 12;
//protected static final int ZEPHYR_SERIAL = 13;
//protected static final int ZEPHYR_DESERIAL = 14;
//protected static final int ZEPHYR_META = 15;
//
//private static final MatrixCursor zephyrFieldTypeCursor;
//
//
///** Uri matcher */
//protected static final UriMatcher uriMatcher;
//
///** Database helper */
//protected SensordemoDatabaseHelper openHelper;
//protected abstract boolean createDatabaseHelper();
//
///**
// * In support of cr.openInputStream
// */
//private static final UriMatcher blobUriMatcher;
//static {
//  blobUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
//    blobUriMatcher.addURI(SensordemoSchemaBase.AUTHORITY, Tables.ZEPHYR_TBL+"/#/*", ZEPHYR_BLOB);
//
//}
//
///**
// * Examines uri's from clients:
// *  long fkId = cursor.getLong(cursor.getColumnIndex(Table.FK));
// *    Drawable icon = null;
// *    Uri fkUri = ContentUris.withAppendedId(TableSchema.CONTENT_URI, fkId);
// *  // then the fkUri can be used to get a tuple using a query.
// *    Cursor categoryCursor = this.managedQuery(categoryUri, null, null, null, null);
// *  // ...or the fkUri can be used to get a file descriptor.
// *    Uri iconUri = Uri.withAppendedPath(categoryUri, CategoryTableSchema.ICON);
// *  InputStream is = this.getContentResolver().openInputStream(iconUri);
// *  Drawableicon = Drawable.createFromStream(is, null);
// *  
// *  It is expected that the uri passed in will be of the form <content_uri>/<table>/<id>/<column>
// *  This is simple enough that a UriMatcher is not needed and 
// *  a simple uri.getPathSegments will suffice to identify the file.
// */
//
//// ===========================================================
//// Content Provider Overrides
//// ===========================================================
//
///**
// * This is used to get fields which are too large to store in the
// * database or would exceed the Binder data size limit of 1MiB.
// * The blob matcher expects a URI post-pended with 
// */
//
//@Override
//public synchronized ParcelFileDescriptor openFile (Uri uri, String mode) {
//    int imode = 0;
//    if (mode.contains("w")) imode |= ParcelFileDescriptor.MODE_WRITE_ONLY;
//    if (mode.contains("r")) imode |= ParcelFileDescriptor.MODE_READ_ONLY;
//    if (mode.contains("+")) imode |= ParcelFileDescriptor.MODE_APPEND;
//
//    final List<String> pseg = uri.getPathSegments();
//    final SQLiteDatabase db = this.openHelper.getReadableDatabase();
//
//    final int match = blobUriMatcher.match(uri);
//    switch (match) {
//     case ZEPHYR_BLOB:
//        if (pseg.size() < 3)
//            return null;
//
//        try {
//            final String tuple = pseg.get(1);
//            final String field = pseg.get(2);
//            logger.trace("open file error tuple=[{}] field=[{}]", tuple, field);
//            logger.trace("pseg=[{}]", pseg);
//            final String selection = new StringBuilder()
//            .append(ZephyrTableSchemaBase._ID).append("=?")
//            .toString();
//            final String[] selectArgs = new String[]{tuple}; 
//
//            Cursor blobCursor = null;
//            final File filePath;
//            try {
//               blobCursor = db
//                    .query(Tables.ZEPHYR_TBL, new String[]{field}, 
//                            selection, selectArgs, 
//                            null, null, null);
//               if (blobCursor.getCount() < 1) return null;
//               blobCursor.moveToFirst();
//
//               filePath = new File(blobCursor.getString(0));
//            } catch (SQLiteException ex) {
//               logger.error("files are not implemented for large blobs {}", field);
//               return null;
//
//            } finally {
//               if (blobCursor != null) blobCursor.close();
//            }
//
//            if (0 != (imode & ParcelFileDescriptor.MODE_WRITE_ONLY)) {
//               logger.trace("candidate blob file {}", filePath);
//               try {
//                   final File newFile = receiveFile(Tables.ZEPHYR_TBL, tuple, filePath);
//                   logger.trace("new blob file {}", newFile);
//
//                   final ContentValues cv = new ContentValues();
//                   cv.put(field, newFile.getCanonicalPath());
//                   db.update(Tables.ZEPHYR_TBL, cv, selection, selectArgs);
//
//                   return ParcelFileDescriptor.open(newFile, imode | ParcelFileDescriptor.MODE_CREATE);
//
//               } catch (FileNotFoundException ex) {
//                   logger.error("could not open file {}\n {}", 
//                           ex.getLocalizedMessage(), ex.getStackTrace());
//                   return null;
//               } catch (IOException ex) {
//                   return null;
//               }
//            }
//            return ParcelFileDescriptor.open(filePath, imode);
//
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//        } 
//     break;
//      
//    default:
//        throw new IllegalArgumentException("Unknown URI " + uri);
//    }
//    return null;
//}
//
//@Override
//public synchronized boolean onCreate() {
//   this.createDatabaseHelper();
//   return true;
//}
//
//@Override
//public synchronized Cursor query(Uri uri, String[] projection, String selection,
//      String[] selectionArgs, String sortOrder) {
//   final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
//   final SQLiteDatabase db = this.openHelper.getReadableDatabase();
//
//   // Switch on the path in the uri for what we want to query.
//   final SQLiteCursor cursor;
//   final List<String> psegs = uri.getPathSegments();
//
//   switch (uriMatcher.match(uri)) {
//   case ZEPHYR_META:
//      logger.debug("meta","provide ZEPHYR meta data {}",uri);
//      return zephyrFieldTypeCursor;
//
//   case ZEPHYR_SET:
//      qb.setTables(Tables.ZEPHYR_TBL);
//      qb.setProjectionMap(zephyrProjectionMap);
//
//      cursor = (SQLiteCursor) qb.query(db, projection, selection, selectionArgs, null, null, 
//     (! TextUtils.isEmpty(sortOrder)) ? sortOrder
//     : ZephyrTableSchemaBase.DEFAULT_SORT_ORDER);
//
//      cursor.setNotificationUri(getContext().getContentResolver(), uri);
//      return cursor;
//
//   case ZEPHYR_ID:
//      qb.setTables(Tables.ZEPHYR_TBL);
//      qb.setProjectionMap(zephyrProjectionMap);
//      qb.appendWhere(ZephyrTableSchemaBase._ID + "="
//        + uri.getPathSegments().get(1));
//
//      cursor = (SQLiteCursor) qb.query(db, projection, selection, selectionArgs, null, null, 
//        (! TextUtils.isEmpty(sortOrder)) ? sortOrder
//     : ZephyrTableSchemaBase.DEFAULT_SORT_ORDER);
//
//      cursor.setNotificationUri(getContext().getContentResolver(), uri);
//      return cursor;
//
//   case ZEPHYR_SERIAL:
//   {
//      qb.setTables(Tables.ZEPHYR_TBL);
//      qb.setProjectionMap(zephyrProjectionMap);
//
//      qb.appendWhere(ZephyrTableSchemaBase._ID + " = " + uri.getPathSegments().get(1));
//
//      final List<String> projectionList = new ArrayList<String>();
//      for (final Meta columnMeta : SensordemoSchemaBase.ZEPHYR_CURSOR_COLUMNS) {
//          switch (columnMeta.type) {
//          case SensordemoSchemaBase.FIELD_TYPE_BLOB: break;
//          default: 
//              projectionList.add(columnMeta.name);
//          }
//      }
//      final String[] projectionArray = projectionList.toArray(new String[projectionList.size()]);
//      cursor = (SQLiteCursor) qb.query(db, projectionArray, null, null, null, null, null);
//      if (1 > cursor.getCount()) {
//       logger.info("no data of type ZEPHYR_ID"); 
//       cursor.close();
//       return null;
//      }
//      if (psegs.size() < 2) {
//          return cursor;
//      }
//      return this.customQueryZephyrTableSchema(psegs, cursor);
//   }
//   case ZEPHYR_BLOB:
//   {
//      final List<String> fieldNameList = new ArrayList<String>();
//
//      for (Meta columnMeta : SensordemoSchemaBase.ZEPHYR_CURSOR_COLUMNS) {
//          switch (columnMeta.type) {
//          case SensordemoSchemaBase.FIELD_TYPE_BLOB:
//          case SensordemoSchemaBase.FIELD_TYPE_FILE:
//              fieldNameList.add(columnMeta.name);
//              break;
//          default:
//          }
//      }
//      if (fieldNameList.size() < 1) return null;
//
//      final String[] fieldNameArray = fieldNameList.toArray(new String[fieldNameList.size()]);
//      final String bselect = ZephyrTableSchemaBase._ID + "=?";
//      final String[] bselectArgs = new String[]{ uri.getPathSegments().get(1) };
//      return db.query(Tables.ZEPHYR_TBL, fieldNameArray, 
//                 bselect, bselectArgs, null, null, null);
//   }
//
//   default: 
//      throw new IllegalArgumentException("Unknown URI " + uri);
//   }
//}
//
//private Cursor customQueryZephyrTableSchema(final List<String> psegs, final SQLiteCursor cursor) {
//    logger.info("no custom cursor ZephyrTableSchema {}", psegs); 
//    return cursor;
//} 
//
//@Override
//public synchronized Uri insert(Uri uri, ContentValues assignedValues) {
//   /** Validate the requested uri and do default initialization. */
//   switch (uriMatcher.match(uri)) {
//   case ZEPHYR_SET:
//      try {
//         final ContentValues values = this.initializeZephyrWithDefaults(assignedValues);
//         if ( SensordemoSchemaBase.ZEPHYR_KEY_COLUMNS.length < 1 ) {
//            final SQLiteDatabase db = openHelper.getWritableDatabase();
//            final long rowID = db.insert(Tables.ZEPHYR_TBL, ZephyrTableSchemaBase.MEASUREMENT_TIME, values);
//            if (rowID < 1) {
//               throw new SQLiteException("Failed to insert row into " + uri);
//            }
//            final Uri playerURI = ContentUris.withAppendedId(ZephyrTableSchemaBase.CONTENT_URI, rowID);
//            getContext().getContentResolver().notifyChange(uri, null);
//            return playerURI;
//         }
//
//         final List<String> selectArgsList = new ArrayList<String>();
//         for (String item : SensordemoSchemaBase.ZEPHYR_KEY_COLUMNS) {
//            selectArgsList.add(values.getAsString(item));
//         } 
//         final String[] selectArgs = selectArgsList.toArray(new String[0]);
//         final SQLiteDatabase db = openHelper.getWritableDatabase();
//
//         final long rowID;
//         final int count = db.update(Tables.ZEPHYR_TBL, values, 
//               SensordemoProviderBase.ZEPHYR_KEY_CLAUSE, selectArgs);
//         if ( count < 1 ) {
//            rowID = db.insert(Tables.ZEPHYR_TBL, ZephyrTableSchemaBase.MEASUREMENT_TIME, values);
//            if (rowID < 1) {
//               throw new SQLiteException("Failed to insert row into " + uri);
//            }
//         }
//         else {
//            final Cursor cursor = db.query(Tables.ZEPHYR_TBL, null, 
//                SensordemoProviderBase.ZEPHYR_KEY_CLAUSE, selectArgs, 
//                null, null, null);
//            cursor.moveToFirst();
//            rowID = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
//            cursor.close();
//         }
//         final Uri playerURI = ContentUris.withAppendedId(ZephyrTableSchemaBase.CONTENT_URI, rowID);
//         getContext().getContentResolver().notifyChange(uri, null);
//         return playerURI;
//      } catch (SQLiteException ex) {
//         logger.warn("bad column set {}", ex.getLocalizedMessage());
//      }
//   return null;
//
//   /**
//     Receive messages from the distributor and deserialize into the content provider.
//   */
//   case ZEPHYR_DESERIAL:
//      try {
//         final ContentValues values = this.initializeZephyrWithDefaults(assignedValues);
//         final String json = assignedValues.getAsString("_serial");
//         final JSONObject input = (JSONObject) new JSONTokener(json).nextValue();
//
//         final ContentValues cv = new ContentValues();
//         for (int ix=0; ix <  SensordemoSchemaBase.ZEPHYR_CURSOR_COLUMNS.length; ix++) {
//             switch (SensordemoSchemaBase.ZEPHYR_CURSOR_COLUMNS[ix].type) {
//             case SensordemoSchemaBase.FIELD_TYPE_BLOB:
//               continue;
//             }
//             final String key = SensordemoSchemaBase.ZEPHYR_CURSOR_COLUMNS[ix].name;
//             try {
//               final String value = input.getString(key);
//               cv.put(key, value);
//             } catch (JSONException ex) {
//               logger.error("could not extract from json {} {}", 
//                   ex.getLocalizedMessage(), ex.getStackTrace());
//             }
//         }
//
//         final List<String> selectArgsList = new ArrayList<String>();
//         for (String item : SensordemoSchemaBase.ZEPHYR_KEY_COLUMNS) {
//            selectArgsList.add(values.getAsString(item));
//         } 
//         final String[] selectArgs = selectArgsList.toArray(new String[0]);
//         final SQLiteDatabase db = this.openHelper.getWritableDatabase();
//         final long rowID;
//         final int count = db.update(Tables.ZEPHYR_TBL, values, 
//                 SensordemoProviderBase.ZEPHYR_KEY_CLAUSE, selectArgs);
//         if ( count < 1 ) {
//            rowID = db.insert(Tables.ZEPHYR_TBL, ZephyrTableSchemaBase.MEASUREMENT_TIME, values);
//            if (rowID < 1) {
//               throw new SQLiteException("Failed to insert row into " + uri);
//            }
//         }
//         else {
//            final Cursor cursor = db.query(Tables.ZEPHYR_TBL, null, 
//                 SensordemoProviderBase.ZEPHYR_KEY_CLAUSE, selectArgs, 
//                 null, null, null);
//            cursor.moveToFirst();
//            rowID = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
//            cursor.close();
//         }
//         final Uri playerURI = ContentUris.withAppendedId(ZephyrTableSchemaBase.CONTENT_URI, rowID);
//         getContext().getContentResolver().notifyChange(uri, null);
//         return playerURI;
//      } catch (JSONException ex) {
//         logger.error("could not parse json {} {}", 
//              ex.getLocalizedMessage(), ex.getStackTrace());
//      }
//      return null;
//
//
//   default:
//      throw new IllegalArgumentException("Unknown URI " + uri);
//   }
//}
//
///** Insert method helper */
//protected ContentValues initializeZephyrWithDefaults(ContentValues assignedValues) {
//   final Long now = Long.valueOf(System.currentTimeMillis());
//   final ContentValues values = (assignedValues == null) 
//      ? new ContentValues() : assignedValues;
//
//     if (!values.containsKey(ZephyrTableSchemaBase.MEASUREMENT_TIME)) {
//        values.put(ZephyrTableSchemaBase.MEASUREMENT_TIME, now);
//     }  
//     if (!values.containsKey(ZephyrTableSchemaBase.INSTANT_PULSE)) {
//        values.put(ZephyrTableSchemaBase.INSTANT_PULSE, 0);
//     }  
//     if (!values.containsKey(ZephyrTableSchemaBase.INSTANT_SPEED)) {
//        values.put(ZephyrTableSchemaBase.INSTANT_SPEED, 0.0);
//     }  
//     if (!values.containsKey(ZephyrTableSchemaBase.MEASUREMENT_TYPE)) {
//        values.put(ZephyrTableSchemaBase.MEASUREMENT_TYPE, ZephyrTableSchemaBase.MEASUREMENT_TYPE_PULSE);
//     }  
//     if (!values.containsKey(ZephyrTableSchemaBase._RECEIVED_DATE)) {
//         values.put(ZephyrTableSchemaBase._RECEIVED_DATE, now);
//     }
//     if (!values.containsKey(ZephyrTableSchemaBase._DISPOSITION)) {
//        values.put(ZephyrTableSchemaBase._DISPOSITION, SensordemoSchemaBase.Disposition.LOCAL.name());
//     }
//   return values;
//} 
//
//
//@Override
//public synchronized int delete(Uri uri, String selection, String[] selectionArgs) {
//   final SQLiteDatabase db = this.openHelper.getWritableDatabase();
//   final int count;
//   Cursor cursor;
//   final int match = uriMatcher.match(uri);
//
//   logger.info("running delete with uri({}) selection({}) match({})",
//       new Object[]{uri, selection, match});
//
//   switch (match) {
//   case ZEPHYR_SET:
//      cursor = db.query(Tables.ZEPHYR_TBL, new String[] {ZephyrTableSchemaBase._ID}, selection, selectionArgs, null, null, null);
//      logger.info("cursor rows: {}", cursor.getCount());
//      if (cursor.moveToFirst()) {
//          do {
//              long rowid = cursor.getLong(cursor.getColumnIndex(ZephyrTableSchemaBase._ID));
//              String tuple = Long.toString(rowid);
//              logger.info("found rowid ({}) and tuple ({}) for deletion", rowid, tuple);
//              final File file = blobDir(Tables.ZEPHYR_TBL, tuple);
//              logger.info("deleting directory: {}", file.getAbsolutePath());
//              recursiveDelete(file);
//          }
//          while (cursor.moveToNext());
//      }
//      cursor.close();
//      count = db.delete(Tables.ZEPHYR_TBL, selection, selectionArgs);
//      break;
//
//   case ZEPHYR_ID:
//      final String zephyrID = uri.getPathSegments().get(1);
//      cursor = db.query(Tables.ZEPHYR_TBL, new String[] {ZephyrTableSchemaBase._ID}, selection, selectionArgs, null, null, null);
//      logger.info("cursor rows: {}", cursor.getCount());
//      if (cursor.moveToFirst()) {
//          do {
//              final long rowid = cursor.getLong(cursor.getColumnIndex(ZephyrTableSchemaBase._ID));
//              final String tuple = Long.toString(rowid);
//              logger.info("found rowid ({}) and tuple ({}) for deletion", rowid, tuple);
//              final File file = blobDir(Tables.ZEPHYR_TBL, tuple);
//              logger.info("deleting directory: {}", file.getAbsolutePath());
//              recursiveDelete(file);
//          }
//          while (cursor.moveToNext());
//      }
//      cursor.close();
//      count = db.delete(Tables.ZEPHYR_TBL,
//            ZephyrTableSchemaBase._ID
//                  + "="
//                  + zephyrID
//                  + (TextUtils.isEmpty(selection) ? "" 
//                         : (" AND (" + selection + ')')),
//                  selectionArgs);
//      break;
//
//
//   default:
//      throw new IllegalArgumentException("Unknown URI " + uri);
//   }
//
//   if (count > 0) getContext().getContentResolver().notifyChange(uri, null);
//   return count;
//}
//
//@Override
//public synchronized int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
//   final SQLiteDatabase db = this.openHelper.getWritableDatabase();
//   final Uri notifyUri;
//   final int count;
//   switch (uriMatcher.match(uri)) {
//   case ZEPHYR_SET:
//      logger.debug("ZEPHYR_SET");
//      notifyUri = uri;
//      count = db.update(Tables.ZEPHYR_TBL, values, selection,
//            selectionArgs);
//      break;
//
//   case ZEPHYR_ID:
//      logger.debug("ZEPHYR_ID");
//      //  notify on the base URI - without the ID ?
//      notifyUri = ZephyrTableSchemaBase.CONTENT_URI; 
//      String zephyrID = uri.getPathSegments().get(1);
//      count = db.update(Tables.ZEPHYR_TBL, values, ZephyrTableSchemaBase._ID
//            + "="
//            + zephyrID
//            + (TextUtils.isEmpty(selection) ? "" 
//                         : (" AND (" + selection + ')')),
//            selectionArgs);
//      break;
//
//
//   default:
//      throw new IllegalArgumentException("Unknown URI " + uri);
//   }
//
//   if (count > 0) 
//      getContext().getContentResolver().notifyChange(notifyUri, null);
//   return count;   
//}
//
//@Override
//public synchronized String getType(Uri uri) {
//   switch (uriMatcher.match(uri)) {
//      case ZEPHYR_SET:
//      case ZEPHYR_ID:
//         return ZephyrTableSchemaBase.CONTENT_TOPIC;
//
//
//   default:
//      throw new IllegalArgumentException("Unknown URI " + uri);
//   }   
//}
//
//// ===========================================================
//// Static declarations
//// ===========================================================
//
//static {
//   uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
//      uriMatcher.addURI(SensordemoSchemaBase.AUTHORITY, Tables.ZEPHYR_TBL, ZEPHYR_SET);
//      uriMatcher.addURI(SensordemoSchemaBase.AUTHORITY, Tables.ZEPHYR_TBL + "/#", ZEPHYR_ID);
//      uriMatcher.addURI(SensordemoSchemaBase.AUTHORITY, Tables.ZEPHYR_TBL + "/#/_serial/*", ZEPHYR_SERIAL);
//      uriMatcher.addURI(SensordemoSchemaBase.AUTHORITY, Tables.ZEPHYR_TBL + "/_deserial/*", ZEPHYR_DESERIAL);
//      uriMatcher.addURI(SensordemoSchemaBase.AUTHORITY, Tables.ZEPHYR_TBL + "/#/_serial", ZEPHYR_SERIAL);
//      uriMatcher.addURI(SensordemoSchemaBase.AUTHORITY, Tables.ZEPHYR_TBL + "/_deserial", ZEPHYR_DESERIAL);
//      uriMatcher.addURI(SensordemoSchemaBase.AUTHORITY, Tables.ZEPHYR_TBL + "/#/_blob", ZEPHYR_BLOB);
//      uriMatcher.addURI(SensordemoSchemaBase.AUTHORITY, Tables.ZEPHYR_TBL + "/#/_data_type", ZEPHYR_META);
//      uriMatcher.addURI(SensordemoSchemaBase.AUTHORITY, Tables.ZEPHYR_TBL + "/_data_type", ZEPHYR_META);
//
//      {
//        zephyrFieldTypeCursor = new MatrixCursor(new String[] {
//         ZephyrTableSchemaBase.MEASUREMENT_TIME, 
//         ZephyrTableSchemaBase.INSTANT_PULSE, 
//         ZephyrTableSchemaBase.INSTANT_SPEED, 
//         ZephyrTableSchemaBase.MEASUREMENT_TYPE, 
//        }, 1);
//
//        final MatrixCursor.RowBuilder row = zephyrFieldTypeCursor.newRow();
//        row.add(SensordemoSchemaBase.FIELD_TYPE_TIMESTAMP); // MEASUREMENT_TIME 
//        row.add(SensordemoSchemaBase.FIELD_TYPE_SHORT); // INSTANT_PULSE 
//        row.add(SensordemoSchemaBase.FIELD_TYPE_REAL); // INSTANT_SPEED 
//        row.add(SensordemoSchemaBase.FIELD_TYPE_EXCLUSIVE); // MEASUREMENT_TYPE 
//      }
//
//
//      zephyrProjectionKey = new String[1];
//      zephyrProjectionKey[0] = ZephyrTableSchemaBase._ID;
//
//      {
//         final HashMap<String, String> columns = new HashMap<String, String>();
//         columns.put(ZephyrTableSchemaBase._ID, ZephyrTableSchemaBase._ID);
//            columns.put(ZephyrTableSchemaBase.MEASUREMENT_TIME, "\""+ZephyrTableSchemaBase.MEASUREMENT_TIME+"\""); 
//            columns.put(ZephyrTableSchemaBase.INSTANT_PULSE, "\""+ZephyrTableSchemaBase.INSTANT_PULSE+"\""); 
//            columns.put(ZephyrTableSchemaBase.INSTANT_SPEED, "\""+ZephyrTableSchemaBase.INSTANT_SPEED+"\""); 
//            columns.put(ZephyrTableSchemaBase.MEASUREMENT_TYPE, "\""+ZephyrTableSchemaBase.MEASUREMENT_TYPE+"\""); 
//            columns.put(ZephyrTableSchemaBase._RECEIVED_DATE, "\""+ZephyrTableSchemaBase._RECEIVED_DATE+"\"");
//            columns.put(ZephyrTableSchemaBase._DISPOSITION, "\""+ZephyrTableSchemaBase._DISPOSITION+"\"");
//
//         zephyrProjectionMap = columns;
//      }
//
//
//}
//
//static public final File applDir;
//static public final File applCacheDir;
//
//static public final File applCacheZephyrDir;
//
//
//static public final File applTempDir;
//static {
//    applDir = new File(Environment.getExternalStorageDirectory(), "support/edu.vu.isis.ammo.biosensor.demo"); 
//    applDir.mkdirs();
//    if (! applDir.exists()) {
//      logger.error("cannot create support files check permissions : {}", 
//           applDir.toString());
//    } else if (! applDir.isDirectory()) {
//      logger.error("support directory is not a directory : {}", 
//           applDir.toString());
//    }
//
//    applCacheDir = new File(applDir, "cache/sensordemo"); 
//    applCacheDir.mkdirs();
//
//    applCacheZephyrDir = new File(applCacheDir, "zephyr"); 
//    applCacheDir.mkdirs();
//
//
//    applTempDir = new File(applDir, "tmp/sensordemo"); 
//    applTempDir.mkdirs();
//}
//
//protected static File blobFile(String table, String tuple, String field) throws IOException {
//   final File tupleCacheDir = blobDir(table, tuple);
//   final File cacheFile = new File(tupleCacheDir, field+".blob");
//   if (cacheFile.exists()) return cacheFile;    
//
//   cacheFile.createNewFile();
//   return cacheFile;
//}
//
//protected static File blobDir(String table, String tuple) {
//   final File tableCacheDir = new File(applCacheDir, table);
//   final File tupleCacheDir = new File(tableCacheDir, tuple);
//   if (!tupleCacheDir.exists()) tupleCacheDir.mkdirs();
//   return tupleCacheDir;
//}
//
//protected static File receiveFile(String table, String tuple, File filePath) {
//  final String baseName = new StringBuilder()
//      .append("recv_")
//      .append(tuple).append("_")
//      .append(filePath.getName())
//      .toString();
//
//  final File dirPath = filePath.getParentFile();
//  final File wipPath;
//  if (dirPath == null) {
//    wipPath = blobDir(table, tuple);
//  } else if (! dirPath.exists()) {
//    if (! dirPath.mkdirs()) {
//      wipPath = blobDir(table, tuple);
//    } else {
//      wipPath = dirPath;
//    }
//  } else { 
//    wipPath = dirPath;
//  }
//  return new File(wipPath, baseName);
//}
//
//protected static File tempFilePath(String table) throws IOException {
//   return File.createTempFile(table, ".tmp", applTempDir);
//}
//
//
//protected static void clearBlobCache(String table, String tuple) {
//   if (table == null) {
//     if (applCacheDir.isDirectory()) {
//        for (File child : applCacheDir.listFiles()) {
//            recursiveDelete(child);
//        }
//        return;
//     }
//   }
//   final File tableCacheDir = new File(applCacheDir, table);
//   if (tuple == null) {
//     if (tableCacheDir.isDirectory()) {
//        for (File child : tableCacheDir.listFiles()) {
//            recursiveDelete(child);
//        }
//        return;
//     }
//   }
//   final File tupleCacheDir = new File(tableCacheDir, tuple);
//   if (tupleCacheDir.isDirectory()) {
//      for (File child : tupleCacheDir.listFiles()) {
//         recursiveDelete(child);
//      }
//   }
//}
//
///** 
// * Recursively delete all children of this directory and the directory itself.
// * 
// * @param dir
// */
//protected static void recursiveDelete(File dir) {
//    if (!dir.exists()) return;
//
//    if (dir.isFile()) {
//        dir.delete();
//        return;
//    }
//    if (dir.isDirectory()) {
//        for (File child : dir.listFiles()) {
//            recursiveDelete(child);
//        }
//        dir.delete();
//        return;
//    }
//} 
//}
