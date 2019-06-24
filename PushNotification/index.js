'use strict'




const functions = require('firebase-functions');


const admin = require('firebase-admin');


admin.initializeApp(functions.config().firebase);




exports.sendNotification = functions.database.ref('/Notifications/{receiver_id}/{notification_id}').onWrite((data, context) => {




  const receiver_id   = context.params.receiver_id ;


  const notification_id = context.params.notification_id;




  console.log('We have a notification to send to: ', receiver_id );




      if(!data.after.val()){




        return console.log('A Notification has been deleted from the database : ', notification_id);




      }



      const sender_id = admin.database().ref(`/Notifications/${receiver_id}/${notification_id}`).once('value');


      return sender_id.then(fromUserResult =>
        {


          const from_sender_id = fromUserResult.val().from;


          console.log('You have a notification from:' , from_sender_id);




          const senderUserQuery = admin.database().ref(`/Users/${from_sender_id}/name`).once('value');


          return senderUserQuery.then(senderUserNameResult =>
            {


              const senderUserName = senderUserNameResult.val();




                 const deviceToken = admin.database().ref(`/Users/${receiver_id}/device_token`).once('value');


                 return deviceToken.then(result =>
                 {


                     const token_id = result.val();


                     const payload =
                     {


                       notification:
                       {


                          title: "New Friend Request",
                          body: `${senderUserName} sent you a Friend Request.`,
                          sound: "default",
                          icon: "notification_icon"
                       },
                       data:
                       {
                         from_sender_id : from_sender_id
                       }


                     };




                      return admin.messaging().sendToDevice(token_id, payload)
                         .then(responde => {


                           console.log("This was a notificaton feature");
                      });
                });


            });
        });




});