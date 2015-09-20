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

    public static final int MESSAGE_FLAG_UNREAD = 1;
    public static final int MESSAGE_FLAG_OUT = 2;
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




    public static class TL_messageEmpty extends Message {
        public static int constructor = 0x83e5de54;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
        }
    }

    public static class TL_messageService extends Message {
        public static int constructor = 0x1d86f70e;


        public void readParams(AbsSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            id = stream.readInt32(exception);
            from_id = stream.readInt32(exception);
            to_id = Peer.TLdeserialize(stream, stream.readInt32(exception), exception);
            date = stream.readInt32(exception);
            action = MessageAction.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeInt32(id);
            stream.writeInt32(from_id);
            to_id.serializeToStream(stream);
            stream.writeInt32(date);
            action.serializeToStream(stream);
        }
    }











    public static class TL_auth_authorization extends TLObject {
        public static int constructor = 0xf6b673a4;

        public int expires;
        public User user;



        public void readParams(AbsSerializedData stream, boolean exception) {
            expires = stream.readInt32(exception);
            user = User.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(expires);
            user.serializeToStream(stream);
        }
    }

    public static class Set_client_DH_params_answer extends TLObject {
        public byte[] nonce;
        public byte[] server_nonce;
        public byte[] new_nonce_hash2;
        public byte[] new_nonce_hash3;
        public byte[] new_nonce_hash1;


    }

    public static class TL_dh_gen_retry extends Set_client_DH_params_answer {
        public static int constructor = 0x46dc1fb9;


        public void readParams(AbsSerializedData stream, boolean exception) {
            nonce = stream.readData(16, exception);
            server_nonce = stream.readData(16, exception);
            new_nonce_hash2 = stream.readData(16, exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeRaw(nonce);
            stream.writeRaw(server_nonce);
            stream.writeRaw(new_nonce_hash2);
        }
    }

    public static class TL_dh_gen_fail extends Set_client_DH_params_answer {
        public static int constructor = 0xa69dae02;


        public void readParams(AbsSerializedData stream, boolean exception) {
            nonce = stream.readData(16, exception);
            server_nonce = stream.readData(16, exception);
            new_nonce_hash3 = stream.readData(16, exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeRaw(nonce);
            stream.writeRaw(server_nonce);
            stream.writeRaw(new_nonce_hash3);
        }
    }

    public static class TL_dh_gen_ok extends Set_client_DH_params_answer {
        public static int constructor = 0x3bcbf734;


        public void readParams(AbsSerializedData stream, boolean exception) {
            nonce = stream.readData(16, exception);
            server_nonce = stream.readData(16, exception);
            new_nonce_hash1 = stream.readData(16, exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeRaw(nonce);
            stream.writeRaw(server_nonce);
            stream.writeRaw(new_nonce_hash1);
        }
    }









    public static class TL_server_DH_inner_data extends TLObject {
        public static int constructor = 0xb5890dba;

        public byte[] nonce;
        public byte[] server_nonce;
        public int g;
        public byte[] dh_prime;
        public byte[] g_a;
        public int server_time;



        public void readParams(AbsSerializedData stream, boolean exception) {
            nonce = stream.readData(16, exception);
            server_nonce = stream.readData(16, exception);
            g = stream.readInt32(exception);
            dh_prime = stream.readByteArray(exception);
            g_a = stream.readByteArray(exception);
            server_time = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeRaw(nonce);
            stream.writeRaw(server_nonce);
            stream.writeInt32(g);
            stream.writeByteArray(dh_prime);
            stream.writeByteArray(g_a);
            stream.writeInt32(server_time);
        }
    }











    public static class TL_msgs_ack extends TLObject {
        public static int constructor = 0x62d6b459;

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

    public static class UserStatus extends TLObject {
        public int expires;

        public static UserStatus TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            UserStatus result = null;
            switch(constructor) {
                case 0x8c703f:
                    result = new TL_userStatusOffline();
                    break;
                case 0x7bf09fc:
                    result = new TL_userStatusLastWeek();
                    break;
                case 0x9d05049:
                    result = new TL_userStatusEmpty();
                    break;
                case 0x77ebc742:
                    result = new TL_userStatusLastMonth();
                    break;
                case 0xedb93949:
                    result = new TL_userStatusOnline();
                    break;
                case 0xe26f42f1:
                    result = new TL_userStatusRecently();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in UserStatus", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_userStatusOffline extends UserStatus {
        public static int constructor = 0x8c703f;


        public void readParams(AbsSerializedData stream, boolean exception) {
            expires = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(expires);
        }
    }

    public static class TL_userStatusLastWeek extends UserStatus {
        public static int constructor = 0x7bf09fc;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_userStatusEmpty extends UserStatus {
        public static int constructor = 0x9d05049;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_userStatusLastMonth extends UserStatus {
        public static int constructor = 0x77ebc742;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_userStatusOnline extends UserStatus {
        public static int constructor = 0xedb93949;


        public void readParams(AbsSerializedData stream, boolean exception) {
            expires = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(expires);
        }
    }

    public static class TL_userStatusRecently extends UserStatus {
        public static int constructor = 0xe26f42f1;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }



    public static class TL_msg_resend_req extends TLObject {
        public static int constructor = 0x7d861a08;

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





    public static class RpcError extends TLObject {
        public int error_code;
        public String error_message;
        public long query_id;


    }

    public static class TL_rpc_error extends RpcError {
        public static int constructor = 0x2144ca19;


        public void readParams(AbsSerializedData stream, boolean exception) {
            error_code = stream.readInt32(exception);
            error_message = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(error_code);
            stream.writeString(error_message);
        }
    }

    public static class TL_rpc_req_error extends RpcError {
        public static int constructor = 0x7ae432f5;


        public void readParams(AbsSerializedData stream, boolean exception) {
            query_id = stream.readInt64(exception);
            error_code = stream.readInt32(exception);
            error_message = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(query_id);
            stream.writeInt32(error_code);
            stream.writeString(error_message);
        }
    }

    public static class TL_privacyKeyStatusTimestamp extends TLObject {
        public static int constructor = 0xbc2eab30;


        public static TL_privacyKeyStatusTimestamp TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            if (TL_privacyKeyStatusTimestamp.constructor != constructor) {
                if (exception) {
                    throw new RuntimeException(String.format("can't parse magic %x in TL_privacyKeyStatusTimestamp", constructor));
                } else {
                    return null;
                }
            }
            TL_privacyKeyStatusTimestamp result = new TL_privacyKeyStatusTimestamp();
            result.readParams(stream, exception);
            return result;
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class ChatParticipants extends TLObject {
        public int chat_id;
        public int admin_id;
        public ArrayList<TL_chatParticipant> participants = new ArrayList<>();
        public int version;

        public static ChatParticipants TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            ChatParticipants result = null;
            switch(constructor) {
                case 0x7841b415:
                    result = new TL_chatParticipants();
                    break;
                case 0xfd2bb8a:
                    result = new TL_chatParticipantsForbidden();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in ChatParticipants", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_chatParticipants extends ChatParticipants {
        public static int constructor = 0x7841b415;


        public void readParams(AbsSerializedData stream, boolean exception) {
            chat_id = stream.readInt32(exception);
            admin_id = stream.readInt32(exception);
            int magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                TL_chatParticipant object = TL_chatParticipant.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                participants.add(object);
            }
            version = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(chat_id);
            stream.writeInt32(admin_id);
            stream.writeInt32(0x1cb5c415);
            int count = participants.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                participants.get(a).serializeToStream(stream);
            }
            stream.writeInt32(version);
        }
    }

    public static class TL_chatParticipantsForbidden extends ChatParticipants {
        public static int constructor = 0xfd2bb8a;


        public void readParams(AbsSerializedData stream, boolean exception) {
            chat_id = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(chat_id);
        }
    }



    public static class DecryptedMessage extends TLObject {
        public long random_id;
        public DecryptedMessageAction action;
        public byte[] random_bytes;
        public String message;
        public DecryptedMessageMedia media;
        public int ttl;

        public static DecryptedMessage TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            DecryptedMessage result = null;
            switch(constructor) {
                case 0x73164160:
                    result = new TL_decryptedMessageService();
                    break;
                case 0x1f814f1f:
                    result = new TL_decryptedMessage_old();
                    break;
                case 0x204d3878:
                    result = new TL_decryptedMessage();
                    break;
                case 0xaa48327d:
                    result = new TL_decryptedMessageService_old();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in DecryptedMessage", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_decryptedMessageService extends DecryptedMessage {
        public static int constructor = 0x73164160;


        public void readParams(AbsSerializedData stream, boolean exception) {
            random_id = stream.readInt64(exception);
            action = DecryptedMessageAction.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(random_id);
            action.serializeToStream(stream);
        }
    }

    public static class TL_decryptedMessage_old extends TL_decryptedMessage {
        public static int constructor = 0x1f814f1f;


        public void readParams(AbsSerializedData stream, boolean exception) {
            random_id = stream.readInt64(exception);
            random_bytes = stream.readByteArray(exception);
            message = stream.readString(exception);
            media = DecryptedMessageMedia.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(random_id);
            stream.writeByteArray(random_bytes);
            stream.writeString(message);
            media.serializeToStream(stream);
        }
    }

    public static class TL_decryptedMessage extends DecryptedMessage {
        public static int constructor = 0x204d3878;


        public void readParams(AbsSerializedData stream, boolean exception) {
            random_id = stream.readInt64(exception);
            ttl = stream.readInt32(exception);
            message = stream.readString(exception);
            media = DecryptedMessageMedia.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(random_id);
            stream.writeInt32(ttl);
            stream.writeString(message);
            media.serializeToStream(stream);
        }
    }

    public static class TL_decryptedMessageService_old extends TL_decryptedMessageService {
        public static int constructor = 0xaa48327d;


        public void readParams(AbsSerializedData stream, boolean exception) {
            random_id = stream.readInt64(exception);
            random_bytes = stream.readByteArray(exception);
            action = DecryptedMessageAction.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(random_id);
            stream.writeByteArray(random_bytes);
            action.serializeToStream(stream);
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

    public static class DecryptedMessageMedia extends TLObject {
        public int thumb_w;
        public int thumb_h;
        public String file_name;
        public String mime_type;
        public int size;
        public byte[] key;
        public byte[] iv;
        public long id;
        public long access_hash;
        public int date;
        public int dc_id;
        public int duration;
        public double lat;
        public double _long;
        public int w;
        public int h;
        public String phone_number;
        public String first_name;
        public String last_name;
        public int user_id;

        public static DecryptedMessageMedia TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            DecryptedMessageMedia result = null;
            switch(constructor) {
                case 0x89f5c4a:
                    result = new TL_decryptedMessageMediaEmpty();
                    break;
                case 0xb095434b:
                    result = new TL_decryptedMessageMediaDocument();
                    break;
                case 0xfa95b0dd:
                    result = new TL_decryptedMessageMediaExternalDocument();
                    break;
                case 0x6080758f:
                    result = new TL_decryptedMessageMediaAudio_old();
                    break;
                case 0x35480a59:
                    result = new TL_decryptedMessageMediaGeoPoint();
                    break;
                case 0x57e0a9cb:
                    result = new TL_decryptedMessageMediaAudio();
                    break;
                case 0x524a415d:
                    result = new TL_decryptedMessageMediaVideo();
                    break;
                case 0x588a0a97:
                    result = new TL_decryptedMessageMediaContact();
                    break;
                case 0x32798a8c:
                    result = new TL_decryptedMessageMediaPhoto();
                    break;
                case 0x4cee6ef3:
                    result = new TL_decryptedMessageMediaVideo_old();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in DecryptedMessageMedia", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_decryptedMessageMediaEmpty extends DecryptedMessageMedia {
        public static int constructor = 0x89f5c4a;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_decryptedMessageMediaDocument extends DecryptedMessageMedia {
        public static int constructor = 0xb095434b;

        public byte[] thumb;

        public void readParams(AbsSerializedData stream, boolean exception) {
            thumb = stream.readByteArray(exception);
            thumb_w = stream.readInt32(exception);
            thumb_h = stream.readInt32(exception);
            file_name = stream.readString(exception);
            mime_type = stream.readString(exception);
            size = stream.readInt32(exception);
            key = stream.readByteArray(exception);
            iv = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeByteArray(thumb);
            stream.writeInt32(thumb_w);
            stream.writeInt32(thumb_h);
            stream.writeString(file_name);
            stream.writeString(mime_type);
            stream.writeInt32(size);
            stream.writeByteArray(key);
            stream.writeByteArray(iv);
        }
    }

    public static class TL_decryptedMessageMediaExternalDocument extends DecryptedMessageMedia {
        public static int constructor = 0xfa95b0dd;

        public PhotoSize thumb;

        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt64(exception);
            access_hash = stream.readInt64(exception);
            date = stream.readInt32(exception);
            mime_type = stream.readString(exception);
            size = stream.readInt32(exception);
            thumb = PhotoSize.TLdeserialize(stream, stream.readInt32(exception), exception);
            dc_id = stream.readInt32(exception);
            int magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
//            int count = stream.readInt32(exception);
//            for (int a = 0; a < count; a++) {
//                DocumentAttribute object = DocumentAttribute.TLdeserialize(stream, stream.readInt32(exception), exception);
//                if (object == null) {
//                    return;
//                }
//                attributes.add(object);
//            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(id);
            stream.writeInt64(access_hash);
            stream.writeInt32(date);
            stream.writeString(mime_type);
            stream.writeInt32(size);
            thumb.serializeToStream(stream);
            stream.writeInt32(dc_id);
            stream.writeInt32(0x1cb5c415);
//            int count = attributes.size();
//            stream.writeInt32(count);
//            for (int a = 0; a < count; a++) {
//                attributes.get(a).serializeToStream(stream);
//            }
        }
    }

    public static class TL_decryptedMessageMediaAudio_old extends TL_decryptedMessageMediaAudio {
        public static int constructor = 0x6080758f;


        public void readParams(AbsSerializedData stream, boolean exception) {
            duration = stream.readInt32(exception);
            size = stream.readInt32(exception);
            key = stream.readByteArray(exception);
            iv = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(duration);
            stream.writeInt32(size);
            stream.writeByteArray(key);
            stream.writeByteArray(iv);
        }
    }

    public static class TL_decryptedMessageMediaGeoPoint extends DecryptedMessageMedia {
        public static int constructor = 0x35480a59;


        public void readParams(AbsSerializedData stream, boolean exception) {
            lat = stream.readDouble(exception);
            _long = stream.readDouble(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeDouble(lat);
            stream.writeDouble(_long);
        }
    }

    public static class TL_decryptedMessageMediaAudio extends DecryptedMessageMedia {
        public static int constructor = 0x57e0a9cb;


        public void readParams(AbsSerializedData stream, boolean exception) {
            duration = stream.readInt32(exception);
            mime_type = stream.readString(exception);
            size = stream.readInt32(exception);
            key = stream.readByteArray(exception);
            iv = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(duration);
            stream.writeString(mime_type);
            stream.writeInt32(size);
            stream.writeByteArray(key);
            stream.writeByteArray(iv);
        }
    }

    public static class TL_decryptedMessageMediaVideo extends DecryptedMessageMedia {
        public static int constructor = 0x524a415d;

        public byte[] thumb;

        public void readParams(AbsSerializedData stream, boolean exception) {
            thumb = stream.readByteArray(exception);
            thumb_w = stream.readInt32(exception);
            thumb_h = stream.readInt32(exception);
            duration = stream.readInt32(exception);
            mime_type = stream.readString(exception);
            w = stream.readInt32(exception);
            h = stream.readInt32(exception);
            size = stream.readInt32(exception);
            key = stream.readByteArray(exception);
            iv = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeByteArray(thumb);
            stream.writeInt32(thumb_w);
            stream.writeInt32(thumb_h);
            stream.writeInt32(duration);
            stream.writeString(mime_type);
            stream.writeInt32(w);
            stream.writeInt32(h);
            stream.writeInt32(size);
            stream.writeByteArray(key);
            stream.writeByteArray(iv);
        }
    }

    public static class TL_decryptedMessageMediaContact extends DecryptedMessageMedia {
        public static int constructor = 0x588a0a97;


        public void readParams(AbsSerializedData stream, boolean exception) {
            phone_number = stream.readString(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            user_id = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(phone_number);
            stream.writeString(first_name);
            stream.writeString(last_name);
            stream.writeInt32(user_id);
        }
    }

    public static class TL_decryptedMessageMediaPhoto extends DecryptedMessageMedia {
        public static int constructor = 0x32798a8c;

        public byte[] thumb;

        public void readParams(AbsSerializedData stream, boolean exception) {
            thumb = stream.readByteArray(exception);
            thumb_w = stream.readInt32(exception);
            thumb_h = stream.readInt32(exception);
            w = stream.readInt32(exception);
            h = stream.readInt32(exception);
            size = stream.readInt32(exception);
            key = stream.readByteArray(exception);
            iv = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeByteArray(thumb);
            stream.writeInt32(thumb_w);
            stream.writeInt32(thumb_h);
            stream.writeInt32(w);
            stream.writeInt32(h);
            stream.writeInt32(size);
            stream.writeByteArray(key);
            stream.writeByteArray(iv);
        }
    }

    public static class TL_decryptedMessageMediaVideo_old extends TL_decryptedMessageMediaVideo {
        public static int constructor = 0x4cee6ef3;

        public byte[] thumb;

        public void readParams(AbsSerializedData stream, boolean exception) {
            thumb = stream.readByteArray(exception);
            thumb_w = stream.readInt32(exception);
            thumb_h = stream.readInt32(exception);
            duration = stream.readInt32(exception);
            w = stream.readInt32(exception);
            h = stream.readInt32(exception);
            size = stream.readInt32(exception);
            key = stream.readByteArray(exception);
            iv = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeByteArray(thumb);
            stream.writeInt32(thumb_w);
            stream.writeInt32(thumb_h);
            stream.writeInt32(duration);
            stream.writeInt32(w);
            stream.writeInt32(h);
            stream.writeInt32(size);
            stream.writeByteArray(key);
            stream.writeByteArray(iv);
        }
    }

    public static class User extends TLObject {
        public int id;
        public String first_name;
        public String last_name;
        public long access_hash;
        public String phone;
        public UserProfilePhoto photo;
        public UserStatus status;
        public boolean inactive;
        public String username;

        public static User TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            User result = null;
            switch(constructor) {
                case 0xf2fb8319:
                    result = new TL_userContact_old();
                    break;
                case 0x720535ec:
                    result = new TL_userSelf_old();
                    break;
                case 0xcab35e18:
                    result = new TL_userContact();
                    break;
                case 0x1c60e608:
                    result = new TL_userSelf();
                    break;
                case 0x75cf7a8:
                    result = new TL_userForeign();
                    break;
                case 0x200250ba:
                    result = new TL_userEmpty();
                    break;
                case 0x22e8ceb0:
                    result = new TL_userRequest_old();
                    break;
                case 0x5214c89d:
                    result = new TL_userForeign_old();
                    break;
                case 0xd9ccc4ef:
                    result = new TL_userRequest();
                    break;
                case 0x7007b451:
                    result = new TL_userSelf_old2();
                    break;
                case 0xb29ad7cc:
                    result = new TL_userDeleted_old();
                    break;
                case 0xd6016d7a:
                    result = new TL_userDeleted();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in User", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_userContact_old extends TL_userContact {
        public static int constructor = 0xf2fb8319;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            access_hash = stream.readInt64(exception);
            phone = stream.readString(exception);
            photo = UserProfilePhoto.TLdeserialize(stream, stream.readInt32(exception), exception);
            status = UserStatus.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeString(first_name);
            stream.writeString(last_name);
            stream.writeInt64(access_hash);
            stream.writeString(phone);
            photo.serializeToStream(stream);
            status.serializeToStream(stream);
        }
    }

    public static class TL_userSelf_old extends TL_userSelf {
        public static int constructor = 0x720535ec;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            phone = stream.readString(exception);
            photo = UserProfilePhoto.TLdeserialize(stream, stream.readInt32(exception), exception);
            status = UserStatus.TLdeserialize(stream, stream.readInt32(exception), exception);
            inactive = stream.readBool(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeString(first_name);
            stream.writeString(last_name);
            stream.writeString(phone);
            photo.serializeToStream(stream);
            status.serializeToStream(stream);
            stream.writeBool(inactive);
        }
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
            status = UserStatus.TLdeserialize(stream, stream.readInt32(exception), exception);
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
            status.serializeToStream(stream);
        }
    }

    public static class TL_userSelf extends User {
        public static int constructor = 0x1c60e608;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            username = stream.readString(exception);
            phone = stream.readString(exception);
            photo = UserProfilePhoto.TLdeserialize(stream, stream.readInt32(exception), exception);
            status = UserStatus.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeString(first_name);
            stream.writeString(last_name);
            stream.writeString(username);
            stream.writeString(phone);
            photo.serializeToStream(stream);
            status.serializeToStream(stream);
        }
    }

    public static class TL_userForeign extends User {
        public static int constructor = 0x75cf7a8;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            username = stream.readString(exception);
            access_hash = stream.readInt64(exception);
            photo = UserProfilePhoto.TLdeserialize(stream, stream.readInt32(exception), exception);
            status = UserStatus.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeString(first_name);
            stream.writeString(last_name);
            stream.writeString(username);
            stream.writeInt64(access_hash);
            photo.serializeToStream(stream);
            status.serializeToStream(stream);
        }
    }

    public static class TL_userRequest_old extends TL_userRequest {
        public static int constructor = 0x22e8ceb0;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            access_hash = stream.readInt64(exception);
            phone = stream.readString(exception);
            photo = UserProfilePhoto.TLdeserialize(stream, stream.readInt32(exception), exception);
            status = UserStatus.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeString(first_name);
            stream.writeString(last_name);
            stream.writeInt64(access_hash);
            stream.writeString(phone);
            photo.serializeToStream(stream);
            status.serializeToStream(stream);
        }
    }

    public static class TL_userForeign_old extends TL_userForeign {
        public static int constructor = 0x5214c89d;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            access_hash = stream.readInt64(exception);
            photo = UserProfilePhoto.TLdeserialize(stream, stream.readInt32(exception), exception);
            status = UserStatus.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeString(first_name);
            stream.writeString(last_name);
            stream.writeInt64(access_hash);
            photo.serializeToStream(stream);
            status.serializeToStream(stream);
        }
    }

    public static class TL_userRequest extends User {
        public static int constructor = 0xd9ccc4ef;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            username = stream.readString(exception);
            access_hash = stream.readInt64(exception);
            phone = stream.readString(exception);
            photo = UserProfilePhoto.TLdeserialize(stream, stream.readInt32(exception), exception);
            status = UserStatus.TLdeserialize(stream, stream.readInt32(exception), exception);
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
            status.serializeToStream(stream);
        }
    }

    public static class TL_userSelf_old2 extends TL_userSelf {
        public static int constructor = 0x7007b451;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            username = stream.readString(exception);
            phone = stream.readString(exception);
            photo = UserProfilePhoto.TLdeserialize(stream, stream.readInt32(exception), exception);
            status = UserStatus.TLdeserialize(stream, stream.readInt32(exception), exception);
            inactive = stream.readBool(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeString(first_name);
            stream.writeString(last_name);
            stream.writeString(username);
            stream.writeString(phone);
            photo.serializeToStream(stream);
            status.serializeToStream(stream);
            stream.writeBool(inactive);
        }
    }

    public static class TL_userDeleted_old extends TL_userDeleted {
        public static int constructor = 0xb29ad7cc;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeString(first_name);
            stream.writeString(last_name);
        }
    }

    public static class TL_userDeleted extends User {
        public static int constructor = 0xd6016d7a;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            username = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeString(first_name);
            stream.writeString(last_name);
            stream.writeString(username);
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
                case 0xc8c45a2a:
                    result = new TL_messageMediaPhoto_old();
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
                case 0xa2d24290:
                    result = new TL_messageMediaVideo_old();
                    break;
                case 0x2fda2204:
                    result = new TL_messageMediaDocument();
                    break;
                case 0x5e7d2f39:
                    result = new TL_messageMediaContact();
                    break;
                case 0x3d8ce53d:
                    result = new TL_messageMediaPhoto();
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

    public static class TL_messageMediaPhoto_old extends TL_messageMediaPhoto {
        public static int constructor = 0xc8c45a2a;


        public void readParams(AbsSerializedData stream, boolean exception) {
            photo = Photo.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            photo.serializeToStream(stream);
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

    public static class TL_messageMediaVideo_old extends TL_messageMediaVideo {
        public static int constructor = 0xa2d24290;


        public void readParams(AbsSerializedData stream, boolean exception) {
//            video = Video.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
//            video.serializeToStream(stream);
        }
    }

    public static class TL_messageMediaDocument extends MessageMedia {
        public static int constructor = 0x2fda2204;


        public void readParams(AbsSerializedData stream, boolean exception) {
//            document = Document.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
//            document.serializeToStream(stream);
        }
    }

    public static class TL_messageMediaContact extends MessageMedia {
        public static int constructor = 0x5e7d2f39;


        public void readParams(AbsSerializedData stream, boolean exception) {
            phone_number = stream.readString(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            user_id = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(phone_number);
            stream.writeString(first_name);
            stream.writeString(last_name);
            stream.writeInt32(user_id);
        }
    }

    public static class TL_messageMediaPhoto extends MessageMedia {
        public static int constructor = 0x3d8ce53d;


        public void readParams(AbsSerializedData stream, boolean exception) {
            photo = Photo.TLdeserialize(stream, stream.readInt32(exception), exception);
            caption = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            photo.serializeToStream(stream);
            stream.writeString(caption);
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









    public static class NotifyPeer extends TLObject {
        public Peer peer;

        public static NotifyPeer TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            NotifyPeer result = null;
            switch(constructor) {
                case 0x74d07c60:
                    result = new TL_notifyAll();
                    break;
                case 0xc007cec3:
                    result = new TL_notifyChats();
                    break;
                case 0xb4c83b4c:
                    result = new TL_notifyUsers();
                    break;
                case 0x9fd40bd8:
                    result = new TL_notifyPeer();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in NotifyPeer", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_notifyAll extends NotifyPeer {
        public static int constructor = 0x74d07c60;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_notifyChats extends NotifyPeer {
        public static int constructor = 0xc007cec3;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_notifyUsers extends NotifyPeer {
        public static int constructor = 0xb4c83b4c;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_notifyPeer extends NotifyPeer {
        public static int constructor = 0x9fd40bd8;


        public void readParams(AbsSerializedData stream, boolean exception) {
            peer = Peer.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            peer.serializeToStream(stream);
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





    public static class Update extends TLObject {
        public int chat_id;
        public int max_date;
        public int date;
        public int user_id;
        public ContactLink my_link;
        public ContactLink foreign_link;
        public int max_id;
        public int pts;
        public int pts_count;
        public int version;
//        public WebPage webpage;
        public String type;
        public MessageMedia media;
        public boolean popup;
        public PeerNotifySettings notify_settings;
        public SendMessageAction action;
        public String first_name;
        public String last_name;
        public String username;
        public int qts;
        public int id;
        public long random_id;
        public ArrayList<Integer> messages = new ArrayList<>();
        public ChatParticipants participants;
        public TL_privacyKeyStatusTimestamp key;
        public boolean blocked;
        public String phone;
        public long auth_key_id;
        public String device;
        public String location;
        public UserProfilePhoto photo;
        public boolean previous;
        public int inviter_id;
        public UserStatus status;

        public static Update TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            Update result = null;
            switch(constructor) {
                case 0x38fe25b7:
                    result = new TL_updateEncryptedMessagesRead();
                    break;
                case 0x9d2e67c5:
                    result = new TL_updateContactLink();
                    break;
                case 0x9961fd5c:
                    result = new TL_updateReadHistoryInbox();
                    break;
                case 0x2f2f21bf:
                    result = new TL_updateReadHistoryOutbox();
                    break;
                case 0x6e5f8c22:
                    result = new TL_updateChatParticipantDelete();
                    break;
                case 0x382dd3e4:
                    result = new TL_updateServiceNotification();
                    break;
                case 0xbec268ef:
                    result = new TL_updateNotifySettings();
                    break;
                case 0x5c486927:
                    result = new TL_updateUserTyping();
                    break;
                case 0x9a65ea1f:
                    result = new TL_updateChatUserTyping();
                    break;
                case 0xa7332b73:
                    result = new TL_updateUserName();
                    break;
                case 0x12bcbd9a:
                    result = new TL_updateNewEncryptedMessage();
                    break;
                case 0x1f2b0afd:
                    result = new TL_updateNewMessage();
                    break;
                case 0x4e90bfd6:
                    result = new TL_updateMessageID();
                    break;
                case 0x8e5e9873:
                    result = new TL_updateDcOptions();
                    break;
                case 0x1710f156:
                    result = new TL_updateEncryptedChatTyping();
                    break;
                case 0xa20db0e5:
                    result = new TL_updateDeleteMessages();
                    break;
                case 0x68c13933:
                    result = new TL_updateReadMessagesContents();
                    break;
                case 0x7761198:
                    result = new TL_updateChatParticipants();
                    break;
                case 0xee3b272a:
                    result = new TL_updatePrivacy();
                    break;
                case 0xb4a2e88d:
                    result = new TL_updateEncryption();
                    break;
                case 0x80ece81a:
                    result = new TL_updateUserBlocked();
                    break;
                case 0x12b9417b:
                    result = new TL_updateUserPhone();
                    break;
                case 0x8f06529a:
                    result = new TL_updateNewAuthorization();
                    break;
                case 0x5a68e3f7:
                    result = new TL_updateNewGeoChatMessage();
                    break;
                case 0x95313b0c:
                    result = new TL_updateUserPhoto();
                    break;
                case 0x2575bbb9:
                    result = new TL_updateContactRegistered();
                    break;
                case 0x3a0eeb22:
                    result = new TL_updateChatParticipantAdd();
                    break;
                case 0x1bfbd823:
                    result = new TL_updateUserStatus();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in Update", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_updateEncryptedMessagesRead extends Update {
        public static int constructor = 0x38fe25b7;


        public void readParams(AbsSerializedData stream, boolean exception) {
            chat_id = stream.readInt32(exception);
            max_date = stream.readInt32(exception);
            date = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(chat_id);
            stream.writeInt32(max_date);
            stream.writeInt32(date);
        }
    }

    public static class TL_updateContactLink extends Update {
        public static int constructor = 0x9d2e67c5;


        public void readParams(AbsSerializedData stream, boolean exception) {
            user_id = stream.readInt32(exception);
            my_link = ContactLink.TLdeserialize(stream, stream.readInt32(exception), exception);
            foreign_link = ContactLink.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(user_id);
            my_link.serializeToStream(stream);
            foreign_link.serializeToStream(stream);
        }
    }

    public static class TL_updateReadHistoryInbox extends Update {
        public static int constructor = 0x9961fd5c;

        public Peer peer;

        public void readParams(AbsSerializedData stream, boolean exception) {
            peer = Peer.TLdeserialize(stream, stream.readInt32(exception), exception);
            max_id = stream.readInt32(exception);
            pts = stream.readInt32(exception);
            pts_count = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            peer.serializeToStream(stream);
            stream.writeInt32(max_id);
            stream.writeInt32(pts);
            stream.writeInt32(pts_count);
        }
    }

    public static class TL_updateReadHistoryOutbox extends Update {
        public static int constructor = 0x2f2f21bf;

        public Peer peer;

        public void readParams(AbsSerializedData stream, boolean exception) {
            peer = Peer.TLdeserialize(stream, stream.readInt32(exception), exception);
            max_id = stream.readInt32(exception);
            pts = stream.readInt32(exception);
            pts_count = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            peer.serializeToStream(stream);
            stream.writeInt32(max_id);
            stream.writeInt32(pts);
            stream.writeInt32(pts_count);
        }
    }

    public static class TL_updateChatParticipantDelete extends Update {
        public static int constructor = 0x6e5f8c22;


        public void readParams(AbsSerializedData stream, boolean exception) {
            chat_id = stream.readInt32(exception);
            user_id = stream.readInt32(exception);
            version = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(chat_id);
            stream.writeInt32(user_id);
            stream.writeInt32(version);
        }
    }



    public static class TL_updateServiceNotification extends Update {
        public static int constructor = 0x382dd3e4;

        public String message;

        public void readParams(AbsSerializedData stream, boolean exception) {
            type = stream.readString(exception);
            message = stream.readString(exception);
            media = MessageMedia.TLdeserialize(stream, stream.readInt32(exception), exception);
            popup = stream.readBool(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(type);
            stream.writeString(message);
            media.serializeToStream(stream);
            stream.writeBool(popup);
        }
    }

    public static class TL_updateNotifySettings extends Update {
        public static int constructor = 0xbec268ef;

        public NotifyPeer peer;

        public void readParams(AbsSerializedData stream, boolean exception) {
            peer = NotifyPeer.TLdeserialize(stream, stream.readInt32(exception), exception);
            notify_settings = PeerNotifySettings.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            peer.serializeToStream(stream);
            notify_settings.serializeToStream(stream);
        }
    }

    public static class TL_updateUserTyping extends Update {
        public static int constructor = 0x5c486927;


        public void readParams(AbsSerializedData stream, boolean exception) {
            user_id = stream.readInt32(exception);
            action = SendMessageAction.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(user_id);
            action.serializeToStream(stream);
        }
    }

    public static class TL_updateChatUserTyping extends Update {
        public static int constructor = 0x9a65ea1f;


        public void readParams(AbsSerializedData stream, boolean exception) {
            chat_id = stream.readInt32(exception);
            user_id = stream.readInt32(exception);
            action = SendMessageAction.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(chat_id);
            stream.writeInt32(user_id);
            action.serializeToStream(stream);
        }
    }

    public static class TL_updateUserName extends Update {
        public static int constructor = 0xa7332b73;


        public void readParams(AbsSerializedData stream, boolean exception) {
            user_id = stream.readInt32(exception);
            first_name = stream.readString(exception);
            last_name = stream.readString(exception);
            username = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(user_id);
            stream.writeString(first_name);
            stream.writeString(last_name);
            stream.writeString(username);
        }
    }

    public static class TL_updateNewEncryptedMessage extends Update {
        public static int constructor = 0x12bcbd9a;

        public EncryptedMessage message;

        public void readParams(AbsSerializedData stream, boolean exception) {
            message = EncryptedMessage.TLdeserialize(stream, stream.readInt32(exception), exception);
            qts = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            message.serializeToStream(stream);
            stream.writeInt32(qts);
        }
    }

    public static class TL_updateNewMessage extends Update {
        public static int constructor = 0x1f2b0afd;

        public Message message;

        public void readParams(AbsSerializedData stream, boolean exception) {
            message = Message.TLdeserialize(stream, stream.readInt32(exception), exception);
            pts = stream.readInt32(exception);
            pts_count = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            message.serializeToStream(stream);
            stream.writeInt32(pts);
            stream.writeInt32(pts_count);
        }
    }

    public static class TL_updateMessageID extends Update {
        public static int constructor = 0x4e90bfd6;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            random_id = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeInt64(random_id);
        }
    }

    public static class TL_updateDcOptions extends Update {
        public static int constructor = 0x8e5e9873;


        public void readParams(AbsSerializedData stream, boolean exception) {
            int magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }

        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(0x1cb5c415);

        }
    }

    public static class TL_updateEncryptedChatTyping extends Update {
        public static int constructor = 0x1710f156;


        public void readParams(AbsSerializedData stream, boolean exception) {
            chat_id = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(chat_id);
        }
    }

    public static class TL_updateDeleteMessages extends Update {
        public static int constructor = 0xa20db0e5;


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
                messages.add(stream.readInt32(exception));
            }
            pts = stream.readInt32(exception);
            pts_count = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(0x1cb5c415);
            int count = messages.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                stream.writeInt32(messages.get(a));
            }
            stream.writeInt32(pts);
            stream.writeInt32(pts_count);
        }
    }

    public static class TL_updateReadMessagesContents extends Update {
        public static int constructor = 0x68c13933;


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
                messages.add(stream.readInt32(exception));
            }
            pts = stream.readInt32(exception);
            pts_count = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(0x1cb5c415);
            int count = messages.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                stream.writeInt32(messages.get(a));
            }
            stream.writeInt32(pts);
            stream.writeInt32(pts_count);
        }
    }

    public static class TL_updateChatParticipants extends Update {
        public static int constructor = 0x7761198;


        public void readParams(AbsSerializedData stream, boolean exception) {
            participants = ChatParticipants.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            participants.serializeToStream(stream);
        }
    }

    public static class TL_updatePrivacy extends Update {
        public static int constructor = 0xee3b272a;


        public void readParams(AbsSerializedData stream, boolean exception) {
            key = TL_privacyKeyStatusTimestamp.TLdeserialize(stream, stream.readInt32(exception), exception);
            int magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
//                PrivacyRule object = PrivacyRule.TLdeserialize(stream, stream.readInt32(exception), exception);
//                if (object == null) {
//                    return;
//                }
//                rules.add(object);
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            key.serializeToStream(stream);
            stream.writeInt32(0x1cb5c415);
//            int count = rules.size();
//            stream.writeInt32(count);
//            for (int a = 0; a < count; a++) {
//                rules.get(a).serializeToStream(stream);
//            }
        }
    }

    public static class TL_updateEncryption extends Update {
        public static int constructor = 0xb4a2e88d;


        public void readParams(AbsSerializedData stream, boolean exception) {
//            chat = EncryptedChat.TLdeserialize(stream, stream.readInt32(exception), exception);
            date = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
//            chat.serializeToStream(stream);
            stream.writeInt32(date);
        }
    }

    public static class TL_updateUserBlocked extends Update {
        public static int constructor = 0x80ece81a;


        public void readParams(AbsSerializedData stream, boolean exception) {
            user_id = stream.readInt32(exception);
            blocked = stream.readBool(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(user_id);
            stream.writeBool(blocked);
        }
    }

    public static class TL_updateUserPhone extends Update {
        public static int constructor = 0x12b9417b;


        public void readParams(AbsSerializedData stream, boolean exception) {
            user_id = stream.readInt32(exception);
            phone = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(user_id);
            stream.writeString(phone);
        }
    }

    public static class TL_updateNewAuthorization extends Update {
        public static int constructor = 0x8f06529a;


        public void readParams(AbsSerializedData stream, boolean exception) {
            auth_key_id = stream.readInt64(exception);
            date = stream.readInt32(exception);
            device = stream.readString(exception);
            location = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(auth_key_id);
            stream.writeInt32(date);
            stream.writeString(device);
            stream.writeString(location);
        }
    }

    public static class TL_updateNewGeoChatMessage extends Update {
        public static int constructor = 0x5a68e3f7;

        public GeoChatMessage message;

        public void readParams(AbsSerializedData stream, boolean exception) {
            message = GeoChatMessage.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            message.serializeToStream(stream);
        }
    }

    public static class TL_updateUserPhoto extends Update {
        public static int constructor = 0x95313b0c;


        public void readParams(AbsSerializedData stream, boolean exception) {
            user_id = stream.readInt32(exception);
            date = stream.readInt32(exception);
            photo = UserProfilePhoto.TLdeserialize(stream, stream.readInt32(exception), exception);
            previous = stream.readBool(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(user_id);
            stream.writeInt32(date);
            photo.serializeToStream(stream);
            stream.writeBool(previous);
        }
    }

    public static class TL_updateContactRegistered extends Update {
        public static int constructor = 0x2575bbb9;


        public void readParams(AbsSerializedData stream, boolean exception) {
            user_id = stream.readInt32(exception);
            date = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(user_id);
            stream.writeInt32(date);
        }
    }

    public static class TL_updateChatParticipantAdd extends Update {
        public static int constructor = 0x3a0eeb22;


        public void readParams(AbsSerializedData stream, boolean exception) {
            chat_id = stream.readInt32(exception);
            user_id = stream.readInt32(exception);
            inviter_id = stream.readInt32(exception);
            version = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(chat_id);
            stream.writeInt32(user_id);
            stream.writeInt32(inviter_id);
            stream.writeInt32(version);
        }
    }

    public static class TL_updateUserStatus extends Update {
        public static int constructor = 0x1bfbd823;


        public void readParams(AbsSerializedData stream, boolean exception) {
            user_id = stream.readInt32(exception);
            status = UserStatus.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(user_id);
            status.serializeToStream(stream);
        }
    }







    public static class PeerNotifySettings extends TLObject {
        public int mute_until;
        public String sound;
        public boolean show_previews;
        public int events_mask;

        public static PeerNotifySettings TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            PeerNotifySettings result = null;
            switch(constructor) {
                case 0x70a68512:
                    result = new TL_peerNotifySettingsEmpty();
                    break;
                case 0x8d5e11ee:
                    result = new TL_peerNotifySettings();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in PeerNotifySettings", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_peerNotifySettingsEmpty extends PeerNotifySettings {
        public static int constructor = 0x70a68512;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_peerNotifySettings extends PeerNotifySettings {
        public static int constructor = 0x8d5e11ee;


        public void readParams(AbsSerializedData stream, boolean exception) {
            mute_until = stream.readInt32(exception);
            sound = stream.readString(exception);
            show_previews = stream.readBool(exception);
            events_mask = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(mute_until);
            stream.writeString(sound);
            stream.writeBool(show_previews);
            stream.writeInt32(events_mask);
        }
    }

    public static class GeoChatMessage extends TLObject {
        public int chat_id;
        public int id;
        public int from_id;
        public int date;
        public String message;
        public MessageMedia media;
        public MessageAction action;

        public static GeoChatMessage TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            GeoChatMessage result = null;
            switch(constructor) {
                case 0x4505f8e1:
                    result = new TL_geoChatMessage();
                    break;
                case 0xd34fa24e:
                    result = new TL_geoChatMessageService();
                    break;
                case 0x60311a9b:
                    result = new TL_geoChatMessageEmpty();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in GeoChatMessage", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_geoChatMessage extends GeoChatMessage {
        public static int constructor = 0x4505f8e1;


        public void readParams(AbsSerializedData stream, boolean exception) {
            chat_id = stream.readInt32(exception);
            id = stream.readInt32(exception);
            from_id = stream.readInt32(exception);
            date = stream.readInt32(exception);
            message = stream.readString(exception);
            media = MessageMedia.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(chat_id);
            stream.writeInt32(id);
            stream.writeInt32(from_id);
            stream.writeInt32(date);
            stream.writeString(message);
            media.serializeToStream(stream);
        }
    }

    public static class TL_geoChatMessageService extends GeoChatMessage {
        public static int constructor = 0xd34fa24e;


        public void readParams(AbsSerializedData stream, boolean exception) {
            chat_id = stream.readInt32(exception);
            id = stream.readInt32(exception);
            from_id = stream.readInt32(exception);
            date = stream.readInt32(exception);
            action = MessageAction.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(chat_id);
            stream.writeInt32(id);
            stream.writeInt32(from_id);
            stream.writeInt32(date);
            action.serializeToStream(stream);
        }
    }

    public static class TL_geoChatMessageEmpty extends GeoChatMessage {
        public static int constructor = 0x60311a9b;


        public void readParams(AbsSerializedData stream, boolean exception) {
            chat_id = stream.readInt32(exception);
            id = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(chat_id);
            stream.writeInt32(id);
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





    public static class TL_chatParticipant extends TLObject {
        public static int constructor = 0xc8d7493e;

        public int user_id;
        public int inviter_id;
        public int date;

        public static TL_chatParticipant TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            if (TL_chatParticipant.constructor != constructor) {
                if (exception) {
                    throw new RuntimeException(String.format("can't parse magic %x in TL_chatParticipant", constructor));
                } else {
                    return null;
                }
            }
            TL_chatParticipant result = new TL_chatParticipant();
            result.readParams(stream, exception);
            return result;
        }

        public void readParams(AbsSerializedData stream, boolean exception) {
            user_id = stream.readInt32(exception);
            inviter_id = stream.readInt32(exception);
            date = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(user_id);
            stream.writeInt32(inviter_id);
            stream.writeInt32(date);
        }
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

        public static Photo TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            Photo result = null;
            switch(constructor) {
                case 0x2331b22d:
                    result = new TL_photoEmpty();
                    break;
                case 0xc3838076:
                    result = new TL_photo();
                    break;
                case 0x22b56751:
                    result = new TL_photo_old();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in Photo", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
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







    public static class EncryptedMessage extends TLObject {
        public long random_id;
        public int chat_id;
        public int date;
        public byte[] bytes;
        public EncryptedFile file;

        public static EncryptedMessage TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            EncryptedMessage result = null;
            switch(constructor) {
                case 0x23734b06:
                    result = new TL_encryptedMessageService();
                    break;
                case 0xed18c118:
                    result = new TL_encryptedMessage();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in EncryptedMessage", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_encryptedMessageService extends EncryptedMessage {
        public static int constructor = 0x23734b06;


        public void readParams(AbsSerializedData stream, boolean exception) {
            random_id = stream.readInt64(exception);
            chat_id = stream.readInt32(exception);
            date = stream.readInt32(exception);
            bytes = stream.readByteArray(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(random_id);
            stream.writeInt32(chat_id);
            stream.writeInt32(date);
            stream.writeByteArray(bytes);
        }
    }

    public static class TL_encryptedMessage extends EncryptedMessage {
        public static int constructor = 0xed18c118;


        public void readParams(AbsSerializedData stream, boolean exception) {
            random_id = stream.readInt64(exception);
            chat_id = stream.readInt32(exception);
            date = stream.readInt32(exception);
            bytes = stream.readByteArray(exception);
            file = EncryptedFile.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(random_id);
            stream.writeInt32(chat_id);
            stream.writeInt32(date);
            stream.writeByteArray(bytes);
            file.serializeToStream(stream);
        }
    }


    public static class DestroySessionRes extends TLObject {
        public long session_id;

        public static DestroySessionRes TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            DestroySessionRes result = null;
            switch(constructor) {
                case 0xe22045fc:
                    result = new TL_destroy_session_ok();
                    break;
                case 0x62d350c9:
                    result = new TL_destroy_session_none();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in DestroySessionRes", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_destroy_session_ok extends DestroySessionRes {
        public static int constructor = 0xe22045fc;


        public void readParams(AbsSerializedData stream, boolean exception) {
            session_id = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(session_id);
        }
    }

    public static class TL_destroy_session_none extends DestroySessionRes {
        public static int constructor = 0x62d350c9;


        public void readParams(AbsSerializedData stream, boolean exception) {
            session_id = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(session_id);
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

        public static MessageAction TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            MessageAction result = null;
            switch(constructor) {
                case 0xc7d53de:
                    result = new TL_messageActionGeoChatCheckin();
                    break;
                case 0x55555557:
                    result = new TL_messageActionCreatedBroadcastList();
                    break;
                case 0xb5a1ce5a:
                    result = new TL_messageActionChatEditTitle();
                    break;
                case 0x555555F5:
                    result = new TL_messageActionLoginUnknownLocation();
                    break;
                case 0x5e3cfc4b:
                    result = new TL_messageActionChatAddUser();
                    break;
                case 0xf89cf5e8:
                    result = new TL_messageActionChatJoinedByLink();
                    break;
                case 0x55555550:
                    result = new TL_messageActionUserJoined();
                    break;
                case 0x555555F7:
                    result = new TL_messageEncryptedAction();
                    break;
                case 0x55555552:
                    result = new TL_messageActionTTLChange();
                    break;
                case 0x55555551:
                    result = new TL_messageActionUserUpdatedPhoto();
                    break;
                case 0xb6aef7b0:
                    result = new TL_messageActionEmpty();
                    break;
                case 0x95e3fbef:
                    result = new TL_messageActionChatDeletePhoto();
                    break;
                case 0xb2ae9b0c:
                    result = new TL_messageActionChatDeleteUser();
                    break;
                case 0x7fcb13a8:
                    result = new TL_messageActionChatEditPhoto();
                    break;
                case 0xa6638b9a:
                    result = new TL_messageActionChatCreate();
                    break;
                case 0x6f038ebc:
                    result = new TL_messageActionGeoChatCreate();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in MessageAction", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_messageActionGeoChatCheckin extends MessageAction {
        public static int constructor = 0xc7d53de;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_messageActionCreatedBroadcastList extends MessageAction {
        public static int constructor = 0x55555557;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_messageActionChatEditTitle extends MessageAction {
        public static int constructor = 0xb5a1ce5a;


        public void readParams(AbsSerializedData stream, boolean exception) {
            title = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(title);
        }
    }

    public static class TL_messageActionLoginUnknownLocation extends MessageAction {
        public static int constructor = 0x555555F5;


        public void readParams(AbsSerializedData stream, boolean exception) {
            title = stream.readString(exception);
            address = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(title);
            stream.writeString(address);
        }
    }

    public static class TL_messageActionChatAddUser extends MessageAction {
        public static int constructor = 0x5e3cfc4b;


        public void readParams(AbsSerializedData stream, boolean exception) {
            user_id = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(user_id);
        }
    }

    public static class TL_messageActionChatJoinedByLink extends MessageAction {
        public static int constructor = 0xf89cf5e8;


        public void readParams(AbsSerializedData stream, boolean exception) {
            inviter_id = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(inviter_id);
        }
    }

    public static class TL_messageActionUserJoined extends MessageAction {
        public static int constructor = 0x55555550;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
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

    public static class TL_messageActionTTLChange extends MessageAction {
        public static int constructor = 0x55555552;


        public void readParams(AbsSerializedData stream, boolean exception) {
            ttl = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(ttl);
        }
    }

    public static class TL_messageActionUserUpdatedPhoto extends MessageAction {
        public static int constructor = 0x55555551;


        public void readParams(AbsSerializedData stream, boolean exception) {
            newUserPhoto = UserProfilePhoto.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            newUserPhoto.serializeToStream(stream);
        }
    }

    public static class TL_messageActionEmpty extends MessageAction {
        public static int constructor = 0xb6aef7b0;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_messageActionChatDeletePhoto extends MessageAction {
        public static int constructor = 0x95e3fbef;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_messageActionChatDeleteUser extends MessageAction {
        public static int constructor = 0xb2ae9b0c;


        public void readParams(AbsSerializedData stream, boolean exception) {
            user_id = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(user_id);
        }
    }

    public static class TL_messageActionChatEditPhoto extends MessageAction {
        public static int constructor = 0x7fcb13a8;


        public void readParams(AbsSerializedData stream, boolean exception) {
            photo = Photo.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            photo.serializeToStream(stream);
        }
    }

    public static class TL_messageActionChatCreate extends MessageAction {
        public static int constructor = 0xa6638b9a;


        public void readParams(AbsSerializedData stream, boolean exception) {
            title = stream.readString(exception);
            int magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                users.add(stream.readInt32(exception));
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(title);
            stream.writeInt32(0x1cb5c415);
            int count = users.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                stream.writeInt32(users.get(a));
            }
        }
    }

    public static class TL_messageActionGeoChatCreate extends MessageAction {
        public static int constructor = 0x6f038ebc;


        public void readParams(AbsSerializedData stream, boolean exception) {
            title = stream.readString(exception);
            address = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(title);
            stream.writeString(address);
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






    public static class InputStickerSet extends TLObject {
        public long id;
        public long access_hash;
        public String short_name;


    }

    public static class TL_inputStickerSetEmpty extends InputStickerSet {
        public static int constructor = 0xffb62b95;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_inputStickerSetID extends InputStickerSet {
        public static int constructor = 0x9de7a269;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt64(exception);
            access_hash = stream.readInt64(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt64(id);
            stream.writeInt64(access_hash);
        }
    }

    public static class TL_inputStickerSetShortName extends InputStickerSet {
        public static int constructor = 0x861cc8a0;


        public void readParams(AbsSerializedData stream, boolean exception) {
            short_name = stream.readString(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeString(short_name);
        }
    }






    public static class TL_p_q_inner_data extends TLObject {
        public static int constructor = 0x83c95aec;

        public byte[] pq;
        public byte[] p;
        public byte[] q;
        public byte[] nonce;
        public byte[] server_nonce;
        public byte[] new_nonce;



        public void readParams(AbsSerializedData stream, boolean exception) {
            pq = stream.readByteArray(exception);
            p = stream.readByteArray(exception);
            q = stream.readByteArray(exception);
            nonce = stream.readData(16, exception);
            server_nonce = stream.readData(16, exception);
            new_nonce = stream.readData(32, exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeByteArray(pq);
            stream.writeByteArray(p);
            stream.writeByteArray(q);
            stream.writeRaw(nonce);
            stream.writeRaw(server_nonce);
            stream.writeRaw(new_nonce);
        }
    }







    public static class Updates extends TLObject {
        public int flags;
        public int id;
        public int from_id;
        public int chat_id;
        public String message;
        public int pts;
        public int pts_count;
        public int date;
        public int fwd_from_id;
        public int fwd_date;
        public int reply_to_msg_id;
        public ArrayList<Update> updates = new ArrayList<>();
        public ArrayList<User> users = new ArrayList<>();
        public ArrayList<Chat> chats = new ArrayList<>();
        public int seq;
        public int user_id;
        public Update update;
        public int seq_start;


    }

    public static class TL_updateShortChatMessage extends Updates {
        public static int constructor = 0x52238b3c;


        public void readParams(AbsSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            id = stream.readInt32(exception);
            from_id = stream.readInt32(exception);
            chat_id = stream.readInt32(exception);
            message = stream.readString(exception);
            pts = stream.readInt32(exception);
            pts_count = stream.readInt32(exception);
            date = stream.readInt32(exception);
            if ((flags & 4) != 0) {
                fwd_from_id = stream.readInt32(exception);
            }
            if ((flags & 4) != 0) {
                fwd_date = stream.readInt32(exception);
            }
            if ((flags & 8) != 0) {
                reply_to_msg_id = stream.readInt32(exception);
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeInt32(id);
            stream.writeInt32(from_id);
            stream.writeInt32(chat_id);
            stream.writeString(message);
            stream.writeInt32(pts);
            stream.writeInt32(pts_count);
            stream.writeInt32(date);
            if ((flags & 4) != 0) {
                stream.writeInt32(fwd_from_id);
            }
            if ((flags & 4) != 0) {
                stream.writeInt32(fwd_date);
            }
            if ((flags & 8) != 0) {
                stream.writeInt32(reply_to_msg_id);
            }
        }
    }

    public static class TL_updates extends Updates {
        public static int constructor = 0x74ae4240;


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
                Update object = Update.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                updates.add(object);
            }
            magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                User object = User.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                users.add(object);
            }
            magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                Chat object = Chat.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                chats.add(object);
            }
            date = stream.readInt32(exception);
            seq = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(0x1cb5c415);
            int count = updates.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                updates.get(a).serializeToStream(stream);
            }
            stream.writeInt32(0x1cb5c415);
            count = users.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                users.get(a).serializeToStream(stream);
            }
            stream.writeInt32(0x1cb5c415);
            count = chats.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                chats.get(a).serializeToStream(stream);
            }
            stream.writeInt32(date);
            stream.writeInt32(seq);
        }
    }

    public static class TL_updateShortMessage extends Updates {
        public static int constructor = 0xed5c2127;


        public void readParams(AbsSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            id = stream.readInt32(exception);
            user_id = stream.readInt32(exception);
            message = stream.readString(exception);
            pts = stream.readInt32(exception);
            pts_count = stream.readInt32(exception);
            date = stream.readInt32(exception);
            if ((flags & 4) != 0) {
                fwd_from_id = stream.readInt32(exception);
            }
            if ((flags & 4) != 0) {
                fwd_date = stream.readInt32(exception);
            }
            if ((flags & 8) != 0) {
                reply_to_msg_id = stream.readInt32(exception);
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeInt32(id);
            stream.writeInt32(user_id);
            stream.writeString(message);
            stream.writeInt32(pts);
            stream.writeInt32(pts_count);
            stream.writeInt32(date);
            if ((flags & 4) != 0) {
                stream.writeInt32(fwd_from_id);
            }
            if ((flags & 4) != 0) {
                stream.writeInt32(fwd_date);
            }
            if ((flags & 8) != 0) {
                stream.writeInt32(reply_to_msg_id);
            }
        }
    }

    public static class TL_updateShort extends Updates {
        public static int constructor = 0x78d4dec1;


        public void readParams(AbsSerializedData stream, boolean exception) {
            update = Update.TLdeserialize(stream, stream.readInt32(exception), exception);
            date = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            update.serializeToStream(stream);
            stream.writeInt32(date);
        }
    }

    public static class TL_updatesCombined extends Updates {
        public static int constructor = 0x725b04c3;


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
                Update object = Update.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                updates.add(object);
            }
            magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                User object = User.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                users.add(object);
            }
            magic = stream.readInt32(exception);
            if (magic != 0x1cb5c415) {
                if (exception) {
                    throw new RuntimeException(String.format("wrong Vector magic, got %x", magic));
                }
                return;
            }
            count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                Chat object = Chat.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                chats.add(object);
            }
            date = stream.readInt32(exception);
            seq_start = stream.readInt32(exception);
            seq = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(0x1cb5c415);
            int count = updates.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                updates.get(a).serializeToStream(stream);
            }
            stream.writeInt32(0x1cb5c415);
            count = users.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                users.get(a).serializeToStream(stream);
            }
            stream.writeInt32(0x1cb5c415);
            count = chats.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                chats.get(a).serializeToStream(stream);
            }
            stream.writeInt32(date);
            stream.writeInt32(seq_start);
            stream.writeInt32(seq);
        }
    }

    public static class TL_updatesTooLong extends Updates {
        public static int constructor = 0xe317af7e;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
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









    public static class ChatPhoto extends TLObject {
        public FileLocation photo_small;
        public FileLocation photo_big;

        public static ChatPhoto TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            ChatPhoto result = null;
            switch(constructor) {
                case 0x37c1011c:
                    result = new TL_chatPhotoEmpty();
                    break;
                case 0x6153276a:
                    result = new TL_chatPhoto();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in ChatPhoto", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_chatPhotoEmpty extends ChatPhoto {
        public static int constructor = 0x37c1011c;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_chatPhoto extends ChatPhoto {
        public static int constructor = 0x6153276a;


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

    public static class TL_decryptedMessageLayer extends TLObject {
        public static int constructor = 0x1be31789;

        public byte[] random_bytes;
        public int layer;
        public int in_seq_no;
        public int out_seq_no;
        public DecryptedMessage message;

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
            message = DecryptedMessage.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeByteArray(random_bytes);
            stream.writeInt32(layer);
            stream.writeInt32(in_seq_no);
            stream.writeInt32(out_seq_no);
            message.serializeToStream(stream);
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






    public static class Chat extends TLObject {
        public int id;
        public String title;
        public int date;
        public long access_hash;
        public String address;
        public String venue;
        public GeoPoint geo;
        public ChatPhoto photo;
        public int participants_count;
        public boolean checked_in;
        public int version;
        public boolean left;

        public static Chat TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            Chat result = null;
            switch(constructor) {
                case 0xfb0ccc41:
                    result = new TL_chatForbidden();
                    break;
                case 0x75eaea5a:
                    result = new TL_geoChat();
                    break;
                case 0x9ba2d800:
                    result = new TL_chatEmpty();
                    break;
                case 0x6e9c9bc7:
                    result = new TL_chat();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in Chat", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_chatForbidden extends Chat {
        public static int constructor = 0xfb0ccc41;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            title = stream.readString(exception);
            date = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeString(title);
            stream.writeInt32(date);
        }
    }

    public static class TL_geoChat extends Chat {
        public static int constructor = 0x75eaea5a;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            access_hash = stream.readInt64(exception);
            title = stream.readString(exception);
            address = stream.readString(exception);
            venue = stream.readString(exception);
            geo = GeoPoint.TLdeserialize(stream, stream.readInt32(exception), exception);
            photo = ChatPhoto.TLdeserialize(stream, stream.readInt32(exception), exception);
            participants_count = stream.readInt32(exception);
            date = stream.readInt32(exception);
            checked_in = stream.readBool(exception);
            version = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeInt64(access_hash);
            stream.writeString(title);
            stream.writeString(address);
            stream.writeString(venue);
            geo.serializeToStream(stream);
            photo.serializeToStream(stream);
            stream.writeInt32(participants_count);
            stream.writeInt32(date);
            stream.writeBool(checked_in);
            stream.writeInt32(version);
        }
    }

    public static class TL_chat extends Chat {
        public static int constructor = 0x6e9c9bc7;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            title = stream.readString(exception);
            photo = ChatPhoto.TLdeserialize(stream, stream.readInt32(exception), exception);
            participants_count = stream.readInt32(exception);
            date = stream.readInt32(exception);
            left = stream.readBool(exception);
            version = stream.readInt32(exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeString(title);
            photo.serializeToStream(stream);
            stream.writeInt32(participants_count);
            stream.writeInt32(date);
            stream.writeBool(left);
            stream.writeInt32(version);
        }
    }



    public static class ContactLink extends TLObject {

        public static ContactLink TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            ContactLink result = null;
            switch(constructor) {
                case 0xfeedd3ad:
                    result = new TL_contactLinkNone();
                    break;
                case 0xd502c2d0:
                    result = new TL_contactLinkContact();
                    break;
                case 0x268f3f59:
                    result = new TL_contactLinkHasPhone();
                    break;
                case 0x5f4f9247:
                    result = new TL_contactLinkUnknown();
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in ContactLink", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_contactLinkNone extends ContactLink {
        public static int constructor = 0xfeedd3ad;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_contactLinkContact extends ContactLink {
        public static int constructor = 0xd502c2d0;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_contactLinkHasPhone extends ContactLink {
        public static int constructor = 0x268f3f59;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
        }
    }

    public static class TL_contactLinkUnknown extends ContactLink {
        public static int constructor = 0x5f4f9247;


        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
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















    public static class TL_destroy_sessions_res extends TLObject {
        public static int constructor = 0xfb95abcd;

        public ArrayList<DestroySessionRes> destroy_results = new ArrayList<>();



        public void readParams(AbsSerializedData stream, boolean exception) {
            int count = stream.readInt32(exception);
            for (int a = 0; a < count; a++) {
                DestroySessionRes object = DestroySessionRes.TLdeserialize(stream, stream.readInt32(exception), exception);
                if (object == null) {
                    return;
                }
                destroy_results.add(object);
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            int count = destroy_results.size();
            stream.writeInt32(count);
            for (int a = 0; a < count; a++) {
                destroy_results.get(a).serializeToStream(stream);
            }
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

        public static Message TLdeserialize(AbsSerializedData stream, int constructor, boolean exception) {
            Message result = null;
            switch(constructor) {
                case 0x1d86f70e:
                    result = new TL_messageService();
                    break;
                case 0xa7ab1991:
                    result = new TL_message();
                    break;
                case 0x83e5de54:
                    result = new TL_messageEmpty();
                    break;
                case 0xa367e716:
                    result = new TL_messageForwarded_old2(); //custom
                    break;
                case 0x5f46804:
                    result = new TL_messageForwarded_old(); //custom
                    break;
                case 0x567699b3:
                    result = new TL_message_old2(); //custom
                    break;
                case 0x9f8d60bb:
                    result = new TL_messageService_old(); //custom
                    break;
                case 0x22eb6aba:
                    result = new TL_message_old(); //custom
                    break;
                case 0x555555F8:
                    result = new TL_message_secret(); //custom
                    break;
            }
            if (result == null && exception) {
                throw new RuntimeException(String.format("can't parse magic %x in Message", constructor));
            }
            if (result != null) {
                result.readParams(stream, exception);
            }
            return result;
        }
    }

    public static class TL_messageForwarded_old2 extends Message {
        public static int constructor = 0xa367e716;


        public void readParams(AbsSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            id = stream.readInt32(exception);
            fwd_from_id = stream.readInt32(exception);
            fwd_date = stream.readInt32(exception);
            from_id = stream.readInt32(exception);
            to_id = Peer.TLdeserialize(stream, stream.readInt32(exception), exception);
            date = stream.readInt32(exception);
            message = stream.readString(exception);
            flags |= MESSAGE_FLAG_FWD;
            media = MessageMedia.TLdeserialize(stream, stream.readInt32(exception), exception);
            if (id < 0) {
                fwd_msg_id = stream.readInt32(exception);
            }
            if (id < 0 || (media != null && !(media instanceof TL_messageMediaEmpty) && message != null && message.length() != 0 && message.startsWith("-1"))) {
                attachPath = stream.readString(exception);
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(flags);
            stream.writeInt32(id);
            stream.writeInt32(fwd_from_id);
            stream.writeInt32(fwd_date);
            stream.writeInt32(from_id);
            to_id.serializeToStream(stream);
            stream.writeInt32(date);
            stream.writeString(message);
            media.serializeToStream(stream);
            if (id < 0) {
                stream.writeInt32(fwd_msg_id);
            }
            stream.writeString(attachPath);
        }
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

    public static class TL_message_old2 extends TL_message {
        public static int constructor = 0x567699b3;


        public void readParams(AbsSerializedData stream, boolean exception) {
            flags = stream.readInt32(exception);
            id = stream.readInt32(exception);
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
            stream.writeInt32(from_id);
            to_id.serializeToStream(stream);
            stream.writeInt32(date);
            stream.writeString(message);
            media.serializeToStream(stream);
            stream.writeString(attachPath);
        }
    }

    public static class TL_messageService_old extends TL_messageService {
        public static int constructor = 0x9f8d60bb;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            from_id = stream.readInt32(exception);
            to_id = Peer.TLdeserialize(stream, stream.readInt32(exception), exception);
            flags |= stream.readBool(exception) ? MESSAGE_FLAG_OUT : 0;
            flags |= stream.readBool(exception) ? MESSAGE_FLAG_UNREAD : 0;
            date = stream.readInt32(exception);
            action = MessageAction.TLdeserialize(stream, stream.readInt32(exception), exception);
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeInt32(from_id);
            to_id.serializeToStream(stream);
            stream.writeBool((flags & MESSAGE_FLAG_OUT) != 0);
            stream.writeBool((flags & MESSAGE_FLAG_UNREAD) != 0);
            stream.writeInt32(date);
            action.serializeToStream(stream);
        }
    }

    public static class TL_messageForwarded_old extends TL_messageForwarded_old2 {
        public static int constructor = 0x5f46804;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            fwd_from_id = stream.readInt32(exception);
            fwd_date = stream.readInt32(exception);
            from_id = stream.readInt32(exception);
            to_id = Peer.TLdeserialize(stream, stream.readInt32(exception), exception);
            flags |= stream.readBool(exception) ? MESSAGE_FLAG_OUT : 0;
            flags |= stream.readBool(exception) ? MESSAGE_FLAG_UNREAD : 0;
            flags |= MESSAGE_FLAG_FWD;
            date = stream.readInt32(exception);
            message = stream.readString(exception);
            media = MessageMedia.TLdeserialize(stream, stream.readInt32(exception), exception);
            if (id < 0) {
                fwd_msg_id = stream.readInt32(exception);
            }
            if (id < 0 || (media != null && !(media instanceof TL_messageMediaEmpty) && message != null && message.length() != 0 && message.startsWith("-1"))) {
                attachPath = stream.readString(exception);
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeInt32(fwd_from_id);
            stream.writeInt32(fwd_date);
            stream.writeInt32(from_id);
            to_id.serializeToStream(stream);
            stream.writeBool((flags & MESSAGE_FLAG_OUT) != 0);
            stream.writeBool((flags & MESSAGE_FLAG_UNREAD) != 0);
            stream.writeInt32(date);
            stream.writeString(message);
            media.serializeToStream(stream);
            if (id < 0) {
                stream.writeInt32(fwd_msg_id);
            }
            stream.writeString(attachPath);
        }
    }

    public static class TL_message_old extends TL_message {
        public static int constructor = 0x22eb6aba;

        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);
            from_id = stream.readInt32(exception);
            to_id = Peer.TLdeserialize(stream, stream.readInt32(exception), exception);
            flags |= stream.readBool(exception) ? MESSAGE_FLAG_OUT : 0;
            flags |= stream.readBool(exception) ? MESSAGE_FLAG_UNREAD : 0;
            date = stream.readInt32(exception);
            message = stream.readString(exception);
            media = MessageMedia.TLdeserialize(stream, stream.readInt32(exception), exception);
            if (id < 0 || (media != null && !(media instanceof TL_messageMediaEmpty) && message != null && message.length() != 0 && message.startsWith("-1"))) {
                attachPath = stream.readString(exception);
            }
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
            stream.writeInt32(from_id);
            to_id.serializeToStream(stream);
            stream.writeBool((flags & MESSAGE_FLAG_OUT) != 0);
            stream.writeBool((flags & MESSAGE_FLAG_UNREAD) != 0);
            stream.writeInt32(date);
            stream.writeString(message);
            media.serializeToStream(stream);
            stream.writeString(attachPath);
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


    //User start
    public static class TL_userEmpty extends User {
        public static int constructor = 0x200250ba;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);

            first_name = "DELETED";
            last_name = "";
            phone = "";
            status = new TL_userStatusEmpty();
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
        }
    }
    //User end

    //Chat start
    public static class TL_chatEmpty extends Chat {
        public static int constructor = 0x9ba2d800;


        public void readParams(AbsSerializedData stream, boolean exception) {
            id = stream.readInt32(exception);

            title = "DELETED";
        }

        public void serializeToStream(AbsSerializedData stream) {
            stream.writeInt32(constructor);
            stream.writeInt32(id);
        }
    }
    //Chat end



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
