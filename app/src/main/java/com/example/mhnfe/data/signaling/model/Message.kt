package com.example.mhnfe.data.signaling.model

import android.util.Base64
import org.webrtc.SessionDescription

class Message(
    var action: String? = null,
    var recipientClientId: String? = null,
    var senderClientId: String? = null,
    var messagePayload: String? = null
) {

    // 기본 생성자 필요 없음, 위에서 기본값을 지정했으므로 생략 가능

    constructor(action: String, recipientClientId: String, senderClientId: String, messagePayload: String) : this() {
        this.action = action
        this.recipientClientId = recipientClientId
        this.senderClientId = senderClientId
        this.messagePayload = messagePayload
    }

    companion object {
        /**
         * @param sessionDescription SDP description to be converted & sent to signaling service
         * @param master             true if local is set to be the master
         * @param recipientClientId  - has to be set to null if this is set as viewer
         * @return SDP Answer message to be sent to signaling service
         */
        fun createAnswerMessage(
            sessionDescription: SessionDescription,
            master: Boolean,
            recipientClientId: String?
        ): Message {
            val description = sessionDescription.description

            val answerPayload = "{\"type\":\"answer\",\"sdp\":\"${description.replace("\r\n", "\\r\\n")}\"}"

            val encodedString = Base64.encodeToString(answerPayload.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)

            // SenderClientId should always be "" for master creating answer case
            return Message("SDP_ANSWER", recipientClientId, "", encodedString)
        }

        /**
         * @param sessionDescription SDP description to be converted as Offer Message & sent to signaling service
         * @param clientId           Client Id to mark this viewer in signaling service
         * @return SDP Offer message to be sent to signaling service
         */
        fun createOfferMessage(sessionDescription: SessionDescription, clientId: String): Message {
            val description = sessionDescription.description

            val offerPayload = "{\"type\":\"offer\",\"sdp\":\"${description.replace("\r\n", "\\r\\n")}\"}"

            val encodedString = Base64.encodeToString(offerPayload.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP)

            return Message("SDP_OFFER", "", clientId, encodedString)
        }
    }
}