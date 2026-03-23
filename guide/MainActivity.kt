package com.blejt.jtcontrol.Activity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.activity.OnBackPressedCallback
import android.widget.Toast
import com.blejt.jtcontrol.R
import com.blejt.jtcontrol.databinding.ActivityMainBinding
import java.util.Calendar

class MainActivity : BaseActivity() {

    lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private var isPowerOn: Boolean = false
    private lateinit var parsed: DataMap
    private val DEFAULT_PW = "1234"

    private val sendDataRunnable = object : Runnable {
        override fun run() {

            // bluetoothService가 null이거나, 아직 데이터를 쓸 준비가 안됐으면 스킵
            if (bluetoothService == null) {
                handler.postDelayed(this, 500)
                return
            }

            val dataToSend = byteArrayOf(0xfa.toByte(), 0x50, 0x01, 0x01, 0x00)
            val xorValue = calculateXOR(dataToSend)
            val dataWithXOR = dataToSend + byteArrayOf(xorValue)
            val sumByte = calculateSUM(dataWithXOR)
            val finalPacket = dataWithXOR + byteArrayOf(sumByte)

            bluetoothService?.writeToCharacteristic(finalPacket)

            // 다시 2초 뒤에 자기 자신을 실행하여 주기적 전송 유지
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Log.i("MainActivity", "onCreate")

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("연결해제")
                        .setMessage("블루투스 연결을 해제하시겠습니까?")
                        .setPositiveButton("네") { dialog, _ ->
                            dialog.dismiss()
                            bluetoothService?.closeBluetooth()
                            startActivity(Intent(this@MainActivity, ScanActivity::class.java))
                            finish()
                        }
                        .setNegativeButton("아니요") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        )

        binding.deviceTimeset.setOnClickListener {
            // 다이얼로그 생성
            AlertDialog.Builder(this)
                .setTitle("시간 설정")
                .setMessage("시간 설정을 진행하시겠습니까?")
                .setPositiveButton("네") { dialog, _ ->
                    dialog.dismiss()
                    sendTimeSetData()
                }
                .setNegativeButton("아니요") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.powerBtn.setOnClickListener {

            showConfirmationDialog(
                title = "전원제어",
                message = "전원 명령을 전송하시겠습니까?",
                onConfirm = {
                    val powerStateByte: Byte
                    when (isPowerOn) {
                        true -> {
                            powerStateByte = 0x00
                        }

                        false -> {
                            powerStateByte = 0x01
                        }
                    }

                    val dataWithoutCheck = byteArrayOf(
                        0xfa.toByte(), 0x50, 0x01, 0x40, 0x01, powerStateByte
                    )

                    val xorValue = calculateXOR(dataWithoutCheck)
                    val dataWithXOR = dataWithoutCheck + byteArrayOf(xorValue)
                    val sumByte = calculateSUM(dataWithXOR)
                    val finalPacket = dataWithXOR + byteArrayOf(sumByte)

                    bluetoothService?.writeToCharacteristic(finalPacket)

                    Log.e("DEBUG", "Power Packet: " + finalPacket.joinToString {
                        String.format("0x%02X", it)
                    })
                }
            )
        }

        binding.subBtn.setOnClickListener {
            sendTemperatureChange(false)
        }

        binding.plusBtn.setOnClickListener {
            sendTemperatureChange(true)
        }

        binding.coldBtn.setOnClickListener {

            showConfirmationDialog(
                title = "기기제어",
                message = "냉방 명령을 전송하시겠습니까?",
                onConfirm = {
                    val dataWithoutCheck = byteArrayOf(
                        0xfa.toByte(), 0x50, 0x01, 0x41, 0x01, 0x00
                    )

                    val xorValue = calculateXOR(dataWithoutCheck)
                    val dataWithXOR = dataWithoutCheck + byteArrayOf(xorValue)
                    val sumByte = calculateSUM(dataWithXOR)
                    val finalPacket = dataWithXOR + byteArrayOf(sumByte)

                    bluetoothService?.writeToCharacteristic(finalPacket)

                    Log.e("DEBUG", "Cold Packet: " + finalPacket.joinToString {
                        String.format("0x%02X", it)
                    })
                }
            )
        }
        binding.heatBtn.setOnClickListener {

            showConfirmationDialog(
                title = "기기제어",
                message = "난방 명령을 전송하시겠습니까?",
                onConfirm = {
                    val dataWithoutCheck = byteArrayOf(
                        0xfa.toByte(), 0x50, 0x01, 0x41, 0x01, 0x01
                    )

                    val xorValue = calculateXOR(dataWithoutCheck)
                    val dataWithXOR = dataWithoutCheck + byteArrayOf(xorValue)
                    val sumByte = calculateSUM(dataWithXOR)
                    val finalPacket = dataWithXOR + byteArrayOf(sumByte)

                    bluetoothService?.writeToCharacteristic(finalPacket)

                    Log.e("DEBUG", "Heat Packet: " + finalPacket.joinToString {
                        String.format("0x%02X", it)
                    })
                }
            )
        }
        binding.windBtn.setOnClickListener {

            showConfirmationDialog(
                title = "기기제어",
                message = "송풍 명령을 전송하시겠습니까?",
                onConfirm = {
                    val dataWithoutCheck = byteArrayOf(
                        0xfa.toByte(), 0x50, 0x01, 0x41, 0x01, 0x02
                    )

                    val xorValue = calculateXOR(dataWithoutCheck)
                    val dataWithXOR = dataWithoutCheck + byteArrayOf(xorValue)
                    val sumByte = calculateSUM(dataWithXOR)
                    val finalPacket = dataWithXOR + byteArrayOf(sumByte)

                    bluetoothService?.writeToCharacteristic(finalPacket)

                    Log.e("DEBUG", "Wind Packet: " + finalPacket.joinToString {
                        String.format("0x%02X", it)
                    })
                }
            )
        }

        binding.filterBtn.setOnClickListener {
            // 다이얼로그 생성
            AlertDialog.Builder(this)
                .setTitle("필터 시간 초기화")
                .setMessage("필터시간을 초기화 하시겠습니까?")
                .setPositiveButton("네") { dialog, _ ->
                    dialog.dismiss()
                    sendTimeInitData()
                }
                .setNegativeButton("아니요") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.initBtn.setOnClickListener {
            // 다이얼로그 생성
            AlertDialog.Builder(this)
                .setTitle("필터 시간 초기화")
                .setMessage("필터시간을 초기화 하시겠습니까?")
                .setPositiveButton("네") { dialog, _ ->
                    dialog.dismiss()
                    sendTimeInitData()
                }
                .setNegativeButton("아니요") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.wind1Btn.setOnClickListener {
            showConfirmationDialog(
                title = "풍속제어",
                message = "약풍 명령을 전송하시겠습니까?",
                onConfirm = {
                    val dataWithoutCheck = byteArrayOf(
                        0xfa.toByte(), 0x50, 0x01, 0x42, 0x01, 0x01
                    )

                    val xorValue = calculateXOR(dataWithoutCheck)
                    val dataWithXOR = dataWithoutCheck + byteArrayOf(xorValue)
                    val sumByte = calculateSUM(dataWithXOR)
                    val finalPacket = dataWithXOR + byteArrayOf(sumByte)

                    bluetoothService?.writeToCharacteristic(finalPacket)

                    Log.e("DEBUG", "Wind Packet: " + finalPacket.joinToString {
                        String.format("0x%02X", it)
                    })
                }
            )
        }
        binding.wind2Btn.setOnClickListener {
            showConfirmationDialog(
                title = "풍속제어",
                message = "중풍 명령을 전송하시겠습니까?",
                onConfirm = {
                    val dataWithoutCheck = byteArrayOf(
                        0xfa.toByte(), 0x50, 0x01, 0x42, 0x01, 0x02
                    )

                    val xorValue = calculateXOR(dataWithoutCheck)
                    val dataWithXOR = dataWithoutCheck + byteArrayOf(xorValue)
                    val sumByte = calculateSUM(dataWithXOR)
                    val finalPacket = dataWithXOR + byteArrayOf(sumByte)

                    bluetoothService?.writeToCharacteristic(finalPacket)

                    Log.e("DEBUG", "Wind Packet: " + finalPacket.joinToString {
                        String.format("0x%02X", it)
                    })
                }
            )
        }
        binding.wind3Btn.setOnClickListener {
            showConfirmationDialog(
                title = "풍속제어",
                message = "강풍 명령을 전송하시겠습니까?",
                onConfirm = {
                    val dataWithoutCheck = byteArrayOf(
                        0xfa.toByte(), 0x50, 0x01, 0x42, 0x01, 0x03
                    )

                    val xorValue = calculateXOR(dataWithoutCheck)
                    val dataWithXOR = dataWithoutCheck + byteArrayOf(xorValue)
                    val sumByte = calculateSUM(dataWithXOR)
                    val finalPacket = dataWithXOR + byteArrayOf(sumByte)

                    bluetoothService?.writeToCharacteristic(finalPacket)

                    Log.e("DEBUG", "Wind Packet: " + finalPacket.joinToString {
                        String.format("0x%02X", it)
                    })
                }
            )
        }
        binding.topSettingBtn.setOnClickListener {
            showPasswordDialogIfNeeded()
        }

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(this, ReservationActivity::class.java))
            finish()
//            val items = arrayOf("기기설정", "예약설정")
//            AlertDialog.Builder(this)
//                .setTitle("설정")
//                .setItems(items) { _, which ->
//                    when (which) {
//                        0 -> {
//                            startActivity(Intent(this, SettingActivity::class.java))
//                            finish()
//                        }
//
//                        1 -> {
//                            startActivity(Intent(this, ReservationActivity::class.java))
//                            finish()
//                        }
//                    }
//                }
//                .show()
        }

        binding.disconnectTxt.setOnClickListener {
            showConfirmationDialog(
                title = "연결해제",
                message = "블루투스 연결을 해제하시겠습니까?",
                onConfirm = {
                    //bluetoothService?.disconnect()
                    bluetoothService?.closeBluetooth()
                    startActivity(Intent(this, ScanActivity::class.java))
                    finish()
                }
            )
        }

        binding.errorBtn.setOnClickListener {
            if (!::parsed.isInitialized) {
                Toast.makeText(this, "데이터를 받아오지 못했습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val errorMessage = parseErrorString(parsed.Error1, parsed.Error2)

            // 에러 항목을 Dialog로 표시
            AlertDialog.Builder(this)
                .setTitle("에러 정보")
                .setMessage(errorMessage)
                .setPositiveButton("확인") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        binding.stateinfoBtn.setOnClickListener {
            startActivity(Intent(this, StateInfoActivity::class.java))
            finish()
        }

    }

    /*override fun onBackPressedDispatcher() {
        showConfirmationDialog(
            title = "연결해제",
            message = "블루투스 연결을 해제하시겠습니까?",
            onConfirm = {
                // bluetoothService?.disconnect()
                bluetoothService?.closeBluetooth()
                //bluetoothService?.refreshGattCache()
                startActivity(Intent(this, ScanActivity::class.java))
                finish()
            }
        )
    }*/


    private fun parseErrorString(error1: Int, error2: Int): String {
        val errorList = mutableListOf<String>()

        // == Error1 파싱 ==
        if ((error1 and 0x01) != 0) {
            errorList.add("- 과열 경보")
        }
        if ((error1 and 0x02) != 0) {
            errorList.add("- 과냉 경보")
        }
        if ((error1 and 0x04) != 0) {
            errorList.add("- 과전류 에러")
        }
        if ((error1 and 0x08) != 0) {
            errorList.add("- 히터 온도센서 에러")
        }
        if ((error1 and 0x10) != 0) {
            errorList.add("- Comp 온도센서 에러")
        }
        if ((error1 and 0x20) != 0) {
            errorList.add("- 온도센서 에러")
        }
        if ((error1 and 0x40) != 0) {
            errorList.add("- 응축팬 에러")
        }
        if ((error1 and 0x80) != 0) {
            errorList.add("- 급기팬 에러")
        }

        // == Error2 파싱 ==
        if ((error2 and 0x01) != 0) {
            errorList.add("- RTC 에러")
        }
        if ((error2 and 0x02) != 0) {
            errorList.add("- 히터 에러")
        }
        if ((error2 and 0x04) != 0) {
            errorList.add("- COMP 에러")
        }
        if ((error2 and 0x08) != 0) {
            errorList.add("- RF 통신 에러")
        }
        if ((error2 and 0x10) != 0) {
            errorList.add("- BLE 통신 에러")
        }

        // 에러가 하나도 없다면
        if (errorList.isEmpty()) {
            errorList.add("에러가 없습니다.")
        }

        return errorList.joinToString("\n")
    }


    override fun onNotificationReceived(data: ByteArray) {
        // 바이트 배열을 16진수 문자열로 변환
        val hexString = data.joinToString(" ") { String.format("0x%02X", it) }
        Log.e("DEBUG", "{MainActivity} Notification received (Hex): $hexString")

        updateUI(data)
    }

    override fun onResume() {
        super.onResume()
        // 액티비티가 화면에 보일 때 전송 시작
        handler.postDelayed(sendDataRunnable, 100)
    }

    override fun onPause() {
        super.onPause()
        // 액티비티가 화면에서 사라질 때 전송 중단
        handler.removeCallbacks(sendDataRunnable)
    }

    private fun sendTemperatureChange(isIncrease: Boolean) {
        if (!::parsed.isInitialized) {
            Toast.makeText(this, "데이터를 받아오지 못했습니다.", Toast.LENGTH_SHORT).show()
            Log.e("MainActivity", "Parsed data not initialized.")
            return
        }
        val mainStatus = (parsed.Operation.toInt() shr 4) and 0x03
        if (mainStatus != 0 && mainStatus != 1) {
            Toast.makeText(this, "송풍 모드에서는 온도를 조절할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        val currentTemp = if (mainStatus == 0) parsed.ColdSettingTemp else parsed.HeatSettingTemp

        var newTemp = if (isIncrease) currentTemp + 1 else currentTemp - 1
        newTemp = newTemp.coerceIn(20, 30)

        // Update the UI
        binding.mainTempText.text = "${newTemp}"  //2025-03-19 ℃ 생략

        // Determine the CMD based on the mode
        val cmd: Byte = when (mainStatus) {
            0 -> 0x43.toByte() // Cooling
            1 -> 0x44.toByte() // Heating
            else -> 0x00.toByte() // Default (should not occur)
        }

        // Construct the data packet
        val dataWithoutCheck = byteArrayOf(
            0xfa.toByte(), // STX
            0x50,          // DEVICE ID
            0x01,          // Board ID
            cmd,           // CMD (0x43 or 0x44)
            0x01,          // Length
            newTemp.toByte() // New Temperature
        )
        val xorValue = calculateXOR(dataWithoutCheck)
        val dataWithXOR = dataWithoutCheck + byteArrayOf(xorValue)
        val sumByte = calculateSUM(dataWithXOR)
        val finalPacket = dataWithXOR + byteArrayOf(sumByte)

        bluetoothService?.writeToCharacteristic(finalPacket)

        Log.e("DEBUG", "Temp Change Packet: " + finalPacket.joinToString(" ") { String.format("0x%02X", it) })

    }

    private fun sendTimeSetData() {
        val cal = Calendar.getInstance()

        val year = (cal.get(Calendar.YEAR) % 100).toByte()
        val month = (cal.get(Calendar.MONTH) + 1).toByte()
        val day = cal.get(Calendar.DAY_OF_MONTH).toByte()
        val hour = cal.get(Calendar.HOUR_OF_DAY).toByte()
        val minute = cal.get(Calendar.MINUTE).toByte()
        val second = cal.get(Calendar.SECOND).toByte()

        val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
            1 -> 7  // 일요일
            2 -> 1  // 월요일
            3 -> 2  // 화요일
            4 -> 3  // 수요일
            5 -> 4  // 목요일
            6 -> 5  // 금요일
            7 -> 6  // 토요일
            else -> 1
        }.toByte()

        val dataWithoutCheck = byteArrayOf(
            0xfa.toByte(), 0x50, 0x01, 0x49, 0x07,
            year, month, day, hour, minute, second, dayOfWeek
        )

        val xorValue = calculateXOR(dataWithoutCheck)
        val dataWithXOR = dataWithoutCheck + byteArrayOf(xorValue)
        val sumByte = calculateSUM(dataWithXOR)
        val finalPacket = dataWithXOR + byteArrayOf(sumByte)

        bluetoothService?.writeToCharacteristic(finalPacket)

        Log.e("DEBUG", "TimeSet Packet: " + finalPacket.joinToString {
            String.format("0x%02X", it)
        })
    }

    fun sendTimeInitData() {

        val dataWithoutCheck = byteArrayOf(
            0xfa.toByte(), 0x50, 0x01, 0x4a, 0x00
        )

        val xorValue = calculateXOR(dataWithoutCheck)
        val dataWithXOR = dataWithoutCheck + byteArrayOf(xorValue)
        val sumByte = calculateSUM(dataWithXOR)
        val finalPacket = dataWithXOR + byteArrayOf(sumByte)

        bluetoothService?.writeToCharacteristic(finalPacket)

        Log.e("DEBUG", "Init Packet: " + finalPacket.joinToString {
            String.format("0x%02X", it)
        })
    }


    private fun showConfirmationDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("예") { dialog, _ ->
                dialog.dismiss()
                onConfirm()
            }
            .setNegativeButton("아니요") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun getSavedPassword(): String {
        val prefs = getSharedPreferences("myPrefs", MODE_PRIVATE)
        return prefs.getString("passwordKey", DEFAULT_PW) ?: DEFAULT_PW
    }

    private fun startSettingActivity(){
        startActivity(Intent(this, SettingActivity::class.java))
        finish()
    }

    private fun showPasswordDialogIfNeeded() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_confirmpass, null)
        val afterPasswordEditText = dialogView.findViewById<EditText>(R.id.after_password)

        // 현재 저장된 비밀번호를 읽어서 확인 로직
        val currentPw = getSavedPassword()

        AlertDialog.Builder(this).apply {
            setTitle("비밀번호 확인")
            setView(dialogView)
            setPositiveButton("확인") { dialog, _ ->
                val inputPw = afterPasswordEditText.text.toString().trim()
                if (inputPw == currentPw) {
                    // 비밀번호 맞으면 스피너를 오픈
                    dialog.dismiss()
                    startSettingActivity()
                } else {
                    Toast.makeText(
                        this@MainActivity,
                        "비밀번호가 틀렸습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                    dialog.dismiss()
                }
            }
            setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }
            create()
            show()
        }
    }

    @SuppressLint("DefaultLocale", "SetTextI18n")
    fun updateUI(data:ByteArray){

        parsed = super.parseReceiveData(data) ?: return

        binding.boardIdTxt.text = "보드 ID : ${parsed.BoardID}"

        val dayOfWeekString = when (parsed.NowTimeDays) {
            0 -> "월요일"
            1 -> "화요일"
            2 -> "수요일"
            3 -> "목요일"
            4 -> "금요일"
            5 -> "토요일"
            6 -> "일요일"
            0 -> "Null"       // 세팅 전인 경우
            else -> "Unknown"  // 예상치 못한 값 처리
        }
        val deviceTimeSet = String.format(
            "%02d-%02d-%02d (%s) %02d:%02d:%02d",
            parsed.NowTimeYear,
            parsed.NowTimeMonth,
            parsed.NowTimeDay,
            dayOfWeekString,
            parsed.NowTimeHour,
            parsed.NowTimeMin,
            parsed.NowTimeSec
        )

        val powerStatus = (parsed.Operation.toInt() shr 6) and 0x03  // 0x03 = 0b00000011
        isPowerOn = when (powerStatus) {
            0 -> false  // 00: 전원 OFF
            1 -> true   // 01: 전원 ON
            2 -> false  // 10: 필요에 따라 설정 (예: OFF)
            3 -> true   // 11: 필요에 따라 설정 (예: ON)
            else -> false
        }

        val windState = parsed.Operation.toInt() and 0x0f
        Log.i("Debug","Wind Mode value = $windState")

        val MainStatus = (parsed.Operation.toInt() shr 4) and 0x03  // 0x03 = 0b00000011
        Log.i("Debug","Operation Mode value = $MainStatus")

        // 에러 파싱
        val errorMessage = parseErrorString(parsed.Error1, parsed.Error2)
        val hasErrors = errorMessage != "에러가 없습니다."

        runOnUiThread {

            if (hasErrors) {
                binding.errorBtn.setTextColor(Color.parseColor("#FF0000"))
            } else {
                binding.errorBtn.setTextColor(Color.parseColor("#505050"))
            }

            binding.deviceTime.setText(deviceTimeSet)

            // Byte 타입일 경우
            val tempByte = parsed.NowTemp.toInt()

            // ERROR 상태 확인
            if ((tempByte and 0xFF) == 0xFF) {
                binding.nowTemp.text = "ERROR"
                binding.nowTemp.setTextColor(Color.RED)
            } else {
                // bit7 확인 (음수 여부)
                val isNegative = (tempByte and 0x80) != 0
                // bit0~6 추출
                val tempValue = tempByte and 0x7F

                // 음수인 경우 온도 값을 음수로 변환
                val displayTemp = if (isNegative) {
                    -tempValue
                } else {
                    tempValue
                }

                binding.nowTemp.text = "${displayTemp}°"
                // 음수일 경우 색상을 다르게 설정할 수도 있습니다.
                binding.nowTemp.setTextColor(if (isNegative) Color.BLUE else Color.BLACK)
            }



            when (isPowerOn) {
                true -> {
                    binding.powerBtn.setBackgroundResource(R.drawable.power_on_icon)
                    binding.powerStateTxt.text = "운전"
                    binding.powerStateTxt.setTextColor(Color.BLUE)
                }

                false -> {
                    binding.powerBtn.setBackgroundResource(R.drawable.power_off_icon)
                    binding.powerStateTxt.text = "정지"
                    binding.powerStateTxt.setTextColor(Color.RED)
                }
            }

            val fanSpeed = parsed.Operation.toInt() and 0x0F  // 0b00001111 (0~3비트 추출)

            if (fanSpeed == 0) {
                binding.powerBtn.setBackgroundResource(R.drawable.power_off_icon)
                binding.powerStateTxt.text = "정지"
                binding.powerStateTxt.setTextColor(Color.RED)
            }

            when (MainStatus) {
                0 -> {                                                                              // 냉방모드
                    binding.stateComp.setTextColor(Color.parseColor("#399FFD"))
                    binding.stateHeat.setTextColor(Color.parseColor("#C8C8C8"))
                    binding.stateWind.setTextColor(Color.parseColor("#C8C8C8"))
                    binding.coldBtn.setBackgroundResource(R.drawable.cold_icon2)
                    binding.heatBtn.setBackgroundResource(R.drawable.heat_icon1)
                    binding.windBtn.setBackgroundResource(R.drawable.wind_icon1)
                    binding.mainTempUnit.visibility = View.VISIBLE
                    binding.mainTempText.text = parsed.ColdSettingTemp.toString()
                }

                1 -> {                                                                              // 난방모드
                    binding.stateComp.setTextColor(Color.parseColor("#C8C8C8"))
                    binding.stateHeat.setTextColor(Color.parseColor("#FF0000"))
                    binding.stateWind.setTextColor(Color.parseColor("#C8C8C8"))
                    binding.coldBtn.setBackgroundResource(R.drawable.cold_icon1)
                    binding.heatBtn.setBackgroundResource(R.drawable.heat_icon2)
                    binding.windBtn.setBackgroundResource(R.drawable.wind_icon1)
                    binding.mainTempUnit.visibility = View.VISIBLE
                    binding.mainTempText.text = parsed.HeatSettingTemp.toString()
                }

                2 -> {                                                                              // 송풍모드
                    binding.stateComp.setTextColor(Color.parseColor("#C8C8C8"))
                    binding.stateHeat.setTextColor(Color.parseColor("#C8C8C8"))
                    binding.stateWind.setTextColor(Color.parseColor("#00E676"))
                    binding.coldBtn.setBackgroundResource(R.drawable.cold_icon1)
                    binding.heatBtn.setBackgroundResource(R.drawable.heat_icon1)
                    binding.windBtn.setBackgroundResource(R.drawable.wind_icon2)
                    binding.mainTempUnit.visibility = View.GONE
                    binding.mainTempText.text = "---"
                    binding.windIcon2.setBackgroundResource(R.drawable.green14)
                }
            }
            if (MainStatus != 2) {
                binding.windIcon2.background = null
            }
            when (windState) {
                0 -> {
                    binding.wind1Btn.setBackgroundResource(R.drawable.wind1_icon1)
                    binding.wind2Btn.setBackgroundResource(R.drawable.wind2_icon1)
                    binding.wind3Btn.setBackgroundResource(R.drawable.wind3_icon1)
                }

                1 -> {
                    binding.wind1Btn.setBackgroundResource(R.drawable.wind1_icon2)
                    binding.wind2Btn.setBackgroundResource(R.drawable.wind2_icon1)
                    binding.wind3Btn.setBackgroundResource(R.drawable.wind3_icon1)
                }

                2 -> {
                    binding.wind1Btn.setBackgroundResource(R.drawable.wind1_icon1)
                    binding.wind2Btn.setBackgroundResource(R.drawable.wind2_icon2)
                    binding.wind3Btn.setBackgroundResource(R.drawable.wind3_icon1)
                }

                3 -> {
                    binding.wind1Btn.setBackgroundResource(R.drawable.wind1_icon1)
                    binding.wind2Btn.setBackgroundResource(R.drawable.wind2_icon1)
                    binding.wind3Btn.setBackgroundResource(R.drawable.wind3_icon2)
                }
            }

            if ((parsed.DayReservation and 0x7F) != 0) {
                binding.reserveState.setTextColor(Color.parseColor("#FF0000"))
            } else {
                binding.reserveState.setTextColor(Color.parseColor("#008000"))

                val mainText = "예약\n"
                val subText = "(OFF)"
                val spannable = SpannableStringBuilder(mainText + subText)

                spannable.setSpan(
                    RelativeSizeSpan(0.7f),
                    mainText.length,
                    mainText.length + subText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                binding.reserveState.text = spannable
            }


            // [필터 시간 vs 필터 시간 세팅 값] 비교 로직 추가
            if (parsed.FilterTime_2byte >= parsed.FilterTimeSetting_2byte) {
                // 필터 시간이 필터 세팅에 도달하거나 초과된 경우 → 파란색
                binding.filterBtn.setTextColor(Color.parseColor("#00B0FF"))
            } else {
                // 아직 도달하지 못한 경우 → 회색
                binding.filterBtn.setTextColor(Color.parseColor("#505050"))
            }
        }
    }

}