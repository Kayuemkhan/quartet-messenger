package code.fortomorrow.messengerapp

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import code.fortomorrow.messengerapp.AdapterClasses.ChatsAdapter
import code.fortomorrow.messengerapp.ModelClasses.Chat
import code.fortomorrow.messengerapp.ModelClasses.Users
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.auth.User
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*
import kotlin.math.PI

class MessageChatActivity : AppCompatActivity() {
    var userIdVisit : String =""
    var firebaseuser:FirebaseUser? = null
    var chatsAdapter:ChatsAdapter? = null
    var mChatList: List<Chat>? = null
    var reference: DatabaseReference? = null
    lateinit var recyler_view_chats:RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)

        intent =intent
        userIdVisit = intent.getStringExtra("visit_id")
        firebaseuser = FirebaseAuth.getInstance().currentUser

        recyler_view_chats = findViewById(R.id.recyler_view_chats)
        recyler_view_chats.setHasFixedSize(true)
        var linearLayoutManager=LinearLayoutManager(this@MessageChatActivity)
        linearLayoutManager.stackFromEnd= true
        recyler_view_chats.layoutManager= linearLayoutManager

        reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIdVisit)
        reference!!.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val user:Users? = p0.getValue(Users::class.java)
                user_name_mc.text =user!!.getUserName()
                Picasso.get().load(user.getProfile()).into(profile_image_mc)

                retrieveMessages(firebaseuser!!.uid,userIdVisit,user.getProfile())
            }

        })

        send_message_btn.setOnClickListener{
            val message = text_message.text.toString()
            if (message ==""){
                Toast.makeText(this,"Pleae write a message first...",Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            else{
                sendMessageToUser(firebaseuser!!.uid,userIdVisit,message)
            }
            text_message.setText("")
        }
        attach_image_file_btn.setOnClickListener{
            val intent = Intent()
            intent.action= Intent.ACTION_GET_CONTENT
            intent.type ="image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick Image"),438)

        }
        seenMessage(userIdVisit)
    }



    private fun sendMessageToUser(senderId: String, receiverId: String?, message: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap=HashMap<String,Any?>()
        messageHashMap["sender"] = senderId
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverId
        messageHashMap["isseen"] = false
        messageHashMap["url"] = "null"
        messageHashMap["messageId"] = messageKey

        reference.child("Chats").child(messageKey!!).setValue(messageHashMap)
            .addOnCompleteListener(OnCompleteListener {task->
                if (task.isSuccessful){
                    val chatsListReference = FirebaseDatabase.getInstance().reference.child("ChatLists")
                        .child(firebaseuser!!.uid)
                        .child(userIdVisit)
                    chatsListReference.addListenerForSingleValueEvent(object :ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {

                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()){
                                chatsListReference.child("id")
                                    .setValue(userIdVisit)
                            }
                            val chatsListReceiverRef = FirebaseDatabase.getInstance().reference.child("ChatLists")
                                .child(userIdVisit)
                                .child(firebaseuser!!.uid)

                            chatsListReceiverRef.child("id").setValue(firebaseuser!!.uid)

                        }

                    })




                    // implement the push notification


                    val reference = FirebaseDatabase.getInstance().reference
                        .child("Users").child(firebaseuser!!.uid)

                }
            })


    }
    private fun retrieveMessages(senderId:  String, receiverId: String?, receiverImageUrl: String?) {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        reference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (snapshot in p0.children){
                    val chat = snapshot.getValue(Chat::class.java)
                    if(chat!!.getReceiver().equals(senderId) && chat.getSender().equals(receiverId)
                        || chat.getReceiver().equals(receiverId) && chat.getSender().equals(senderId))
                    {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatsAdapter = ChatsAdapter(this@MessageChatActivity,(mChatList as ArrayList<Chat>),receiverImageUrl!!)

                    recyler_view_chats.adapter = chatsAdapter

                }
            }

        })

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 438 && resultCode == RESULT_OK && data!=null  && data!!.data != null) {
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("Image is uploading, please Wait...")
            progressBar.show()

            val fireuri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageId = ref.push().key
            val filepath = storageReference.child("$messageId.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filepath.putFile(fireuri!!)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                return@continueWithTask filepath.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    progressBar.cancel()
                    val downloadurl = task.result
                    val url = downloadurl.toString()

                    val messageHashMap = HashMap<String, Any?>()
                    messageHashMap["sender"] = firebaseuser!!.uid
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageId"] = messageId

                    ref.child("Chats").child(messageId!!).setValue(messageHashMap)

                }
            }
        }

    }
    var seenListener:ValueEventListener? = null

    private fun seenMessage(userId:String){
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")
        seenListener = reference!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                for (dataSnapshot in p0.children){
                    val chat = dataSnapshot.getValue(Chat::class.java)

                    if (chat!!.getReceiver().equals(firebaseuser!!.uid) && chat!!.getSender().equals(userId))
                    {
                        val hashMap = HashMap<String,Any>()
                        hashMap["isseen"]=true
                        dataSnapshot.ref.updateChildren(hashMap)
                    }
                }
            }

        })
    }

    override fun onPause() {
        super.onPause()
        reference!!.removeEventListener(seenListener!!)
    }
}
