package com.example.duos.ui.main.chat

import android.content.Intent
import android.widget.EditText
import androidx.recyclerview.widget.RecyclerView
import com.example.duos.databinding.ActivityChattingBinding
import androidx.recyclerview.widget.DefaultItemAnimator

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.example.duos.data.entities.MessageItem
import com.example.duos.data.entities.ChatType

import android.util.Log
import android.text.Editable
import android.text.TextWatcher

import android.view.View
import android.widget.ImageView
import com.example.duos.R
import com.example.duos.data.entities.MessageData
import com.example.duos.ui.BaseActivity
import java.text.SimpleDateFormat
import java.util.*


class ChattingActivity: BaseActivity<ActivityChattingBinding>(ActivityChattingBinding::inflate) {
    //lateinit var binding: ActivityChattingBinding
    var roomIdx: Int = 0
    var userId: String = "tennis01"
    var partnerId: String = "djeikd0620"
    var thisUserIdx = 76
    var targetUserIdx = 110
    private var layoutManager: LayoutManager? = null
    lateinit var chattingMessagesRVAdapter: ChattingMessagesRVAdapter
    lateinit var chattingRV: RecyclerView
    lateinit var chattingEt: EditText

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        //binding = ActivityChattingBinding.inflate(layoutInflater)
//        //setContentView(binding.root)
//
//        chattingEt = binding.chattingEt
//        chattingRV = binding.chattingMessagesRv
//
//        var sendBtn: ImageView = binding.chattingSendBtn
//
//        chattingRV.setHasFixedSize(true)
//        layoutManager = LinearLayoutManager(this)
//        (layoutManager as LinearLayoutManager).setStackFromEnd(true)    //
//        chattingRV.setLayoutManager(layoutManager)
//        chattingRV.setItemAnimator(DefaultItemAnimator())
//        chattingMessagesRVAdapter = ChattingMessagesRVAdapter()
//        chattingRV.setAdapter(chattingMessagesRVAdapter)
//
//        // chatting test code
//        var chattingMessage = MessageData("DATE", userId, roomIdx.toString(),"2022년 01월 21일", System.currentTimeMillis())
//        addChat(chattingMessage)
//        chattingRV.scrollToPosition(chattingMessagesRVAdapter.itemCount - 1)
//
//        chattingMessage = MessageData("MESSAGE", partnerId, userId,"상대방이 보낸 메세지입니다.", System.currentTimeMillis())
//        addChat(chattingMessage)
//        chattingRV.scrollToPosition(chattingMessagesRVAdapter.itemCount - 1)
//
//
//        // 채팅 EditText focus되면 전송 아이콘(비행기 아이콘) primary색으로 활성화, 아닐때 비활성화
//        chattingEt.addTextChangedListener(object: TextWatcher {
//            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//
//            }
//
//            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//                if(chattingEt.length() > 0){
//                    sendBtn.isClickable = true
//                    sendBtn.setImageResource(R.drawable.ic_btn_airplane_send_blue)
//                } else{
//                    sendBtn.isClickable = false
//                    sendBtn.setImageResource(R.drawable.ic_btn_airplane_send_gray)
//                }
//            }
//
//            override fun afterTextChanged(p0: Editable?) {
//
//            }
//        })
//
////        chattingEt.setOnFocusChangeListener(object : View.OnFocusChangeListener {
////            override fun onFocusChange(view: View, hasFocus: Boolean) {
////                if (hasFocus || !chattingEt.text.equals("")) {
////                    sendBtn.setImageResource(R.drawable.ic_btn_airplane_send_blue)
////                } else {
////                    sendBtn.setImageResource(R.drawable.ic_btn_airplane_send_gray)
////                }
////            }
////        })
//        sendBtn.setOnClickListener{
//                v -> sendMessage(v)
//        }
//
//        binding.chattingMakePlanBtn.setOnClickListener ({
//            val intent = Intent(this, MakePlanActivity::class.java)
//            startActivity(intent)
//        })
//    }

    // 리사이클러뷰에 채팅 추가
    private fun addChat(data: MessageData) {
        this.runOnUiThread {
            if (data.type.equals("DATE")) {    //
                chattingMessagesRVAdapter.addItem(
                    MessageItem(
                        userId,
                        data.message,
                        toDate(data.sendTime),
                        ChatType.CENTER_MESSAGE
                    )
                )
                chattingRV.scrollToPosition(chattingMessagesRVAdapter.itemCount - 1)
            } else {
                chattingMessagesRVAdapter.addItem(
                    MessageItem(
                        userId,
                        data.message,
                        toDate(data.sendTime),
                        ChatType.LEFT_MESSAGE
                    )
                )
                chattingRV.scrollToPosition(chattingMessagesRVAdapter.itemCount - 1)
            }
        }
    }

    private fun sendMessage(view: View) {
        Log.d(
            "MESSAGE", MessageData(
                "957cfc80-481c-4ae4-88a0-25a9599dd511",
                "MESSAGE",
                thisUserIdx, targetUserIdx,
                chattingEt.text.toString(),
                System.currentTimeMillis()
            ).toString()
        )
        chattingMessagesRVAdapter.addItem(
            MessageItem(
                userId,
                chattingEt.text.toString(),
                toDate(System.currentTimeMillis()),
                ChatType.RIGHT_MESSAGE
            )
        )
        chattingRV.scrollToPosition(chattingMessagesRVAdapter.itemCount - 1)
        chattingEt.setText("")
    }

    // System.currentTimeMillis를 몇시:몇분 am/pm 형태의 문자열로 반환
    private fun toDate(currentMiliis: Long): String {
        return SimpleDateFormat("a hh:mm").format(Date(currentMiliis))
    }

    override fun initAfterBinding() {
        chattingEt = binding.chattingEt
        chattingRV = binding.chattingMessagesRv

        var sendBtn: ImageView = binding.chattingSendBtn

        chattingRV.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        (layoutManager as LinearLayoutManager).setStackFromEnd(true)    //
        chattingRV.setLayoutManager(layoutManager)
        chattingRV.setItemAnimator(DefaultItemAnimator())
        chattingMessagesRVAdapter = ChattingMessagesRVAdapter()
        chattingRV.setAdapter(chattingMessagesRVAdapter)

        // chatting test code
        var chattingMessage = MessageData("957cfc80-481c-4ae4-88a0-25a9599dd511", "DATE", thisUserIdx, thisUserIdx,"2022년 01월 21일", System.currentTimeMillis())
        addChat(chattingMessage)
        chattingRV.scrollToPosition(chattingMessagesRVAdapter.itemCount - 1)

        chattingMessage = MessageData("957cfc80-481c-4ae4-88a0-25a9599dd511", "MESSAGE", targetUserIdx, thisUserIdx,"상대방이 보낸 메세지입니다.", System.currentTimeMillis())
        addChat(chattingMessage)
        chattingRV.scrollToPosition(chattingMessagesRVAdapter.itemCount - 1)


        // 채팅 EditText focus되면 전송 아이콘(비행기 아이콘) primary색으로 활성화, 아닐때 비활성화
        chattingEt.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(chattingEt.length() > 0){
                    sendBtn.isClickable = true
                    sendBtn.setImageResource(R.drawable.ic_btn_airplane_send_blue)
                } else{
                    sendBtn.isClickable = false
                    sendBtn.setImageResource(R.drawable.ic_btn_airplane_send_gray)
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })

//        chattingEt.setOnFocusChangeListener(object : View.OnFocusChangeListener {
//            override fun onFocusChange(view: View, hasFocus: Boolean) {
//                if (hasFocus || !chattingEt.text.equals("")) {
//                    sendBtn.setImageResource(R.drawable.ic_btn_airplane_send_blue)
//                } else {
//                    sendBtn.setImageResource(R.drawable.ic_btn_airplane_send_gray)
//                }
//            }
//        })
        sendBtn.setOnClickListener{
                v -> sendMessage(v)
        }

        binding.chattingMakePlanBtn.setOnClickListener ({
            val intent = Intent(this, MakePlanActivity::class.java)
            startActivity(intent)
        })

        binding.chattingBackIv.setOnClickListener {
            finish()
        }
    }

//    override fun onNewIntent(intent: Intent?) {
////        var bundle = intent?.extras
////        val type = bundle?.getString("type")
////        val from = bundle?.getString("from")
////        val to = bundle?.getString("to")
////        val body = bundle?.getString("messageBody")
////        val sendTime = bundle?.getLong("sendTime")
//        val chatRoomIdx = intent?.getStringExtra("chatRoomIdx")
//        val type = intent?.getStringExtra("type")
//        val from = intent?.getStringExtra("from")
//        val to = intent?.getStringExtra("to")
//        val body = intent?.getStringExtra("messageBody")
//        val sendTime = intent?.getLongExtra("sendTime", 0)
//
//        Log.d("채팅액티비티"," 1")
//
//        if (chatRoomIdx != null && type != null && from != null && to != null && body != null && sendTime != null) {
//            Log.d("채팅액티비티", "2")
//            updateMessage(chatRoomIdx, type, from, to, body, sendTime)
//        }
//        super.onNewIntent(intent)
//    }
//
//    fun updateMessage(roomIdx: String, type: String, from: Int, to: Int, body: String, sendTime: Long) {
//        Log.d("채팅액티비티", "3")
//        val chattingMessage = MessageData(roomIdx, type, from, to, body, sendTime)
//        addChat(chattingMessage)
//        chattingRV.scrollToPosition(chattingMessagesRVAdapter.itemCount - 1)
//    }
}