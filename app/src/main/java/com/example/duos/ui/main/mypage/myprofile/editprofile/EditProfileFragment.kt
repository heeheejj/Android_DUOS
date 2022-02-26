package com.example.duos.ui.main.mypage.myprofile.editprofile

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.duos.R
import com.example.duos.data.entities.duplicate.DuplicateNicknameListView
import com.example.duos.data.entities.editProfile.*
import com.example.duos.data.local.UserDatabase
import com.example.duos.data.remote.duplicate.DuplicateNicknameResponse
import com.example.duos.data.remote.duplicate.DuplicateNicknameService
import com.example.duos.data.remote.editProfile.EditProfileGetService
import com.example.duos.data.remote.editProfile.EditProfilePutResponse
import com.example.duos.data.remote.editProfile.EditProfilePutService
import com.example.duos.databinding.FragmentEditProfileBinding
import com.example.duos.ui.main.mypage.myprofile.MyProfileActivity
import com.example.duos.ui.signup.localSearch.LocationDialogFragment
import com.example.duos.utils.ViewModel
import com.example.duos.utils.getUserIdx
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.util.regex.Pattern

class EditProfileFragment : Fragment(), EditProfileListView,
    EditProfilePutListView, DuplicateNicknameListView {
    val TAG = "EditProfileFragment"
    lateinit var binding: FragmentEditProfileBinding

    lateinit var mContext: EditProfileActivity

    lateinit var viewModel: ViewModel
    var savedState: Bundle? = null
    val myUserIdx = getUserIdx()!!
    var locationText: TextView? = null
    var checkStore: Boolean = false
    var inputIntroduction: String = ""
    var originExperience: Int? = null


    // 카메라 접근 권한
    lateinit var contentUri: Uri

    val CAMERA_PERMISSION = arrayOf(Manifest.permission.CAMERA)
    val CAMERA_PERMISSION_REQUEST = 100

    // 멀티퍼미션 갤러리(앨범)
    @RequiresApi(Build.VERSION_CODES.Q)
    val permission_list = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.ACCESS_MEDIA_LOCATION
    )
    val multiplePermissionsCode2 = 300

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is EditProfileActivity) {
            mContext = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity()).get(ViewModel::class.java)
        return binding.root
    }


    @SuppressLint("SetTextI18n", "ResourceType")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        onApplyDisable()    // 적용하기 비활
        EditProfileGetService.getEditProfile(this, myUserIdx)   // API 로 내 데이터 불러오기
        val db = UserDatabase.getInstance(requireContext().applicationContext)
//        val db = UserDatabase.getInstance(requireContext())
        val myProfileDB = db!!.userDao().getUser(myUserIdx) /* 룸에 내 idx에 맞는 데이터 있으면 불러오기... */
        originExperience = myProfileDB.experience
        viewModel = ViewModelProvider(requireActivity()).get(ViewModel::class.java)
        binding.viewmodel = viewModel

        if (savedInstanceState != null && savedState == null) {
            Log.d(TAG, "savedInstanceState != null && savedState == null")
            savedState = savedInstanceState.getBundle("savedState")
            Log.d(TAG, " savedState : ${savedState}")
        }
        if (savedInstanceState != null) {
            Log.d(TAG, "savedInstanceState != null")
            //  저장
            checkStore = true
            binding.btnCheckDuplicationTv.isEnabled = false
            binding.btnCheckDuplicationTv.setBackgroundResource(R.drawable.signup_phone_verifying_rectangular)
            binding.btnCheckDuplicationTv.setTextColor(ContextCompat.getColor(mContext, R.color.dark_gray_A))

        } else {
            //  저장 X
//            viewModel.editProfileNickname.value = myProfileDB.nickName
            viewModel.editProfileLocationDialogShowing.value = false
            viewModel.editProfileExperience.value = myProfileDB.experience!!.toInt()
            viewModel.editProfileLocation.value = myProfileDB.location
            viewModel.setEditProfileNickName.value = false
            viewModel.editProfileImg.value = myProfileDB.profileImg
            viewModel.setEditProfileImgUrl.value = false
            viewModel.setEditProfileLocation.value = false
            viewModel.setEditProfileIntroduction.value = false
            viewModel.setEditProfileExperience.value = false
            viewModel.setEditProfileIsDuplicated.value = false
            viewModel.isChangedNickname.value = false
            Log.d(
                TAG,
                "saveInstanceState == null Or savedState != null : viewModel.editProfileNickname : ${viewModel.editProfileNickname.value}"
            )
        }
        savedState = null


        Log.d(TAG, " 원래 내 DB 데이터 $myProfileDB")
        Log.d(
            TAG,
            "setEditProfileNickname : ${viewModel.setEditProfileNickName.value}  " +
                    "setEditProfileLocation : ${viewModel.setEditProfileLocation.value}" +
                    "setEditProfileIntroduction : ${viewModel.setEditProfileIntroduction.value} " +
                    "setEditProfileExperience : ${viewModel.setEditProfileExperience.value} " +
                    "setEditProfileIsDuplicated : ${viewModel.setEditProfileIsDuplicated.value}  " +
                    "editProfileNickname : ${viewModel.editProfileNickname.value} "

        )

        // 소개글 전체 삭제 클릭 리스너
        binding.btnClearIntroductionTv.setOnClickListener {
            binding.contentIntroductionEt.text.clear()
        }

        // 사진 관련
        val file_path = requireActivity().getExternalFilesDir(null).toString()
        binding.myProfileImgIv.setOnClickListener {

            val dialogBuilder = AlertDialog.Builder(activity)
            dialogBuilder.setTitle(R.string.upload_pic_dialog_title)
                // setItems 대신 setAdapter()를 사용하여 목록을 지정 가능
                // 이렇게 하면 동적 데이터가 있는 목록(예: 데이터베이스에서 가져온 것을 ListAdapter로 지원할 수 있다.)
                .setItems(R.array.upload_pic_dialog_title, DialogInterface.OnClickListener { dialog, which ->
                    when (which) {
                        // 카메라 0 썸네일로
                        0 -> {
                            val permissionResult0 = ContextCompat.checkSelfPermission(
                                requireContext(),
                                CAMERA_PERMISSION[0]
                            )
                            Log.d("Signup_Image_Upload_Dialog", "checkSelfPermission$which")

                            when (permissionResult0) {
                                PackageManager.PERMISSION_GRANTED -> {
                                    Log.d(
                                        "Signup_Image_Upload_Dialog",
                                        "PERMISSION_GRANTED$which"
                                    )
                                    // 카메라 권한이 이미 허용된 상태일 때 바로 카메라 액티비티 호출
                                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                                    startActivityForResult(intent, CAMERA_PERMISSION_REQUEST)
                                }
                                PackageManager.PERMISSION_DENIED -> {
                                    Log.d(
                                        "Signup_Image_Upload_Dialog",
                                        "PERMISSION_DENIED$which"
                                    )
                                    // 카메라 권한이 허용된 상태가 아닐 때
                                    // ActivityCompat.requestPermissions(requireActivity(), CAMERA_PERMISSION, CAMERA_PERMISSION_REQUEST)
                                    // Fragment에서 onRequestPermissionsResult 호출하려면 requestPermissions만 쓰기
                                    requestPermissions(
                                        CAMERA_PERMISSION,
                                        CAMERA_PERMISSION_REQUEST
                                    )
                                    // 이 떄 onRequestPermissionsResult 메소드 호출

                                }
                            }

                        }

                        // 내 앨범에서 선택
                        1 -> {
                            Log.d("Signup_Image_Upload_Dialog", "파일 접근 $which")
                            val rejectedPermissionList = ArrayList<String>()
                            // 필요한 퍼미션들이 현재 권한을 받았는지 체크
                            for (permission in permission_list) {
                                if (ContextCompat.checkSelfPermission(
                                        requireContext(),
                                        permission
                                    ) != PackageManager.PERMISSION_GRANTED
                                ) {
                                    //만약 권한이 없다면 rejectedPermissionList에 추가
                                    rejectedPermissionList.add(permission)
                                }
                            }
                            if (rejectedPermissionList.isNotEmpty()) {   // 거절된 퍼미션 있다면?
                                val array = arrayOfNulls<String>(rejectedPermissionList.size)
                                requestPermissions(
                                    rejectedPermissionList.toArray(array),
                                    multiplePermissionsCode2
                                )
                            } else {

                                // 앨범에서 사진을 선택할 수 있도록 Intent
                                val albumIntent = Intent(
                                    Intent.ACTION_PICK,
                                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                )
                                // 실행할 액티비티의 타입 설정(이미지를 선택할 수 있는 것)
                                albumIntent.type = "image/*"
                                // 선택할 파일의 타입 지정
                                val mimeType = arrayOf("image/*")
                                albumIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
                                startActivityForResult(albumIntent, multiplePermissionsCode2)
                            }
                        }
                    }
                })
            dialogBuilder.create().show()

        }

        // 닉네임 관련
        viewModel.editProfileNickname.observe(viewLifecycleOwner, {
            val pattern = "^[0-9|a-z|A-Z|ㄱ-ㅎ|ㅏ-ㅣ|가-힣]*$"
            if (!checkStore) {  /* 변경할 수 있는 닉네임인가? */
                if (it!!.isNotEmpty()) {
                    Log.d(TAG, "닉네임이 공백이 아님")
                    binding.nicknameEtField.isEndIconVisible = true
                    if (!Pattern.matches(pattern, it.toString()) or (it.length < 2)) {  /* 안되도록 */
                        binding.nickNameErrorTv.visibility = View.VISIBLE
                        binding.nicknameCheckIconIv.visibility = View.VISIBLE
                        binding.nicknameCheckIconIv.setImageResource(R.drawable.ic_signup_nickname_unable)
                        binding.btnCheckDuplicationTv.isEnabled = false
                        binding.btnCheckDuplicationTv.setBackgroundResource(R.drawable.signup_phone_verifying_rectangular)
                        binding.btnCheckDuplicationTv.setTextColor(ContextCompat.getColor(mContext, R.color.dark_gray_A))
                    } else {            /* 되도록 */
                        binding.nickNameErrorTv.visibility = View.INVISIBLE
                        binding.nicknameCheckIconIv.visibility = View.VISIBLE
                        binding.nicknameCheckIconIv.setImageResource(R.drawable.ic_signup_phone_verifying_check_done)
                        binding.btnCheckDuplicationTv.isEnabled = true
                        binding.btnCheckDuplicationTv.setBackgroundResource(R.drawable.signup_phone_verifying_done_rectangular)
                        binding.btnCheckDuplicationTv.setTextColor(ContextCompat.getColor(mContext, R.color.primary))
                    }
                }
            }
            checkStore = false
        })

        val nicknameEt = binding.nicknameEt
        nicknameEt.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (hasFocus) {
                    binding.guideNicknameTv.setTextColor(ContextCompat.getColor(mContext, R.color.nero))
                    binding.nicknameEt.setTextColor(ContextCompat.getColor(mContext, R.color.nero))
                } else {
                    binding.guideNicknameTv.setTextColor(ContextCompat.getColor(mContext, R.color.grey))
                    binding.nicknameEt.setTextColor(ContextCompat.getColor(mContext, R.color.grey))
                }
            }
        })

        nicknameEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == myProfileDB.nickName) {
                    viewModel.isChangedNickname.value = false
                    viewModel.setEditProfileNickName.value = false
                    viewModel.editProfileNickname.value = s.toString()
                } else {
                    viewModel.isChangedNickname.value = true
                    viewModel.setEditProfileNickName.value = true
                    viewModel.editProfileNickname.value = s.toString()
                }
            }
        })

        binding.btnCheckDuplicationTv.setOnClickListener {  /* 중복 확인 */
            DuplicateNicknameService.getDuplicateNickname(this, myUserIdx, binding.nicknameEt.text.toString())
        }

        locationText = binding.locationInfoTv


        // 지역 설정 관련
        binding.locationInfoTv.setOnClickListener { /* 다이얼로그 띄우기 */
            val dialog = LocationDialogFragment()
            activity?.supportFragmentManager?.let { fragmentManager ->
                dialog.show(
                    fragmentManager, "지역 선택"
                )
            }
        }

        viewModel.editProfileLocationDialogShowing.observe(viewLifecycleOwner,
            Observer {   /* 다이얼로그의 값 observe 해서 값 띄우기*/
                if (it) {
                    binding.locationInfoTv.text =
                        viewModel.editProfileLocationCateName.value + " " + viewModel.editProfileLocationName.value
                    Log.d(
                        TAG, "${viewModel.editProfileLocationCateName.value} " +
                                "${viewModel.editProfileLocationName.value}"
                    )

                    if (viewModel.editProfileLocation.value != myProfileDB.location) {
                        viewModel.setEditProfileLocation.value = true
                        Log.d(TAG, "viewModel.setEditProfileLocation.value : ${viewModel.setEditProfileLocation.value}")
                    } else {
                        viewModel.setEditProfileLocation.value = false
                        Log.d(TAG, "viewModel.setEditProfileLocation.value : ${viewModel.setEditProfileLocation.value}")
                    }
                }
            })


        // 구력 관련
        for (i in 1..14) {
            val btnId: Int = resources.getIdentifier(
                "edit_profile_table_" + i.toString() + "_btn",
                "id",
                requireActivity().packageName
            )
            val btn: Button = requireView().findViewById(btnId)
            val num: String = i.toString()
            btn.text = resources.getString(
                resources.getIdentifier(
                    "signup_length_of_play_$num", "string",
                    requireActivity().packageName
                )
            )
            btn.tag = i.toString()
        }


        /* TODO 적용하기를 누를 수 있는 조건이 있을 때 적용하기 버튼 활성화 */

        binding.contentIntroductionEt.setOnFocusChangeListener(object : View.OnFocusChangeListener {
            override fun onFocusChange(v: View?, hasFocus: Boolean) {
                if (hasFocus) {
                    binding.guideIntroductionTv.setTextColor(ContextCompat.getColor(mContext, R.color.nero))
                    binding.contentIntroductionEt.setTextColor(ContextCompat.getColor(mContext, R.color.nero))
                    binding.dimensionIntroductionIv.setImageDrawable(
                        ContextCompat.getDrawable(
                            mContext,
                            R.drawable.ic_rectangler_introduction_on
                        )
                    )
                } else {
                    binding.guideIntroductionTv.setTextColor(ContextCompat.getColor(mContext, R.color.grey))
                    binding.contentIntroductionEt.setTextColor(ContextCompat.getColor(mContext, R.color.grey))
                    binding.dimensionIntroductionIv.setImageDrawable(
                        ContextCompat.getDrawable(
                            mContext,
                            R.drawable.ic_rectangler_introduction_off
                        )
                    )
                }
            }
        })

        val introductionEt = binding.contentIntroductionEt
        introductionEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                viewModel.setEditProfileIntroduction.value = false
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() == myProfileDB.introduce) {
                    viewModel.setEditProfileIntroduction.value = false
                } else {
                    if (s.toString().length > 0 && s.toString().length < 300) { /* EditText 에 글이 1 ~ 300 자면 파란 작성완료 버튼*/
                        viewModel.setEditProfileIntroduction.value = true
                    } else if (s.toString().length >= 300) {    /* EditText 에 글이 400 자보다 길면 */
                        viewModel.setEditProfileIntroduction.value = false
                        Toast.makeText(context, "자기소개는 최대 300자까지 입력 가능합니다.", Toast.LENGTH_LONG).show()
                    } else if (s.toString().length == 0) { /* EditText 에 글이 없으면 회색 작성완료 버튼*/
                        viewModel.setEditProfileIntroduction.value = false
                    }
                }
            }

        })

        viewModel.editProfileNickname.observe(viewLifecycleOwner, {
            if (it != myProfileDB.nickName) {
                viewModel.setEditProfileIsDuplicated.observe(viewLifecycleOwner, { it2 ->
                    if (it2) {
                        onApplyEnable()
                    } else {
                        onApplyDisable()
                    }
                })
            }
        })
        viewModel.editProfileNickname.observe(viewLifecycleOwner, {
            if (it == myProfileDB.nickName) {
                viewModel.setEditProfileLocation.observe(viewLifecycleOwner, { it3 ->
                    if (it3 && (viewModel.editProfileNickname.value == myProfileDB.nickName || viewModel.setEditProfileIsDuplicated.value == true)) {    /* 지역 변경 */
                        onApplyEnable()
                    }
                })
                viewModel.setEditProfileExperience.observe(viewLifecycleOwner, { it4 ->
                    if (it4 && (viewModel.editProfileNickname.value == myProfileDB.nickName || viewModel.setEditProfileIsDuplicated.value == true)) {
                        onApplyEnable()
                    }
                })
                viewModel.setEditProfileIntroduction.observe(viewLifecycleOwner, { it5 ->
                    if (it5 && (viewModel.editProfileNickname.value == myProfileDB.nickName || viewModel.setEditProfileIsDuplicated.value == true)) {
                        onApplyEnable()
                    }
                })
            } else {
                onApplyDisable()
            }
        })


        binding.activatingApplyBtn.setOnClickListener {
            val phoneNumber = myProfileDB.phoneNumber.toString()
            val nickname = binding.nicknameEt.text.toString()    ////
            val birth = myProfileDB.birth.toString()
            val gender = myProfileDB.gender!!.toInt()
            val locationIdx = viewModel.editProfileLocation.value!!.toInt()     ////
            val experienceIdx = viewModel.editProfileExperience.value!!.toInt()     ////
            val profileImg = viewModel.editProfileImg.value
            val introduction = binding.contentIntroductionEt.text.toString()            ////

            Log.d(
                TAG, " phoneNumber : $phoneNumber , nickname : $nickname , birth : $birth , gender : $gender ," +
                        " locationIdx : $locationIdx , experienceIdx : $experienceIdx , introduction : $introduction,"
            )

            //TODO : 프로필 이미지 변경 X 나머지 변경됨.
            EditProfilePutService.putEditProfile(
                this, phoneNumber, nickname, birth, gender,
                locationIdx, experienceIdx, introduction, myUserIdx
            )

            //TODO : 프로필 이미지 변경되고 나머지도 변경됨

            //TODO : 프로필 이미지만 변경됨


            Log.d(
                TAG, " phoneNumber : $phoneNumber , nickname : $nickname , birth : $birth , gender : $gender ," +
                        " locationIdx : $locationIdx , experienceIdx : $experienceIdx , introduction : $introduction  "
            )

        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("$TAG _ onDestroyView", "onDestroyView")
        savedState = saveState()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Log.d("Signup_Image_Upload_Dialog", "OnRequestPermissionsResult 호출댐.")
        val file_path = requireActivity().getExternalFilesDir(null).toString()
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 허용을 눌렀을 때 바로 카메라로 ㄱㄱ
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, CAMERA_PERMISSION_REQUEST)

                } else {
                    // 권한 거부 시 로그 띄우기
                    Log.d("Signup_Image_Upload_Dialog", "OnRequestPermissionsResult에서 카메라0 권한 거부.")
                    Toast.makeText(
                        requireContext(),
                        "프로필 사진을 업로드하려면 카메라 접근 권한을 허용해야 합니다.",
                        Toast.LENGTH_LONG
                    ).show()

                }
            }

//            multiplePermissionsCode1 -> {
//                var startCam = true
//                if (grantResults.isNotEmpty()) {
//                    for ((i, permission) in permissions.withIndex()) {
//                        // 권한이 없는 permission이 있다면
//                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                            Log.d("Signup", "사용하려면 권한 체크 해야되")
//                            Toast.makeText(
//                                requireContext(),
//                                "프로필 사진을 업로드하려면 카메라 접근 권한을 허용해야 합니다.",
//                                Toast.LENGTH_LONG
//                            ).show()
//                            startCam = false
//                        }
//                    }
//                    if (startCam) {
//                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//
//                        // 촬영한 사진이 저장될 파일 이름
//                        val file_name = "/temp_${System.currentTimeMillis()}.jpg"
//                        // 경로 + 파일 이름
//                        val pic_path = "$file_path/$file_name"
//                        val file = File(pic_path)
//
//                        // 사진이 저장될 위치를 관리하는 Uri 객체
//                        // val contentUri = Uri(pic_path) // 예전에는 파일명을 기술하면 바로 접근 가능
//                        // -> 현재 안드로이드 OS 6.0 부터는 OS에서 해당 경로를 집어 넣으면 이 경로로 접근할 수 있는지 없는지를 판단. 접근할 수 있으면 Uri 객체를 넘겨줌.
//                        contentUri = FileProvider.getUriForFile(
//                            requireContext(),
//                            "com.duos.camera.file_provider",
//                            file
//                        )
//
//                        intent.putExtra(MediaStore.EXTRA_OUTPUT, contentUri)
//                        startActivityForResult(intent, 200)
//                    }
//                }
//            }
            multiplePermissionsCode2 -> {
                var startAlb = true
                if (grantResults.isNotEmpty()) {
                    for ((i, permission) in permissions.withIndex()) {
                        // 권한이 없는 permission이 있다면
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            Log.d("Signup", "사용하려면 권한 체크 해야되")
                            Toast.makeText(
                                requireContext(),
                                "프로필 사진을 업로드하려면 카메라 접근 권한을 허용해야 합니다.",
                                Toast.LENGTH_LONG
                            ).show()
                            startAlb = false
                        }
                    }
                    if (startAlb) {
                        // 앨범에서 사진을 선택할 수 있도록 Intent
                        val albumIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        // 실행할 액티비티의 타입 설정(이미지를 선택할 수 있는 것)
                        albumIntent.type = "image/*"
                        // 선택할 파일의 타입 지정
                        val mimeType = arrayOf("image/*")
                        albumIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeType)
                        startActivityForResult(albumIntent, multiplePermissionsCode2)
                    }
                }
            }
        }
    }

    @SuppressLint("Recycle")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CAMERA_PERMISSION_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    // data : Intent 안에 사진 정보가 들어감
                    val bitmap = data?.getParcelableExtra<Bitmap>("data")
                    binding.myProfileImgIv.setImageBitmap(bitmap)
                    binding.myProfileImgIv.scaleType = ImageView.ScaleType.FIT_XY
                }
            }

//            multiplePermissionsCode1 -> {
//                if (resultCode == Activity.RESULT_OK) {
//                    val bitmap = BitmapFactory.decodeFile(contentUri.path)
//                    // 사진 조정 된것
//                    val degree = getDegree(
//                        contentUri,
//                        contentUri.path!!
//                    )   // contentUri 는 안드로이드 10버전 이상, contentUri.path!! 는 9버전 이하를 위해 넣음
//                    val bitmap2 = resizeBitmap(1024, bitmap)
//                    val bitmap3 = rotateBitmap(bitmap2, degree)
//
//                    binding.myProfileImgIv.setImageBitmap(bitmap3)
//
//
////                    // 사진 파일 삭제한다.
////                    val file = File(contentUri.path)
////                    file.delete()
//
//                }
//            }
            multiplePermissionsCode2 -> {
                if (resultCode == Activity.RESULT_OK) {
                    // 선택한 이미지의 경로 데이터를 관리하는 Uri 객체를 추출

                    val uri = data?.data

                    if (uri != null) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            // 안드로이드 10버전 이상
                            val source =
                                ImageDecoder.createSource(requireActivity().contentResolver, uri)
                            val bitmap = ImageDecoder.decodeBitmap(source)
                            binding.myProfileImgIv.setImageBitmap(bitmap)
                        } else {
                            val cursor =
                                requireActivity().contentResolver.query(uri, null, null, null, null)
                            if (cursor != null) {
                                cursor.moveToNext()
                                // 이미지 경로를 가져온다.
                                val index = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                                val source = cursor.getString(index)
                                // 이미지 생성
                                val bitmap = BitmapFactory.decodeFile(source)
                                binding.myProfileImgIv.setImageBitmap(bitmap)
                            }
                        }
                    }
                }
            }
        }
    }


    override fun onGetEditProfileItemSuccess(getEditProfileResDto: GetEditProfileResDto) {
        val db = UserDatabase.getInstance(requireContext().applicationContext)
//        val db = UserDatabase.getInstance(requireContext())
        val myProfileDB = db!!.userDao().getUser(myUserIdx) /* 룸에 내 idx에 맞는 데이터 있으면 불러오기... */
        Log.d(TAG,"처음 내 데이터 가져왔을 때 내 UserDB : ${myProfileDB}")
        // 닉네임, 지역, 소개, 프로필 이미지, 구력
        binding.nicknameEt.hint = getEditProfileResDto.existingProfileInfo.nickname
        viewModel.editProfileNickname.value = getEditProfileResDto.existingProfileInfo.nickname
        Log.d(TAG, "처음 API로 내 프로필 데이터 가져왔을 때 editProfileNickname : ${viewModel.editProfileNickname.value}")
//        binding.nicknameEt.text = getEditProfileResDto.existingProfileInfo.nickname
        binding.locationInfoTv.text = toLocationStr(myProfileDB.location!!)
        // 소개글 API 로 값 가져오기, Editable 형태로 넣기
        inputIntroduction = getEditProfileResDto.existingProfileInfo.introduction
        binding.contentIntroductionEt.text = Editable.Factory.getInstance().newEditable(inputIntroduction)
        Glide.with(binding.myProfileImgIv.context)
            .load(getEditProfileResDto.existingProfileInfo.profileImgUrl)
            .into(binding.myProfileImgIv)
        binding.editProfileTableLayoutTl.checkedRadioButtonId = getEditProfileResDto.existingProfileInfo.experienceIdx



    }

    override fun onGetEditItemFailure(code: Int, message: String) {
        Log.d(TAG, "code: $code , message : $message ")
        val db = UserDatabase.getInstance(requireContext().applicationContext)
//        val db = UserDatabase.getInstance(requireContext())
        val myProfileDB = db!!.userDao().getUser(myUserIdx) /* 룸에 내 idx에 맞는 데이터 있으면 불러오기... */

        binding.nicknameEt.hint = myProfileDB.nickName
        binding.locationInfoTv.hint = toLocationStr(myProfileDB.location!!)
        inputIntroduction = myProfileDB.introduce.toString()
        binding.contentIntroductionEt.text = Editable.Factory.getInstance().newEditable(inputIntroduction)
        binding.editProfileTableLayoutTl.checkedRadioButtonId = myProfileDB.experience!!

        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        Toast.makeText(context, "네트워크 상태를 확인하세요", Toast.LENGTH_LONG).show()

    }


    override fun onPutEditProfileItemSuccess(
        editPutProfileResponse: EditProfilePutResponse,
        message: String
    ) {
        Log.d(TAG, "onPutEditProfileItemSuccess")
        // DB 업데이트
        val db = UserDatabase.getInstance(requireContext().applicationContext)
//        val db = UserDatabase.getInstance(requireContext())
        val myProfileDB = db!!.userDao().getUser(myUserIdx) /* 룸에 내 idx에 맞는 데이터 있으면 불러오기... */

        myProfileDB.nickName = viewModel.editProfileNickname.value!!
        myProfileDB.gender = myProfileDB.gender!!.toInt()
        myProfileDB.location = viewModel.editProfileLocation.value!!.toInt()     ////
        myProfileDB.experience = viewModel.editProfileExperience.value!!.toInt()     ////
        myProfileDB.introduce = viewModel.editProfileIntroduce.value            ////
        Log.d(TAG, "현재 뷰모델의 editProfileIntroduce는 ? ${viewModel.editProfileIntroduce.value}")
        Log.d(TAG, "현재 뷰모델의 editProfileNickname ? ${viewModel.editProfileNickname.value}")
        Log.d(TAG, "Put 이후에 DB에 데이터 ${myProfileDB.toString()}")
        // go to MyPageFrag!
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        val intent = Intent(activity, MyProfileActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun onPutEditProfileItemFailure(code: Int, message: String) {
        Log.d(TAG, "onPutEditProfileItemFailure")
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    // 사진의 사이즈를 조정하는 메서드
    fun resizeBitmap(targetWidth: Int, source: Bitmap): Bitmap {
        // 이미지 비율 계산
        val ratio = targetWidth.toDouble() / source.width.toDouble()
        // 보정될 세로 길이 구하기
        val targetHeight = (source.height * ratio).toInt()
        // 크기를 조정한 bitmap 객체를 생성
        val result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false)
        return result

    }

    // 이미지의 회전 각도값을 구하기
    // 11버전 이상부터 달라짐 (외부저장소 보안 때문에)
    fun getDegree(uri: Uri, source: String): Float {
        var exif: ExifInterface? = null

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val photoUri = MediaStore.setRequireOriginal(uri)
            val stream = requireActivity().contentResolver.openInputStream(photoUri)
            exif = ExifInterface(source)
        } else {
            exif = ExifInterface(source)
        }
        var degree = 0
        val ori = exif.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            -1
        )   // 만약 회전값이 저장이 안되어 있으면 default값으로 -1 넣기 (0 넣으면 안댐)
        when (ori) {
            ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
            ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
            ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
        }
        return degree.toFloat()
    }

    // 사진 돌리기
    fun rotateBitmap(bitmap: Bitmap, degree: Float): Bitmap {

        // 각도값을 관리하는 객체
        val matrix = Matrix()
        matrix.postRotate(degree)
        // 회전된 이미지를 받아온다.
        val bitmap2 = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return bitmap2
    }

    override fun onSaveInstanceState(outState: Bundle) {
        Log.d("onsave", "ㅎㅇ")
        super.onSaveInstanceState(outState)
        outState.putBundle(
            "savedState",
            if (savedState != null) savedState else saveState()
        )
    }

    private fun saveState(): Bundle { /* called either from onDestroyView() or onSaveInstanceState() */
        val state = Bundle()
        state.putCharSequence("save", "true")

        return state
    }

    fun toLocationStr(index: Int): String? {
        val array = resources.getStringArray(R.array.location_full_name)
        return array[index]

    }

    @SuppressLint("ResourceAsColor", "UseCompatLoadingForDrawables")
    private fun onApplyEnable() {
        binding.activatingApplyBtn.setTextColor(ContextCompat.getColor(mContext, R.color.white))
        binding.activatingApplyBtn.background =
            requireActivity().getDrawable(R.drawable.signup_next_btn_done_rectangular)
        binding.activatingApplyBtn.isEnabled = true


    }

    @SuppressLint("ResourceAsColor", "UseCompatLoadingForDrawables")
    private fun onApplyDisable() {
        binding.activatingApplyBtn.background =
            requireActivity().getDrawable(R.drawable.signup_next_btn_rectangular)
        binding.activatingApplyBtn.setTextColor(ContextCompat.getColor(mContext, R.color.dark_gray_B0))
        binding.activatingApplyBtn.isEnabled = false

    }


    // 중복 확인 됨!!
    override fun onGetDuplicateNicknameSuccess(duplicateNicknameResponse: DuplicateNicknameResponse) {
        viewModel.setEditProfileNickName.value = true
        viewModel.isValidNicknameEditCondition.value = true
        viewModel.setEditProfileIsDuplicated.value = true
        Log.d(TAG, "viewModel.setEditProfileNickName.value : ${viewModel.setEditProfileNickName.value}")
        binding.btnCheckDuplicationTv.setBackgroundResource(R.drawable.signup_phone_verifying_done_rectangular)
        binding.btnCheckDuplicationTv.setTextColor(ContextCompat.getColor(mContext, R.color.dark_gray_A))
        binding.btnCheckDuplicationTv.isEnabled = false
        binding.nicknameEtField.isEndIconVisible = false
        binding.nicknameEt.isEnabled = false
    }

    override fun onGetDuplicateNicknameFailure(code: Int, message: String) {
        viewModel.setEditProfileIsDuplicated.value = false
        binding.nickNameErrorTv.visibility = View.VISIBLE
        binding.nickNameErrorTv.text = message
        binding.nicknameCheckIconIv.visibility = View.VISIBLE
        binding.nicknameCheckIconIv.setImageResource(R.drawable.ic_signup_nickname_unable)
        binding.btnCheckDuplicationTv.setBackgroundResource(R.drawable.signup_phone_verifying_rectangular)
        binding.btnCheckDuplicationTv.setTextColor(ContextCompat.getColor(mContext, R.color.dark_gray_A))
    }

    fun setRadioButton(tag: Int) {
        viewModel.editProfileExperience.value = tag
        Log.d(TAG, "setRadioButton() ${viewModel.editProfileExperience.value}")
        if (viewModel.editProfileExperience.value != originExperience) {
            viewModel.setEditProfileExperience.value = true
            Log.d(TAG, " viewModel.setEditProfileExperience.value ${viewModel.setEditProfileExperience.value}")
        } else {
            viewModel.setEditProfileExperience.value = false
            Log.d(TAG, " viewModel.setEditProfileExperience.value ${viewModel.setEditProfileExperience.value}")
        }
    }


}