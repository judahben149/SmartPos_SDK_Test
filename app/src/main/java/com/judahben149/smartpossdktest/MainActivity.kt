package com.judahben149.smartpossdktest

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.interswitchng.smartpos.IswPos
import com.interswitchng.smartpos.emv.pax.services.POSDeviceImpl
import com.interswitchng.smartpos.shared.errors.NotConfiguredException
import com.interswitchng.smartpos.shared.models.core.Environment
import com.interswitchng.smartpos.shared.models.core.POSConfig
import com.interswitchng.smartpos.shared.models.core.Transaction
import com.interswitchng.smartpos.shared.models.results.IswTransactionResult
import com.interswitchng.smartpos.shared.models.transaction.PaymentType
import com.judahben149.smartpossdktest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), IswPos.IswPaymentCallback {

    private val iswPosInstance: IswPos by lazy { IswPos.getInstance() }
    private val device by lazy { POSDeviceImpl.create(this.applicationContext) }

    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        configureTerminal()

        binding.payButton.setOnClickListener {
            initiatePayment()
        }
    }


    private fun configureTerminal() {
        val environment = Environment.Production

        val config = POSConfig(
            alias = BuildConfig.ALIAS,
            clientId = BuildConfig.CLIENT_ID,
            clientSecret = BuildConfig.CLIENT_SECRET,
            merchantCode = BuildConfig.MERCHANT_CODE,
            merchantTelephone = BuildConfig.MERCHANT_TELEPHONE,
            environment = environment,
            appVersion = ""
        )

        IswPos.setupTerminal(this.application, device, null, config, true)
//        KozenModuleHelper().createModule(this.application)

        IswPos.setDeviceSetialNumber(device.serialNumber())

        this.resources?.let { resources ->
            val drawable = AppCompatResources.getDrawable(this, R.drawable.ic_isw_amazon)
            drawable?.let {
                val bitmap = Bitmap.createBitmap(
                    it.intrinsicWidth,
                    it.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                it.setBounds(0, 0, canvas.width, canvas.height)
                it.draw(canvas)

                IswPos.setGeneralCompanyLogo(bitmap)
                device.setCompanyLogo(bitmap)
            }
        }

        // Set company logo here if you wish, this is left out because it is set in CompanyActivity
//         device.setCompanyLogo(logoBitmap)
//         IswPos.setGeneralCompanyLogo(logoBitmap)

        iswPosInstance.callHome()
    }

    private fun initiatePayment() {
        val kobo = 100L
        val amount = 2 * kobo

        val transaction: Transaction = Transaction.Purchase(PaymentType.Card)

        try {
            iswPosInstance.pay(amount, this, transaction)
        } catch (ex: NotConfiguredException) {
            Toast.makeText(this, "Error - Terminal not configured", Toast.LENGTH_LONG).show()
        }
    }

    override fun onPaymentCompleted(result: IswTransactionResult) {
        Toast.makeText(this, "Payment completed", Toast.LENGTH_LONG).show()
    }

    override fun onUserCancel() {
        Toast.makeText(this, "User cancelled", Toast.LENGTH_LONG).show()
    }
}