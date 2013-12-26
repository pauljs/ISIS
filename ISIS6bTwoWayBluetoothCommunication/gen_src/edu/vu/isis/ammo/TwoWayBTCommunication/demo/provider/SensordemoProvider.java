//// THIS IS GENERATED CODE, WHEN YOU COMPLETE YOUR HAND EDITS REMOVE THIS LINE
//package edu.vu.isis.ammo.biosensor.demo.provider;
//
//import android.content.Context;
//
///**
// * Implements and overrides those elements not completed
// * 
// * @author <yourself>
// *    
// */
//public class SensordemoProvider extends SensordemoProviderBase {
//   public final static String VERSION = "1.7.0";
//   protected class SensordemoDatabaseHelper extends SensordemoProviderBase.SensordemoDatabaseHelper {
//      protected SensordemoDatabaseHelper(Context context) { 
//         super(context, SensordemoSchema.DATABASE_NAME, SensordemoSchema.DATABASE_VERSION);
//      }
//
///**
//   @Override
//   protected void preloadTables(SQLiteDatabase db) {
//      db.execSQL("INSERT INTO \""+Tables.*_TBL+"\" (" + *Schema.*+") "+"VALUES ('" + *TableSchema.* + "');");
//   }
//*/
//   }
//
//   // ===========================================================
//   // Content Provider Overrides
//   // ===========================================================
//
//   @Override
//   public synchronized boolean onCreate() {
//       super.onCreate();
//       this.openHelper = new SensordemoDatabaseHelper(getContext());
//
//       return true;
//   }
//
//   @Override
//   protected synchronized boolean createDatabaseHelper() {
//      return false;
//   }
//
//}