package edu.vu.isis.ammo.TwoWayBTCommunication;

import java.io.Serializable;

import android.graphics.Bitmap;

/**
 * Created by demouser on 6/13/13.
 */

// This object is what is sent over when sending strings over bluetooth
// An object MUST BE SERIALIZABLE to send over bluetooth
public class Testing  implements Serializable {
	
	// This enum can be used later in case other objects want to be sent over bluetooth
	public enum dataTypeEnum {
		  STRING, IMAGE
	}    
	
	
	public String test = "Hello World";
	public dataTypeEnum type = dataTypeEnum.STRING;
//	public Serializable image = null;
//	public byte[] bytes = null;
//	public SerialBitmap image = null;
}
