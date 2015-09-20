/*
 * This is the source code of Telegram for Android v. 2.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2015.
 */

package org.telegram.messenger;

@SuppressWarnings("unchecked")
public class TLRPC {


    // Foursquare classes start
    public static class GeoPoint extends TLObject {
        public double _long;
        public double lat;

        public static GeoPoint TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            GeoPoint result = null;
            switch (constructor) {
                case 0x1117dd5f:
                    result = new TL_geoPointEmpty();
                    break;
                case 0x2049d70c:
                    result = new TL_geoPoint();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in GeoPoint", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_geoPointEmpty extends GeoPoint {
        public static int constructor = 0x1117dd5f;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_geoPoint extends GeoPoint {
        public static int constructor = 0x2049d70c;


        public void readParams(AbsSerializedData stream, boolean exception) {
            _long = stream.readDouble(exception);
            lat = stream.readDouble(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeDouble(_long);
            stream.writeDouble(lat);
        }
    }


    public static class MessageMedia extends TLObject {
        public GeoPoint geo;
        public String title;
        public String address;
        public String provider;
        public String venue_id;
        //TODO-aragats
        public String iconUrl;

    }


    public static class TL_messageMediaVenue extends MessageMedia {
        public static int constructor = 0x7912b71f;


        public void readParams(AbsSerializedData stream, boolean exception) {
            geo = GeoPoint.TLdeserialize(stream, stream.readInt32(exception), exception);
            title = stream.readString(exception);
            address = stream.readString(exception);
            provider = stream.readString(exception);
            venue_id = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            geo.serializeToStream(stream);
            stream.writeString(title);
            stream.writeString(address);
            stream.writeString(provider);
            stream.writeString(venue_id);
        }
    }


    public static class TL_messageMediaGeo extends MessageMedia {
        public static int constructor = 0x56e0d474;


        public void readParams(AbsSerializedData stream, boolean exception) {
            geo = GeoPoint.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            geo.serializeToStream(stream);
        }
    }
    // Foursquare classes end


    public static class FileLocation extends TLObject {
        public int dc_id;
        public long volume_id;
        public int local_id;
        public long secret;
        public byte[] key;
        public byte[] iv;

        public static FileLocation TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            FileLocation result = null;
            switch (constructor) {
                case 0x53d69076:
                    result = new TL_fileLocation();
                    break;
//                case 0x55555554:
//                    result = new TL_fileEncryptedLocation();
//                    break;
//                case 0x7c596b46:
//                    result = new TL_fileLocationUnavailable();
//                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in FileLocation", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_fileLocation extends FileLocation {
        public static int constructor = 0x53d69076;


        public void readParams(AbsSerializedData stream, boolean exception) {
            dc_id = stream.readInt32(exception);
            volume_id = stream.readInt64(exception);
            local_id = stream.readInt32(exception);
            secret = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(dc_id);
            stream.writeInt64(volume_id);
            stream.writeInt32(local_id);
            stream.writeInt64(secret);
        }
    }


    public static class PhotoSize extends TLObject {
        public String type;
        public FileLocation location;
        public int w;
        public int h;
        public int size;
        public byte[] bytes;

        public static PhotoSize TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            PhotoSize result = null;
            switch (constructor) {
                case 0x77bfb61b:
                    result = new TL_photoSize();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in PhotoSize", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_photoSize extends PhotoSize {
        public static int constructor = 0x77bfb61b;


        public void readParams(AbsSerializedData stream, boolean exception) {
            type = stream.readString(exception);
            location = FileLocation.TLdeserialize(stream, stream.readInt32(exception), exception);
            w = stream.readInt32(exception);
            h = stream.readInt32(exception);
            size = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(type);
            location.serializeToStream(stream);
            stream.writeInt32(w);
            stream.writeInt32(h);
            stream.writeInt32(size);
        }
    }


}
