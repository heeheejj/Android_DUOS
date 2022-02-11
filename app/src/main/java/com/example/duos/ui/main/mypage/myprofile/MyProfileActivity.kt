package com.example.duos.ui.main.mypage.myprofile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.example.duos.R
import com.example.duos.data.local.UserDatabase
import com.example.duos.data.remote.chat.chat.ChatService
import com.example.duos.databinding.ActivityMyprofileBinding
import com.example.duos.ui.BaseActivity
import com.example.duos.ui.main.chat.ChattingActivity
import com.example.duos.ui.main.chat.CreateChatRoomView
import com.example.duos.ui.main.mypage.myprofile.editprofile.EditProfileActivity
import com.example.duos.ui.main.mypage.myprofile.frag.EditProfileFragment
import com.example.duos.ui.main.mypage.myprofile.frag.MyProfileFragment
import com.example.duos.ui.main.mypage.myprofile.frag.PlayerFragment
import com.example.duos.utils.getUserIdx

class MyProfileActivity : BaseActivity<ActivityMyprofileBinding>(ActivityMyprofileBinding::inflate),
    CreateChatRoomView {
    var thisUserIdx = 102
    var targetUserIdx = 76
    val userIdx = getUserIdx()

    override fun initAfterBinding() {
        val db = UserDatabase.getInstance(applicationContext)
        val myProfileDB = db!!.userDao().getUser(userIdx!!)

        // PartnerSearchFragment 에서 넘어온 정보 받기
        val isFromSearch = intent.getBooleanExtra(
            "isFromSearch",
            false
        )   /* PartnerSearchFragment 에서 넘어왔다면 True else> 기본값 false*/
        val isFromAppointment = intent.getBooleanExtra(
            "isFromAppointment",
            false
        ) /* AppointmentFragment 에서 넘어왔다면 True else> 기본값 false*/
        val partnerUserIdx = intent.getIntExtra("partnerUserIdx", 0)
        /*TODO :위 thisIdx는 PartnerSearchFragment 에서 혹은 LastAppointmentFragment 에서
            아이템 클릭시 해당 회원의 고유 인덱스 값이 들어가야 해. 0이 default지만 할당될 일 없음*/
        Log.d("넘겨주기", "PartnerSearchFrag 에서 옴? : ${isFromSearch} 파트너 userIdx는? :${partnerUserIdx}")

        if (partnerUserIdx == userIdx) {  // PlayerFrag 에서 내가 쓴 후기를 눌렀을 때 나의 프로필로 이동한다.
            supportFragmentManager.beginTransaction()
                .replace(R.id.my_profile_into_fragment_container_fc, MyProfileFragment())
                .commitAllowingStateLoss()
        }

        if (isFromSearch) {    /* 만약 PartnerSearchFrag 으로부터 이 액티비티가 호출되었다면 */
            goToPlayerProfile(partnerUserIdx)

        } else if (isFromAppointment) {   /* 만약 AppointmentFrag 으로부터 이 액티비티가 호출되었다면 */
            goToPlayerProfile(partnerUserIdx)

        } else {    // PartnerSearchFrag 으로부터 이 액티비티가 호출되지 않았다면 내 프로필로 이동
            supportFragmentManager.beginTransaction()
                .replace(R.id.my_profile_into_fragment_container_fc, MyProfileFragment())
                .commitAllowingStateLoss()
        }

//        binding.editMyProfileTv.setOnClickListener{
//            supportFragmentManager.beginTransaction()
//                .replace(R.id.my_profile_into_fragment_container_fc, EditProfileFragment())
//                .commitAllowingStateLoss()
//            binding.topLeftArrowIv.setImageResource(R.drawable.ic_btn_close_iv)
//            binding.editMyProfileTv.visibility= View.GONE
//            binding.topMyProfileTv.text= "나의 프로필 수정"
//        }
        binding.editMyProfileTv.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            startActivity(intent)
        }

        binding.partnerProfileChattingBtn.setOnClickListener {
            createRoom()
        }

        binding.topLeftArrowIv.setOnClickListener {
            finish()
        }
    }

    private fun goToPlayerProfile(partnerUserIdx: Int) {
        val playerFragment = PlayerFragment()
        val bundle = Bundle()
        bundle.putInt("partnerUserIdx", partnerUserIdx)   // bundle로 PlayerFragment 에
        playerFragment.arguments = bundle                  // PartnerSearchFrag 에서 넘어온 thisIdx 넘겨줌
        supportFragmentManager.beginTransaction()
            .replace(R.id.my_profile_into_fragment_container_fc, playerFragment)
            .commitAllowingStateLoss()
    }


    /*  아래 희주님이 작성하신 코드                 */
    fun createRoom() {
        // val chatRoom = ChatRoom(thisUserIdx, targetUserIdx)
        Log.d("채팅방 생성한 user의 userIdx", thisUserIdx.toString())
        Log.d("채팅방 생성: 상대 user의 userIdx", targetUserIdx.toString())
        ChatService.createChatRoom(this, thisUserIdx, targetUserIdx)
    }

    private fun startChattingActivity() {
        var intent = Intent(this, ChattingActivity::class.java)
        startActivity(intent)
    }

    fun startLoadingProgress() {
        Log.d("로딩중", "채팅방 생성 api")
        Handler(Looper.getMainLooper()).postDelayed(Runnable { progressOFF() }, 3500)
    }

    override fun onCreateChatRoomLoading() {
        startLoadingProgress()
    }

    override fun onCreateChatRoomSuccess() {
        startChattingActivity()
    }

    override fun onCreateChatRoomFailure(code: Int, message: String) {
//        Toast.makeText(this, "code: $code, message: $message", Toast.LENGTH_LONG).show()
    }

}

