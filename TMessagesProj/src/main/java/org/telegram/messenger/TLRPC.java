/*
 * This is the source code of Telegram for Android v. 2.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2015.
 */

package org.telegram.messenger;

import java.util.ArrayList;

@SuppressWarnings("unchecked")
public class TLRPC {


    public static final int MESSAGE_FLAG_FWD = 4;








    public static class InputEncryptedFile extends TLObject {
        public long id;
        public long access_hash;
        public int parts;
        public int key_fingerprint;
        public String md5_checksum;


    }





    public static class GeoPoint extends TLObject {
        public double _long;
        public double lat;

        public static GeoPoint TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            GeoPoint result = null;
            switch(constructor) {
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





    public static class BadMsgNotification extends TLObject {
        public long bad_msg_id;
        public int bad_msg_seqno;
        public int error_code;
        public long new_server_salt;


    }

    public static class TL_bad_msg_notification extends BadMsgNotification {
        public static int constructor = 0xa7eff811;


        public void readParams(AbsSerializedData stream, boolean exception) {
            bad_msg_id = stream.readInt64(exception);
            bad_msg_seqno = stream.readInt32(exception);
            error_code = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(bad_msg_id);
            stream.writeInt32(bad_msg_seqno);
            stream.writeInt32(error_code);
        }
    }

    public static class TL_bad_server_salt extends BadMsgNotification {
        public static int constructor = 0xedab447b;


        public void readParams(AbsSerializedData stream, boolean exception) {
            bad_msg_id = stream.readInt64(exception);
            bad_msg_seqno = stream.readInt32(exception);
            error_code = stream.readInt32(exception);
            new_server_salt = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(bad_msg_id);
            stream.writeInt32(bad_msg_seqno);
            stream.writeInt32(error_code);
            stream.writeInt64(new_server_salt);
        }
    }



    public static class User extends TLObject {
        public int id;
        public String first_name;
        public String last_name;
        public long access_hash;
        public String phone;
        public UserProfilePhoto photo;
//        public UserStatus status;
        public boolean inactive;
        public String username;


    }





    public static class TL_userContact extends User {
        public static int constructor = 0xcab35e18;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            username = stream.readString(exception);
            access_hash = stream.readInt64(exception);
            phone = stream.readString(exception);
            photo = UserProfilePhoto.TLdeserialize(stream, stream.readInt32(exception), exception);
//            status = UserStatus.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeString(first_name);
            stream.writeString(last_name);
            stream.writeString(username);
            stream.writeInt64(access_hash);
            stream.writeString(phone);
            photo.serializeToStream(stream);
//            status.serializeToStream(stream);
        }
    }

















    public static class MessageMedia extends TLObject {
        public byte[] bytes;
//        public Video video;
        public String caption;
        public Photo photo;
        public Audio audio;
        public GeoPoint geo;
        public String title;
        public String address;
        public String provider;
        public String venue_id;
//        public Document document;
        public String phone_number;
        public String first_name;
        public String last_name;
        public int user_id;
//        public WebPage webpage;
        //TODO-aragats
        public String iconUrl;

        public static MessageMedia TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            MessageMedia result = null;
            switch(constructor) {
                case 0x29632a36:
                    result = new TL_messageMediaUnsupported_old();
                    break;
                case 0x5bcf1675:
                    result = new TL_messageMediaVideo();
                    break;
                case 0xc6b68300:
                    result = new TL_messageMediaAudio();
                    break;
                case 0x9f84f49e:
                    result = new TL_messageMediaUnsupported();
                    break;
                case 0x3ded6320:
                    result = new TL_messageMediaEmpty();
                    break;
                case 0x7912b71f:
                    result = new TL_messageMediaVenue();
                    break;
                case 0x56e0d474:
                    result = new TL_messageMediaGeo();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in MessageMedia", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_messageMediaUnsupported_old extends TL_messageMediaUnsupported {
        public static int constructor = 0x29632a36;


        public void readParams(AbsSerializedData stream, boolean exception) {
            bytes = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeByteArray(bytes);
        }
    }

    public static class TL_messageMediaVideo extends MessageMedia {
        public static int constructor = 0x5bcf1675;


        public void readParams(AbsSerializedData stream, boolean exception) {
//            video = Video.TLdeserialize(stream, stream.readInt32(exception), exception);
            caption = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
//            video.serializeToStream(stream);
            stream.writeString(caption);
        }
    }



    public static class TL_messageMediaAudio extends MessageMedia {
        public static int constructor = 0xc6b68300;


        public void readParams(AbsSerializedData stream, boolean exception) {
            audio = Audio.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            audio.serializeToStream(stream);
        }
    }

    public static class TL_messageMediaUnsupported extends MessageMedia {
        public static int constructor = 0x9f84f49e;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_messageMediaEmpty extends MessageMedia {
        public static int constructor = 0x3ded6320;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
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





    public static class TL_new_session_created extends TLObject {
        public static int constructor = 0x9ec20908;

        public long first_msg_id;
        public long unique_id;
        public long server_salt;



        public void readParams(AbsSerializedData stream, boolean exception) {
            first_msg_id = stream.readInt64(exception);
            unique_id = stream.readInt64(exception);
            server_salt = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(first_msg_id);
            stream.writeInt64(unique_id);
            stream.writeInt64(server_salt);
        }
    }















    public static class TL_resPQ extends TLObject {
        public static int constructor = 0x05162463;

        public byte[] nonce;
        public byte[] server_nonce;
        public byte[] pq;
        public ArrayList<Long> server_public_key_fingerprints = new ArrayList<>();



        public void readParams(AbsSerializedData stream, boolean exception) {
            nonce = stream.readData(16, exception);
            server_nonce = stream.readData(16, exception);
            pq = stream.readByteArray(exception);
            int magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                server_public_key_fingerprints.add(stream.readInt64(exception));
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeRaw(nonce);
            stream.writeRaw(server_nonce);
            stream.writeByteArray(pq);
            stream.writeInt32(0x1cb5c415);
            int count = server_public_key_fingerprints.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                stream.writeInt64(server_public_key_fingerprints.get(a));
            }
        }
    }













    public static class TL_msg_copy extends TLObject {
        public static int constructor = 0xe06046b2;

        public TL_protoMessage orig_message;



        public void readParams(AbsSerializedData stream, boolean exception) {
            orig_message = TL_protoMessage.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            orig_message.serializeToStream(stream);
        }
    }

    public static class FileLocation extends TLObject {
        public int dc_id;
        public long volume_id;
        public int local_id;
        public long secret;
        public byte[] key;
        public byte[] iv;

        public static FileLocation TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            FileLocation result = null;
            switch(constructor) {
                case 0x53d69076:
                    result = new TL_fileLocation();
                    break;
                case 0x55555554:
                    result = new TL_fileEncryptedLocation();
                    break;
                case 0x7c596b46:
                    result = new TL_fileLocationUnavailable();
                    break;
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

    public static class TL_fileEncryptedLocation extends FileLocation {
        public static int constructor = 0x55555554;


        public void readParams(AbsSerializedData stream, boolean exception) {
            dc_id = stream.readInt32(exception);
            volume_id = stream.readInt64(exception);
            local_id = stream.readInt32(exception);
            secret = stream.readInt64(exception);
            key = stream.readByteArray(exception);
            iv = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(dc_id);
            stream.writeInt64(volume_id);
            stream.writeInt32(local_id);
            stream.writeInt64(secret);
            stream.writeByteArray(key);
            stream.writeByteArray(iv);
        }
    }

    public static class TL_fileLocationUnavailable extends FileLocation {
        public static int constructor = 0x7c596b46;


        public void readParams(AbsSerializedData stream, boolean exception) {
            volume_id = stream.readInt64(exception);
            local_id = stream.readInt32(exception);
            secret = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(volume_id);
            stream.writeInt32(local_id);
            stream.writeInt64(secret);
        }
    }



    public static class TL_pong extends TLObject {
        public static int constructor = 0x347773c5;

        public long msg_id;
        public long ping_id;



        public void readParams(AbsSerializedData stream, boolean exception) {
            msg_id = stream.readInt64(exception);
            ping_id = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(msg_id);
            stream.writeInt64(ping_id);
        }
    }








    public static class RpcDropAnswer extends TLObject {
        public long msg_id;
        public int seq_no;
        public int bytes;

    }

    public static class TL_rpc_answer_unknown extends RpcDropAnswer {
        public static int constructor = 0x5e2ad36e;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_rpc_answer_dropped extends RpcDropAnswer {
        public static int constructor = 0xa43ad8b7;


        public void readParams(AbsSerializedData stream, boolean exception) {
            msg_id = stream.readInt64(exception);
            seq_no = stream.readInt32(exception);
            bytes = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(msg_id);
            stream.writeInt32(seq_no);
            stream.writeInt32(bytes);
        }
    }

    public static class TL_rpc_answer_dropped_running extends RpcDropAnswer {
        public static int constructor = 0xcd78e586;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }








    public static class Peer extends TLObject {
        public int user_id;
        public int chat_id;

        public static Peer TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            Peer result = null;
            switch(constructor) {
                case 0x9db1bc6d:
                    result = new TL_peerUser();
                    break;
                case 0xbad0e5bb:
                    result = new TL_peerChat();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in Peer", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_peerUser extends Peer {
        public static int constructor = 0x9db1bc6d;


        public void readParams(AbsSerializedData stream, boolean exception) {
            user_id = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(user_id);
        }
    }

    public static class TL_peerChat extends Peer {
        public static int constructor = 0xbad0e5bb;


        public void readParams(AbsSerializedData stream, boolean exception) {
            chat_id = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(chat_id);
        }
    }



    public static class InputFile extends TLObject {
        public long id;
        public int parts;
        public String name;
        public String md5_checksum;


    }







    public static class EncryptedFile extends TLObject {
        public long id;
        public long access_hash;
        public int size;
        public int dc_id;
        public int key_fingerprint;

        public static EncryptedFile TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            EncryptedFile result = null;
            switch(constructor) {
                case 0x4a70994c:
                    result = new TL_encryptedFile();
                    break;
                case 0xc21f497e:
                    result = new TL_encryptedFileEmpty();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in EncryptedFile", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_encryptedFile extends EncryptedFile {
        public static int constructor = 0x4a70994c;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt64(exception);
            access_hash = stream.readInt64(exception);
            size = stream.readInt32(exception);
            dc_id = stream.readInt32(exception);
            key_fingerprint = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(id);
            stream.writeInt64(access_hash);
            stream.writeInt32(size);
            stream.writeInt32(dc_id);
            stream.writeInt32(key_fingerprint);
        }
    }

    public static class TL_encryptedFileEmpty extends EncryptedFile {
        public static int constructor = 0xc21f497e;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }



    public static class TL_auth_exportedAuthorization extends TLObject {
        public static int constructor = 0xdf969c2d;

        public int id;
        public byte[] bytes;



        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            bytes = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeByteArray(bytes);
        }
    }





    public static class Photo extends TLObject {
        public long id;
        public long access_hash;
        public int user_id;
        public int date;
        public GeoPoint geo;
        public ArrayList<PhotoSize> sizes = new ArrayList<>();
        public String caption;


    }

    public static class TL_photoEmpty extends Photo {
        public static int constructor = 0x2331b22d;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(id);
        }
    }

    public static class TL_photo extends Photo {
        public static int constructor = 0xc3838076;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt64(exception);
            access_hash = stream.readInt64(exception);
            user_id = stream.readInt32(exception);
            date = stream.readInt32(exception);
            geo = GeoPoint.TLdeserialize(stream, stream.readInt32(exception), exception);
            int magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                PhotoSize object = PhotoSize.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                sizes.add(object);
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(id);
            stream.writeInt64(access_hash);
            stream.writeInt32(user_id);
            stream.writeInt32(date);
            geo.serializeToStream(stream);
            stream.writeInt32(0x1cb5c415);
            int count = sizes.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                sizes.get(a).serializeToStream(stream);
            }
        }
    }

    public static class TL_photo_old extends TL_photo {
        public static int constructor = 0x22b56751;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt64(exception);
            access_hash = stream.readInt64(exception);
            user_id = stream.readInt32(exception);
            date = stream.readInt32(exception);
            caption = stream.readString(exception);
            geo = GeoPoint.TLdeserialize(stream, stream.readInt32(exception), exception);
            int magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                PhotoSize object = PhotoSize.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                sizes.add(object);
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(id);
            stream.writeInt64(access_hash);
            stream.writeInt32(user_id);
            stream.writeInt32(date);
            stream.writeString(caption);
            geo.serializeToStream(stream);
            stream.writeInt32(0x1cb5c415);
            int count = sizes.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                sizes.get(a).serializeToStream(stream);
            }
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
            switch(constructor) {
                case 0x77bfb61b:
                    result = new TL_photoSize();
                    break;
                case 0xe17e23c:
                    result = new TL_photoSizeEmpty();
                    break;
                case 0xe9a734fa:
                    result = new TL_photoCachedSize();
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

    public static class TL_photoSizeEmpty extends PhotoSize {
        public static int constructor = 0xe17e23c;


        public void readParams(AbsSerializedData stream, boolean exception) {
            type = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(type);
        }
    }

    public static class TL_photoCachedSize extends PhotoSize {
        public static int constructor = 0xe9a734fa;


        public void readParams(AbsSerializedData stream, boolean exception) {
            type = stream.readString(exception);
            location = FileLocation.TLdeserialize(stream, stream.readInt32(exception), exception);
            w = stream.readInt32(exception);
            h = stream.readInt32(exception);
            bytes = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(type);
            location.serializeToStream(stream);
            stream.writeInt32(w);
            stream.writeInt32(h);
            stream.writeByteArray(bytes);
        }
    }

    public static class MessageAction extends TLObject {
        public String title;
        public String address;
        public int user_id;
        public int inviter_id;
        public DecryptedMessageAction encryptedAction;
        public int ttl;
        public UserProfilePhoto newUserPhoto;
        public Photo photo;
        public ArrayList<Integer> users = new ArrayList<>();


    }



    public static class TL_messageEncryptedAction extends MessageAction {
        public static int constructor = 0x555555F7;


        public void readParams(AbsSerializedData stream, boolean exception) {
            encryptedAction = DecryptedMessageAction.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            encryptedAction.serializeToStream(stream);
        }
    }

    public static class DecryptedMessageAction extends TLObject {
        public int ttl_seconds;
        public int layer;
        public ArrayList<Long> random_ids = new ArrayList<>();
        public long exchange_id;
        public long key_fingerprint;
        public SendMessageAction action;
        public byte[] g_b;
        public int start_seq_no;
        public int end_seq_no;
        public byte[] g_a;

        public static DecryptedMessageAction TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            DecryptedMessageAction result = null;
            switch(constructor) {
                case 0xa1733aec:
                    result = new TL_decryptedMessageActionSetMessageTTL();
                    break;
                case 0xf3048883:
                    result = new TL_decryptedMessageActionNotifyLayer();
                    break;
                case 0x65614304:
                    result = new TL_decryptedMessageActionDeleteMessages();
                    break;
                case 0xec2e0b9b:
                    result = new TL_decryptedMessageActionCommitKey();
                    break;
                case 0xdd05ec6b:
                    result = new TL_decryptedMessageActionAbortKey();
                    break;
                case 0x6719e45c:
                    result = new TL_decryptedMessageActionFlushHistory();
                    break;
                case 0xccb27641:
                    result = new TL_decryptedMessageActionTyping();
                    break;
                case 0x6fe1735b:
                    result = new TL_decryptedMessageActionAcceptKey();
                    break;
                case 0xc4f40be:
                    result = new TL_decryptedMessageActionReadMessages();
                    break;
                case 0x511110b0:
                    result = new TL_decryptedMessageActionResend();
                    break;
                case 0xf3c9611b:
                    result = new TL_decryptedMessageActionRequestKey();
                    break;
                case 0x8ac1f475:
                    result = new TL_decryptedMessageActionScreenshotMessages();
                    break;
                case 0xa82fdd63:
                    result = new TL_decryptedMessageActionNoop();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in DecryptedMessageAction", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_decryptedMessageActionSetMessageTTL extends DecryptedMessageAction {
        public static int constructor = 0xa1733aec;


        public void readParams(AbsSerializedData stream, boolean exception) {
            ttl_seconds = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(ttl_seconds);
        }
    }

    public static class TL_decryptedMessageActionNotifyLayer extends DecryptedMessageAction {
        public static int constructor = 0xf3048883;


        public void readParams(AbsSerializedData stream, boolean exception) {
            layer = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(layer);
        }
    }

    public static class TL_decryptedMessageActionDeleteMessages extends DecryptedMessageAction {
        public static int constructor = 0x65614304;


        public void readParams(AbsSerializedData stream, boolean exception) {
            int magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                random_ids.add(stream.readInt64(exception));
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(0x1cb5c415);
            int count = random_ids.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                stream.writeInt64(random_ids.get(a));
            }
        }
    }

    public static class TL_decryptedMessageActionCommitKey extends DecryptedMessageAction {
        public static int constructor = 0xec2e0b9b;


        public void readParams(AbsSerializedData stream, boolean exception) {
            exchange_id = stream.readInt64(exception);
            key_fingerprint = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(exchange_id);
            stream.writeInt64(key_fingerprint);
        }
    }

    public static class TL_decryptedMessageActionAbortKey extends DecryptedMessageAction {
        public static int constructor = 0xdd05ec6b;


        public void readParams(AbsSerializedData stream, boolean exception) {
            exchange_id = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(exchange_id);
        }
    }

    public static class TL_decryptedMessageActionFlushHistory extends DecryptedMessageAction {
        public static int constructor = 0x6719e45c;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_decryptedMessageActionTyping extends DecryptedMessageAction {
        public static int constructor = 0xccb27641;


        public void readParams(AbsSerializedData stream, boolean exception) {
            action = SendMessageAction.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            action.serializeToStream(stream);
        }
    }

    public static class TL_decryptedMessageActionAcceptKey extends DecryptedMessageAction {
        public static int constructor = 0x6fe1735b;


        public void readParams(AbsSerializedData stream, boolean exception) {
            exchange_id = stream.readInt64(exception);
            g_b = stream.readByteArray(exception);
            key_fingerprint = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(exchange_id);
            stream.writeByteArray(g_b);
            stream.writeInt64(key_fingerprint);
        }
    }

    public static class TL_decryptedMessageActionReadMessages extends DecryptedMessageAction {
        public static int constructor = 0xc4f40be;


        public void readParams(AbsSerializedData stream, boolean exception) {
            int magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                random_ids.add(stream.readInt64(exception));
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(0x1cb5c415);
            int count = random_ids.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                stream.writeInt64(random_ids.get(a));
            }
        }
    }

    public static class TL_decryptedMessageActionResend extends DecryptedMessageAction {
        public static int constructor = 0x511110b0;


        public void readParams(AbsSerializedData stream, boolean exception) {
            start_seq_no = stream.readInt32(exception);
            end_seq_no = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(start_seq_no);
            stream.writeInt32(end_seq_no);
        }
    }

    public static class TL_decryptedMessageActionRequestKey extends DecryptedMessageAction {
        public static int constructor = 0xf3c9611b;


        public void readParams(AbsSerializedData stream, boolean exception) {
            exchange_id = stream.readInt64(exception);
            g_a = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(exchange_id);
            stream.writeByteArray(g_a);
        }
    }

    public static class TL_decryptedMessageActionScreenshotMessages extends DecryptedMessageAction {
        public static int constructor = 0x8ac1f475;


        public void readParams(AbsSerializedData stream, boolean exception) {
            int magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                random_ids.add(stream.readInt64(exception));
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(0x1cb5c415);
            int count = random_ids.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                stream.writeInt64(random_ids.get(a));
            }
        }
    }

    public static class TL_decryptedMessageActionNoop extends DecryptedMessageAction {
        public static int constructor = 0xa82fdd63;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }



    public static class TL_msgs_state_req extends TLObject {
        public static int constructor = 0xda69fb52;

        public ArrayList<Long> msg_ids = new ArrayList<>();



        public void readParams(AbsSerializedData stream, boolean exception) {
            int magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                msg_ids.add(stream.readInt64(exception));
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(0x1cb5c415);
            int count = msg_ids.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                stream.writeInt64(msg_ids.get(a));
            }
        }
    }






    public static class UserProfilePhoto extends TLObject {
        public long photo_id;
        public FileLocation photo_small;
        public FileLocation photo_big;

        public static UserProfilePhoto TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            UserProfilePhoto result = null;
            switch(constructor) {
                case 0x4f11bae1:
                    result = new TL_userProfilePhotoEmpty();
                    break;
                case 0xd559d8c8:
                    result = new TL_userProfilePhoto();
                    break;
                case 0x990d1493:
                    result = new TL_userProfilePhoto_old();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in UserProfilePhoto", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_userProfilePhotoEmpty extends UserProfilePhoto {
        public static int constructor = 0x4f11bae1;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_userProfilePhoto extends UserProfilePhoto {
        public static int constructor = 0xd559d8c8;


        public void readParams(AbsSerializedData stream, boolean exception) {
            photo_id = stream.readInt64(exception);
            photo_small = FileLocation.TLdeserialize(stream, stream.readInt32(exception), exception);
            photo_big = FileLocation.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(photo_id);
            photo_small.serializeToStream(stream);
            photo_big.serializeToStream(stream);
        }
    }

    public static class TL_userProfilePhoto_old extends TL_userProfilePhoto {
        public static int constructor = 0x990d1493;


        public void readParams(AbsSerializedData stream, boolean exception) {
            photo_small = FileLocation.TLdeserialize(stream, stream.readInt32(exception), exception);
            photo_big = FileLocation.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            photo_small.serializeToStream(stream);
            photo_big.serializeToStream(stream);
        }
    }



    public static class Server_DH_Params extends TLObject {
        public byte[] nonce;
        public byte[] server_nonce;
        public byte[] new_nonce_hash;
        public byte[] encrypted_answer;


    }

    public static class TL_server_DH_params_fail extends Server_DH_Params {
        public static int constructor = 0x79cb045d;


        public void readParams(AbsSerializedData stream, boolean exception) {
            nonce = stream.readData(16, exception);
            server_nonce = stream.readData(16, exception);
            new_nonce_hash = stream.readData(16, exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeRaw(nonce);
            stream.writeRaw(server_nonce);
            stream.writeRaw(new_nonce_hash);
        }
    }

    public static class TL_server_DH_params_ok extends Server_DH_Params {
        public static int constructor = 0xd0e8075c;


        public void readParams(AbsSerializedData stream, boolean exception) {
            nonce = stream.readData(16, exception);
            server_nonce = stream.readData(16, exception);
            encrypted_answer = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeRaw(nonce);
            stream.writeRaw(server_nonce);
            stream.writeByteArray(encrypted_answer);
        }
    }

    public static class TL_protoMessage extends TLObject {
        public static int constructor = 0x5bb8e511;

        public long msg_id;
        public int seqno;
        public int bytes;
        public TLObject body;

        public static TL_protoMessage TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            if (TL_protoMessage.constructor != constructor) {
                if (exception) {
                    throw new RuntimeException(String.format("can't parse magic %x in TL_protoMessage", constructor));
                } else {
                    return null;
                }
            }
            TL_protoMessage result = new TL_protoMessage();
            result.readParams(stream, exception);
            return result;
        }

        public void readParams(AbsSerializedData stream, boolean exception) {
            msg_id = stream.readInt64(exception);
            seqno = stream.readInt32(exception);
            bytes = stream.readInt32(exception);
            body = TLClassStore.Instance().TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(msg_id);
            stream.writeInt32(seqno);
            stream.writeInt32(bytes);
            body.serializeToStream(stream);
        }
    }




















    public static class TL_msgs_state_info extends TLObject {
        public static int constructor = 0x04deb57d;

        public long req_msg_id;
        public String info;



        public void readParams(AbsSerializedData stream, boolean exception) {
            req_msg_id = stream.readInt64(exception);
            info = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(req_msg_id);
            stream.writeString(info);
        }
    }












    public static class TL_decryptedMessageLayer extends TLObject {
        public static int constructor = 0x1be31789;

        public byte[] random_bytes;
        public int layer;
        public int in_seq_no;
        public int out_seq_no;

        public static TL_decryptedMessageLayer TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            if (TL_decryptedMessageLayer.constructor != constructor) {
                if (exception) {
                    throw new RuntimeException(String.format("can't parse magic %x in TL_decryptedMessageLayer", constructor));
                } else {
                    return null;
                }
            }
            TL_decryptedMessageLayer result = new TL_decryptedMessageLayer();
            result.readParams(stream, exception);
            return result;
        }

        public void readParams(AbsSerializedData stream, boolean exception) {
            random_bytes = stream.readByteArray(exception);
            layer = stream.readInt32(exception);
            in_seq_no = stream.readInt32(exception);
            out_seq_no = stream.readInt32(exception);
//            message = DecryptedMessage.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeByteArray(random_bytes);
            stream.writeInt32(layer);
            stream.writeInt32(in_seq_no);
            stream.writeInt32(out_seq_no);
//            message.serializeToStream(stream);
        }
    }

    public static class Audio extends TLObject {
        public long id;
        public long access_hash;
        public int user_id;
        public int date;
        public int duration;
        public int size;
        public int dc_id;
        public String mime_type;
        public byte[] key;
        public byte[] iv;

        public static Audio TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            Audio result = null;
            switch(constructor) {
                case 0x427425e7:
                    result = new TL_audio_old();
                    break;
                case 0xc7ac6496:
                    result = new TL_audio();
                    break;
                case 0x555555F6:
                    result = new TL_audioEncrypted();
                    break;
                case 0x586988d8:
                    result = new TL_audioEmpty();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in Audio", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_audio_old extends TL_audio {
        public static int constructor = 0x427425e7;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt64(exception);
            access_hash = stream.readInt64(exception);
            user_id = stream.readInt32(exception);
            date = stream.readInt32(exception);
            duration = stream.readInt32(exception);
            size = stream.readInt32(exception);
            dc_id = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(id);
            stream.writeInt64(access_hash);
            stream.writeInt32(user_id);
            stream.writeInt32(date);
            stream.writeInt32(duration);
            stream.writeInt32(size);
            stream.writeInt32(dc_id);
        }
    }

    public static class TL_audio extends Audio {
        public static int constructor = 0xc7ac6496;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt64(exception);
            access_hash = stream.readInt64(exception);
            user_id = stream.readInt32(exception);
            date = stream.readInt32(exception);
            duration = stream.readInt32(exception);
            mime_type = stream.readString(exception);
            size = stream.readInt32(exception);
            dc_id = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(id);
            stream.writeInt64(access_hash);
            stream.writeInt32(user_id);
            stream.writeInt32(date);
            stream.writeInt32(duration);
            stream.writeString(mime_type);
            stream.writeInt32(size);
            stream.writeInt32(dc_id);
        }
    }

    public static class TL_audioEncrypted extends TL_audio {
        public static int constructor = 0x555555F6;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt64(exception);
            access_hash = stream.readInt64(exception);
            user_id = stream.readInt32(exception);
            date = stream.readInt32(exception);
            duration = stream.readInt32(exception);
            size = stream.readInt32(exception);
            dc_id = stream.readInt32(exception);
            key = stream.readByteArray(exception);
            iv = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(id);
            stream.writeInt64(access_hash);
            stream.writeInt32(user_id);
            stream.writeInt32(date);
            stream.writeInt32(duration);
            stream.writeInt32(size);
            stream.writeInt32(dc_id);
            stream.writeByteArray(key);
            stream.writeByteArray(iv);
        }
    }

    public static class TL_audioEmpty extends Audio {
        public static int constructor = 0x586988d8;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(id);
        }
    }
















    public static class TL_http_wait extends TLObject {
        public static int constructor = 0x9299359f;

        public int max_delay;
        public int wait_after;
        public int max_wait;



        public void readParams(AbsSerializedData stream, boolean exception) {
            max_delay = stream.readInt32(exception);
            wait_after = stream.readInt32(exception);
            max_wait = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(max_delay);
            stream.writeInt32(wait_after);
            stream.writeInt32(max_wait);
        }
    }


























    public static class SendMessageAction extends TLObject {
        public int progress;

        public static SendMessageAction TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            SendMessageAction result = null;
            switch(constructor) {
                case 0xd52f73f7:
                    result = new TL_sendMessageRecordAudioAction();
                    break;
                case 0x92042ff7:
                    result = new TL_sendMessageUploadVideoAction_old();
                    break;
                case 0xe6ac8a6f:
                    result = new TL_sendMessageUploadAudioAction_old();
                    break;
                case 0xf351d7ab:
                    result = new TL_sendMessageUploadAudioAction();
                    break;
                case 0xd1d34a26:
                    result = new TL_sendMessageUploadPhotoAction();
                    break;
                case 0x8faee98e:
                    result = new TL_sendMessageUploadDocumentAction_old();
                    break;
                case 0xe9763aec:
                    result = new TL_sendMessageUploadVideoAction();
                    break;
                case 0xfd5ec8f5:
                    result = new TL_sendMessageCancelAction();
                    break;
                case 0x176f8ba1:
                    result = new TL_sendMessageGeoLocationAction();
                    break;
                case 0x628cbc6f:
                    result = new TL_sendMessageChooseContactAction();
                    break;
                case 0x16bf744e:
                    result = new TL_sendMessageTypingAction();
                    break;
                case 0x990a3c1a:
                    result = new TL_sendMessageUploadPhotoAction_old();
                    break;
                case 0xaa0cd9e4:
                    result = new TL_sendMessageUploadDocumentAction();
                    break;
                case 0xa187d66f:
                    result = new TL_sendMessageRecordVideoAction();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in SendMessageAction", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_sendMessageRecordAudioAction extends SendMessageAction {
        public static int constructor = 0xd52f73f7;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_sendMessageUploadVideoAction_old extends TL_sendMessageUploadVideoAction {
        public static int constructor = 0x92042ff7;

        public void readParams(AbsSerializedData stream, boolean exception) {
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_sendMessageUploadAudioAction_old extends TL_sendMessageUploadAudioAction {
        public static int constructor = 0xe6ac8a6f;

        public void readParams(AbsSerializedData stream, boolean exception) {
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_sendMessageUploadAudioAction extends SendMessageAction {
        public static int constructor = 0xf351d7ab;


        public void readParams(AbsSerializedData stream, boolean exception) {
            progress = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(progress);
        }
    }

    public static class TL_sendMessageUploadPhotoAction extends SendMessageAction {
        public static int constructor = 0xd1d34a26;


        public void readParams(AbsSerializedData stream, boolean exception) {
            progress = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(progress);
        }
    }

    public static class TL_sendMessageUploadDocumentAction_old extends TL_sendMessageUploadDocumentAction {
        public static int constructor = 0x8faee98e;

        public void readParams(AbsSerializedData stream, boolean exception) {
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_sendMessageUploadVideoAction extends SendMessageAction {
        public static int constructor = 0xe9763aec;


        public void readParams(AbsSerializedData stream, boolean exception) {
            progress = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(progress);
        }
    }

    public static class TL_sendMessageCancelAction extends SendMessageAction {
        public static int constructor = 0xfd5ec8f5;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_sendMessageGeoLocationAction extends SendMessageAction {
        public static int constructor = 0x176f8ba1;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_sendMessageChooseContactAction extends SendMessageAction {
        public static int constructor = 0x628cbc6f;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_sendMessageTypingAction extends SendMessageAction {
        public static int constructor = 0x16bf744e;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_sendMessageUploadPhotoAction_old extends TL_sendMessageUploadPhotoAction {
        public static int constructor = 0x990a3c1a;

        public void readParams(AbsSerializedData stream, boolean exception) {
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_sendMessageUploadDocumentAction extends SendMessageAction {
        public static int constructor = 0xaa0cd9e4;


        public void readParams(AbsSerializedData stream, boolean exception) {
            progress = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(progress);
        }
    }

    public static class TL_sendMessageRecordVideoAction extends SendMessageAction {
        public static int constructor = 0xa187d66f;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_client_DH_inner_data extends TLObject {
        public static int constructor = 0x6643b654;

        public byte[] nonce;
        public byte[] server_nonce;
        public long retry_id;
        public byte[] g_b;



        public void readParams(AbsSerializedData stream, boolean exception) {
            nonce = stream.readData(16, exception);
            server_nonce = stream.readData(16, exception);
            retry_id = stream.readInt64(exception);
            g_b = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeRaw(nonce);
            stream.writeRaw(server_nonce);
            stream.writeInt64(retry_id);
            stream.writeByteArray(g_b);
        }
    }

    public static class TL_null extends TLObject {
        public static int constructor = 0x56730bcc;




        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

















    //Message start
    public static class Message extends TLObject {
        public int id;
        public int from_id;
        public Peer to_id;
        public int date;
        public MessageAction action;
        public int fwd_from_id;
        public int fwd_date;
        public int reply_to_msg_id;
        public String message;
        public MessageMedia media;
        public int flags;
        public int send_state = 0; //custom
        public int fwd_msg_id = 0; //custom
        public String attachPath = ""; //custom
        public long random_id; //custom
        public int local_id = 0; //custom
        public long dialog_id; //custom
        public int ttl; //custom
        public int destroyTime; //custom
        public int layer; //custom
        public int seq_in; //custom
        public int seq_out; //custom
        public TLRPC.Message replyMessage; //custom


    }



    public static class TL_message extends Message {
        public static int constructor = 0xa7ab1991;

        public void readParams(AbsSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            id = stream.readInt32(exception);
            from_id = stream.readInt32(exception);
            to_id = Peer.TLdeserialize(stream, stream.readInt32(exception), exception);
            if ((flags & 4) != 0) {
                fwd_from_id = stream.readInt32(exception);
            }
            if ((flags & 4) != 0) {
                fwd_date = stream.readInt32(exception);
            }
            if ((flags & 8) != 0) {
                reply_to_msg_id = stream.readInt32(exception);
            }
            date = stream.readInt32(exception);
            message = stream.readString(exception);
            media = MessageMedia.TLdeserialize(stream, stream.readInt32(exception), exception);
            if (id < 0 || (media != null && !(media instanceof TL_messageMediaEmpty) && message != null && message.length() != 0 && message.startsWith("-1"))) {
                attachPath = stream.readString(exception);
            }
            if ((flags & MESSAGE_FLAG_FWD) != 0 && id < 0) {
                fwd_msg_id = stream.readInt32(exception);
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeInt32(id);
            stream.writeInt32(from_id);
            to_id.serializeToStream(stream);
            if ((flags & 4) != 0) {
                stream.writeInt32(fwd_from_id);
            }
            if ((flags & 4) != 0) {
                stream.writeInt32(fwd_date);
            }
            if ((flags & 8) != 0) {
                stream.writeInt32(reply_to_msg_id);
            }
            stream.writeInt32(date);
            stream.writeString(message);
            media.serializeToStream(stream);
            stream.writeString(attachPath);
            if ((flags & MESSAGE_FLAG_FWD) != 0 && id < 0) {
                stream.writeInt32(fwd_msg_id);
            }
        }
    }







    public static class TL_message_secret extends TL_message {
        public static int constructor = 0x555555F8;

        public void readParams(AbsSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            id = stream.readInt32(exception);
            ttl = stream.readInt32(exception);
            from_id = stream.readInt32(exception);
            to_id = Peer.TLdeserialize(stream, stream.readInt32(exception), exception);
            date = stream.readInt32(exception);
            message = stream.readString(exception);
            media = MessageMedia.TLdeserialize(stream, stream.readInt32(exception), exception);
            if (id < 0 || (media != null && !(media instanceof TL_messageMediaEmpty) && message != null && message.length() != 0 && message.startsWith("-1"))) {
                attachPath = stream.readString(exception);
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeInt32(id);
            stream.writeInt32(ttl);
            stream.writeInt32(from_id);
            to_id.serializeToStream(stream);
            stream.writeInt32(date);
            stream.writeString(message);
            media.serializeToStream(stream);
            stream.writeString(attachPath);
        }
    }
    //Message end





    public static class TL_futureSalt extends TLObject {
        public static int constructor = 0x0949d9dc;

        public int valid_since;
        public int valid_until;
        public long salt;

        public void readParams(AbsSerializedData stream, boolean exception) {
            valid_since = stream.readInt32(exception);
            valid_until = stream.readInt32(exception);
            salt = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(valid_since);
            stream.writeInt32(valid_until);
            stream.writeInt64(salt);
        }
    }



    public static class TL_gzip_packed extends TLObject {
        public static int constructor = 0x3072cfa1;

        public byte[] packed_data;

        public void readParams(AbsSerializedData stream, boolean exception) {
            packed_data = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeByteArray(packed_data);
        }
    }

    public static class Vector extends TLObject {
        public static int constructor = 0x1cb5c415;
        public ArrayList<Object> objects = new ArrayList<>();
    }

    public static class TL_decryptedMessageHolder extends TLObject {
        public static int constructor = 0x555555F9;

        public long random_id;
        public int date;
        public TL_decryptedMessageLayer layer;
        public EncryptedFile file;
        public boolean new_key_used;

        public void readParams(AbsSerializedData stream, boolean exception) {
            random_id = stream.readInt64(exception);
            date = stream.readInt32(exception);
            layer = TL_decryptedMessageLayer.TLdeserialize(stream, stream.readInt32(exception), exception);
            if (stream.readBool(exception)) {
                file = EncryptedFile.TLdeserialize(stream, stream.readInt32(exception), exception);
            }
            new_key_used = stream.readBool(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(random_id);
            stream.writeInt32(date);
            layer.serializeToStream(stream);
            stream.writeBool(file != null);
            if (file != null) {
                file.serializeToStream(stream);
            }
            stream.writeBool(new_key_used);
        }
    }
}
