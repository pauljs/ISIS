//// THIS IS GENERATED CODE, MAKE SURE ANY CHANGES MADE HERE ARE PROPAGATED INTO THE GENERATOR TEMPLATES
//package edu.vu.isis.ammo.biosensor.demo.provider;
//
//import android.net.Uri;
//import android.provider.BaseColumns;
//import android.content.ContentResolver;
//import android.database.Cursor;
//
//public abstract class SensordemoSchemaBase {
//  public final static String VERSION = "1.7.0";
//public static final String AUTHORITY = "edu.vu.isis.ammo.biosensor.demo.provider.sensordemoprovider";
//
//public static final String DATABASE_NAME = "sensordemo.db";
//
///**
// see AmmoProviderSchema for details
//*/
//public enum Disposition {
//   REMOTE(0), LOCAL(1);
//
//   private final int code;
//
//   private Disposition(int code) {
//      this.code = code;
//   }
//
//   public int toCode() {
//      return this.code;
//   }
//
//   public static Disposition fromCode(final int code) {
//      switch (code) {
//      case 0: return REMOTE;
//      case 1: return LOCAL;
//      }
//      return LOCAL;
//   }
//
//   @Override
//   public String toString() {
//      return this.name();
//   }
//
//   public static Disposition fromString(final String value) {
//      try {
//         return (value == null) ? Disposition.LOCAL 
//               : (value.startsWith( "REMOTE" )) ? Disposition.REMOTE
//                     : Disposition.LOCAL;
//      } catch (Exception ex) {
//         return Disposition.LOCAL;
//      }
//   }
//}
//
//protected SensordemoSchemaBase() {}
//
//public static final int FIELD_TYPE_NULL = 0;
//public static final int FIELD_TYPE_BOOL = 1;
//public static final int FIELD_TYPE_BLOB = 2;
//public static final int FIELD_TYPE_FLOAT = 3;
//public static final int FIELD_TYPE_INTEGER = 4;
//public static final int FIELD_TYPE_LONG = 5;
//public static final int FIELD_TYPE_TEXT = 6;
//public static final int FIELD_TYPE_REAL = 7;
//public static final int FIELD_TYPE_FK = 8;
//public static final int FIELD_TYPE_GUID = 9;
//public static final int FIELD_TYPE_EXCLUSIVE = 10;
//public static final int FIELD_TYPE_INCLUSIVE = 11;
//public static final int FIELD_TYPE_TIMESTAMP = 12;
//public static final int FIELD_TYPE_SHORT = 13;
//public static final int FIELD_TYPE_FILE = 14;
//
//// BEGIN CUSTOM Sensordemo CONSTANTS
//// END   CUSTOM Sensordemo CONSTANTS
//
//public static class Meta {
//   final public String name;
//   final public int type;
//   public Meta(String name, int type) { this.name = name; this.type = type; }
//}
//
//public static final Meta[] ZEPHYR_CURSOR_COLUMNS = new Meta[] {
//new Meta(ZephyrTableSchemaBase.MEASUREMENT_TIME, FIELD_TYPE_TIMESTAMP)  ,
//   new Meta(ZephyrTableSchemaBase.INSTANT_PULSE, FIELD_TYPE_SHORT)  ,
//   new Meta(ZephyrTableSchemaBase.INSTANT_SPEED, FIELD_TYPE_REAL)  ,
//   new Meta(ZephyrTableSchemaBase.MEASUREMENT_TYPE, FIELD_TYPE_EXCLUSIVE)  
//};
//
//public static final String[] ZEPHYR_KEY_COLUMNS = new String[] {
//};
//
//public static class ZephyrTableSchemaBase implements BaseColumns {
//   protected ZephyrTableSchemaBase() {} // No instantiation.
//
//   /**
//    * The content:// style URL for this table
//    */
//   public static final Uri CONTENT_URI =
//      Uri.parse("content://"+AUTHORITY+"/zephyr");
//
//   public static Uri getUri(Cursor cursor) {
//     final Integer id = cursor.getInt(cursor.getColumnIndex(BaseColumns._ID));
//     return  Uri.withAppendedPath(ZephyrTableSchemaBase.CONTENT_URI, id.toString());
//   }
//
//   /**
//    * The MIME type of {@link #CONTENT_URI} providing a directory
//    */
//   public static final String CONTENT_TYPE =
//      ContentResolver.CURSOR_DIR_BASE_TYPE+"/vnd.edu.vu.isis.ammo.biosensor.demo.zephyr";
//
//   /**
//    * A mime type used for publisher subscriber.
//    */
//   public static final String CONTENT_TOPIC =
//      "ammo/edu.vu.isis.ammo.biosensor.demo.zephyr";
//
//   /**
//    * The MIME type of a {@link #CONTENT_URI} sub-directory of a single zephyr entry.
//    */
//   public static final String CONTENT_ITEM_TYPE = 
//      ContentResolver.CURSOR_ITEM_BASE_TYPE+"/vnd.edu.vu.isis.ammo.biosensor.demo.zephyr";
//
//
//   public static final String DEFAULT_SORT_ORDER = ""; //"modified_date DESC";
//
//   // ========= Field Name and Type Constants ================
//      /** 
//      * Description: 
//      * <P>Type: TIMESTAMP</P> 
//      */
//          public static final String MEASUREMENT_TIME = "measurement_time";
//      /** 
//      * Description: 
//      * <P>Type: SHORT</P> 
//      */
//          public static final String INSTANT_PULSE = "instant_pulse";
//      /** 
//      * Description: 
//      * <P>Type: REAL</P> 
//      */
//          public static final String INSTANT_SPEED = "instant_speed";
//      /** 
//      * Description: 
//      * <P>Type: EXCLUSIVE</P> 
//      */
//              public static final int MEASUREMENT_TYPE_PULSE = 1;
//              public static final int MEASUREMENT_TYPE_SPEED = 2;
//
//         public static final String MEASUREMENT_TYPE = "measurement_type";
//
//
//   public static final String _DISPOSITION = "_disp"; 
//
//   public static final String _RECEIVED_DATE = "_received_date";
//
//// BEGIN CUSTOM ZEPHYR_SCHEMA PROPERTIES
//// END   CUSTOM ZEPHYR_SCHEMA PROPERTIES
//} 
//
// 
//}
