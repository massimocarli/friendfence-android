package uk.co.massimocarli.friendfence.gcm;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * An utility class we use as a container of builders for the requests to send to the server
 * Created by Massimo Carli on 11/10/14.
 */
public interface GcmRequest {


    /**
     * The Builder for the registration request
     */
    public static class RegistrationBuilder implements GcmRequest {

        /**
         * The JsonObject we use to build the request data
         */
        private JSONObject mJsonObject;

        /**
         * Private constructor
         */
        private RegistrationBuilder() {
            mJsonObject = new JSONObject();
        }

        /**
         * @return The Builder for the registration data
         */
        public static RegistrationBuilder create() {
            return new RegistrationBuilder();
        }

        /**
         * Adds the deviceId information
         *
         * @param deviceId The deviceId information
         * @return The RegistrationBuilder itself for chaining
         */
        public RegistrationBuilder withDeviceId(final String deviceId) {
            try {
                mJsonObject.put("deviceId", deviceId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        /**
         * Adds the registrationId information
         *
         * @param registrationId The registrationId information
         * @return The RegistrationBuilder itself for chaining
         */
        public RegistrationBuilder withRegistrationId(final String registrationId) {
            try {
                mJsonObject.put("registrationId", registrationId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        /**
         * Adds the username information
         *
         * @param username The username information
         * @return The RegistrationBuilder itself for chaining
         */
        public RegistrationBuilder withUsername(final String username) {
            try {
                mJsonObject.put("username", username);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        public String getJsonAsString() {
            return mJsonObject.toString();
        }

        @Override
        public JSONObject getJson() {
            return mJsonObject;
        }
    }


    /**
     * The Builder for the send request
     */
    public static class SendBuilder implements GcmRequest {

        /**
         * The JsonObject we use to build the request data
         */
        private JSONObject mJsonObject;

        /**
         * Private constructor
         */
        private SendBuilder() {
            mJsonObject = new JSONObject();
        }

        /**
         * @return The Builder for the registration data
         */
        public static SendBuilder create() {
            return new SendBuilder();
        }

        /**
         * Adds the message information
         *
         * @param message The message information
         * @return The SendBuilder itself for chaining
         */
        public SendBuilder withMessageBody(final String message) {
            try {
                mJsonObject.put("msgBody", message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        /**
         * Adds the to information
         *
         * @param to The message information
         * @return The SendBuilder itself for chaining
         */
        public SendBuilder withTo(final String to) {
            try {
                mJsonObject.put("to", to);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }

        /**
         * Adds the sender information
         *
         * @param sender The message information
         * @return The SendBuilder itself for chaining
         */
        public SendBuilder withSender(final String sender) {
            try {
                mJsonObject.put("senderUser", sender);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }


        public String getJsonAsString() {
            return mJsonObject.toString();
        }

        @Override
        public JSONObject getJson() {
            return mJsonObject;
        }
    }

    /**
     * @return The Json as String
     */
    String getJsonAsString();

    /**
     * @return The Json as JSONObject
     */
    JSONObject getJson();

}
