package edu.vu.isis.ammo.TwoWayBTCommunication;

import java.util.UUID;

/**
 * Created by demouser on 6/13/13.
 */

// This is used in the MainActivity in order to 
// use btAdapter.listenUsingRfcommWithServiceRecord("Host", constants.uuid);
// for the bluetooth threads
public class constants {
    public static UUID uuid = UUID.fromString("02cd5e2c-a844-46ba-a2bd-faa89df1e9fe");
}
