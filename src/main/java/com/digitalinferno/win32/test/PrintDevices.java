package com.digitalinferno.win32.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.Kernel32Util;
import com.sun.jna.platform.win32.SetupApi;
import com.sun.jna.platform.win32.W32Errors;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.SetupApi.SP_DEVINFO_DATA;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APIOptions;

/**
 * Class for using JNA to get SP_DEVINFO_DATA references for "Plug'n'Play" devices
 * on the local windows machine, as well as a few methods to get some info about the devices.
 *
 * The JNA library (currently) consist of two jar files.
 * They are required to build and run this thing, and can be downloaded from:
 *      https://github.com/twall/jna#readme
 * (I used version 3.5.1 when testing this class, and it worked on both a Win7 64 bit, and a Win7 32 bit machine)
 *
 *
 * I use an interface named DISetupApi instead of the "regular" JNA SetupApi interface throughout this class,
 * but don't let that confuse you.
 *
 * The DISetupApi is an inner interface contained in this class, and it simply extends SetupApi,
 * and it contains one native method call that for some reason was missing from JNA:
 *
 *      boolean SetupDiEnumDeviceInfo(WinNT.HANDLE hDevInfo, int memberIndex, SP_DEVINFO_DATA.ByReference deviceInfoData);
 *
 * as well as a few variables I have stolen from various windows .h (header) files.
 *
 * (Obviously, the DISetupApi interface should be contained in a separate java file, but I like having an
 * all-in-one-class for now since I am still trying to figure out what all this stuff is.)
 *
 * @author geir
 * @version 1.5 - 2013-03-29 - Minor changes only, one SetupApi instance used.
 * @version 1.6 - 2013-03-29 - Corrected a spelling error. Rearranged some comments and info.
 * @version 1.7 - 2013-04-04 - Split a very long method
 */
public class PrintDevices {

    // A main method for testing only
    public static void main(String[] args) {
        PrintDevices pd = PrintDevices.getInstance();

        // Try to retrieve all SP_DEVINFO_DATA references, for this windows machine
        List<SP_DEVINFO_DATA.ByReference> deviceDevInfoDataReferences = pd.getAllDevInfoDataReferences();
        System.out.println("Found " + deviceDevInfoDataReferences.size() + " SP_DEVINFO_DATA references");

        // Next, using the found SP_DEVINFO_DATA references, get some value objects with info found in the registry
        List<DeviceInformation> infoObjects = pd.getAllDevInfoForDataFound(deviceDevInfoDataReferences);

        // Finally, print the value object info stuff found
        pd.printAllDevInfoDataFound(infoObjects);
    }

    // --------------------------------------------

    // This is set once, and used for all "querying"
    private final WinNT.HANDLE hDevInfo;

    // And while were at it, lets do this one once also
    private final DISetupApi setupApi;

    /*
     * A private default constructor, use the getInstance to get an instance, because I feel like it.
     */
    private PrintDevices() {
        // Get an instance to the updated SetupApi
        setupApi = DISetupApi.DI_INSTANCE;

        // Get a WinNT.HANDLE (which is sometimes referred to as "DevInfo" and "hDevInfo", just to confuse you)
        hDevInfo = getWinNtHandleAlsoKnownAsDevInfo(DISetupApi.DIGCF_PRESENT, DISetupApi.DIGCF_ALLCLASSES);
        if (hDevInfo == WinBase.INVALID_HANDLE_VALUE) {
            // Nah, something is bad. Giving up.
            throw new RuntimeException("Unable to get a valid WinNT.HANDLE");
        }
    }

    /**
     * Just because it makes sense to have a static "factory" method (or whatever it is called).
     * No need for any outsiders to call the default constructor, this class is almost read only anyways.
     *
     * @return An instance of this class
     */
    public static PrintDevices getInstance() {
        return new PrintDevices();
    }

    /**
     * Attempts to enumerate and get DEVINFO_DATA references to all devices on the local windows machine.
     * This method is probably of limited public value, and might benefit from being set to private or protected.
     *
     * @return A List of all the DEVINFO_DATA references found, or empty list if something blows up
     */
    public List<SP_DEVINFO_DATA.ByReference> getAllDevInfoDataReferences() {

        // Create a reference reference, that will/may be filled with data
        SP_DEVINFO_DATA.ByReference devNfoDataRef = new SP_DEVINFO_DATA.ByReference();

        // A List that will hold all the SP_DEVINFO_DATA references we find
        List<SP_DEVINFO_DATA.ByReference> deviceDevInfoDataReferences = new ArrayList<SP_DEVINFO_DATA.ByReference>();

        // For every device info found in the handle, fill the SP_DEVINFO_DATA with data,
        // and update the ByReference reference: devNfoDataRef
        for (int i = 0; setupApi.SetupDiEnumDeviceInfo(hDevInfo, i, devNfoDataRef); i++) {
            deviceDevInfoDataReferences.add(devNfoDataRef);
            // create a new SP_DEVINFO_DATA reference to be used for the next iteration
            devNfoDataRef = new SP_DEVINFO_DATA.ByReference();
        }

        return deviceDevInfoDataReferences;
    }

    /**
     * Get a List of information value objects for all devices (that any info was found).
     * The info is actually gotten by querying the registry, I think.
     * This method first retrieves all the SP_DEVINFO_DATA references.
     *
     * @return A List containing zero or more value objects with some info
     */
    public List<DeviceInformation> getAllDevInfoForDataFound() {
        List<SP_DEVINFO_DATA.ByReference> deviceDevInfoDataReferences = getAllDevInfoDataReferences();
        return getAllDevInfoForDataFound(deviceDevInfoDataReferences);
    }

    /**
     * Get a List of information value objects for all devices (that any info was found).
     * The info is actually gotten by querying the registry, I think.
     *
     * @param deviceDevInfoDataReferences A List of already retrieved SP_DEVINFO_DATA references,
     *              which probably must have been gotten from this instance!
     * @return A List containing zero or more value objects with some info
     */
    public List<DeviceInformation> getAllDevInfoForDataFound(List<SP_DEVINFO_DATA.ByReference> deviceDevInfoDataReferences) {

        // For some reason this does not work.
        // We must use the same WinNT.HANDLE instance as was used when finding the SP_DEVINFO_DATA references.
        // Surely there must be some way to avoid this, or else the WinNT.HANDLE instance need to be stored
        // with the SP_DEVINFO_DATA reference.
        // That just seems stoopid.
/*
        // First get a WinNT.HANDLE (which is sometimes referred to as "DevInfo" and "hDevInfo", just to confuse you)
        WinNT.HANDLE hDevInfo = pd.getHandleFromSetupDiGetClassDevs(DISetupApi.DIGCF_PRESENT, DISetupApi.DIGCF_ALLCLASSES);
        if (hDevInfo == WinBase.INVALID_HANDLE_VALUE) {
            // Nah, something is bad. Giving up.
            System.err.println("Did not find a valid HANDLE");
            return;
        }
*/
        List<DeviceInformation> returnValue = new ArrayList<DeviceInformation>();

        for (SP_DEVINFO_DATA.ByReference devNfoDataRef : deviceDevInfoDataReferences) {
            // For each reference found, we attempt to extract some (hardcoded) predefined registry key "name",
            // to hopefully get some more or less human readable registry property values
            String friendlyName = getRegistryPropertyForDevInfoAndDevinfoData(hDevInfo, devNfoDataRef, DISetupApi.SPDRP_FRIENDLYNAME);
            String hardwareId = getRegistryPropertyForDevInfoAndDevinfoData(hDevInfo, devNfoDataRef, DISetupApi.SPDRP_HARDWAREID);
            String busTypeGuid = getRegistryPropertyForDevInfoAndDevinfoData(hDevInfo, devNfoDataRef, DISetupApi.SPDRP_BUSTYPEGUID);
            String classGuid = getRegistryPropertyForDevInfoAndDevinfoData(hDevInfo, devNfoDataRef, DISetupApi.SPDRP_CLASSGUID);
            String serviceDescription = getRegistryPropertyForDevInfoAndDevinfoData(hDevInfo, devNfoDataRef, DISetupApi.SPDRP_DEVICEDESC);
            String devType = getRegistryPropertyForDevInfoAndDevinfoData(hDevInfo, devNfoDataRef, DISetupApi.SPDRP_DEVTYPE);
            String enumeratorName = getRegistryPropertyForDevInfoAndDevinfoData(hDevInfo, devNfoDataRef, DISetupApi.SPDRP_ENUMERATOR_NAME);
            String legacyBusType = getRegistryPropertyForDevInfoAndDevinfoData(hDevInfo, devNfoDataRef, DISetupApi.SPDRP_LEGACYBUSTYPE);
            String locationInformation = getRegistryPropertyForDevInfoAndDevinfoData(hDevInfo, devNfoDataRef, DISetupApi.SPDRP_LOCATION_INFORMATION);
            String locationPaths = getRegistryPropertyForDevInfoAndDevinfoData(hDevInfo, devNfoDataRef, DISetupApi.SPDRP_LOCATION_PATHS);
            String manufacturer = getRegistryPropertyForDevInfoAndDevinfoData(hDevInfo, devNfoDataRef, DISetupApi.SPDRP_MFG);
            String physicalDeviceObjectName = getRegistryPropertyForDevInfoAndDevinfoData(hDevInfo, devNfoDataRef, DISetupApi.SPDRP_PHYSICAL_DEVICE_OBJECT_NAME);

            // Fill a temporary, local, value object... plain laziness, so I can eventually call the toString method
            DeviceInformation.DeviceInformationBuilder diBuilder = new DeviceInformation.DeviceInformationBuilder();
            diBuilder.busTypeGuid(busTypeGuid).classGuid(classGuid).devType(devType).enumeratorName(enumeratorName);
            diBuilder.friendlyName(friendlyName).hardwareId(hardwareId).legacyBusType(legacyBusType);
            diBuilder.locationInformation(locationInformation).locationPaths(locationPaths).manufacturer(manufacturer);
            diBuilder.physicalDeviceObjectName(physicalDeviceObjectName).serviceDescription(serviceDescription);

            // Build it and add to return list
            returnValue.add(diBuilder.build());
        }
        return returnValue;
    }

    /**
     * Dump the DeviceInformation value object, for those too lazy to do it themselves.
     *
     * @param infoObjects A List of all devices found, crudely packaged as simple PrintDevices.DeviceInformation value objects
     */
    public void printAllDevInfoDataFound(List<DeviceInformation> infoObjects) {
        for (DeviceInformation devInfo : infoObjects) {
            System.out.println(devInfo.toString());
        }
    }

    /**
     * TODO: In a sane implementation this method should probably be protected or private.. or not here at all.
     * @return The WinNT.HANDLE handle used for this instance
     */
    public WinNT.HANDLE getWinNtHandleAlsoKnownAsDevInfo() {
        return hDevInfo;
    }

    /*
     * Get some sort of "handle" to something.
     * Called once per instance.
     *
     * @param flag A required flag (at least one flag must be set) for what to search for, as found in SetupApi.DIGCF_xxxx
     * @param flags Optional more flags to search for, as found in SetupApi.DIGCF_xxxx, all flags will be OR'ed
     * @return A WinNT.HANDLE if found/valid
     */
    private WinNT.HANDLE getWinNtHandleAlsoKnownAsDevInfo(int flag, int... flags) {
        Guid.GUID.ByReference classGuid = null;
        Pointer enumerator = null;
        Pointer hwndParent = null;
        int flagsOr = flag;
        for (int f : flags) {
            flagsOr = flagsOr | f;
        }
        WinNT.HANDLE hDevInfo = null;
        try {
            hDevInfo = setupApi.SetupDiGetClassDevs(classGuid, enumerator, hwndParent, flagsOr);
        } catch(Throwable t) {
            throw new RuntimeException(t);
        }
        return hDevInfo;
    }

    /*
     * Get the Registry Property Value for a given SP_DEVINFO_DATA reference using the DevInfo (WinNT.HANDLE) it was retrieved with.
     *
     * @param devInfo The WinNT.HANDLE / "DevInfo"
     * @param devNfoDataRef One SP_DEVINFO_DATA.ByReference, probably gotten by enumerating/calling SetupApi.SetupDiEnumDeviceInfo
     * @param property The property key to look for, one of the SetupApi.SPDRP_xxxx properties
     * @return String containing the "Plug and Play" Registry Property Value for the given property, or empty String if none found
     */
    private String getRegistryPropertyForDevInfoAndDevinfoData(final HANDLE hDevInfo,
            final SP_DEVINFO_DATA.ByReference devNfoDataRef, final int property) {

        String retValue = ""; // default, nuthin'

        IntByReference propertyRegDataType = new IntByReference(0);
        IntByReference requiredSize = new IntByReference();

        // It seems we need to call this method twice, first to get the length
        // of stupid "buffer", then again to populate the stupid buffer..
        //
        // This first call _should_ always "fail", since we do not give it a buffer to put data into..
        // we only want the size, remember?
        //
        // 1. call
        boolean isOk = setupApi.SetupDiGetDeviceRegistryProperty(
                hDevInfo,           // WinNT.HANDLE; the handle we use throughout this instance
                devNfoDataRef,      // SP_DEVINFO_DATA reference; we retrieved this previously
                property,           // int; property to get
                propertyRegDataType,// IntByReference; probably don't need/use this in this 1. call
                null,               // Pointer; we don't have this, since we ain't done no call yet,
                                    //      and besides we are not interested in it for THIS call
                0,                  // int; the size... we don't know this one yet, this is what we are
                                    //      trying to figure out, so we just set it to 0
                requiredSize);      // IntByReference; a reference to value containing the required size...
                                    //      after the call, this will probably contai what we are after!

        if (isOk) {
            // Ahh.. wtf, we got no error!? We don't want to get here. How did this happen?
            System.out.println("Funky! First call to SetupApi.SetupDiGetDeviceRegistryProperty did not fail!");
            // Just short circuit and return the empty value?
            // Nah, let's just plodge on and see what happens..
            // return retValue;
        } else {
            // Double check that the "error" is actually what we expected it to be
            int errorCode = getLastError();
            if (errorCode == W32Errors.ERROR_INVALID_DATA) {
                // This should be ok. Probably no data existed for the given property.
                // Short circuit here and return the default empty value
                return retValue;
            }
            // We only want an W32Errors.ERROR_INSUFFICIENT_BUFFER error, others are bad!
            if (errorCode != W32Errors.ERROR_INSUFFICIENT_BUFFER) {
                // Some other (real) error occured.
                // If so, something funky is going on and we need to give up
                throw new RuntimeException(getLastErrorAsString());
            }
        }

        // Check that the returned IntByReference value contain a required size of the data
        // buffer that is greater than 0, or else there is nothing to print
        if (requiredSize.getValue() <= 0) {
            // No data exist for the given property, so short circuit and return the default empty value
            return retValue;
        }

        // allocate "sufficient native memory" to hold the java array.
        // We do as in the original C code, and double it.
        // (Actually in the original C code, for some reason, they seem to test that this
        // new size is actually big enough)
        Pointer ptr = new Memory(requiredSize.getValue() * 2);

        // 2. call, the "real" one since we now have the size
        isOk = setupApi.SetupDiGetDeviceRegistryProperty(
                hDevInfo,
                devNfoDataRef,
                property,
                propertyRegDataType,    // IntByReference; this is the "type" contained in the Registry..
                                        //      Used to check how to get the value we are after I presume
                ptr,                    // Pointer; a pointer to the buffer into which the property value will be read
                requiredSize.getValue(),// int; the size of the property value buffer to read into,
                                        //      don't know why this info is needed twice
                requiredSize);          // IntByReference; the reference we found in the first call,
                                        //      containing the size of the property value buffer.
                                        //      Don't know why this info is needed twice.

        retValue = extractDataFromRegistryLookup(propertyRegDataType, requiredSize.getValue(), ptr);
        return retValue;
    }

    /*
     * @param propertyRegDataType The "type" of data contained in a registry entry
     * @param requiredSize The size (in byte?) of the data retrieved for a registry entry
     * @param ptr A Pointer to the buffer that contain the data of a registry entry
     * @return A liberally interpreted/converted String containing the data found in a registry entry
     */
    private String extractDataFromRegistryLookup(IntByReference propertyRegDataType,
            int requiredSize,
            Pointer ptr) {
        String retValue = null;

        // Get the registry value "type"
        int propValue = propertyRegDataType.getValue();
        // 'switch : case' never seems right in Java, I much prefer if-else for clarity!
        if (propValue == 0 || propValue == 1 || propValue == 2) {
            // unicode nul terminated, or no value type
            retValue = ptr.getString(0, true);
        } else
        if (propValue == 3) {
            // free form binary...
            retValue = bytesToHex(ptr.getByteArray(0, requiredSize));
        } else
        if (propValue == 4 || propValue == 5) {
            // int
            retValue = "" + ptr.getInt(0);
        } else
        if (propValue == 6) {
            // symbolic link.. what is that???
            retValue = ptr.getString(0);
        } else
        if (propValue == 7) {
            // multiple unicode strings
            retValue = nullTerminatedStrings(ptr.getCharArray(0, requiredSize));
        } else
        if (propValue == 8 || propValue == 9) {
            // resource list..??
            retValue = "" + Arrays.asList(ptr.getStringArray(0, true)).toString();
        } else
        if (propValue == 10) {
            // REG_RESOURCE_REQUIREMENTS_LIST.. what is this???
            retValue = nullTerminatedStrings(ptr.getCharArray(0, requiredSize));
        } else
        if (propValue == 11 || propValue == 12) {
            // long
            retValue = "" + ptr.getLong(0);
        } else {
            // uhm... something else?
            retValue = ptr.getString(0, true);
        }
        return retValue;
    }

    /*
     * TODO: In a sane implementation this method could possibly be protected or public, or better yet; part of some utility class.. HEY, this IS part of a utility class (Kernel32Util)!
     *
     * Get the last error that occured in the native calls.
     * I assume this is with the condition that no other/further calls has been made since the error occured..
     *
     * @return the last errorCode that occured in the native calls
     */
    private int getLastError() {
        return Kernel32.INSTANCE.GetLastError();
    }

    /*
     * TODO: In a sane implementation this method could possibly be protected or public, or better yet; part of some utility class..
     * @return The last error code that occured in the native calls as a human readable message
     */
    private String getLastErrorAsString() {
        return getErrorCodeAsString(getLastError());
    }

    /*
     * TODO: In a sane implementation this method could possibly be protected or public, or better yet; part of some utility class.. HEY, this IS part of a utility class (Kernel32Util)!
     * @param errorCode The errorCode to find the
     * @return The error code in a human readable message, or empty String if no such error code
     */
    private String getErrorCodeAsString(int errorCode) {
        String ret = "";
        try {
            ret = Kernel32Util.formatMessageFromLastErrorCode(errorCode) + " (ErrorCode: "+errorCode+")";
        } catch(Throwable t) {
            // unable to get the errorCode text
        }
        return ret;
    }

    /*
     * TODO: Should be moved to a util class.
     * Just a helper method to extract null (let's call it 'nil') terminated ('\0') Strings from a char array.
     * I.e.: we want to extract a "String array".
     * If a double nil is found, it is probably the end of the entire array.
     *
     * @param array The char array to investigate
     * @return A flattened String containing all Strings
     */
    private String nullTerminatedStrings(char array[]) {
        List<String> strings = new ArrayList<String>();
        StringBuilder sb = new StringBuilder();
        boolean previousNull = false;
        boolean done = false;
        for (char c : array) {
            if (c == '\0') {
                if (previousNull) {
                    // We are done!
                    done = true;
                    break;
                }
                previousNull = true;
                strings.add(sb.toString());
                sb = new StringBuilder();
            } else {
                previousNull = false;
                sb.append(c);
            }
        }
        if (!done) {
            strings.add(sb.toString());
        }
        return strings.toString();
    }

    /*
     * TODO: Should be moved to a util class, or a util class should be used.
     * Just a helper method to convert a byte array to a hex String,
     * since bytes can give awful ouput when printed.
     *
     * @param bytes The bytes to hexify
     * @return A String with the bytes as hex
     */
    private String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * TODO: In a sane implementation this should definately be more generic, AND in a separate file.
     * Inner class holding the registry properties, for now basically so the toString method can be called on it.
     */
    public static class DeviceInformation {
        final String friendlyName, hardwareId, busTypeGuid, classGuid, serviceDescription,
                devType, enumeratorName, legacyBusType, locationInformation, locationPaths,
                manufacturer, physicalDeviceObjectName;

        // Use the builder, not this one
        private DeviceInformation(String friendlyName, String hardwareId, String busTypeGuid,
                String classGuid, String serviceDescription, String devType, String enumeratorName,
                String legacyBusType, String locationInformation, String locationPaths, String manufacturer,
                String physicalDeviceObjectName) {
            this.friendlyName = friendlyName;
            this.hardwareId = hardwareId;
            this.busTypeGuid = busTypeGuid;
            this.classGuid = classGuid;
            this.serviceDescription = serviceDescription;
            this.devType = devType;
            this.enumeratorName = enumeratorName;
            this.legacyBusType = legacyBusType;
            this.locationInformation = locationInformation;
            this.locationPaths = locationPaths;
            this.manufacturer = manufacturer;
            this.physicalDeviceObjectName = physicalDeviceObjectName;
        }

        // A Builder class
        static class DeviceInformationBuilder {
            String friendlyName = "", hardwareId = "", busTypeGuid = "", classGuid = "",
                    serviceDescription = "", devType = "", enumeratorName = "", legacyBusType = "",
                    locationInformation = "", locationPaths = "", manufacturer = "",
                    physicalDeviceObjectName = "";
            public DeviceInformationBuilder() {}
            public DeviceInformationBuilder friendlyName(String friendlyName) { this.friendlyName = friendlyName; return this; }
            public DeviceInformationBuilder hardwareId(String hardwareId) { this.hardwareId = hardwareId; return this; }
            public DeviceInformationBuilder busTypeGuid(String busTypeGuid) { this.busTypeGuid = busTypeGuid; return this; }
            public DeviceInformationBuilder classGuid(String classGuid) { this.classGuid = classGuid; return this; }
            public DeviceInformationBuilder serviceDescription(String serviceDescription) { this.serviceDescription = serviceDescription; return this; }
            public DeviceInformationBuilder devType(String devType) { this.devType = devType; return this; }
            public DeviceInformationBuilder enumeratorName(String enumeratorName) { this.enumeratorName = enumeratorName; return this; }
            public DeviceInformationBuilder legacyBusType(String legacyBusType) { this.legacyBusType = legacyBusType; return this; }
            public DeviceInformationBuilder locationInformation(String locationInformation) { this.locationInformation = locationInformation; return this; }
            public DeviceInformationBuilder locationPaths(String locationPaths) { this.locationPaths = locationPaths; return this; }
            public DeviceInformationBuilder manufacturer(String manufacturer) { this.manufacturer = manufacturer; return this; }
            public DeviceInformationBuilder physicalDeviceObjectName(String physicalDeviceObjectName) { this.physicalDeviceObjectName = physicalDeviceObjectName; return this; }

            public DeviceInformation build() {
                return new DeviceInformation(friendlyName, hardwareId, busTypeGuid, classGuid,
                        serviceDescription, devType, enumeratorName, legacyBusType, locationInformation,
                        locationPaths, manufacturer, physicalDeviceObjectName);
            }
        }

        public String getFriendlyName() { return friendlyName; }
        public String getHardwareId() { return hardwareId; }
        public String getBusTypeGuid() { return busTypeGuid; }
        public String getClassGuid() { return classGuid; }
        public String getServiceDescription() { return serviceDescription; }
        public String getDevType() { return devType; }
        public String getEnumeratorName() { return enumeratorName; }
        public String getLegacyBusType() { return legacyBusType; }
        public String getLocationInformation() { return locationInformation; }
        public String getLocationPaths() { return locationPaths; }
        public String getManufacturer() { return manufacturer; }
        public String getPhysicalDeviceObjectName() { return physicalDeviceObjectName; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("  friendlyName:").append(friendlyName).append("\n");
            sb.append("  hardwareId:").append(hardwareId).append("\n");
            sb.append("  busTypeGuid:").append(busTypeGuid).append("\n");
            sb.append("  classGuid:").append(classGuid).append("\n");
            sb.append("  serviceDescription:").append(serviceDescription).append("\n");
            sb.append("  devType:").append(devType).append("\n");
            sb.append("  enumeratorName:").append(enumeratorName).append("\n");
            sb.append("  legacyBusType:").append(legacyBusType).append("\n");
            sb.append("  locationInformation:").append(locationInformation).append("\n");
            sb.append("  locationPaths:").append(locationPaths).append("\n");
            sb.append("  manufacturer:").append(manufacturer).append("\n");
            sb.append("  physicalDeviceObjectName:").append(physicalDeviceObjectName).append("\n");
            return sb.toString();
        }

    }

    /*
     * TODO: Should be in a separate file, and the properties/variables should possibly be in another, separate, file
     * Inner static interface adding some missing properties and one missing method for windows SetupApi.
     */
    static interface DISetupApi extends SetupApi {

        DISetupApi DI_INSTANCE = (DISetupApi)
                Native.loadLibrary("setupapi", DISetupApi.class, W32APIOptions.DEFAULT_OPTIONS);

        // From setupapi.h:
        // Device registry property codes
        // (Codes marked as read-only (R) may only be used for
        // SetupDiGetDeviceRegistryProperty)
        //
        // These values should cover the same set of registry properties
        // as defined by the CM_DRP codes in cfgmgr32.h.
        //
        // Note that SPDRP codes are zero based while CM_DRP codes are one based!
        //
        public static final int SPDRP_DEVICEDESC                 = (0x00000000);  // DeviceDesc (R/W)
        public static final int SPDRP_HARDWAREID                 = (0x00000001);  // HardwareID (R/W)
        public static final int SPDRP_COMPATIBLEIDS              = (0x00000002);  // CompatibleIDs (R/W)
        public static final int SPDRP_UNUSED0                    = (0x00000003);  // unused
        public static final int SPDRP_SERVICE                    = (0x00000004);  // Service (R/W)
        public static final int SPDRP_UNUSED1                    = (0x00000005);  // unused
        public static final int SPDRP_UNUSED2                    = (0x00000006);  // unused
        public static final int SPDRP_CLASS                      = (0x00000007);  // Class (R--tied to ClassGUID)
        public static final int SPDRP_CLASSGUID                  = (0x00000008);  // ClassGUID (R/W)
        public static final int SPDRP_DRIVER                     = (0x00000009);  // Driver (R/W)
        public static final int SPDRP_CONFIGFLAGS                = (0x0000000A);  // ConfigFlags (R/W)
        public static final int SPDRP_MFG                        = (0x0000000B);  // Mfg (R/W)
        public static final int SPDRP_FRIENDLYNAME               = (0x0000000C);  // FriendlyName (R/W)
        public static final int SPDRP_LOCATION_INFORMATION       = (0x0000000D);  // LocationInformation (R/W)
        public static final int SPDRP_PHYSICAL_DEVICE_OBJECT_NAME= (0x0000000E);  // PhysicalDeviceObjectName (R)
        public static final int SPDRP_CAPABILITIES               = (0x0000000F);  // Capabilities (R)
        public static final int SPDRP_UI_NUMBER                  = (0x00000010);  // UiNumber (R)
        public static final int SPDRP_UPPERFILTERS               = (0x00000011);  // UpperFilters (R/W)
        public static final int SPDRP_LOWERFILTERS               = (0x00000012);  // LowerFilters (R/W)
        public static final int SPDRP_BUSTYPEGUID                = (0x00000013);  // BusTypeGUID (R)
        public static final int SPDRP_LEGACYBUSTYPE              = (0x00000014);  // LegacyBusType (R)
        public static final int SPDRP_BUSNUMBER                  = (0x00000015);  // BusNumber (R)
        public static final int SPDRP_ENUMERATOR_NAME            = (0x00000016);  // Enumerator Name (R)
        public static final int SPDRP_SECURITY                   = (0x00000017);  // Security (R/W, binary form)
        public static final int SPDRP_SECURITY_SDS               = (0x00000018);  // Security (W, SDS form)
        public static final int SPDRP_DEVTYPE                    = (0x00000019);  // Device Type (R/W)
        public static final int SPDRP_EXCLUSIVE                  = (0x0000001A);  // Device is exclusive-access (R/W)
        public static final int SPDRP_CHARACTERISTICS            = (0x0000001B);  // Device Characteristics (R/W)
        public static final int SPDRP_ADDRESS                    = (0x0000001C);  // Device Address (R)
        public static final int SPDRP_UI_NUMBER_DESC_FORMAT      = (0X0000001D);  // UiNumberDescFormat (R/W)
        public static final int SPDRP_DEVICE_POWER_DATA          = (0x0000001E);  // Device Power Data (R)
        public static final int SPDRP_REMOVAL_POLICY             = (0x0000001F);  // Removal Policy (R)
        public static final int SPDRP_REMOVAL_POLICY_HW_DEFAULT  = (0x00000020);  // Hardware Removal Policy (R)
        public static final int SPDRP_REMOVAL_POLICY_OVERRIDE    = (0x00000021);  // Removal Policy Override (RW)
        public static final int SPDRP_INSTALL_STATE              = (0x00000022);  // Device Install State (R)
        public static final int SPDRP_LOCATION_PATHS             = (0x00000023);  // Device Location Paths (R)
        public static final int SPDRP_BASE_CONTAINERID           = (0x00000024);  // Base ContainerID (R)

        public static final int SPDRP_MAXIMUM_PROPERTY           = (0x00000025);  // Upper bound on ordinals

        //
        // Class registry property codes
        // (Codes marked as read-only (R) may only be used for
        // SetupDiGetClassRegistryProperty)
        //
        // These values should cover the same set of registry properties
        // as defined by the CM_CRP codes in cfgmgr32.h.
        // they should also have a 1:1 correspondence with Device registers, where applicable
        // but no overlap otherwise
        //
        public static final int SPCRP_UPPERFILTERS                = (0x00000011);  // UpperFilters (R/W)
        public static final int SPCRP_LOWERFILTERS                = (0x00000012);  // LowerFilters (R/W)
        public static final int SPCRP_SECURITY                    = (0x00000017);  // Security (R/W, binary form)
        public static final int SPCRP_SECURITY_SDS                = (0x00000018);  // Security (W, SDS form)
        public static final int SPCRP_DEVTYPE                     = (0x00000019);  // Device Type (R/W)
        public static final int SPCRP_EXCLUSIVE                   = (0x0000001A);  // Device is exclusive-access (R/W)
        public static final int SPCRP_CHARACTERISTICS             = (0x0000001B);  // Device Characteristics (R/W)
        public static final int SPCRP_MAXIMUM_PROPERTY            = (0x0000001C);  // Upper bound on ordinals


        // From winnt.h
        // propertyRegDataType :
        //        0 = REG_NONE                  == No value type
        //        1 = REG_SZ                    == Unicode nul terminated string
        //        2 = REG_EXPAND_SZ             == Unicode nul terminated string (with environment variable references)
        //        3 = REG_BINARY                == Free form binary
        //        4 = REG_DWORD                 == 32-bit number
        //        5 = REG_DWORD_BIG_ENDIAN      == 32-bit number
        //        6 = REG_LINK                  == Symbolic Link (unicode)
        //        7 = REG_MULTI_SZ              == Multiple Unicode strings, array of null-terminated strings that are terminated by two null characters.
        //        8 = REG_RESOURCE_LIST         ==  Resource list in the resource map
        //        9 = REG_FULL_RESOURCE_DESCRIPTOR == Resource list in the hardware description
        //        10 = REG_RESOURCE_REQUIREMENTS_LIST == ??
        //        11 = REG_QWORD                == 64-bit number
        //        12 = REG_QWORD_LITTLE_ENDIAN  == 64-bit number (same as REG_QWORD)
        public static final int REG_NONE                   = ( 0 );   // No value type
        public static final int REG_SZ                     = ( 1 );   // Unicode nul terminated string
        public static final int REG_EXPAND_SZ              = ( 2 );   // Unicode nul terminated string (with environment variable references)
        public static final int REG_BINARY                 = ( 3 );   // Free form binary
        public static final int REG_DWORD                  = ( 4 );   // 32-bit number
        public static final int REG_DWORD_LITTLE_ENDIAN    = ( 4 );   // 32-bit number (same as REG_DWORD)
        public static final int REG_DWORD_BIG_ENDIAN       = ( 5 );   // 32-bit number
        public static final int REG_LINK                   = ( 6 );   // Symbolic Link (unicode)
        public static final int REG_MULTI_SZ               = ( 7 );   // Multiple Unicode strings
        public static final int REG_RESOURCE_LIST          = ( 8 );   // Resource list in the resource map
        public static final int REG_FULL_RESOURCE_DESCRIPTOR= ( 9 );  // Resource list in the hardware description
        public static final int REG_RESOURCE_REQUIREMENTS_LIST= ( 10 );
        public static final int REG_QWORD                  = ( 11 );  // 64-bit number
        public static final int REG_QWORD_LITTLE_ENDIAN    = ( 11 );  // 64-bit number (same as REG_QWORD)


        /**
         * Native call that seems to be missing from SetupApi.
         * The SetupDiEnumDeviceInterfaces function enumerates the device interfaces that are contained in a device
         * information set.
         *
         * @param hDevInfo
         *   A pointer to a device information set that contains the device interfaces for which to return information. This
         *   handle is typically returned by SetupDiGetClassDevs.
         *
         *
         * @param memberIndex
         *   A zero-based index into the list of interfaces in the device information set. The caller should call this
         *   function first with MemberIndex set to zero to obtain the first interface. Then, repeatedly increment
         *   MemberIndex and retrieve an interface until this function fails and GetLastError returns ERROR_NO_MORE_ITEMS.
         *
         *   If DeviceInfoData specifies a particular device, the MemberIndex is relative to only the interfaces exposed by
         *   that device.
         *
         * @param deviceInfoData
         *   A pointer to a buffer that receives information about the device that supports the requested interface. The
         *   caller must set DeviceInfoData.cbSize to sizeof(SP_DEVINFO_DATA). This parameter is optional and can be NULL.
         *
         * @return
         *   SetupDiGetDeviceInterfaceDetail returns TRUE if the function completed without error. If the function completed
         *   with an error, FALSE is returned and the error code for the failure can be retrieved by calling GetLastError.
         */
        boolean SetupDiEnumDeviceInfo(WinNT.HANDLE hDevInfo,
                int memberIndex, SP_DEVINFO_DATA.ByReference deviceInfoData);
    }

}