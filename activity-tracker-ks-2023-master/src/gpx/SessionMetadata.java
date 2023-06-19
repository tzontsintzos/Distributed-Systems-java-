package src.gpx;

import java.io.Serializable;

/**
 * @param time unix time
 */


//defines a metadata object associated with a GPS session that includes information about the session
// (distance, speed, elevation gain, elevation loss, and time)
// The class is a Java record(simplifies the creation of simple classes that mainly serve as simple data carriers)
// The class is also marked as serializable(it can be written to an output stream and later read back into a Java object)
// This allows for storing and transmitting the metadata object as a binary representation( data transfer and storage)


public record SessionMetadata(
        double distance, // in meters
        double speed, // km/h
        double elevationGain,
        double elevationLoss,
        long time // unix time
) implements Serializable {

}
