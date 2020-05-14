package code.fortomorrow.messengerapp.Fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import code.fortomorrow.messengerapp.AdapterClasses.UserAdapter
import code.fortomorrow.messengerapp.ModelClasses.Chat
import code.fortomorrow.messengerapp.ModelClasses.Chatlist
import code.fortomorrow.messengerapp.ModelClasses.Users

import code.fortomorrow.messengerapp.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.firestore.auth.User

/**
 * A simple [Fragment] subclass.
 */
class ChatFragment : Fragment() {
    private var userAdapter: UserAdapter? = null

    private var mUsers:List<Users>? = null
    private var usersChatList:List<Chatlist>? = null
    lateinit var recyler_view_chatlist:RecyclerView
    private var firebaseUser: FirebaseUser? = null
    var ref: DatabaseReference? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        recyler_view_chatlist = view.findViewById(R.id.recyler_view_chatlist)
        recyler_view_chatlist.setHasFixedSize(true)
        recyler_view_chatlist.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        usersChatList = ArrayList()

         ref = FirebaseDatabase.getInstance().reference.child("ChatLists").child(firebaseUser!!.uid)
        ref!!.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                (usersChatList as ArrayList).clear()
                for (datSnapshot in p0.children){
                    val chatlist = datSnapshot.getValue(Chatlist::class.java)

                    (usersChatList as ArrayList).add(chatlist!!)
                }
                retriveChatLists()
            }

        })

        return view
    }
    var usersValListener:ValueEventListener? = null
    private fun retriveChatLists(){

        mUsers = ArrayList()

        val ref = FirebaseDatabase.getInstance().reference.child("Users")

        usersValListener= ref!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()

                for (dataSnapshot in p0.children){
                    val user = dataSnapshot.getValue(Users::class.java)
                    for (eachChatList in usersChatList!!){
                        if (user!!.getUID().equals(eachChatList.getId())){
                            (mUsers as ArrayList).add(user!!)
                        }
                    }
                }
                userAdapter = context?.let { UserAdapter(it,(mUsers as ArrayList<Users>),true) }
                recyler_view_chatlist?.adapter = userAdapter
            }

        })
    }

   override fun onPause() {
      super.onPause()
       ref!!.removeEventListener(usersValListener!!)
   }
}
