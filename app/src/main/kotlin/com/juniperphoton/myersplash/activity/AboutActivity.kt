package com.juniperphoton.myersplash.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.RecyclerView
import android.view.View
import butterknife.ButterKnife
import butterknife.OnClick
import com.google.android.flexbox.FlexboxLayoutManager
import com.juniperphoton.myersplash.R
import com.juniperphoton.myersplash.adapter.ThanksToAdapter
import com.juniperphoton.myersplash.extension.getVersionName
import com.juniperphoton.myersplash.extension.hasNavigationBar
import com.juniperphoton.myersplash.utils.StatusBarCompat
import kotlinx.android.synthetic.main.activity_about.*
import moe.feng.alipay.zerosdk.AlipayZeroSdk

@Suppress("UNUSED")
class AboutActivity : BaseActivity() {
    private var adapter: ThanksToAdapter? = null
    private val marginLeft by lazy {
        resources.getDimensionPixelSize(R.dimen.about_thanks_item_margin)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        ButterKnife.bind(this)

        updateVersion()
        initThanks()
        if (!hasNavigationBar()) {
            blank.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        StatusBarCompat.setDarkText(this, true)
    }

    override fun onPause() {
        super.onPause()
        StatusBarCompat.setDarkText(this, false)
    }

    private fun updateVersion() {
        versionTextView.text = getVersionName()
    }

    private fun initThanks() {
        adapter = ThanksToAdapter(this)
        val strs = resources.getStringArray(R.array.thanks_array)
        val list = strs.toList()
        adapter!!.refresh(list)
        thanksList.addItemDecoration(object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect?, view: View?, parent: RecyclerView?, state: RecyclerView.State?) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect?.set(0, marginLeft, marginLeft, marginLeft)
            }
        })
        thanksList.layoutManager = FlexboxLayoutManager()
        thanksList.adapter = adapter
    }

    @OnClick(R.id.email_item)
    internal fun onClickEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.type = "message/rfc822"
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_url)))

        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MyerSplash for Android ${getVersionName()} feedback")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")

        try {
            startActivity(emailIntent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    @OnClick(R.id.rate_item)
    internal fun onClickRate() {
        val uri = Uri.parse("market://details?id=" + packageName)
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    @OnClick(R.id.donate_item)
    internal fun onClickDonate() {
        if (AlipayZeroSdk.hasInstalledAlipayClient(this)) {
            AlipayZeroSdk.startAlipayClient(this, getString(R.string.alipay_url_code))
        }
    }

    @OnClick(R.id.github_item)
    internal fun onClickGitHub() {
        val uri = Uri.parse(getString(R.string.github_url))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }

    @OnClick(R.id.twitter_item)
    internal fun onClickTwitter() {
        val uri = Uri.parse(getString(R.string.twitter_url))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
    }
}