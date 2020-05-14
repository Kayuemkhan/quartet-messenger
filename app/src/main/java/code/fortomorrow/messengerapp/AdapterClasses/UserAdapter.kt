package code.fortomorrow.messengerapp.AdapterClasses

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import code.fortomorrow.messengerapp.MessageChatActivity
import code.fortomorrow.messengerapp.ModelClasses.Users
import code.fortomorrow.messengerapp.R
import code.fortomorrow.messengerapp.WelcomeActivity
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.user_search_item_layout.view.*
import kotlinx.android.synthetic.main.user_search_item_layout.view.profile_image

class UserAdapter(
    mContext:Context,
    mUsers:List<Users>,
    isChatCheck:Boolean
    ) :RecyclerView.Adapter<UserAdapter.Viewholder?>()
{
    private val mContext:Context
    private val mUsers:List<Users>
    private val isChatCheck:Boolean
    init {
        this.mUsers = mUsers
        this.mContext = mContext
        this.isChatCheck=isChatCheck
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): Viewholder {
        val view:View = LayoutInflater.from(mContext).inflate(R.layout.user_search_item_layout,viewGroup,false)
        return UserAdapter.Viewholder(view)
    }

    override fun getItemCount(): Int {
        return mUsers.size
    }

    override fun onBindViewHolder(holder: Viewholder, position: Int) {
        val user :Users = mUsers[position]
        holder.userNameTxt.text  = user!!.getUserName()
        Picasso.get().load(user.getProfile()).placeholder(R.drawable.pp).into(holder.profileImageView)

        holder.itemView.setOnClickListener{
            val options = arrayOf<CharSequence>(
                "Send Message",
                "Visit Profile"
            )
            val builder: AlertDialog.Builder = AlertDialog.Builder(mContext)
            builder.setTitle("What do you want?")
            builder.setItems(options,DialogInterface.OnClickListener{dialog, position ->
                if (position ==0){
                    val intent = Intent(mContext, MessageChatActivity::class.java)
                    intent.putExtra("visit_id",user.getUID())
                    mContext.startActivity(intent)
                }
                else if(position ==1){

                }
            })
            builder.show()
        }
    }
    class Viewholder(itemview: View):RecyclerView.ViewHolder(itemview) {
        var userNameTxt:TextView
        var profileImageView:CircleImageView
        var onlineImageView:CircleImageView
        var offlineImageView:CircleImageView
        var lastMessageTxt:TextView

        init {
            userNameTxt = itemview.findViewById(R.id.username)
            profileImageView = itemview.findViewById(R.id.profile_image)
            onlineImageView = itemview.findViewById(R.id.image_online)
            offlineImageView = itemview.findViewById(R.id.image_offline)
            lastMessageTxt = itemview.findViewById(R.id.message_last)
        }

    }



}