package com.crypto.sick.trade.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

    @Autowired
    private FirebaseMessaging messaging;

    public void sendNotificationForNigers() {
        Message msg = Message.builder()
                .setToken("dUQ6Cl5FxzALbaFJGSVwLu:APA91bFdljDtQdxPWurEd9k8orsHr741-_g4c4WMKAPhCEBCkjsmWMNEE4bs2Kr1ay4TO9ftrcp6YveH7y43c3Xb-qh_G2suukXl2Qoba8U-Iimzrj0AAXc")
                .putData("title", "Nigers title")
                .putData("body", "NigerData")
                .build();

        try {
            messaging.send(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
