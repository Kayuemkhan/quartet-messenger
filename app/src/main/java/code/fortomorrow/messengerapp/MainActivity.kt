package code.fortomorrow.messengerapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import code.fortomorrow.messengerapp.Fragments.ChatFragment
import code.fortomorrow.messengerapp.Fragments.SearchFragment
import code.fortomorrow.messengerapp.Fragments.SettingsFragment
import code.fortomorrow.messengerapp.ModelClasses.Chat
import code.fortomorrow.messengerapp.ModelClasses.Chatlist
import code.fortomorrow.messengerapp.ModelClasses.Users
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    var refUsers: DatabaseReference? = null
    var firebaseUser:FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar_main)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.title=""

        val tabLayout: TabLayout = findViewById(R.id.tab_layout)
        val viewPager: ViewPager = findViewById(R.id.view_pager)

        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val viewpagerAdapter = ViewPagerAdapter(supportFragmentManager)
                var countUnreadMessages = 0
                for (dataSnapshot in p0.children){
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && !chat.isIsSeen()!!)
                    {
                        countUnreadMessages += 1

                    }
                }
                if (countUnreadMessages ==0){
                    viewpagerAdapter.addFragment(ChatFragment(),"Chats")
                }
                else{
                    viewpagerAdapter.addFragment(ChatFragment(),"($countUnreadMessages) Chats")
                }
                viewpagerAdapter.addFragment(SearchFragment(),"Search")
                viewpagerAdapter.addFragment(SettingsFragment(),"Settings")

                viewPager.adapter= viewpagerAdapter
                tabLayout.setupWithViewPager(viewPager)
            }

        })

        // display username and profile picture
        refUsers!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()){
                    val user :Users? = p0.getValue(Users::class.java)

                    user_name.text  = user!!.getUserName()
                    Picasso.get().load(user.getProfile()).placeholder(R.drawable.pp).into(profile_image)
                }
            }

        })
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_logout ->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,WelcomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                finish()

                return true
            }

        }
        return false
    }

    internal class ViewPagerAdapter(fragmentManager: FragmentManager):FragmentPagerAdapter(fragmentManager) {
        private val fragments:ArrayList<Fragment>
        private val titles:ArrayList<String>

        init {
            fragments = ArrayList<Fragment>()
            titles = ArrayList<String>()
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
        return fragments.size
        }
        fun  addFragment(fragment: Fragment,title:String){
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }

    }
}
